package com.dyxiaojiazi.player_interact.network;

import com.dyxiaojiazi.player_interact.config.ActionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record BindingSyncS2CPacket(boolean active, boolean isLeader, ActionType actionType, UUID partnerUUID)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BindingSyncS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("player_interact", "binding_sync"));

    public static final StreamCodec<FriendlyByteBuf, BindingSyncS2CPacket> CODEC =
            CustomPacketPayload.codec(BindingSyncS2CPacket::write, BindingSyncS2CPacket::new);

    private BindingSyncS2CPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean(), ActionType.read(buf), buf.readUUID());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeBoolean(active);
        buf.writeBoolean(isLeader);
        actionType.write(buf);
        buf.writeUUID(partnerUUID == null ? new UUID(0, 0) : partnerUUID);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}