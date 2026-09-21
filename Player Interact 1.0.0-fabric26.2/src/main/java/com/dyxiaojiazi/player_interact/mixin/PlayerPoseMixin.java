package com.dyxiaojiazi.player_interact.mixin;

import com.dyxiaojiazi.player_interact.config.ActionType;
import com.dyxiaojiazi.player_interact.manager.ActionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerPoseMixin {

    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void playerInteract$forcePose(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!(self instanceof ServerPlayer sp)) return;

        for (ActionManager.Binding b : ActionManager.getActiveBindings()) {
            if (b.leader.equals(sp.getUUID())) {
                if (b.type == ActionType.RIDE) {
                    sp.setPose(Pose.SWIMMING);   // 骑行：发起者趴下
                } else {
                    sp.setPose(Pose.STANDING);   // 牵手 / 背背：站立
                }
                ci.cancel();
                return;
            }
        }
    }
}