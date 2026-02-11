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
    String tpsFormatted = String.format("%.2f", tps);

    if (io.github.keufcp.utils.ColoredTextBuilder.shouldUseColoredText(source)) {
      // 色付きテキストを生成
      io.github.keufcp.utils.ColoredTextBuilder.Builder builder =
          new io.github.keufcp.utils.ColoredTextBuilder.Builder();

      // [ServerUtils] TPS: の部分
      builder
          .append("[ServerUtils] TPS: ", io.github.keufcp.utils.ModStyle.LABEL)
          .append(createClickableTpsText(tpsFormatted, tps));

      source.sendMessage(builder.build());
    } else {
      // プレーンテキスト
      String label = ServerUtils.LANG.get("tps.result", tpsFormatted);
      source.sendMessage(Text.of(label));
    }

    return Command.SINGLE_SUCCESS;
  }

  /**
   * クリック可能なTPS値テキストを生成する．
   *
   * @param tpsFormatted フォーマット済みTPS文字列
   * @param tps TPS値
   * @return クリック可能なテキスト
   */
  private static Text createClickableTpsText(String tpsFormatted, double tps) {
    net.minecraft.util.Formatting color = getTpsColor(tps);

    return Text.literal(tpsFormatted)
        .formatted(color)
        .styled(
            style ->
                style.withClickEvent(
                    new net.minecraft.text.ClickEvent(
                        net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/suMspt")));
  }

  /**
   * TPS値に基づいて色を決定する．
   *
   * @param tps TPS値
   * @return 適切な色フォーマット
   */
  private static net.minecraft.util.Formatting getTpsColor(double tps) {
    if (tps >= 19.5) {
      return io.github.keufcp.utils.ModStyle.VALUE_GOOD;
    } else if (tps >= 15.0) {
      return io.github.keufcp.utils.ModStyle.VALUE_WARN;
    } else {
      return io.github.keufcp.utils.ModStyle.VALUE_BAD;
    }
  }
}
