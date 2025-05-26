package io.github.keufcp.commands;

import static io.github.keufcp.ServerUtils.MOD_ID;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;

import eu.midnightdust.lib.config.MidnightConfig;

import io.github.keufcp.ServerUtils;
import io.github.keufcp.ServerUtilsMidnightConfig;
import io.github.keufcp.utils.WebhookSender;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * ServerUtils設定リロードコマンドクラス．
 *
 * <p>/suReload コマンド登録・実行処理担当， 設定・言語リソース再読み込み．
 */
public class ServerUtilsReloadCommand {
    /** /suReload コマンドのコマンドディスパッチャへの登録． */
    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        dispatcher.register(
                                CommandManager.literal("suReload")
                                        .executes(ServerUtilsReloadCommand::runReloadCommand)
                                        .requires(source -> source.hasPermissionLevel(4)) // OP権限要求
                                ));
    }

    /**
     * /suReloadコマンド実行時処理． 設定・ロケール・LangManager再初期化．
     *
     * @param context コマンド実行コンテキスト
     * @return コマンド実行結果
     */
    private static int runReloadCommand(CommandContext<ServerCommandSource> context) {
        // 既存Webhookスケジューラーシャットダウン
        WebhookSender.shutdown();

        // MidnightConfig再初期化
        MidnightConfig.init(MOD_ID, ServerUtilsMidnightConfig.class);

        // バリデーション実行（デフォルト値リセット要否判断含む）
        ServerUtilsMidnightConfig.validateAll();

        // 言語設定更新
        String localeCode = ServerUtilsMidnightConfig.locale;
        // アンダースコア区切り言語コード(en_US)のハイフン区切り(en-US)への変換
        String languageTag = localeCode.replace('_', '-');
        ServerUtils.LOCALE = java.util.Locale.forLanguageTag(languageTag);
        ServerUtils.LANG = new io.github.keufcp.LangManager(localeCode);

        // Webhook設定有効時，再初期化
        if (ServerUtilsMidnightConfig.enableSendWebhook) {
            WebhookSender.initialize();
        }

        context.getSource()
                .sendFeedback(
                        () ->
                                Text.literal(
                                        ServerUtils.LANG.get("serverutils.prefix")
                                                + ServerUtils.LANG.get("config.reload")),
                        false);
        return Command.SINGLE_SUCCESS;
    }
}
