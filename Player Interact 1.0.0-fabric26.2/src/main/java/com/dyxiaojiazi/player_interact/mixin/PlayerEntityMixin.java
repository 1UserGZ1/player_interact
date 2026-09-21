package com.dyxiaojiazi.player_interact.mixin;

import com.dyxiaojiazi.player_interact.manager.ActionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!(self instanceof ServerPlayer serverPlayer)) return;

        // 玩家死亡时清理邀请与绑定
        if (!serverPlayer.isAlive()) {
            ActionManager.cancelPlayerInvite(serverPlayer);
            ActionManager.releaseBinding(serverPlayer);
        }
    }
}