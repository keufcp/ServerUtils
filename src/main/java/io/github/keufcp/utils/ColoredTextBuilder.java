package io.github.keufcp.utils;

import io.github.keufcp.ServerUtilsMidnightConfig;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * 色付きテキスト作成のユーティリティクラス.
 *
 * <p>環境に応じて色付きテキストまたはプレーンテキストを適切に出力し、 再利用可能な色分けロジックを提供する。
 */
public class ColoredTextBuilder {

    /**
     * 色付きテキストの有効性を判定する.
     *
     * @param source コマンドソース
     * @return 色付きテキストが有効な場合はtrue
     */
    public static boolean shouldUseColoredText(ServerCommandSource source) {
        // 設定で無効化されている場合はfalse
        if (!ServerUtilsMidnightConfig.enableColoredOutput) {
            return false;
        }

        // プレイヤーからの実行でない場合（コンソールなど）はfalse
        return source.getEntity() != null;
    }

    /**
     * 環境に応じてテキストを送信する.
     *
     * @param source コマンドソース
     * @param coloredText 色付きテキスト
     * @param plainText プレーンテキスト
     */
    public static void sendFormattedFeedback(
            ServerCommandSource source, Text coloredText, String plainText) {
        if (shouldUseColoredText(source)) {
            source.sendFeedback(() -> coloredText, false);
        } else {
            source.sendFeedback(() -> Text.literal(plainText), false);
        }
    }

    /** 数値比較結果に基づく色分けフォーマットを取得する. */
    public static class ComparisonColors {

        /**
         * 分子と分母の比較に基づく色を取得する.
         *
         * @param numerator 分子
         * @param denominator 分母
         * @return 適切なフォーマット
         */
        public static Formatting getNumeratorColor(long numerator, int denominator) {
            if (denominator == 0) {
                return Formatting.GRAY; // 分母が0の場合
            }

            if (numerator > denominator) {
                return Formatting.RED; // 上限超過
            } else {
                return Formatting.GREEN; // 正常範囲
            }
        }

        /**
         * 分母の状態に基づく色を取得する.
         *
         * @param denominator 分母
         * @param isValidState 有効な状態かどうか
         * @return 適切なフォーマット
         */
        public static Formatting getDenominatorColor(int denominator, boolean isValidState) {
            if (denominator == 0) {
                if (isValidState) {
                    return Formatting.GRAY; // 正常な0状態
                } else {
                    return Formatting.RED; // 異常な0状態
                }
            } else {
                return Formatting.WHITE; // 正常値
            }
        }
    }

    /** パーセンテージベースの色分けフォーマットを取得する. */
    public static class PercentageColors {

        /**
         * パーセンテージに基づく色を取得する.
         *
         * @param percentage パーセンテージ（0.0 - 1.0+）
         * @return 適切なフォーマット
         */
        public static Formatting getPercentageColor(double percentage) {
            if (percentage >= 1.0) {
                return Formatting.RED; // 100%以上
            } else if (percentage >= 0.9) {
                return Formatting.YELLOW; // 90%以上
            } else if (percentage >= 0.7) {
                return Formatting.GREEN; // 70%以上
            } else if (percentage >= 0.5) {
                return Formatting.DARK_GREEN; // 50%以上
            } else {
                return Formatting.AQUA; // 50%未満
            }
        }
    }

    /** 状態に基づく色分けフォーマットを取得する. */
    public static class StatusColors {

        /**
         * エラーメッセージの色を取得する.
         *
         * @return エラー用フォーマット
         */
        public static Formatting getErrorColor() {
            return Formatting.RED;
        }

        /**
         * 警告メッセージの色を取得する.
         *
         * @return 警告用フォーマット
         */
        public static Formatting getWarningColor() {
            return Formatting.YELLOW;
        }

        /**
         * 情報メッセージの色を取得する.
         *
         * @return 情報用フォーマット
         */
        public static Formatting getInfoColor() {
            return Formatting.AQUA;
        }

        /**
         * 成功メッセージの色を取得する.
         *
         * @return 成功用フォーマット
         */
        public static Formatting getSuccessColor() {
            return Formatting.GREEN;
        }

        /**
         * 無効/無効化状態の色を取得する.
         *
         * @return 無効用フォーマット
         */
        public static Formatting getDisabledColor() {
            return Formatting.GRAY;
        }

        /**
         * 通常テキストの色を取得する.
         *
         * @return 通常用フォーマット
         */
        public static Formatting getNormalColor() {
            return Formatting.WHITE;
        }
    }

    /** MobCap専用の色分けロジック. */
    public static class MobCapColors {

        /**
         * MobCap使用状況に基づく分子の色を取得する.
         *
         * @param current 現在のモンスター数
         * @param cap MobCap上限
         * @return 適切なフォーマット
         */
        public static Formatting getCurrentCountColor(long current, int cap) {
            return ComparisonColors.getNumeratorColor(current, cap);
        }

        /**
         * MobCap状況に基づく分母の色を取得する.
         *
         * @param cap MobCap値
         * @param spawnChunkCount スポーンチャンク数
         * @return 適切なフォーマット
         */
        public static Formatting getCapLimitColor(int cap, int spawnChunkCount) {
            return ComparisonColors.getDenominatorColor(cap, spawnChunkCount == 0);
        }

        /**
         * MobCap情報の表示テキストを作成する.
         *
         * @param current 現在のモンスター数
         * @param cap MobCap上限
         * @param spawnChunkCount スポーンチャンク数
         * @return 色付きテキスト
         */
        public static Text createMobCapDisplay(long current, int cap, int spawnChunkCount) {
            return Text.empty()
                    .append(Text.literal("  "))
                    .append(
                            Text.literal(String.valueOf(current))
                                    .formatted(getCurrentCountColor(current, cap)))
                    .append(Text.literal("/").formatted(StatusColors.getNormalColor()))
                    .append(
                            Text.literal(String.valueOf(cap))
                                    .formatted(getCapLimitColor(cap, spawnChunkCount)));
        }
    }

    /**
     * フォーマット済みテキストビルダー.
     *
     * <p>メソッドチェーンで色付きテキストを構築する。
     */
    public static class Builder {
        private final MutableText text;

        public Builder() {
            this.text = Text.empty();
        }

        public Builder append(String content, Formatting formatting) {
            text.append(Text.literal(content).formatted(formatting));
            return this;
        }

        public Builder append(String content) {
            text.append(Text.literal(content));
            return this;
        }

        public Builder append(Text textComponent) {
            text.append(textComponent);
            return this;
        }

        public Builder appendLine(String content, Formatting formatting) {
            text.append(Text.literal(content).formatted(formatting));
            text.append(Text.literal("\n"));
            return this;
        }

        public Builder appendLine(String content) {
            text.append(Text.literal(content));
            text.append(Text.literal("\n"));
            return this;
        }

        public Builder newLine() {
            text.append(Text.literal("\n"));
            return this;
        }

        public Text build() {
            return text;
        }
    }
}
