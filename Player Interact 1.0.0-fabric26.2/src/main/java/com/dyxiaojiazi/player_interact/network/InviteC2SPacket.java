package com.dyxiaojiazi.player_interact.network;

import com.dyxiaojiazi.player_interact.config.ActionType;
import com.dyxiaojiazi.player_interact.manager.ActionManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;
import java.util.List;

public record InviteC2SPacket(ActionType actionType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InviteC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("player_interact", "invite"));

    public static final StreamCodec<FriendlyByteBuf, InviteC2SPacket> CODEC =
            CustomPacketPayload.codec(InviteC2SPacket::write, InviteC2SPacket::new);

    private InviteC2SPacket(FriendlyByteBuf buf) {
        this(ActionType.read(buf));
    }

    private void write(FriendlyByteBuf buf) {
        actionType.write(buf);
    }

    public static void handle(InviteC2SPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();

        // 服务端校验副手（仅跟随动作）
        if (packet.actionType == ActionType.HOLD_HANDS) {
            if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.player_interact.no_offhand"));
                return;
            }
        }

        // 找 20 格内所有其他玩家
        List<ServerPlayer> targets = new ArrayList<>();
        double maxDistSq = 20.0 * 20.0;
        if (player.level() instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                if (other == player) continue;
                if (player.distanceToSqr(other) <= maxDistSq) {
                    targets.add(other);
                }
            }
        }
        if (targets.isEmpty()) return;

        ActionManager.createInvite(player, targets, packet.actionType);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}