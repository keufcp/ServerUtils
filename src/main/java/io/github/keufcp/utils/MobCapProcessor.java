package io.github.keufcp.utils;

import io.github.keufcp.ServerUtils;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;

/** MobCap情報の取得と処理を担当するクラス． */
public class MobCapProcessor {

  public static final int SPAWN_CHUNK_AREA_CONSTANT = 17 * 17; // スポーンチャンクエリアの定数 (289)

  /**
   * ディメンションのMobCap情報を取得する．
   *
   * @param world 対象のServerWorld
   * @return MobCap情報オブジェクト
   */
  public static MobCapInfo getMobCapInfo(ServerWorld world) {
    SpawnHelper.Info spawnHelperInfo = world.getChunkManager().getSpawnInfo();

    if (spawnHelperInfo == null) {
      return new MobCapInfo(0, 0, 0, SpawnGroup.MONSTER.getCapacity(), false, false);
    }

    long currentMonsterCount =
        spawnHelperInfo.getGroupToCount().getOrDefault(SpawnGroup.MONSTER, 0);
    int spawnChunkCount = spawnHelperInfo.getSpawningChunkCount();
    int capacity = SpawnGroup.MONSTER.getCapacity();

    int mobCap = calculateMobCap(spawnChunkCount, capacity);
    boolean hasZeroChunkWarning = (spawnChunkCount == 0);

    return new MobCapInfo(
        currentMonsterCount, mobCap, spawnChunkCount, capacity, true, hasZeroChunkWarning);
  }

  /**
   * ディメンション表示名を取得する．
   *
   * @param world 対象のServerWorld
   * @return 表示用ディメンション名
   */
  public static String getDisplayDimensionName(ServerWorld world) {
    Identifier dimensionId = world.getRegistryKey().getValue();

    // 標準ディメンションの場合は翻訳キーを使用
    if (dimensionId.equals(World.OVERWORLD.getValue())) {
      return ServerUtils.LANG.get("mobcap.dimension.overworld");
    } else if (dimensionId.equals(World.NETHER.getValue())) {
      return ServerUtils.LANG.get("mobcap.dimension.nether");
    } else if (dimensionId.equals(World.END.getValue())) {
      return ServerUtils.LANG.get("mobcap.dimension.end");
    }

    // カスタムディメンションの場合は短縮表示
    String namespace = dimensionId.getNamespace();
    String path = dimensionId.getPath();

    // デフォルトnamespace（minecraft）の場合は省略
    if ("minecraft".equals(namespace)) {
      return path;
    }

    return dimensionId.toString();
  }

  private static int calculateMobCap(int spawnChunkCount, int capacity) {
    if (SPAWN_CHUNK_AREA_CONSTANT > 0 && spawnChunkCount > 0) {
      return capacity * spawnChunkCount / SPAWN_CHUNK_AREA_CONSTANT;
    }
    return 0;
  }

  /** MobCap情報を格納するデータクラス． */
  public static class MobCapInfo {
    private final long currentMonsterCount;
    private final int mobCap;
    private final int spawnChunkCount;
    private final int capacity;
    private final boolean hasValidInfo;
    private final boolean hasZeroChunkWarning;

    public MobCapInfo(
        long currentMonsterCount,
        int mobCap,
        int spawnChunkCount,
        int capacity,
        boolean hasValidInfo,
        boolean hasZeroChunkWarning) {
      this.currentMonsterCount = currentMonsterCount;
      this.mobCap = mobCap;
      this.spawnChunkCount = spawnChunkCount;
      this.capacity = capacity;
      this.hasValidInfo = hasValidInfo;
      this.hasZeroChunkWarning = hasZeroChunkWarning;
    }

    public long getCurrentMonsterCount() {
      return currentMonsterCount;
    }

    public int getMobCap() {
      return mobCap;
    }

    public int getSpawnChunkCount() {
      return spawnChunkCount;
    }

    public int getCapacity() {
      return capacity;
    }

    public boolean hasValidInfo() {
      return hasValidInfo;
    }

    public boolean hasZeroChunkWarning() {
      return hasZeroChunkWarning;
    }
  }
}
