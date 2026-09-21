package com.dyxiaojiazi.player_interact.mixin;

import com.dyxiaojiazi.player_interact.manager.ActionManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class FollowerStepMixin {

    /**
     * 牵手跟随者自动爬坡：把 maxUpStep 从 0.6 提升到 1.0（马的高度）。
     * 只对牵手绑定的接受者生效，其他情况不变。
     */
    @Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true)
    private void playerInteract$increaseStepHeight(CallbackInfoReturnable<Float> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player)) return;

        if (ActionManager.isHoldHandsFollower(player.getUUID())) {
            cir.setReturnValue(1.0f);
        }
    }
}