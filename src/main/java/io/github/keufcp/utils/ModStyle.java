package io.github.keufcp.utils;

import net.minecraft.util.Formatting;

/**
 * モッドの統一的な色スタイル定義クラス．
 *
 * <p>色定義を一元管理し、意味論的な名前でアクセスできるようにする．
 */
public class ModStyle {

  /** 項目名（ラベル）用の色 */
  public static final Formatting LABEL = Formatting.GRAY;

  /** 通常値用の色 */
  public static final Formatting VALUE_NORMAL = Formatting.WHITE;

  /** 良好な状態を示す色 */
  public static final Formatting VALUE_GOOD = Formatting.GREEN;

  /** 警告・注意を示す色 */
  public static final Formatting VALUE_WARN = Formatting.YELLOW;

  /** 異常・危険を示す色 */
  public static final Formatting VALUE_BAD = Formatting.RED;

  /** クリック可能な要素を示す色 */
  public static final Formatting CLICKABLE = Formatting.AQUA;

  private ModStyle() {
    // ユーティリティクラスなのでインスタンス化を禁止
  }
}
