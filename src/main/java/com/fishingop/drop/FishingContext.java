package com.fishingop.drop;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Tudo que a rolagem precisa saber sobre uma pescaria. */
public record FishingContext(int rodLuckLevel, int luckOfTheSea, RandomSource random) {

    public static FishingContext of(ItemStack rod, int luckOfTheSea, RandomSource random) {
        return new FishingContext(RodLuck.levelOf(rod), Math.max(0, luckOfTheSea), random);
    }
}
