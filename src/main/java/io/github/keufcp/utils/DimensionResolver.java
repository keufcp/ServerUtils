package io.github.keufcp.utils;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** ディメンション文字列からServerWorldオブジェクトを解決するユーティリティクラス. */
public class DimensionResolver {

    private static final Map<String, RegistryKey<World>> DIMENSION_ALIASES =
            Map.of(
                    "o", World.OVERWORLD,
                    "overworld", World.OVERWORLD,
                    "n", World.NETHER,
                    "nether", World.NETHER,
                    "e", World.END,
                    "end", World.END);

    /**
     * ディメンション文字列からServerWorldオブジェクトを解決する.
     *
     * @param source コマンドソース
     * @param dimString ディメンション指定文字列
     * @return 解決されたServerWorldオブジェクト、見つからない場合はnull
     */
    public static ServerWorld resolve(ServerCommandSource source, String dimString) {
        String lowerDimString = dimString.toLowerCase();

        // エイリアスでの検索
        RegistryKey<World> worldKey = DIMENSION_ALIASES.get(lowerDimString);
        if (worldKey != null) {
            return source.getServer().getWorld(worldKey);
        }

        // 完全なIdentifierでの検索
        try {
            Identifier id = Identifier.tryParse(dimString);
            if (id != null) {
                worldKey = findWorldKeyById(source, id);
                if (worldKey != null) {
                    return source.getServer().getWorld(worldKey);
                }
            }
        } catch (Exception ignored) {
            // パース失敗時は null を返す
        }

        return null;
    }

    /**
     * ディメンション候補のリストを取得する.
     *
     * @param source コマンドソース
     * @return ディメンション候補のリスト
     */
    public static List<String> getSuggestions(ServerCommandSource source) {
        List<String> suggestions = new ArrayList<>();

        // エイリアス候補を追加
        suggestions.addAll(DIMENSION_ALIASES.keySet());

        // 利用可能なディメンションのIdentifierを追加
        for (RegistryKey<World> worldKey : source.getServer().getWorldRegistryKeys()) {
            suggestions.add(worldKey.getValue().toString());
        }

        return suggestions;
    }

    private static RegistryKey<World> findWorldKeyById(ServerCommandSource source, Identifier id) {
        return source.getServer().getWorldRegistryKeys().stream()
                .filter(key -> key.getValue().equals(id))
                .findFirst()
                .orElse(null);
    }
}
