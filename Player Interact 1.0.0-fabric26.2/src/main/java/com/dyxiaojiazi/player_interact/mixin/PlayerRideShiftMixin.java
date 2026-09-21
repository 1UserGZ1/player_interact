package com.dyxiaojiazi.player_interact.mixin;

import com.dyxiaojiazi.player_interact.ClientInitializer;
import com.dyxiaojiazi.player_interact.config.ActionType;
import com.dyxiaojiazi.player_interact.manager.ActionManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerRideShiftMixin {

    /**
     * 背起/骑行的乘客按 Shift 原版会自动下车。
     * 服务端 + 客户端都拦，避免"服务端不让下，客户端本地却下了"。
     */
    @Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
    private void playerInteract$blockShiftDismount(CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;
        if (self.level().isClientSide()) {
            // 客户端：用本地状态判断
            if ((ClientInitializer.localActionType == ActionType.CARRY
                    || ClientInitializer.localActionType == ActionType.RIDE)
                    && !ClientInitializer.localIsLeader) {
                cir.setReturnValue(false);
            }
        } else {
            // 服务端：用绑定数据判断
            if (ActionManager.isCarryOrRidePassenger(self.getUUID())) {
                cir.setReturnValue(false);
            }
        }
    }

    /**
     * 双方处于任意绑定关系时，互相攻击无效。
     */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void playerInteract$blockAttackPartner(Entity target, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (self.level().isClientSide()) return;
        if (!(target instanceof Player targetPlayer)) return;

        if (ActionManager.isPartners(self.getUUID(), targetPlayer.getUUID())) {
            ci.cancel();
        }
    }
}