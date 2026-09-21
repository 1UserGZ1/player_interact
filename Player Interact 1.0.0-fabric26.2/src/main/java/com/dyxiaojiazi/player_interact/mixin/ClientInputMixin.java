package com.dyxiaojiazi.player_interact.mixin;

import com.dyxiaojiazi.player_interact.ClientInitializer;
import net.minecraft.client.player.ClientInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientInput.class)
public class ClientInputMixin {

    /**
     * 跟随状态下，取消本地移动输入的 tick 更新。
     * 这样客户端就不会再把自己的位置发给服务端，避免与服务端下发坐标冲突。
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if (ClientInitializer.shouldBlockInput) {
            ci.cancel();
        }
    }
}