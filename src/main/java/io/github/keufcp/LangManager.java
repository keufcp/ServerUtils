package io.github.keufcp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 多言語リソース管理クラス．
 *
 * <p>指定言語コードに対応するJSONファイルからのメッセージ読み込み， キーに基づくメッセージ取得・フォーマット提供．
 */
public class LangManager {
  /** 読み込まれたメッセージのマップ (キー: メッセージキー，値: メッセージ文字列) */
  private final Map<String, String> messages;

  /**
   * 指定言語コードリソース読み込みコンストラクタ．
   *
   * @param langCode 言語コード（例: "en_US"）
   */
  public LangManager(String langCode) {
    String path = String.format("assets/serverutils/lang/%s.json", langCode);
    Map<String, String> loaded = new HashMap<>();
    Type type = new TypeToken<Map<String, String>>() {}.getType();
    try (InputStreamReader reader = getReader(path)) {
      if (reader != null) {
        loaded = new Gson().fromJson(reader, type);
      }
    } catch (Exception e) {
      // フォールバック：en_US試行
      if (!"en_US".equals(langCode)) {
        try (InputStreamReader reader = getReader("assets/serverutils/lang/en_US.json")) {
          if (reader != null) {
            loaded = new Gson().fromJson(reader, type);
          }
        } catch (Exception ignored) {
        }
      }
    }
    this.messages = loaded;
  }

  /**
   * 指定パスリソースファイルのUTF-8でのオープン．
   *
   * @param path リソースパス
   * @return {@link InputStreamReader} または null
   */
  private InputStreamReader getReader(String path) {
    var isr = LangManager.class.getClassLoader().getResourceAsStream(path);
    if (isr == null) return null;
    return new InputStreamReader(isr, StandardCharsets.UTF_8);
  }

  /**
   * メッセージキー対応文言取得と引数フォーマット．
   *
   * @param key メッセージキー
   * @param args フォーマット引数
   * @return フォーマット済みメッセージ
   */
  public String get(String key, Object... args) {
    String template = messages.getOrDefault(key, key);
    return java.text.MessageFormat.format(template, args);
  }
}
