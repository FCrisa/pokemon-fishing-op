package com.fishingop.drop;

import net.minecraft.world.item.ItemStack;

public record DropResult(ItemStack stack, Tier tier, boolean summoner) {

    public static final DropResult EMPTY = new DropResult(ItemStack.EMPTY, Tier.COMMON, false);

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
