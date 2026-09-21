package com.dyxiaojiazi.player_interact.network;

import com.dyxiaojiazi.player_interact.manager.ActionManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record ReleaseActionC2SPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ReleaseActionC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("player_interact", "release"));

    public static final StreamCodec<FriendlyByteBuf, ReleaseActionC2SPacket> CODEC =
            CustomPacketPayload.codec((buf, p) -> {}, buf -> new ReleaseActionC2SPacket());

    public static void handle(ReleaseActionC2SPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        // 只解绑自己参与的绑定；未绑定的玩家按 X 不会影响任何人
        ActionManager.releaseBinding(player);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}