package com.fishingop.drop;

import com.fishingop.FishingOpItems;
import com.fishingop.config.FishingOpConfig;
import com.fishingop.config.ConfigManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Sorteia o que sai de cada fisgada. */
public final class DropRoller {

    private DropRoller() {
    }

    /** Drop principal: Invocador Lendário, ou um item aleatório da pool. */
    public static DropResult rollMain(FishingContext ctx) {
        RandomSource random = ctx.random();

        if (random.nextDouble() < summonerChance(ctx)) {
            return new DropResult(new ItemStack(FishingOpItems.LEGENDARY_SUMMONER), Tier.LEGENDARY, true);
        }

        ItemPools.Snapshot pools = ItemPools.get();
        Tier tier = rollTier(ctx, pools);
        if (tier == null) {
            return DropResult.EMPTY;
        }
        List<Item> candidates = pools.byTier().get(tier);
        if (candidates.isEmpty()) {
            return DropResult.EMPTY;
        }
        Item item = candidates.get(random.nextInt(candidates.size()));
        ItemStack stack = new ItemStack(item);
        stack.setCount(rollCount(ctx, tier, stack.getMaxStackSize()));
        return new DropResult(stack, tier, false);
    }

    /** Rolagem separada do bônus de pokébola. */
    public static boolean rollPokeballBonus(FishingContext ctx) {
        FishingOpConfig cfg = ConfigManager.get();
        double chance = cfg.pokeballBonus.chance
                + cfg.pokeballBonus.bonusPerRodLuckLevel * ctx.rodLuckLevel()
                + cfg.pokeballBonus.bonusPerLuckOfTheSea * ctx.luckOfTheSea();
        return ctx.random().nextDouble() < chance;
    }

    /**
     * Pokébola do bônus. Usa os mesmos pesos de tier, então a Master Ball
     * continua rara dentro desta rolagem.
     */
    public static ItemStack rollBonusPokeball(FishingContext ctx) {
        ItemPools.Snapshot pools = ItemPools.get();
        Map<Tier, List<Item>> balls = pools.pokeballsByTier();

        Map<Tier, Double> weights = tierWeights(ctx);
        weights.entrySet().removeIf(e -> balls.get(e.getKey()).isEmpty());
        Tier tier = pick(weights, ctx.random());
        if (tier == null) {
            return ItemStack.EMPTY;
        }
        List<Item> candidates = balls.get(tier);
        Item ball = candidates.get(ctx.random().nextInt(candidates.size()));
        ItemStack stack = new ItemStack(ball);
        stack.setCount(rollCount(ctx, tier, stack.getMaxStackSize()));
        return stack;
    }

    public static double summonerChance(FishingContext ctx) {
        FishingOpConfig cfg = ConfigManager.get();
        double luckMultiplier = 1.0 + cfg.luckOfTheSea.summonerMultiplierPerLevel * ctx.luckOfTheSea();
        return cfg.summoner.chance * RodLuck.summonerMultiplier(ctx.rodLuckLevel()) * luckMultiplier;
    }

    private static Tier rollTier(FishingContext ctx, ItemPools.Snapshot pools) {
        Map<Tier, Double> weights = tierWeights(ctx);
        weights.entrySet().removeIf(e -> pools.byTier().get(e.getKey()).isEmpty());
        return pick(weights, ctx.random());
    }

    /** Pesos efetivos por tier, já com sorte da pokébola e Luck of the Sea. */
    public static Map<Tier, Double> tierWeights(FishingContext ctx) {
        FishingOpConfig cfg = ConfigManager.get();
        double rodMultiplier = RodLuck.tierMultiplier(ctx.rodLuckLevel());
        double seaMultiplier = 1.0 + cfg.luckOfTheSea.rareTierMultiplierPerLevel * ctx.luckOfTheSea();

        Map<Tier, Double> weights = new EnumMap<>(Tier.class);
        for (Tier tier : Tier.values()) {
            double base = cfg.tierWeights.getOrDefault(tier.name(), 0.0);
            if (base <= 0) {
                continue;
            }
            // Só os tiers bons ganham com a pokébola e com Luck of the Sea.
            double weight = tier.atLeast(Tier.RARE) ? base * rodMultiplier * seaMultiplier : base;
            weights.put(tier, weight);
        }
        return weights;
    }

    private static Tier pick(Map<Tier, Double> weights, RandomSource random) {
        double total = 0;
        for (double w : weights.values()) {
            total += w;
        }
        if (total <= 0) {
            return null;
        }
        double roll = random.nextDouble() * total;
        for (Map.Entry<Tier, Double> entry : weights.entrySet()) {
            roll -= entry.getValue();
            if (roll <= 0) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static int rollCount(FishingContext ctx, Tier tier, int maxStackSize) {
        if (maxStackSize <= 1) {
            return 1;
        }
        FishingOpConfig.Duplication dup = ConfigManager.get().duplication;
        double bonus = RodLuck.duplicationBonus(ctx.rodLuckLevel())
                + ConfigManager.get().luckOfTheSea.duplicationBonusPerLevel * ctx.luckOfTheSea()
                + (tier.atLeast(Tier.RARE) ? dup.rarePlusBonus : 0.0);

        RandomSource random = ctx.random();
        if (random.nextDouble() < dup.bigChance + bonus) {
            return clampCount(randomBetween(random, dup.bigMin, dup.bigMax), maxStackSize);
        }
        if (random.nextDouble() < dup.smallChance + bonus) {
            return clampCount(randomBetween(random, dup.smallMin, dup.smallMax), maxStackSize);
        }
        return 1;
    }

    private static int randomBetween(RandomSource random, int min, int max) {
        int lo = Math.max(1, Math.min(min, max));
        int hi = Math.max(lo, Math.max(min, max));
        return lo + random.nextInt(hi - lo + 1);
    }

    private static int clampCount(int count, int maxStackSize) {
        return Math.max(1, Math.min(count, maxStackSize));
    }
}
