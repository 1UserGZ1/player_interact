package com.dyxiaojiazi.player_interact.mixin;

import com.dyxiaojiazi.player_interact.config.ActionType;
import com.dyxiaojiazi.player_interact.manager.ActionManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class RideSpeedMixin {

    private static final Identifier SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("player_interact", "ride_speed");
    private static final Identifier JUMP_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("player_interact", "ride_jump");

    @Inject(method = "tick", at = @At("TAIL"))
    private void playerInteract$rideAttributes(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!(self instanceof ServerPlayer sp)) return;

        boolean isRiding = false;
        for (ActionManager.Binding b : ActionManager.getActiveBindings()) {
            if (b.leader.equals(sp.getUUID()) && b.type == ActionType.RIDE) {
                isRiding = true;
                break;
            }
        }

        // 速度：抵消趴下减速，目标约 6 格/秒
        AttributeInstance speed = sp.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            if (isRiding) {
                if (speed.getModifier(SPEED_MODIFIER_ID) == null) {
                    speed.addTransientModifier(new AttributeModifier(
                            SPEED_MODIFIER_ID, 3.6,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ));
                }
            } else {
                if (speed.getModifier(SPEED_MODIFIER_ID) != null) {
                    speed.removeModifier(SPEED_MODIFIER_ID);
                }
            }
        }

        // 跳跃：目标约 1.5 格高
        AttributeInstance jump = sp.getAttribute(Attributes.JUMP_STRENGTH);
        if (jump != null) {
            if (isRiding) {
                if (jump.getModifier(JUMP_MODIFIER_ID) == null) {
                    jump.addTransientModifier(new AttributeModifier(
                            JUMP_MODIFIER_ID, 0.17,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ));
                }
            } else {
                if (jump.getModifier(JUMP_MODIFIER_ID) != null) {
                    jump.removeModifier(JUMP_MODIFIER_ID);
                }
            }
        }
    }
}