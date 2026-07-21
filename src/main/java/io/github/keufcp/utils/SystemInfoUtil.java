package io.github.keufcp.utils;

import com.mojang.brigadier.context.CommandContext;
import com.sun.management.OperatingSystemMXBean;
import io.github.keufcp.ServerUtils;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import net.minecraft.server.command.ServerCommandSource;

/** システム情報（CPU使用率，メモリ使用率など）を取得し，コマンド実行者に送信するためのユーティリティクラス． JavaのManagement APIを使用して情報を取得． */
public class SystemInfoUtil {

  /**
   * オペレーティングシステムに関する情報を取得するためのMXBeanインスタンス． CPU使用率やシステムロードアベレージの取得に使用．
   *
   * <p>標準インターフェース型で保持する．com.sun.management 拡張への直接キャストで初期化すると，
   * 拡張が存在しないJVMでクラス初期化が失敗し，本クラスの全コマンドが動作しなくなるため．
   */
  private static final java.lang.management.OperatingSystemMXBean osBean =
      ManagementFactory.getOperatingSystemMXBean();

  /** Java仮想マシンのメモリ管理に関する情報を取得するためのMXBeanインスタンス． ヒープメモリおよび非ヒープメモリの使用状況の取得に使用． */
  private static final MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();

  /**
   * 現在のCPU使用率（プロセスおよびシステム全体）をコマンド実行者に送信． プロセスCPU使用率はJVMプロセスのCPU負荷を，システムロードアベレージはシステム全体の負荷を示す．
   *
   * @param context コマンドの実行コンテキスト．メッセージの送信に使用．
   */
  public static void sendCpuInfo(CommandContext<ServerCommandSource> context) {
    try {
      // システムCPU使用率 (0.0 - 1.0 の範囲で，1.0が100%)．取得不能時は負値が返る
      double systemCpuLoad = -1;
      if (osBean instanceof OperatingSystemMXBean sunOsBean) {
        systemCpuLoad = sunOsBean.getCpuLoad();
      }
      // システムロードアベレージ (過去1分間のシステム負荷平均)．取得不能時は負値が返る
      double systemLoadAverage = osBean.getSystemLoadAverage();

      String cpuLoadStr = systemCpuLoad < 0 ? "N/A" : String.format("%.2f%%", systemCpuLoad * 100);
      String loadAvgStr = systemLoadAverage < 0 ? "N/A" : String.format("%.2f", systemLoadAverage);

      StyledText.send(
          context.getSource(),
          ("<gold>CPU Usage:</gold>\n"
                  + "  <aqua>System:</aqua> <white>%s</white>\n"
                  + "  <aqua>System Load Average:</aqua> <white>%s</white>")
              .formatted(cpuLoadStr, loadAvgStr));
    } catch (Exception e) {
      ServerUtils.LOGGER.error("Failed to get CPU info", e);
      StyledText.send(
          context.getSource(),
          "<red>Failed to retrieve CPU information. See server log for details.</red>");
    }
  }

  /**
   * 現在のメモリ使用率（ヒープおよび非ヒープ）をコマンド実行者に送信． 使用量と最大容量を表示．
   *
   * @param context コマンドの実行コンテキスト．メッセージの送信に使用．
   */
  public static void sendMemInfo(CommandContext<ServerCommandSource> context) {
    try {
      MemoryUsage heapMemory = memBean.getHeapMemoryUsage();
      MemoryUsage nonHeapMemory = memBean.getNonHeapMemoryUsage();

      long heapUsed = heapMemory.getUsed();
      long heapMax = heapMemory.getMax();
      long nonHeapUsed = nonHeapMemory.getUsed();
      long nonHeapMax = nonHeapMemory.getMax();

      StyledText.send(
          context.getSource(),
          ("<gold>Memory Usage:</gold>\n"
                  + "  <aqua>Heap:</aqua> <white>%s</white> <gray>/</gray> <white>%s</white>\n"
                  + "  <aqua>Non-Heap:</aqua> <white>%s</white> <gray>/</gray> <white>%s</white>")
              .formatted(
                  formatBytes(heapUsed),
                  formatBytes(heapMax),
                  formatBytes(nonHeapUsed),
                  formatBytes(nonHeapMax)));
    } catch (Exception e) {
      ServerUtils.LOGGER.error("Failed to get Memory info", e);
      StyledText.send(
          context.getSource(),
          "<red>Failed to retrieve Memory information. See server log for details.</red>");
    }
  }

  /**
   * バイト数を読みやすい形式（KB，MB，GBなど）にフォーマット．
   *
   * @param bytes フォーマットするバイト数．
   * @return フォーマットされた文字列．
   */
  private static String formatBytes(long bytes) {
    // MemoryUsage.getMax() は最大値未定義時に -1 を返す
    if (bytes < 0) return "N/A";
    if (bytes < 1024) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    String pre = "KMGTPE".charAt(exp - 1) + "";
    return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
  }
}
