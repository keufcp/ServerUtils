package io.github.keufcp.utils;

import net.minecraft.util.Formatting;

/**
 * コンソール出力のスタイル定義を集約するクラス．
 *
 * <p>意味論的な名前で色定義にアクセスでき、単一責任原則 (SRP) に基づいて保守性を向上させる．
 */
public class ModStyle {

  /** 項目名やラベルの色 */
  public static final Formatting LABEL = Formatting.GRAY;

  /** 通常値の色 */
  public static final Formatting VALUE_NORMAL = Formatting.WHITE;

  /** 良好な状態を示す色 */
  public static final Formatting VALUE_GOOD = Formatting.GREEN;

  /** 警告・注意を示す色 */
  public static final Formatting VALUE_WARN = Formatting.YELLOW;

  /** 異常・危険を示す色 */
  public static final Formatting VALUE_BAD = Formatting.RED;

  /** クリック可能な要素の色 */
  public static final Formatting CLICKABLE = Formatting.AQUA;

  private ModStyle() {
    // ユーティリティクラスのため、インスタンス化を防ぐ
  }
}
