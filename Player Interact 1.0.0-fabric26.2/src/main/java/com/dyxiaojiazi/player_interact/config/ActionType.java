package com.dyxiaojiazi.player_interact.config;

import net.minecraft.network.FriendlyByteBuf;

public enum ActionType {
    HOLD_HANDS("hold_hands"),
    CARRY("carry"),
    RIDE("ride");

    private final String id;

    ActionType(String id) { this.id = id; }

    public String getId() { return id; }

    public static ActionType byId(String id) {
        for (ActionType type : values()) {
            if (type.id.equals(id)) return type;
        }
        return HOLD_HANDS;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(id);
    }

    public static ActionType read(FriendlyByteBuf buf) {
        return byId(buf.readUtf());
    }
}