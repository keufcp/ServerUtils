package io.github.keufcp.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.github.keufcp.ServerUtils;
import io.github.keufcp.utils.StyledText;
import io.github.keufcp.utils.TickTimeUtil;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

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

    // TPS値はしきい値で色分けし，クリックで /suMspt を実行する
    String color = StyledText.tpsColor(tps);
    String value =
        "<%1$s><run_cmd '/suMspt'>%2$s</run_cmd></%1$s>"
            .formatted(color, String.format("%.2f", tps));

    StyledText.send(source, ServerUtils.LANG.get("tps.result", value));

    return Command.SINGLE_SUCCESS;
  }
}
