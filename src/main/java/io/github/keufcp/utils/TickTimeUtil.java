package io.github.keufcp.utils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * サーバーのTick時間関連ユーティリティクラス．
 * TPS (Ticks Per Second) および MSPT (Milliseconds Per Tick) の計算機能提供．
 */
public class TickTimeUtil {

    /** Minecraftデフォルト目標TPS */
    private static final int TPS_TARGET = 20;
    /** Tick時間キャッシュ数 */
    private static final int TICK_TIME_SAMPLES = 100;
    /** Tick時間キャッシュ */
    private static final Queue<Long> recentTickTimes = new ConcurrentLinkedQueue<>();

    /**
     * サーバー現在TPS計算．
     *
     * @return 現在のTPS値
     */
    public static double calculateTPS() {
        double meanTickTime = getMeanTickTime();

        if (meanTickTime == 0.0) {
            return TPS_TARGET; // データがない場合は目標TPSを返す
        }

        // MSPTから理論TPSを計算
        double theoreticalTps = 1000.0 / meanTickTime;

        // TPSは目標TPSを超えないようにする
        return Math.min(theoreticalTps, TPS_TARGET);
    }

    /**
     * 平均Tick時間取得．
     *
     * @return 平均Tick時間（ミリ秒，小数点含む可能性有）
     */
    public static double getMeanTickTime() {
        long sum = 0;
        int sampleCount = 0;

        for (long time : recentTickTimes) {
            sum += time;
            sampleCount++;
        }

        if (sampleCount == 0) {
            return 50.0; // デフォルト値
        }

        // ナノ秒からミリ秒へ変換（1,000,000.0で除算し小数点保持）
        return sum / (double) sampleCount / 1_000_000.0;
    }

    /**
     * Tick時間キャッシュ追加．
     *
     * @param tickTime Tick時間（ナノ秒）
     */
    public static void addTickTime(long tickTime) {
        recentTickTimes.add(tickTime);
        while (recentTickTimes.size() > TICK_TIME_SAMPLES) {
            recentTickTimes.remove();
        }
    }
}
