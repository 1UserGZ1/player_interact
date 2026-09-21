package com.dyxiaojiazi.player_interact.mixin;

import com.dyxiaojiazi.player_interact.config.ActionType;
import com.dyxiaojiazi.player_interact.manager.ActionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class PassengerPositionMixin {

    /**
     * 自定义乘客座位位置。
     * - CARRY: 骑头，高度 1.8，后方 0.4（避免脚挡视线）
     * - RIDE:  骑背，高度 0.8，后方 0.4
     * - HOLD_HANDS: 走 ActionManager.applyHoldHandsAnchor，此分支不使用
     */
    @Inject(method = "getPassengerRidingPosition", at = @At("HEAD"), cancellable = true)
    private void playerInteract$onGetPassengerRidingPosition(Entity passenger, CallbackInfoReturnable<Vec3> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof ServerPlayer leader)) return;
        if (!(passenger instanceof ServerPlayer follower)) return;

        for (ActionManager.Binding b : ActionManager.getActiveBindings()) {
            if (b.leader.equals(leader.getUUID()) && b.follower.equals(follower.getUUID())) {
                Vec3 base = leader.position();
                double yaw = Math.toRadians(leader.yBodyRot);
                double sin = Math.sin(yaw), cos = Math.cos(yaw);

                Vec3 offset = switch (b.type) {
                    case HOLD_HANDS -> null;
                    case CARRY      -> new Vec3(-sin * 0.4, 1.8, cos * 0.4);
                    case RIDE       -> new Vec3(-sin * 0.4, 0.8, cos * 0.4);
                };
                if (offset != null) {
                    cir.setReturnValue(base.add(offset));
                }
                return;
            }
        }
    }
}