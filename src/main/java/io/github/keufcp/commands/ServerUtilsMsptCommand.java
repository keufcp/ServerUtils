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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
    String msptFormatted = String.format("%.2f", mspt);

    // 色付きテキストを使用する場合
    if (ColoredTextBuilder.shouldUseColoredText(source)) {
      Formatting msptColor = getMsptColor(mspt);

      MutableText message =
          Text.literal(ServerUtils.LANG.get("serverutils.prefix"))
              .append(Text.literal("MSPT: ").formatted(ModStyle.LABEL))
              .append(Text.literal(msptFormatted + " ms").formatted(msptColor));

      source.sendMessage(message);
    } else {
      // プレーンテキスト出力
      String label = ServerUtils.LANG.get("mspt.result", msptFormatted);
      source.sendMessage(Text.of(label));
    }

    return Command.SINGLE_SUCCESS;
  }

  /**
   * MSPT値に応じた色を取得する．
   *
   * @param mspt MSPT値
   * @return 適切なフォーマット
   */
  private static Formatting getMsptColor(double mspt) {
    if (mspt <= 40.0) {
      return ModStyle.VALUE_GOOD; // 緑: 良好 (40ms以下)
    } else if (mspt <= 50.0) {
      return ModStyle.VALUE_WARN; // 黄: 警告 (40-50ms)
    } else {
      return ModStyle.VALUE_BAD; // 赤: 危険 (50ms以上)
    }
  }
}
