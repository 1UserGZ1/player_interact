package com.dyxiaojiazi.player_interact;

import com.dyxiaojiazi.player_interact.manager.ActionManager;
import com.dyxiaojiazi.player_interact.network.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

public class PlayerInteract implements ModInitializer {
    public static final String MOD_ID = "player_interact";
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "main");

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(InviteC2SPacket.TYPE, InviteC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(AcceptInviteC2SPacket.TYPE, AcceptInviteC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ReleaseActionC2SPacket.TYPE, ReleaseActionC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(LocalHintC2SPacket.TYPE, LocalHintC2SPacket.CODEC);

        PayloadTypeRegistry.clientboundPlay().register(InviteNotifyS2CPacket.TYPE, InviteNotifyS2CPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(InviteExpireS2CPacket.TYPE, InviteExpireS2CPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BindingSyncS2CPacket.TYPE, BindingSyncS2CPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(InviteC2SPacket.TYPE, InviteC2SPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(AcceptInviteC2SPacket.TYPE, AcceptInviteC2SPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(ReleaseActionC2SPacket.TYPE, ReleaseActionC2SPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(LocalHintC2SPacket.TYPE, LocalHintC2SPacket::handle);

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level.isClientSide()) return InteractionResult.PASS;
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            if (!(entity instanceof ServerPlayer target)) return InteractionResult.PASS;

            // 已是绑定关系，互相右键不触发任何交互
            if (ActionManager.isPartners(sp.getUUID(), target.getUUID())) {
                return InteractionResult.FAIL;
            }

            if (ActionManager.tryAcceptInvite(sp, target)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        ActionManager.init();
    }
}