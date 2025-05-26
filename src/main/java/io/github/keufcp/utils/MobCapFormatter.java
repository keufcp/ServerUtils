package io.github.keufcp.utils;

import io.github.keufcp.ServerUtils;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** MobCap情報の出力フォーマッティングを担当するクラス. */
public class MobCapFormatter {

    /** 全ディメンションのMobCap情報を色付きテキストで作成する. */
    public static Text createColoredAllDimensionsOutput(ServerCommandSource source, boolean debug) {
        ColoredTextBuilder.Builder builder = new ColoredTextBuilder.Builder();
        builder.appendLine(
                ServerUtils.LANG.get("mobcap.title.all"),
                ColoredTextBuilder.StatusColors.getInfoColor());

        List<RegistryKey<World>> worldKeys =
                new ArrayList<>(source.getServer().getWorldRegistryKeys());
        for (int i = 0; i < worldKeys.size(); i++) {
            ServerWorld world = source.getServer().getWorld(worldKeys.get(i));
            if (world != null) {
                boolean isLast = (i == worldKeys.size() - 1);
                builder.append(createColoredDimensionMobCapInfo(world, debug, isLast));
                if (!isLast) {
                    builder.newLine();
                }
            }
        }

        return builder.build();
    }

    /** 単一ディメンションのMobCap情報を色付きテキストで作成する. */
    public static Text createColoredSingleDimensionOutput(
            ServerWorld world, String dimensionDisplayName, boolean debug) {
        ColoredTextBuilder.Builder builder = new ColoredTextBuilder.Builder();
        builder.appendLine(
                ServerUtils.LANG.get("mobcap.title.single", dimensionDisplayName),
                ColoredTextBuilder.StatusColors.getInfoColor());
        builder.append(createColoredDimensionMobCapInfo(world, debug, true));
        return builder.build();
    }

    /** 全ディメンションのMobCap情報をプレーンテキストで作成する. */
    public static String createPlainAllDimensionsOutput(ServerCommandSource source, boolean debug) {
        StringBuilder response = new StringBuilder(ServerUtils.LANG.get("mobcap.title.all") + "\n");

        List<RegistryKey<World>> worldKeys =
                new ArrayList<>(source.getServer().getWorldRegistryKeys());
        for (int i = 0; i < worldKeys.size(); i++) {
            ServerWorld world = source.getServer().getWorld(worldKeys.get(i));
            if (world != null) {
                boolean isLast = (i == worldKeys.size() - 1);
                appendDimensionMobCapInfo(world, response, debug, isLast);
            }
        }

        return response.toString();
    }

    /** 単一ディメンションのMobCap情報をプレーンテキストで作成する. */
    public static String createPlainSingleDimensionOutput(
            ServerWorld world, String dimensionDisplayName, boolean debug) {
        StringBuilder response =
                new StringBuilder(
                        ServerUtils.LANG.get("mobcap.title.single", dimensionDisplayName) + "\n");
        appendDimensionMobCapInfo(world, response, debug, true);
        return response.toString();
    }

    private static Text createColoredDimensionMobCapInfo(
            ServerWorld world, boolean debug, boolean isLast) {
        MobCapProcessor.MobCapInfo info = MobCapProcessor.getMobCapInfo(world);
        String dimensionDisplayName = MobCapProcessor.getDisplayDimensionName(world);

        ColoredTextBuilder.Builder builder = new ColoredTextBuilder.Builder();

        if (!info.hasValidInfo()) {
            return builder.appendLine(
                            ServerUtils.LANG.get("mobcap.error.spawn_info"),
                            ColoredTextBuilder.StatusColors.getErrorColor())
                    .build();
        }

        builder.appendLine(
                ServerUtils.LANG.get("mobcap.dimension", dimensionDisplayName),
                ColoredTextBuilder.StatusColors.getInfoColor());

        if (debug) {
            appendDebugInfo(builder, info, isLast);
        } else {
            appendBasicInfo(builder, info, isLast);
        }

        if (info.hasZeroChunkWarning()) {
            builder.append(
                    ServerUtils.LANG.get("mobcap.warning.zero_chunks"),
                    ColoredTextBuilder.StatusColors.getWarningColor());
            if (!isLast) {
                builder.newLine();
            }
        }

        return builder.build();
    }

    private static void appendDebugInfo(
            ColoredTextBuilder.Builder builder, MobCapProcessor.MobCapInfo info, boolean isLast) {
        builder.append("  ")
                .append(
                        String.valueOf(info.getCurrentMonsterCount()),
                        ColoredTextBuilder.MobCapColors.getCurrentCountColor(
                                info.getCurrentMonsterCount(), info.getMobCap()))
                .append("/")
                .append(
                        String.valueOf(info.getMobCap()),
                        ColoredTextBuilder.MobCapColors.getCapLimitColor(
                                info.getMobCap(), info.getSpawnChunkCount()))
                .append(
                        ServerUtils.LANG.get("mobcap.debug.parentheses.open"),
                        ColoredTextBuilder.StatusColors.getNormalColor())
                .append(
                        ServerUtils.LANG.get("mobcap.debug.base_capacity"),
                        ColoredTextBuilder.StatusColors.getNormalColor())
                .append(
                        String.valueOf(info.getCapacity()),
                        ColoredTextBuilder.StatusColors.getDisabledColor())
                .append(
                        ServerUtils.LANG.get("mobcap.debug.separator"),
                        ColoredTextBuilder.StatusColors.getNormalColor())
                .append(
                        ServerUtils.LANG.get("mobcap.debug.active_chunks"),
                        ColoredTextBuilder.StatusColors.getNormalColor())
                .append(
                        String.valueOf(info.getSpawnChunkCount()),
                        ColoredTextBuilder.StatusColors.getDisabledColor())
                .append(
                        ServerUtils.LANG.get("mobcap.debug.separator"),
                        ColoredTextBuilder.StatusColors.getNormalColor())
                .append(
                        ServerUtils.LANG.get("mobcap.debug.max_spawn_chunks"),
                        ColoredTextBuilder.StatusColors.getNormalColor())
                .append(
                        String.valueOf(MobCapProcessor.SPAWN_CHUNK_AREA_CONSTANT),
                        ColoredTextBuilder.StatusColors.getDisabledColor())
                .append(
                        ServerUtils.LANG.get("mobcap.debug.parentheses.close"),
                        ColoredTextBuilder.StatusColors.getNormalColor());

        if (!isLast || info.hasZeroChunkWarning()) {
            builder.newLine();
        }
    }

    private static void appendBasicInfo(
            ColoredTextBuilder.Builder builder, MobCapProcessor.MobCapInfo info, boolean isLast) {
        builder.append(
                ColoredTextBuilder.MobCapColors.createMobCapDisplay(
                        info.getCurrentMonsterCount(),
                        info.getMobCap(),
                        info.getSpawnChunkCount()));

        if (!isLast || info.hasZeroChunkWarning()) {
            builder.newLine();
        }
    }

    private static void appendDimensionMobCapInfo(
            ServerWorld world, StringBuilder response, boolean debug, boolean isLast) {
        MobCapProcessor.MobCapInfo info = MobCapProcessor.getMobCapInfo(world);
        String dimensionDisplayName = MobCapProcessor.getDisplayDimensionName(world);

        if (!info.hasValidInfo()) {
            response.append(ServerUtils.LANG.get("mobcap.error.spawn_info"));
            if (!isLast) {
                response.append("\n");
            }
            return;
        }

        response.append(ServerUtils.LANG.get("mobcap.dimension", dimensionDisplayName))
                .append("\n");

        if (debug) {
            response.append(
                    ServerUtils.LANG.get(
                            "mobcap.info.debug",
                            info.getCurrentMonsterCount(),
                            info.getMobCap(),
                            info.getCapacity(),
                            info.getSpawnChunkCount(),
                            MobCapProcessor.SPAWN_CHUNK_AREA_CONSTANT));
        } else {
            response.append(
                    ServerUtils.LANG.get(
                            "mobcap.info", info.getCurrentMonsterCount(), info.getMobCap()));
        }

        if (!isLast || info.hasZeroChunkWarning()) {
            response.append("\n");
        }

        if (info.hasZeroChunkWarning()) {
            response.append(ServerUtils.LANG.get("mobcap.warning.zero_chunks"));
            if (!isLast) {
                response.append("\n");
            }
        }
    }
}
