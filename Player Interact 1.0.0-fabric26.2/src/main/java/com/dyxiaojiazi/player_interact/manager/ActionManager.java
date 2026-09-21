package com.dyxiaojiazi.player_interact.manager;

import com.dyxiaojiazi.player_interact.config.ActionType;
import com.dyxiaojiazi.player_interact.network.BindingSyncS2CPacket;
import com.dyxiaojiazi.player_interact.network.InviteExpireS2CPacket;
import com.dyxiaojiazi.player_interact.network.InviteNotifyS2CPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ActionManager {
    public static final int INVITE_TIMEOUT_TICKS = 200;
    public static final double CHAIN_LENGTH = 0.7;

    public static class Invite {
        public final UUID inviter;
        public final List<UUID> targets;
        public final ActionType type;
        public int tickCount;
        public Invite(UUID inviter, List<UUID> targets, ActionType type) {
            this.inviter = inviter;
            this.targets = new ArrayList<>(targets);
            this.type = type;
            this.tickCount = 0;
        }
    }
    public static class Binding {
        public final UUID leader, follower;
        public final ActionType type;
        public Binding(UUID leader, UUID follower, ActionType type) {
            this.leader = leader; this.follower = follower; this.type = type;
        }
    }

    private static final Map<UUID, Invite> pendingInvites = new HashMap<>();
    private static final Map<UUID, Invite> incomingInvites = new HashMap<>();
    private static final List<Binding> activeBindings = new ArrayList<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<Map.Entry<UUID, Invite>> it = pendingInvites.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Invite> entry = it.next();
                Invite invite = entry.getValue();
                invite.tickCount++;
                ServerPlayer inviterPlayer = server.getPlayerList().getPlayer(invite.inviter);
                if (inviterPlayer != null && invite.tickCount % 20 == 0) {
                    int remain = (INVITE_TIMEOUT_TICKS - invite.tickCount) / 20;
                    if (remain > 0) sendActionBar(inviterPlayer, Component.literal(remain + " 秒后自动结束邀请"));
                }
                if (invite.tickCount >= INVITE_TIMEOUT_TICKS) {
                    if (inviterPlayer != null) {
                        inviterPlayer.removeEffect(MobEffects.GLOWING);
                        sendExpireNotify(inviterPlayer);
                    }
                    for (UUID tid : invite.targets) {
                        ServerPlayer t = server.getPlayerList().getPlayer(tid);
                        if (t != null) sendExpireNotify(t);
                        incomingInvites.remove(tid);
                    }
                    it.remove();
                }
            }

            Iterator<Binding> bindIt = activeBindings.iterator();
            while (bindIt.hasNext()) {
                Binding binding = bindIt.next();
                ServerPlayer leader = server.getPlayerList().getPlayer(binding.leader);
                ServerPlayer follower = server.getPlayerList().getPlayer(binding.follower);
                if (leader == null || follower == null) { bindIt.remove(); continue; }
                if (binding.type == ActionType.HOLD_HANDS) {
                    applyChainConstraint(leader, follower);
                }
            }
        });
    }

    public static void sendActionBar(ServerPlayer player, Component message) {
        player.connection.send(new ClientboundSetActionBarTextPacket(message));
    }

    private static void sendExpireNotify(ServerPlayer player) {
        ServerPlayNetworking.send(player, new InviteExpireS2CPacket());
    }

    public static MinecraftServer getServer(ServerPlayer player) {
        return player.level() instanceof ServerLevel sl ? sl.getServer() : null;
    }

    public static void createInvite(ServerPlayer inviter, List<ServerPlayer> targets, ActionType type) {
        cancelPlayerInvite(inviter);

        List<UUID> targetIds = new ArrayList<>();
        for (ServerPlayer t : targets) targetIds.add(t.getUUID());

        Invite inv = new Invite(inviter.getUUID(), targetIds, type);
        pendingInvites.put(inviter.getUUID(), inv);
        for (UUID tid : targetIds) incomingInvites.put(tid, inv);

        inviter.addEffect(new MobEffectInstance(MobEffects.GLOWING, INVITE_TIMEOUT_TICKS + 20, 0, false, false));

        for (ServerPlayer t : targets) {
            ServerPlayNetworking.send(t, new InviteNotifyS2CPacket(inviter.getUUID(), type));
        }
        sendActionBar(inviter, Component.literal("已发起邀请，10 秒后自动结束，先到先得"));
    }

    public static void cancelPlayerInvite(ServerPlayer player) {
        Invite old = pendingInvites.remove(player.getUUID());
        if (old != null) {
            for (UUID tid : old.targets) {
                incomingInvites.remove(tid);
            }
            player.removeEffect(MobEffects.GLOWING);
        }
    }

    public static boolean tryAcceptInvite(ServerPlayer accepter, ServerPlayer inviter) {
        Invite invite = incomingInvites.get(accepter.getUUID());
        if (invite == null) return false;
        if (!invite.inviter.equals(inviter.getUUID())) return false;
        if (isPlayerBound(accepter.getUUID()) || isPlayerBound(inviter.getUUID())) return false;

        // 先到先得：清除所有候选目标
        pendingInvites.remove(invite.inviter);
        for (UUID tid : invite.targets) {
            incomingInvites.remove(tid);
        }
        inviter.removeEffect(MobEffects.GLOWING);

        if (invite.type == ActionType.CARRY || invite.type == ActionType.RIDE) {
            boolean ok = accepter.startRiding(inviter, true, false);
            if (!ok) {
                sendActionBar(accepter, Component.literal("骑乘失败，请重试"));
                sendActionBar(inviter, Component.literal("骑乘失败，请重试"));
                return false;
            }
        }

        // ★ 关键修复：follower 是 accepter，不是 invite.targets.get(0)
        activeBindings.add(new Binding(invite.inviter, accepter.getUUID(), invite.type));

        if (invite.type == ActionType.RIDE) {
            inviter.setPose(Pose.SWIMMING);
        }

        ServerPlayNetworking.send(inviter, new BindingSyncS2CPacket(true, true, invite.type, accepter.getUUID()));
        ServerPlayNetworking.send(accepter, new BindingSyncS2CPacket(true, false, invite.type, inviter.getUUID()));

        Component msg = Component.literal("已" + switch (invite.type) {
            case HOLD_HANDS -> "跟随";
            case CARRY -> "背起";
            case RIDE -> "骑行";
        } + "，按 X 脱离");
        sendActionBar(accepter, msg);
        sendActionBar(inviter, msg);
        return true;
    }

    public static boolean releaseBinding(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Binding remove = null;
        for (Binding b : activeBindings) {
            if (b.leader.equals(uuid) || b.follower.equals(uuid)) { remove = b; break; }
        }
        if (remove == null) return false;
        activeBindings.remove(remove);

        MinecraftServer server = getServer(player);
        if (server != null) {
            ServerPlayer leader = server.getPlayerList().getPlayer(remove.leader);
            ServerPlayer follower = server.getPlayerList().getPlayer(remove.follower);

            if (follower != null) {
                if (follower.isPassenger()) follower.stopRiding();
                sendActionBar(follower, Component.literal("已脱离交互状态"));
                ServerPlayNetworking.send(follower, new BindingSyncS2CPacket(false, false, ActionType.HOLD_HANDS, null));
            }
            if (leader != null) {
                leader.setPose(Pose.STANDING);
                sendActionBar(leader, Component.literal("已脱离交互状态"));
                ServerPlayNetworking.send(leader, new BindingSyncS2CPacket(false, false, ActionType.HOLD_HANDS, null));
            }
        }
        return true;
    }

    public static boolean isPlayerBound(UUID uuid) {
        for (Binding b : activeBindings)
            if (b.leader.equals(uuid) || b.follower.equals(uuid)) return true;
        return false;
    }

    public static boolean isPartners(UUID a, UUID b) {
        for (Binding bind : activeBindings) {
            if ((bind.leader.equals(a) && bind.follower.equals(b))
                    || (bind.leader.equals(b) && bind.follower.equals(a))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCarryOrRidePassenger(UUID uuid) {
        for (Binding b : activeBindings) {
            if (b.follower.equals(uuid)
                    && (b.type == ActionType.CARRY || b.type == ActionType.RIDE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHoldHandsFollower(UUID uuid) {
        for (Binding b : activeBindings) {
            if (b.follower.equals(uuid) && b.type == ActionType.HOLD_HANDS) {
                return true;
            }
        }
        return false;
    }

    private static void applyChainConstraint(ServerPlayer leader, ServerPlayer follower) {
        double dist = leader.distanceTo(follower);
        double dx = leader.getX() - follower.getX();
        double dz = leader.getZ() - follower.getZ();
        double horizLen = Math.sqrt(dx * dx + dz * dz);
        if (horizLen < 1.0e-4) return;

        double ux = dx / horizLen;
        double uz = dz / horizLen;

        boolean shouldStepUp = checkStepUp(follower, ux, uz);

        if (dist > CHAIN_LENGTH) {
            double overshoot = dist - CHAIN_LENGTH;
            double pull = Math.min(overshoot * 0.25, 0.4);
            follower.setDeltaMovement(ux * pull, follower.getDeltaMovement().y, uz * pull);
            follower.hurtMarked = true;
        }

        if (shouldStepUp && follower.onGround()) {
            follower.setDeltaMovement(follower.getDeltaMovement().x, 0.42, follower.getDeltaMovement().z);
            follower.hurtMarked = true;
        }

        follower.setYRot(leader.getYRot());
        follower.setXRot(leader.getXRot());
        follower.setYHeadRot(leader.getYRot());
    }

    private static boolean checkStepUp(ServerPlayer follower, double ux, double uz) {
        var level = follower.level();
        double fx = follower.getX() + ux * 0.5;
        double fz = follower.getZ() + uz * 0.5;
        double baseY = follower.getY();

        var belowPos = net.minecraft.core.BlockPos.containing(fx, baseY + 0.1, fz);
        var abovePos = net.minecraft.core.BlockPos.containing(fx, baseY + 1.1, fz);
        var topPos   = net.minecraft.core.BlockPos.containing(fx, baseY + 2.1, fz);

        boolean footSolid = !level.getBlockState(belowPos).isAir();
        boolean headFree  = level.getBlockState(abovePos).isAir();
        boolean topFree   = level.getBlockState(topPos).isAir();
        boolean currentFree = level.getBlockState(net.minecraft.core.BlockPos.containing(follower.getX(), baseY + 1.1, follower.getZ())).isAir();

        return footSolid && headFree && topFree && currentFree;
    }

    public static List<Binding> getActiveBindings() { return Collections.unmodifiableList(activeBindings); }
    public static void clearAll() { pendingInvites.clear(); incomingInvites.clear(); activeBindings.clear(); }
}