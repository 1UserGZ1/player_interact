package com.dyxiaojiazi.player_interact.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    /**
     * 绕过原版限制：玩家类型 canSerialize() 返回 false，导致玩家无法作为载具。
     * 只针对 EntityTypes.PLAYER 返回 true，其它实体保持原逻辑。
     */
    @WrapOperation(
            method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z")
    )
    private boolean playerInteract$allowRidingPlayers(EntityType<?> instance, Operation<Boolean> original) {
        if (instance == EntityTypes.PLAYER) {
            return true;
        }
        return original.call(instance);
    }

    /**
     * 玩家作为载具时，必须手动同步乘客列表给客户端。
     * 原版只对非玩家载具自动同步。
     */
    @Inject(method = "addPassenger", at = @At("TAIL"))
    private void playerInteract$onAddPassenger(Entity passenger, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide() && self instanceof ServerPlayer sp) {
            sp.connection.send(new ClientboundSetPassengersPacket(self));
        }
    }

    @Inject(method = "removePassenger", at = @At("TAIL"))
    private void playerInteract$onRemovePassenger(Entity passenger, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide() && self instanceof ServerPlayer sp) {
            sp.connection.send(new ClientboundSetPassengersPacket(self));
        }
    }
}