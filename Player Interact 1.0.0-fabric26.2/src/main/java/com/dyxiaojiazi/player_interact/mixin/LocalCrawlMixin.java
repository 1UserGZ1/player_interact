package com.dyxiaojiazi.player_interact.mixin;

import com.dyxiaojiazi.player_interact.ClientInitializer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class LocalCrawlMixin {

    /**
     * 按住 C 键时本地玩家保持趴下（匍匐）状态。
     */
    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void playerInteract$localCrawl(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!(self instanceof LocalPlayer)) return;

        if (ClientInitializer.keyCrawl != null && ClientInitializer.keyCrawl.isDown()) {
            self.setPose(Pose.SWIMMING);
            ci.cancel();
        }
    }
}