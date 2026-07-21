package io.github.keufcp.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.keufcp.ServerUtilsMidnightConfig;
import io.github.keufcp.utils.SystemInfoUtil;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

/**
 * ServerUtils メモリ使用率表示コマンドクラス．
 *
 * <p>/suMem コマンドの登録・実行処理を担当し，サーバーのメモリ使用率情報を表示する．
 */
public class ServerUtilsMemCommand {

  /** /suMem コマンドのコマンドディスパッチャへの登録． */
  public static void register() {
    CommandRegistrationCallback.EVENT.register(ServerUtilsMemCommand::registerCommand);
  }

  /** コマンドの詳細登録処理． */
  private static void registerCommand(
      CommandDispatcher<ServerCommandSource> dispatcher,
      CommandRegistryAccess registryAccess,
      CommandManager.RegistrationEnvironment environment) {
    dispatcher.register(
        CommandManager.literal("suMem")
            .requires(
                source -> source.hasPermissionLevel(ServerUtilsMidnightConfig.memPermissionLevel))
            .executes(
                context -> {
                  SystemInfoUtil.sendMemInfo(context);
                  return 1;
                }));
  }
}
