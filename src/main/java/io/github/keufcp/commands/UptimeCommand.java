package io.github.keufcp.commands;

import static io.github.keufcp.ServerUtilsMidnightConfig.uptimePermissionLevel;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

/**
 * サーバー稼働時間（Uptime）表示コマンドクラス．
 *
 * <p>/uptime コマンド登録・実行処理， サーバー起動時刻からの経過時間計算担当．
 */
public class UptimeCommand {
    /** /uptime コマンドのコマンドディスパッチャへの登録． */
    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    dispatcher.register(
                            CommandManager.literal("uptime")
                                    .executes(UptimeCommand::runUptimeCommand)
                                    .requires(
                                            source ->
                                                    source.hasPermissionLevel(
                                                            uptimePermissionLevel)) // configから取得
                            );
                });
    }

    /**
     * サーバー起動時刻から現在までの[日，時，分，秒]計算ユーティリティメソッド．
     *
     * @return {@code List.of(days, hours, minutes, seconds)}
     */
    public static List<Long> calculateUptime() {
        long uptimeMillis =
                System.currentTimeMillis() - io.github.keufcp.ServerUtils.serverStartTimeMillis;
        long seconds = uptimeMillis / 1000 % 60;
        long minutes = uptimeMillis / (1000 * 60) % 60;
        long hours = (uptimeMillis / (1000 * 60 * 60)) % 24;
        long days = uptimeMillis / (1000 * 60 * 60 * 24);
        return List.of(days, hours, minutes, seconds);
    }

    /**
     * 計算済み稼働時間リストからの基本的時間文字列生成．
     *
     * @param uptimeList 日・時・分・秒リスト
     * @return フォーマット済み時間文字列
     */
    public static String formatUptimeValue(List<Long> uptimeList) {
        return io.github.keufcp.ServerUtils.LANG.get(
                "uptime.format",
                uptimeList.get(0),
                uptimeList.get(1),
                uptimeList.get(2),
                uptimeList.get(3));
    }

    /**
     * フォーマット済み時間文字列への適切ラベル付与．
     *
     * @param formattedUptime フォーマット済み時間文字列
     * @return ラベル付き表示用文字列
     */
    public static String formatUptimeLabel(String formattedUptime) {
        return io.github.keufcp.ServerUtils.LANG.get("uptime.label", formattedUptime);
    }

    /**
     * /uptimeコマンド実行時処理．
     *
     * @param context コマンド実行コンテキスト
     * @return コマンド実行結果
     */
    private static int runUptimeCommand(CommandContext<ServerCommandSource> context) {
        List<Long> uptimeList = calculateUptime();
        String formattedUptime = formatUptimeValue(uptimeList);
        String label = formatUptimeLabel(formattedUptime);
        context.getSource().sendFeedback(() -> Text.literal(label), false);
        return Command.SINGLE_SUCCESS;
    }
}
