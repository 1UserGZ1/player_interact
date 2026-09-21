package com.dyxiaojiazi.player_interact.mixin;

import com.dyxiaojiazi.player_interact.manager.ActionManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class PlayerCollisionMixin {

    /**
     * 正在交互中的玩家之间不产生推挤。
     * 这样背起/骑行/牵手时不会因为碰撞箱把对方推开。
     */
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void onIsPushable(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Player player && ActionManager.isPlayerBound(player.getUUID())) {
            cir.setReturnValue(false);
        }
    }
}