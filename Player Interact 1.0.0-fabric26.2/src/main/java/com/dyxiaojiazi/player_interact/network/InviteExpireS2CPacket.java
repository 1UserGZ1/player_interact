package com.dyxiaojiazi.player_interact.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record InviteExpireS2CPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InviteExpireS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("player_interact", "expire"));

    public static final StreamCodec<FriendlyByteBuf, InviteExpireS2CPacket> CODEC =
            CustomPacketPayload.codec((buf, p) -> {}, buf -> new InviteExpireS2CPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}