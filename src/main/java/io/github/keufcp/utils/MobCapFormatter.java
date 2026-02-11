package io.github.keufcp.utils;

import io.github.keufcp.ServerUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/** MobCap情報の出力フォーマッティングを担当するクラス． */
public class MobCapFormatter {

  /** 全ディメンションのMobCap情報を色付きテキストで作成する． */
  public static Text createColoredAllDimensionsOutput(ServerCommandSource source, boolean debug) {
    ColoredTextBuilder.Builder builder = new ColoredTextBuilder.Builder();
    builder.appendLine(
        ServerUtils.LANG.get("mobcap.title.all"), ColoredTextBuilder.StatusColors.getTitleColor());

    List<RegistryKey<World>> worldKeys = new ArrayList<>(source.getServer().getWorldRegistryKeys());
    for (int i = 0; i < worldKeys.size(); i++) {
      ServerWorld world = source.getServer().getWorld(worldKeys.get(i));
      if (world != null) {
        boolean isLast = (i == worldKeys.size() - 1);
        builder.append(createColoredDimensionMobCapInfo(world, debug, isLast));
      }
    }

    return builder.build();
  }

  /** 単一ディメンションのMobCap情報を色付きテキストで作成する． */
  public static Text createColoredSingleDimensionOutput(
      ServerWorld world, String dimensionDisplayName, boolean debug) {
    ColoredTextBuilder.Builder builder = new ColoredTextBuilder.Builder();
    builder.appendLine(
        ServerUtils.LANG.get("mobcap.title.single", dimensionDisplayName),
        ColoredTextBuilder.StatusColors.getTitleColor());
    builder.append(createColoredDimensionMobCapInfo(world, debug, true));
    return builder.build();
  }

  /** 全ディメンションのMobCap情報をプレーンテキストで作成する． */
  public static String createPlainAllDimensionsOutput(ServerCommandSource source, boolean debug) {
    StringBuilder response = new StringBuilder(ServerUtils.LANG.get("mobcap.title.all") + "\n");

    List<RegistryKey<World>> worldKeys = new ArrayList<>(source.getServer().getWorldRegistryKeys());
    for (int i = 0; i < worldKeys.size(); i++) {
      ServerWorld world = source.getServer().getWorld(worldKeys.get(i));
      if (world != null) {
        boolean isLast = (i == worldKeys.size() - 1);
        appendDimensionMobCapInfo(world, response, debug, isLast);
      }
    }

    return response.toString();
  }

  /** 単一ディメンションのMobCap情報をプレーンテキストで作成する． */
  public static String createPlainSingleDimensionOutput(
      ServerWorld world, String dimensionDisplayName, boolean debug) {
    StringBuilder response =
        new StringBuilder(ServerUtils.LANG.get("mobcap.title.single", dimensionDisplayName) + "\n");
    appendDimensionMobCapInfo(world, response, debug, true);
    return response.toString();
  }

  private static Text createColoredDimensionMobCapInfo(
      ServerWorld world, boolean debug, boolean isLast) {
    MobCapProcessor.MobCapInfo info = MobCapProcessor.getMobCapInfo(world);
    String dimensionDisplayName = MobCapProcessor.getDisplayDimensionName(world);
    String dimensionId = world.getRegistryKey().getValue().toString();

    ColoredTextBuilder.Builder builder = new ColoredTextBuilder.Builder();

    if (!info.hasValidInfo()) {
      return builder
          .appendLine(
              ServerUtils.LANG.get("mobcap.error.spawn_info"),
              ColoredTextBuilder.StatusColors.getErrorColor())
          .build();
    }

    // クリック可能なディメンション名を追加
    builder.append(createClickableDimensionName(dimensionDisplayName, dimensionId));
    builder
        .append(": ", ModStyle.VALUE_NORMAL)
        .append(
            String.valueOf(info.getCurrentMonsterCount()),
            ColoredTextBuilder.MobCapColors.getCurrentCountColor(
                info.getCurrentMonsterCount(), info.getMobCap()))
        .append("/", ModStyle.VALUE_NORMAL)
        .append(
            String.valueOf(info.getMobCap()),
            ColoredTextBuilder.MobCapColors.getCapLimitColor(
                info.getMobCap(), info.getSpawnChunkCount()));

    if (debug) {
      builder
          .newLine()
          .append(
              ServerUtils.LANG.get(
                  "mobcap.debug.base",
                  info.getCapacity(),
                  info.getSpawnChunkCount(),
                  MobCapProcessor.SPAWN_CHUNK_AREA_CONSTANT),
              ModStyle.LABEL);
    }

    if (info.hasZeroChunkWarning()) {
      builder
          .append(" - ", ModStyle.VALUE_NORMAL)
          .append(ServerUtils.LANG.get("mobcap.warning.zero_chunks"), ModStyle.VALUE_WARN);
    }

    if (!isLast) {
      builder.newLine();
    }

    return builder.build();
  }

  /**
   * クリック可能なディメンション名テキストを生成する．
   *
   * @param dimensionDisplayName 表示用ディメンション名
   * @param dimensionId ディメンションID (例: "minecraft:overworld")
   * @return クリック可能なテキスト
   */
  private static Text createClickableDimensionName(
      String dimensionDisplayName, String dimensionId) {
    return Text.literal(dimensionDisplayName)
        .formatted(ModStyle.CLICKABLE)
        .styled(
            style ->
                style.withClickEvent(
                    new net.minecraft.text.ClickEvent(
                        net.minecraft.text.ClickEvent.Action.RUN_COMMAND,
                        "/suMobCap " + dimensionId + " debug")));
  }

  private static void appendDimensionMobCapInfo(
      ServerWorld world, StringBuilder response, boolean debug, boolean isLast) {
    MobCapProcessor.MobCapInfo info = MobCapProcessor.getMobCapInfo(world);
    String dimensionDisplayName = MobCapProcessor.getDisplayDimensionName(world);

    if (!info.hasValidInfo()) {
      response.append(ServerUtils.LANG.get("mobcap.error.spawn_info"));
      return;
    }

    response.append(
        ServerUtils.LANG.get(
            "mobcap.info", dimensionDisplayName, info.getCurrentMonsterCount(), info.getMobCap()));

    if (debug) {
      response
          .append("\n")
          .append(
              ServerUtils.LANG.get(
                  "mobcap.debug.base",
                  info.getCapacity(),
                  info.getSpawnChunkCount(),
                  MobCapProcessor.SPAWN_CHUNK_AREA_CONSTANT));
    }

    if (info.hasZeroChunkWarning()) {
      response.append(" - ").append(ServerUtils.LANG.get("mobcap.warning.zero_chunks"));
    }

    if (!isLast) {
      response.append("\n");
    }
  }
}
