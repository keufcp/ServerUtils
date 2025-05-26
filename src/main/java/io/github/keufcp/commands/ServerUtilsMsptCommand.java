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
 * サーバーMSPT (平均ティック時間) 表示コマンドクラス．
 *
 * <p>/suMspt コマンド登録・実行処理， サーバーMSPT計算・表示担当．
 */
public class ServerUtilsMsptCommand {

    /** /suMspt コマンドのコマンドディスパッチャへの登録． */
    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    dispatcher.register(
                            CommandManager.literal("suMspt")
                                    .executes(ServerUtilsMsptCommand::runMsptCommand)
                                    .requires(source -> source.hasPermissionLevel(2)) // OP権限レベル2以上要
                            );
                });
    }

    /**
     * MSPTコマンド実行ハンドラ．
     *
     * @param context コマンドコンテキスト
     * @return コマンド結果
     */
    private static int runMsptCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        double mspt = TickTimeUtil.getMeanTickTime();

        String label = ServerUtils.LANG.get("mspt.result", String.format("%.2f", mspt));

        source.sendMessage(Text.of(label));

        return Command.SINGLE_SUCCESS;
    }
}
