package io.github.keufcp.utils;

import io.github.keufcp.ServerUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * MobCap情報の出力フォーマッティングを担当するクラス．
 *
 * <p>QuickTextタグ記法のテンプレート文字列を生成する．色付き/プレーンの切り替えは {@link StyledText#send} が行う．
 */
public class MobCapFormatter {

  /** 全ディメンションのMobCap情報のテンプレートを作成する． */
  public static String buildAllDimensionsTemplate(ServerCommandSource source, boolean debug) {
    StringBuilder sb = new StringBuilder();
    sb.append("<dark_purple>")
        .append(ServerUtils.LANG.get("mobcap.title.all"))
        .append("</dark_purple>\n");

    List<RegistryKey<World>> worldKeys = new ArrayList<>(source.getServer().getWorldRegistryKeys());
    for (int i = 0; i < worldKeys.size(); i++) {
      ServerWorld world = source.getServer().getWorld(worldKeys.get(i));
      if (world != null) {
        sb.append(buildDimensionTemplate(world, debug));
        if (i < worldKeys.size() - 1) {
          sb.append("\n");
        }
      }
    }

    return sb.toString();
  }

  /** 単一ディメンションのMobCap情報のテンプレートを作成する． */
  public static String buildSingleDimensionTemplate(
      ServerWorld world, String dimensionDisplayName, boolean debug) {
    return "<dark_purple>"
        + ServerUtils.LANG.get("mobcap.title.single", dimensionDisplayName)
        + "</dark_purple>\n"
        + buildDimensionTemplate(world, debug);
  }

  /** 1ディメンション分のMobCap情報テンプレートを作成する．ディメンション名クリックでdebug表示を実行できる． */
  private static String buildDimensionTemplate(ServerWorld world, boolean debug) {
    MobCapProcessor.MobCapInfo info = MobCapProcessor.getMobCapInfo(world);

    if (!info.hasValidInfo()) {
      return "<red>" + ServerUtils.LANG.get("mobcap.error.spawn_info") + "</red>";
    }

    String dimensionDisplayName = MobCapProcessor.getDisplayDimensionName(world);
    String dimensionId = world.getRegistryKey().getValue().toString();
    String countColor = StyledText.mobCountColor(info.getCurrentMonsterCount(), info.getMobCap());
    String capColor = StyledText.mobCapColor(info.getMobCap(), info.getSpawnChunkCount());

    StringBuilder sb = new StringBuilder();
    sb.append(
            "<aqua><run_cmd '/suMobCap \"%s\" debug'>%s</run_cmd></aqua>"
                .formatted(dimensionId, dimensionDisplayName))
        .append("<white>: </white>")
        .append("<%1$s>%2$d</%1$s>".formatted(countColor, info.getCurrentMonsterCount()))
        .append("<white>/</white>")
        .append("<%1$s>%2$d</%1$s>".formatted(capColor, info.getMobCap()));

    if (debug) {
      sb.append("\n<gray>")
          .append(
              ServerUtils.LANG.get(
                  "mobcap.debug.base",
                  info.getCapacity(),
                  info.getSpawnChunkCount(),
                  MobCapProcessor.SPAWN_CHUNK_AREA_CONSTANT))
          .append("</gray>");
    }

    if (info.hasZeroChunkWarning()) {
      sb.append("<white> - </white><yellow>")
          .append(ServerUtils.LANG.get("mobcap.warning.zero_chunks"))
          .append("</yellow>");
    }

    return sb.toString();
  }
}
