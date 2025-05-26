package io.github.keufcp.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;

import io.github.keufcp.ServerUtils;
import io.github.keufcp.utils.TickTimeUtil;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * サーバーTPS (Ticks Per Second) 表示コマンドクラス．
 *
 * <p>/suTps コマンド登録・実行処理， サーバーTPS計算・表示担当．
 */
public class ServerUtilsTpsCommand {

    /** /suTps コマンドのコマンドディスパッチャへの登録． */
    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    dispatcher.register(
                            CommandManager.literal("suTps")
                                    .executes(ServerUtilsTpsCommand::runTpsCommand)
                                    .requires(source -> source.hasPermissionLevel(2)) // OP権限レベル2以上要
                            );
                });
    }

    /**
     * TPSコマンド実行ハンドラ．
     *
     * @param context コマンドコンテキスト
     * @return コマンド結果
     */
    private static int runTpsCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        double tps = TickTimeUtil.calculateTPS();

        String label = ServerUtils.LANG.get("tps.result", String.format("%.2f", tps));

        // TPS結果をプレイヤーに表示
        source.sendMessage(Text.of(label));

        return Command.SINGLE_SUCCESS;
    }
}
