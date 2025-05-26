package io.github.keufcp.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import io.github.keufcp.ServerUtils;
import io.github.keufcp.ServerUtilsMidnightConfig;
import io.github.keufcp.utils.ColoredTextBuilder;
import io.github.keufcp.utils.DimensionResolver;
import io.github.keufcp.utils.MobCapFormatter;
import io.github.keufcp.utils.MobCapProcessor;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

/**
 * ServerUtils MobCap情報表示コマンドクラス.
 *
 * <p>/suMobCap コマンドの登録・実行処理を担当し、 サーバー内の各ディメンションのモンスターキャップ情報を表示する。
 */
public class ServerUtilsMobCapCommand {

    /** ディメンション候補を提供するSuggestionProvider. */
    private static final SuggestionProvider<ServerCommandSource> DIMENSION_SUGGESTIONS =
            (context, builder) ->
                    CommandSource.suggestMatching(
                            DimensionResolver.getSuggestions(context.getSource()), builder);

    /** /suMobCap コマンドのコマンドディスパッチャへの登録. */
    public static void register() {
        CommandRegistrationCallback.EVENT.register(ServerUtilsMobCapCommand::registerCommand);
    }

    /** コマンドの詳細登録処理. */
    private static void registerCommand(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                CommandManager.literal("suMobCap")
                        .requires(
                                source ->
                                        source.hasPermissionLevel(
                                                ServerUtilsMidnightConfig.mobcapPermissionLevel))
                        .executes(
                                context -> executeAllDimensionsCommand(context.getSource(), false))
                        .then(
                                CommandManager.literal("debug")
                                        .executes(
                                                context ->
                                                        executeAllDimensionsCommand(
                                                                context.getSource(), true)))
                        .then(
                                CommandManager.argument(
                                                "dimension_alias_or_id",
                                                StringArgumentType.string())
                                        .suggests(DIMENSION_SUGGESTIONS)
                                        .executes(
                                                context ->
                                                        executeSpecificDimensionCommand(
                                                                context.getSource(),
                                                                StringArgumentType.getString(
                                                                        context,
                                                                        "dimension_alias_or_id"),
                                                                false))
                                        .then(
                                                CommandManager.literal("debug")
                                                        .executes(
                                                                context ->
                                                                        executeSpecificDimensionCommand(
                                                                                context.getSource(),
                                                                                StringArgumentType
                                                                                        .getString(
                                                                                                context,
                                                                                                "dimension_alias_or_id"),
                                                                                true)))));
    }

    /** 全ディメンションコマンドの実行. */
    private static int executeAllDimensionsCommand(ServerCommandSource source, boolean debug) {
        Text output;
        if (ColoredTextBuilder.shouldUseColoredText(source)) {
            output = MobCapFormatter.createColoredAllDimensionsOutput(source, debug);
        } else {
            output = Text.literal(MobCapFormatter.createPlainAllDimensionsOutput(source, debug));
        }

        source.sendFeedback(() -> output, false);
        return 1;
    }

    /** 単一ディメンションコマンドの実行. */
    private static int executeSpecificDimensionCommand(
            ServerCommandSource source, String dimensionString, boolean debug)
            throws CommandSyntaxException {
        ServerWorld world = DimensionResolver.resolve(source, dimensionString);

        if (world == null) {
            throw createInvalidDimensionException().create();
        }

        String dimensionDisplayName = MobCapProcessor.getDisplayDimensionName(world);

        Text output;
        if (ColoredTextBuilder.shouldUseColoredText(source)) {
            output =
                    MobCapFormatter.createColoredSingleDimensionOutput(
                            world, dimensionDisplayName, debug);
        } else {
            output =
                    Text.literal(
                            MobCapFormatter.createPlainSingleDimensionOutput(
                                    world, dimensionDisplayName, debug));
        }

        source.sendFeedback(() -> output, false);
        return 1;
    }

    /** 無効なディメンション例外を作成する. */
    private static SimpleCommandExceptionType createInvalidDimensionException() {
        return new SimpleCommandExceptionType(
                Text.literal(ServerUtils.LANG.get("mobcap.error.invalid_dimension")));
    }
}
