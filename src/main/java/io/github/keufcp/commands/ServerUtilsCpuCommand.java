package io.github.keufcp.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.keufcp.ServerUtilsMidnightConfig;
import io.github.keufcp.utils.SystemInfoUtil;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

/**
 * ServerUtils CPU使用率表示コマンドクラス．
 *
 * <p>/suCpu コマンドの登録・実行処理を担当し，サーバーのCPU使用率情報を表示する．
 */
public class ServerUtilsCpuCommand {

  /** /suCpu コマンドのコマンドディスパッチャへの登録． */
  public static void register() {
    CommandRegistrationCallback.EVENT.register(ServerUtilsCpuCommand::registerCommand);
  }

  /** コマンドの詳細登録処理． */
  private static void registerCommand(
      CommandDispatcher<ServerCommandSource> dispatcher,
      CommandRegistryAccess registryAccess,
      CommandManager.RegistrationEnvironment environment) {
    dispatcher.register(
        CommandManager.literal("suCpu")
            .requires(
                source -> source.hasPermissionLevel(ServerUtilsMidnightConfig.cpuPermissionLevel))
            .executes(
                context -> {
                  SystemInfoUtil.sendCpuInfo(context);
                  return 1;
                }));
  }
}
