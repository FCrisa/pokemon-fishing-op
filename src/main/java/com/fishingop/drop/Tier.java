package com.fishingop.drop;

import net.minecraft.ChatFormatting;

public enum Tier {
    COMMON(ChatFormatting.WHITE),
    UNCOMMON(ChatFormatting.GREEN),
    RARE(ChatFormatting.AQUA),
    EPIC(ChatFormatting.LIGHT_PURPLE),
    LEGENDARY(ChatFormatting.GOLD);

    public final ChatFormatting color;

    Tier(ChatFormatting color) {
        this.color = color;
    }

    public String translationKey() {
        return "fishingop.tier." + name().toLowerCase(java.util.Locale.ROOT);
    }

    public boolean atLeast(Tier other) {
        return ordinal() >= other.ordinal();
    }

    public static Tier parse(String name, Tier fallback) {
        if (name == null) {
            return fallback;
        }
        for (Tier t : values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return fallback;
    }
}
