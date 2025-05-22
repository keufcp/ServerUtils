package io.github.keufcp;

import eu.midnightdust.lib.config.MidnightConfig;
import io.github.keufcp.commands.ServerUtilsReloadCommand;
import io.github.keufcp.commands.ServerUtilsTpsCommand;
import io.github.keufcp.commands.UptimeCommand;
import io.github.keufcp.commands.ServerUtilsMsptCommand;
import io.github.keufcp.utils.WebhookSender;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * ServerUtils MODメインクラス．
 * <p>
 * サーバー起動時初期化処理，
 * 各種コマンド・多言語管理・設定初期化を担当．
 * </p>
 * <ul>
 *   <li>MOD ID・ロガー定義
 *   <li>サーバー起動時刻記録
 *   <li>言語設定・LangManager初期化
 *   <li>コマンド登録
 * </ul>
 */
public class ServerUtils implements ModInitializer {
    /** MOD ID */
    public static final String MOD_ID = "serverutils";
    /** MODロガー */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    /** 現在ロケール */
    public static Locale LOCALE;
    /** サーバー起動時刻 (ミリ秒) */
    public static long serverStartTimeMillis;
    /** 言語マネージャー */
    public static LangManager LANG;

    @Override
    public void onInitialize() {
        LOGGER.info("{} is initialized.", MOD_ID);
        serverStartTimeMillis = System.currentTimeMillis();
        MidnightConfig.init(MOD_ID, ServerUtilsMidnightConfig.class);
        ServerUtilsMidnightConfig.validateAll();
        String localeCode = ServerUtilsMidnightConfig.locale;
        LOCALE = Locale.forLanguageTag(localeCode);
        LANG = new LangManager(localeCode);
        UptimeCommand.register();
        ServerUtilsTpsCommand.register();
        ServerUtilsReloadCommand.register();
        ServerUtilsMsptCommand.register();

        if (ServerUtilsMidnightConfig.enableSendWebhook) {
            WebhookSender.initialize();
        }

        // サーバーシャットダウン時，WebhookSenderシャットダウンメソッド呼び出し
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("Server is stopping, shutting down webhook sender...");
            WebhookSender.shutdown();
        });
    }
}
