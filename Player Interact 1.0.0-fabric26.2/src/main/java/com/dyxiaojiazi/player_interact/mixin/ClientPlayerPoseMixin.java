package com.dyxiaojiazi.player_interact.mixin;

import com.dyxiaojiazi.player_interact.ClientInitializer;
import com.dyxiaojiazi.player_interact.config.ActionType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class ClientPlayerPoseMixin {

    /**
     * 客户端本地维持骑行发起者的趴下姿势，防止被本地 updatePlayerPose 改回。
     */
    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void playerInteract$clientForcePose(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!(self instanceof LocalPlayer)) return;

        if (ClientInitializer.localIsLeader && ClientInitializer.localActionType == ActionType.RIDE) {
            self.setPose(Pose.SWIMMING);
            ci.cancel();
        }
    }
}