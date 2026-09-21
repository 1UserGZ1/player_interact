package com.dyxiaojiazi.player_interact.mixin;

import com.dyxiaojiazi.player_interact.manager.ActionManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class PassengerBoxMixin {

    /**
     * 背起 / 骑行 时，让乘客的判定箱变成一个极小的点，
     * 让发起者的射线穿透乘客，可以正常挖方块 / 攻击 / 交互。
     *
     * 注意：只在服务端缩判定箱。
     * 客户端缩判定箱会让乘客模型被视锥体裁剪掉，导致"透明人"。
     */
    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    private void playerInteract$shrinkFollowerBox(CallbackInfoReturnable<AABB> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player)) return;

        // 只在服务端缩
        if (self.level().isClientSide()) return;

        if (ActionManager.isCarryOrRidePassenger(player.getUUID())) {
            double cx = player.getX();
            double cy = player.getY() + 0.01;
            double cz = player.getZ();
            cir.setReturnValue(new AABB(cx - 0.01, cy, cz - 0.01, cx + 0.01, cy + 0.02, cz + 0.01));
        }
    }
}