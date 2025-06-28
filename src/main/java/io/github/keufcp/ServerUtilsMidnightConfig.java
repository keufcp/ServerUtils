package io.github.keufcp;

import eu.midnightdust.lib.config.MidnightConfig;

/**
 * ServerUtils設定クラス．
 *
 * <p>MidnightConfig継承によるMOD各種設定（言語・コマンド権限・Webhook等）管理．
 *
 * <ul>
 *   <li>locale: 使用言語コード
 *   <li>uptimePermissionLevel: uptimeコマンド実行権限レベル
 *   <li>mobcapPermissionLevel: mobcapコマンド実行権限レベル
 *   <li>enableSendWebhook: Webhook送信有効化
 *   <li>webhookUrl: Webhook送信先URL
 *   <li>webhookCronExpression: Webhook送信間隔（cron式）
 * </ul>
 */
public class ServerUtilsMidnightConfig extends MidnightConfig {
  /** 言語コード選択肢 */
  public static final String[] localeOptions = {"en_US", "ja_JP"};

  /** 使用する言語コード */
  @Entry public static String locale = "en_US";

  /**
   * uptimeコマンド実行権限レベル．
   *
   * <p>権限レベル一覧：
   *
   * <ul>
   *   <li>レベル0: 全プレイヤー
   *   <li>レベル1: モデレーター
   *   <li>レベル2: ゲームマスター
   *   <li>レベル3: 管理者
   *   <li>レベル4: オーナー (フルOP，デフォルト)
   * </ul>
   */
  @Entry public static int uptimePermissionLevel = 4;

  /**
   * mobcapコマンド実行権限レベル．
   *
   * <p>権限レベル一覧：
   *
   * <ul>
   *   <li>レベル0: 全プレイヤー
   *   <li>レベル1: モデレーター
   *   <li>レベル2: ゲームマスター
   *   <li>レベル3: 管理者
   *   <li>レベル4: オーナー (フルOP，デフォルト)
   * </ul>
   */
  @Entry public static int mobcapPermissionLevel = 4;

  /** Webhook送信有効化フラグ */
  @Entry public static boolean enableSendWebhook = false;

  /** Webhook送信先URL */
  @Entry public static String webhookUrl = "";

  /** Webhook送信間隔（cron式）． デフォルトは毎日午前0時（"0 0 0 * * ?"）． */
  @Entry public static String webhookCronExpression = "0 0 0 * * ?";

  /** 色付きテキスト出力有効化フラグ */
  @Entry public static boolean enableColoredOutput = true;

  /**
   * 全バリデーション実行．
   *
   * <p>サーバー起動時およびリロード時呼出による，各種設定値検証． 無効設定値はデフォルト値へリセット．
   */
  public static void validateAll() {
    // 各バリデーション実行
    validateLocale();
    validateUptimePermissionLevel();
    validateMobcapPermissionLevel();
    validateEnableSendWebhook();
    validateWebhookUrl();
    validateWebhookCronExpression();

    // バリデーション後，変更有無に関わらず設定ファイルへ書き込み
    ServerUtils.LOGGER.info("Writing configuration values to file after validation.");
    MidnightConfig.write(ServerUtils.MOD_ID);
  }

  /** 言語コードバリデーション．無効時はen_USへ戻す． サーバー起動時やリロード時呼出． */
  public static void validateLocale() {
    boolean valid = false;
    for (String opt : localeOptions) {
      if (opt.equals(locale)) {
        valid = true;
        break;
      }
    }
    if (!valid) {
      locale = "en_US";
      ServerUtils.LOGGER.warn("Invalid locale code. Reset to default: en_US");
    }
  }

  /**
   * 権限レベルを検証し，無効な場合はデフォルト値にリセットします．
   *
   * @param level 検証する権限レベル．
   * @param commandName 関連するコマンド名（ログ出力用）．
   * @return 有効な権限レベル．
   */
  private static int validatePermissionLevel(int level, String commandName) {
    if (level < 0 || level > 4) {
      ServerUtils.LOGGER.warn(
          "Invalid permission level for " + commandName + " command. Reset to default: 4 (Owner)");
      return 4;
    }
    return level;
  }

  /**
   * uptimeコマンド実行権限レベルバリデーション．
   *
   * <p>権限レベル0〜4範囲外の場合，デフォルト値4（オーナー権限）へリセット． サーバー起動時やリロード時呼出．
   */
  public static void validateUptimePermissionLevel() {
    uptimePermissionLevel = validatePermissionLevel(uptimePermissionLevel, "uptime");
  }

  /**
   * mobcapコマンド実行権限レベルバリデーション．
   *
   * <p>権限レベル0〜4範囲外の場合，デフォルト値4（オーナー権限）へリセット． サーバー起動時やリロード時呼出．
   */
  public static void validateMobcapPermissionLevel() {
    mobcapPermissionLevel = validatePermissionLevel(mobcapPermissionLevel, "mobcap");
  }

  /**
   * Webhook有効化設定バリデーション．
   *
   * <p>Webhook URL設定との整合性確保．有効だがURL空時は無効化．
   */
  public static void validateEnableSendWebhook() {
    if (enableSendWebhook && (webhookUrl == null || webhookUrl.isEmpty())) {
      enableSendWebhook = false;
      ServerUtils.LOGGER.warn("Webhook is enabled but URL is empty. Disabled webhook sending.");
    }
  }

  /**
   * Webhook URL設定バリデーション．
   *
   * <p>Webhook URLの有効なDiscord Webhook URL形式チェック．
   */
  public static void validateWebhookUrl() {
    if (webhookUrl != null && !webhookUrl.isEmpty()) {
      if (!webhookUrl.startsWith("https://discord.com/api/webhooks/")
          && !webhookUrl.startsWith("https://discordapp.com/api/webhooks/")) {
        // Discord Webhook URL形式でない場合リセット
        webhookUrl = "";
        enableSendWebhook = false;
        ServerUtils.LOGGER.warn(
            "Invalid Discord webhook URL format. Webhook sending has been disabled.");
      }
    }
  }

  /**
   * Webhook cron式バリデーション．
   *
   * <p>cron式構文有効性チェック．無効時はデフォルト値へ戻す．
   */
  public static void validateWebhookCronExpression() {
    if (webhookCronExpression == null || webhookCronExpression.isEmpty()) {
      webhookCronExpression = "0 0 0 * * ?"; // デフォルト値：毎日午前0時
      ServerUtils.LOGGER.warn(
          "Empty cron expression. Reset to default: 0 0 0 * * ? (daily at midnight)");
    } else {
      // 基本的cron式フォーマットチェック
      String[] parts = webhookCronExpression.trim().split("\\s+");
      if (parts.length < 6) {
        webhookCronExpression = "0 0 0 * * ?"; // デフォルト値へリセット
        ServerUtils.LOGGER.warn(
            "Invalid cron expression format. Reset to default: 0 0 0 * * ? (daily at"
                + " midnight)");
      }
    }
  }
}
