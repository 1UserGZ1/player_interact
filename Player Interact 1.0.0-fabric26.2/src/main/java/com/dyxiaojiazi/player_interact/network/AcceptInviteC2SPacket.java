package com.dyxiaojiazi.player_interact.network;

import com.dyxiaojiazi.player_interact.manager.ActionManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record AcceptInviteC2SPacket(UUID inviterUUID) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AcceptInviteC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("player_interact", "accept"));

    public static final StreamCodec<FriendlyByteBuf, AcceptInviteC2SPacket> CODEC =
            CustomPacketPayload.codec(AcceptInviteC2SPacket::write, AcceptInviteC2SPacket::new);

    private AcceptInviteC2SPacket(FriendlyByteBuf buf) {
        this(buf.readUUID());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(inviterUUID);
    }

    public static void handle(AcceptInviteC2SPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayer accepter = context.player();
        MinecraftServer server = ActionManager.getServer(accepter);
        if (server == null) return;
        ServerPlayer inviter = server.getPlayerList().getPlayer(packet.inviterUUID);
        if (inviter == null) return;
        ActionManager.tryAcceptInvite(accepter, inviter);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}