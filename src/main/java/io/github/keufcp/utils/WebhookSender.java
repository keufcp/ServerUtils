package io.github.keufcp.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.keufcp.ServerUtils;
import io.github.keufcp.ServerUtilsMidnightConfig;
import io.github.keufcp.commands.UptimeCommand;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Discord Webhookを用いたサーバー情報送信クラス．
 *
 * <p>cron式指定間隔でのWebhook送信． {@link ServerUtilsMidnightConfig#webhookCronExpression} で送信間隔設定可能．
 */
public class WebhookSender {
    /** MOD名称．Webhook送信者名として利用 */
    private static final String MOD_NAME = "ServerUtils";

    /** WebhookSender初期化済みフラグ */
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    /** Webhook送信用Quartzスケジューラー */
    private static Scheduler scheduler;

    /**
     * WebhookSender初期化とスケジューラーセットアップ．
     *
     * <p>設定ファイルに基づくWebhook送信有効時，指定cron式に従った定期的Webhook送信ジョブのスケジュール． 初期化済みの場合，一度シャットダウン後の再初期化．
     */
    public static void initialize() {
        // リロード時対応のため，初期化済みの場合，一度シャットダウン
        if (initialized.get()) {
            shutdown();
            initialized.set(false);
        }

        if (!ServerUtilsMidnightConfig.enableSendWebhook
                || ServerUtilsMidnightConfig.webhookUrl.isEmpty()) {
            ServerUtils.LOGGER.info("Webhook sending is disabled or URL not configured.");
            return;
        }

        try {
            // Quartzスケジューラー初期化
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();

            // WebhookJob設定
            JobDetail job =
                    JobBuilder.newJob(WebhookJob.class)
                            .withIdentity("webhookJob", "serverutils")
                            .storeDurably()
                            .build();

            // Webhook cron式からの送信間隔設定
            Trigger trigger =
                    TriggerBuilder.newTrigger()
                            .withIdentity("webhookTrigger", "serverutils")
                            .forJob(job)
                            .withSchedule(
                                    CronScheduleBuilder.cronSchedule(
                                            ServerUtilsMidnightConfig.webhookCronExpression))
                            .build();

            // スケジューラーへのジョブとトリガー登録
            scheduler.scheduleJob(job, trigger);
            scheduler.start();
            initialized.set(true);

            ServerUtils.LOGGER.info(
                    "Webhook scheduler initialized with cron: {}",
                    ServerUtilsMidnightConfig.webhookCronExpression);
        } catch (SchedulerException e) {
            ServerUtils.LOGGER.error("Failed to initialize webhook scheduler", e);
        }
    }

    /**
     * WebhookSenderスケジューラー停止処理．
     *
     * <p>実行中ジョブの中断とスケジューラーのシャットダウン．
     */
    public static void shutdown() {
        if (scheduler != null) {
            try {
                scheduler.shutdown(true); // 即時シャットダウン
                ServerUtils.LOGGER.info("Webhook scheduler shutdown");
            } catch (SchedulerException e) {
                ServerUtils.LOGGER.error("Failed to shutdown webhook scheduler", e);
            }
        }
    }

    /**
     * 手動でのWebhook送信．
     *
     * <p>設定ファイルでのWebhook送信有効かつURL設定時， サーバー情報埋め込みWebhookメッセージの送信．
     *
     * @return 送信成功時は {@code true}，失敗時は {@code false}
     */
    public static boolean sendWebhook() {
        if (!ServerUtilsMidnightConfig.enableSendWebhook
                || ServerUtilsMidnightConfig.webhookUrl.isEmpty()) {
            return false;
        }

        try {
            HttpClient client =
                    HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

            // Webhookペイロード作成
            String jsonPayload = buildWebhookPayload();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(ServerUtilsMidnightConfig.webhookUrl))
                            .header("Content-Type", "application/json")
                            .header("User-Agent", "ServerUtils-Webhook")
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            jsonPayload, StandardCharsets.UTF_8))
                            .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (IOException | InterruptedException e) {
            ServerUtils.LOGGER.error("Error sending webhook", e);
            return false;
        }
    }

    /**
     * Discord Webhook送信用JSONペイロード構築．
     *
     * <p>サーバー稼働時間等情報を含むembedメッセージ作成．
     *
     * @return Webhook送信用構築済みJSON文字列
     */
    private static String buildWebhookPayload() {
        JsonObject payload = new JsonObject();

        // Webhook送信者名とアイコン設定
        payload.addProperty("username", MOD_NAME);

        // embeds配列作成
        JsonArray embeds = new JsonArray();
        JsonObject embed = new JsonObject();

        // サーバー稼働時間情報取得
        List<Long> uptimeList = UptimeCommand.calculateUptime();
        String uptimeValue = UptimeCommand.formatUptimeValue(uptimeList);

        // embed一般設定
        embed.addProperty("title", ServerUtils.LANG.get("webhook.title"));
        embed.addProperty("description", ServerUtils.LANG.get("webhook.description"));
        embed.addProperty("color", 5814783); // 青色

        // fields配列作成
        JsonArray fields = new JsonArray();

        // 稼働時間フィールド追加
        JsonObject uptimeField = new JsonObject();
        uptimeField.addProperty("name", ServerUtils.LANG.get("webhook.uptime.title"));
        uptimeField.addProperty("value", uptimeValue);
        uptimeField.addProperty("inline", true);
        fields.add(uptimeField);

        // fieldsをembedに追加
        embed.add("fields", fields);

        // フッター情報追加
        JsonObject footer = new JsonObject();
        footer.addProperty("text", ServerUtils.MOD_ID);
        embed.add("footer", footer);

        // 現在時刻ISO形式追加
        embed.addProperty("timestamp", java.time.Instant.now().toString());

        // embedをembedsに追加し，payloadに設定
        embeds.add(embed);
        payload.add("embeds", embeds);

        return payload.toString();
    }

    /**
     * Quartzスケジューラーによる定期的Webhook送信ジョブ．
     *
     * <p>ジョブ実行時，{@link WebhookSender#sendWebhook()} メソッド呼出によるWebhookメッセージ送信．
     */
    public static class WebhookJob implements Job {
        /**
         * ジョブ実行ロジック．Webhook送信．
         *
         * @param context ジョブ実行コンテキスト
         */
        @Override
        public void execute(JobExecutionContext context) {
            try {
                sendWebhook();
                ServerUtils.LOGGER.info("Webhook sent successfully");
            } catch (Exception e) {
                ServerUtils.LOGGER.error("Failed to send webhook", e);
            }
        }
    }
}
