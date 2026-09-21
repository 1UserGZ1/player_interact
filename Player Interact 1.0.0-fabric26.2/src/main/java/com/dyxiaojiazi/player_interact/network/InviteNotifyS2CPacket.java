package com.dyxiaojiazi.player_interact.network;

import com.dyxiaojiazi.player_interact.config.ActionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record InviteNotifyS2CPacket(UUID inviterUUID, ActionType actionType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InviteNotifyS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("player_interact", "notify"));

    public static final StreamCodec<FriendlyByteBuf, InviteNotifyS2CPacket> CODEC =
            CustomPacketPayload.codec(InviteNotifyS2CPacket::write, InviteNotifyS2CPacket::new);

    private InviteNotifyS2CPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), ActionType.read(buf));
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(inviterUUID);
        actionType.write(buf);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}