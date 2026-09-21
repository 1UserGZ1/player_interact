package com.dyxiaojiazi.player_interact.network;

import com.dyxiaojiazi.player_interact.manager.ActionManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record LocalHintC2SPacket(String text) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LocalHintC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("player_interact", "local_hint"));

    public static final StreamCodec<FriendlyByteBuf, LocalHintC2SPacket> CODEC =
            CustomPacketPayload.codec(LocalHintC2SPacket::write, LocalHintC2SPacket::new);

    private LocalHintC2SPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUtf(text);
    }

    public static void handle(LocalHintC2SPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        ActionManager.sendActionBar(player, Component.literal(packet.text));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}