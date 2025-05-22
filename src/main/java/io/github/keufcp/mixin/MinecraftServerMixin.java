package io.github.keufcp.mixin;

import io.github.keufcp.utils.TickTimeUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MinecraftServerクラスへのMixin．
 * サーバーTick処理にフックし，Tick時間を記録．
 */
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    // スレッドローカルでTick開始時間を保持
    private static final ThreadLocal<Long> tickStartTime = new ThreadLocal<>();

    /**
     * サーバーTick毎処理メソッドの先頭へのインジェクション．
     * Tick開始時間を記録．
     *
     * @param ci コールバック情報
     */
    @Inject(at = @At("HEAD"), method = "tick")
    private void onTickStart(CallbackInfo ci) {
        // Tick開始時間を記録
        tickStartTime.set(System.nanoTime());
    }

    /**
     * サーバーTick毎処理メソッドの末尾へのインジェクション．
     * Tick処理時間を計算し {@link TickTimeUtil} へ追加．
     *
     * @param ci コールバック情報
     */
    @Inject(at = @At("TAIL"), method = "tick")
    private void onTickEnd(CallbackInfo ci) {
        Long startTime = tickStartTime.get();
        if (startTime != null) {
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            TickTimeUtil.addTickTime(duration);
            // 次のTickのためにクリア (任意だが推奨)
            tickStartTime.remove();
        }
    }
}
