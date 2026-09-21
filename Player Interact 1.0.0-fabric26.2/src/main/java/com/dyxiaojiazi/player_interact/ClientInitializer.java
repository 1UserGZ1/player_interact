package com.dyxiaojiazi.player_interact;

import com.dyxiaojiazi.player_interact.config.ActionType;
import com.dyxiaojiazi.player_interact.network.BindingSyncS2CPacket;
import com.dyxiaojiazi.player_interact.network.InviteC2SPacket;
import com.dyxiaojiazi.player_interact.network.InviteExpireS2CPacket;
import com.dyxiaojiazi.player_interact.network.InviteNotifyS2CPacket;
import com.dyxiaojiazi.player_interact.network.LocalHintC2SPacket;
import com.dyxiaojiazi.player_interact.network.ReleaseActionC2SPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class ClientInitializer implements ClientModInitializer {

    public static KeyMapping keyInviteHoldHands;
    public static KeyMapping keyInviteCarry;
    public static KeyMapping keyInviteRide;
    public static KeyMapping keyRelease;
    public static KeyMapping keyCrawl;

    public static boolean shouldBlockInput = false;

    public static boolean localIsLeader = false;
    public static ActionType localActionType = null;
    public static UUID localPartnerUUID = null;

    private static boolean wasMoving = false;
    private static long lastMoveHintTime = 0L;

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("player_interact", "custom_category")
    );

    @Override
    public void onInitializeClient() {
        keyInviteHoldHands = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.player_interact.invite_holdhands",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY
        ));
        keyInviteCarry = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.player_interact.invite_carry",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY
        ));
        keyInviteRide = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.player_interact.invite_ride",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY
        ));
        keyRelease = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.player_interact.release",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY
        ));
        keyCrawl = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.player_interact.crawl",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (keyInviteHoldHands.consumeClick()) {
                ClientPlayNetworking.send(new InviteC2SPacket(ActionType.HOLD_HANDS));
            }
            while (keyInviteCarry.consumeClick()) {
                ClientPlayNetworking.send(new InviteC2SPacket(ActionType.CARRY));
            }
            while (keyInviteRide.consumeClick()) {
                ClientPlayNetworking.send(new InviteC2SPacket(ActionType.RIDE));
            }
            while (keyRelease.consumeClick()) {
                ClientPlayNetworking.send(new ReleaseActionC2SPacket());
            }

            updateFollowingState(client);
        });

        ClientPlayNetworking.registerGlobalReceiver(InviteNotifyS2CPacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft mc = context.client();
                if (mc.player == null) return;
                String key = switch (payload.actionType()) {
                    case HOLD_HANDS -> "message.player_interact.invite_holdhands";
                    case CARRY      -> "message.player_interact.invite_carry";
                    case RIDE       -> "message.player_interact.invite_ride";
                };
                String inviterName = payload.inviterUUID().toString();
                if (mc.level != null) {
                    var inviter = mc.level.getPlayerByUUID(payload.inviterUUID());
                    if (inviter != null) inviterName = inviter.getName().getString();
                }
                mc.player.sendSystemMessage(Component.translatable(key, inviterName));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(InviteExpireS2CPacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft mc = context.client();
                if (mc.player == null) return;
                mc.player.sendSystemMessage(Component.translatable("message.player_interact.invite_expired"));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(BindingSyncS2CPacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.active()) {
                    localIsLeader = payload.isLeader();
                    localActionType = payload.actionType();
                    localPartnerUUID = payload.partnerUUID();
                } else {
                    localIsLeader = false;
                    localActionType = null;
                    localPartnerUUID = null;
                }
            });
        });
    }

    private static void updateFollowingState(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        boolean following = player.isPassenger() && player.getVehicle() instanceof Player;

        boolean isRideFollower = following
                && localActionType == ActionType.RIDE
                && !localIsLeader;

        shouldBlockInput = following && !isRideFollower;

        if (!following) {
            wasMoving = false;
            if (client.getCameraEntity() != player) {
                client.setCameraEntity(player);
            }
            return;
        }

        boolean moving =
                client.options.keyUp.isDown() ||
                        client.options.keyDown.isDown() ||
                        client.options.keyLeft.isDown() ||
                        client.options.keyRight.isDown();

        long now = System.currentTimeMillis();
        if (moving && !wasMoving && now - lastMoveHintTime > 1500L) {
            lastMoveHintTime = now;
            ClientPlayNetworking.send(new LocalHintC2SPacket("按 X 脱离（如已改按键请自行查询）"));
        }
        wasMoving = moving;
    }
}