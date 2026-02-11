package io.github.keufcp.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.github.keufcp.ServerUtils;
import io.github.keufcp.utils.ColoredTextBuilder;
import io.github.keufcp.utils.ModStyle;
import io.github.keufcp.utils.TickTimeUtil;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

    // 色付きテキストを使用する場合
    if (ColoredTextBuilder.shouldUseColoredText(source)) {
      Formatting tpsColor = getTpsColor(tps);

      MutableText message =
          Text.literal(ServerUtils.LANG.get("serverutils.prefix"))
              .append(Text.literal("TPS: ").formatted(ModStyle.LABEL))
              .append(
                  Text.literal(tpsFormatted)
                      .formatted(tpsColor)
                      .styled(
                          style ->
                              style.withClickEvent(
                                  new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/suMspt"))));

      source.sendMessage(message);
    } else {
      // プレーンテキスト出力
      String label = ServerUtils.LANG.get("tps.result", tpsFormatted);
      source.sendMessage(Text.of(label));
    }

    return Command.SINGLE_SUCCESS;
  }

  /**
   * TPS値に応じた色を取得する．
   *
   * @param tps TPS値
   * @return 適切なフォーマット
   */
  private static Formatting getTpsColor(double tps) {
    if (tps >= 19.5) {
      return ModStyle.VALUE_GOOD; // 緑: 良好
    } else if (tps >= 17.0) {
      return ModStyle.VALUE_WARN; // 黄: 警告
    } else {
      return ModStyle.VALUE_BAD; // 赤: 危険
    }
  }
}
