package io.github.keufcp.utils;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import io.github.keufcp.ServerUtilsMidnightConfig;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * QuickTextタグ記法によるスタイル付きテキスト送信ユーティリティクラス．
 *
 * <p>Text Placeholder APIのタグ記法（例: {@code <gold>CPU:</gold> <white>12.3%</white>}）
 * で書かれたテンプレート文字列をパースし，実行元に応じて色付き/プレーンを自動で切り替えて送信する．
 */
public final class StyledText {

  private StyledText() {}

  /**
   * QuickTextテンプレートをパースして色付きテキストを生成する．
   *
   * @param template QuickTextタグ記法のテンプレート文字列
   * @return パース済みテキスト
   */
  public static Text parse(String template) {
    return TagParser.DEFAULT.parseText(template, ParserContext.of());
  }

  /**
   * 色付きテキストの有効性を判定する．
   *
   * @param source コマンドソース
   * @return 色付きテキストが有効な場合はtrue
   */
  public static boolean shouldUseColoredText(ServerCommandSource source) {
    return ServerUtilsMidnightConfig.enableColoredOutput && source.getEntity() != null;
  }

  /**
   * 実行元に応じて色付き/プレーンを切り替えてフィードバック送信する．
   *
   * <p>プレーン側はパース結果から装飾を剥がした文字列を用いるため，出力文言は色付き側と常に一致する．
   *
   * @param source コマンドソース
   * @param template QuickTextタグ記法のテンプレート文字列
   */
  public static void send(ServerCommandSource source, String template) {
    Text parsed = parse(template);
    Text output = shouldUseColoredText(source) ? parsed : Text.literal(parsed.getString());
    source.sendFeedback(() -> output, false);
  }

  /**
   * TPS値に応じた色タグ名を取得する（19.5以上: green，17.0以上: yellow，未満: red）．
   *
   * @param tps TPS値
   * @return QuickTextの色タグ名
   */
  public static String tpsColor(double tps) {
    if (tps >= 19.5) {
      return "green";
    }
    return tps >= 17.0 ? "yellow" : "red";
  }

  /**
   * MSPT値に応じた色タグ名を取得する（40ms以下: green，50ms以下: yellow，超過: red）．
   *
   * @param mspt MSPT値（ミリ秒）
   * @return QuickTextの色タグ名
   */
  public static String msptColor(double mspt) {
    if (mspt <= 40.0) {
      return "green";
    }
    return mspt <= 50.0 ? "yellow" : "red";
  }

  /**
   * MobCap使用状況に基づく分子（現在数）の色タグ名を取得する．
   *
   * @param current 現在のモンスター数
   * @param cap MobCap上限
   * @return QuickTextの色タグ名
   */
  public static String mobCountColor(long current, int cap) {
    if (cap == 0) {
      return "gray";
    }
    return current > cap ? "red" : "green";
  }

  /**
   * MobCap状況に基づく分母（上限値）の色タグ名を取得する．
   *
   * @param cap MobCap上限
   * @param spawnChunkCount スポーンチャンク数
   * @return QuickTextの色タグ名
   */
  public static String mobCapColor(int cap, int spawnChunkCount) {
    if (cap == 0) {
      return spawnChunkCount == 0 ? "gray" : "red";
    }
    return "white";
  }
}
