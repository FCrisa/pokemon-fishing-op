package com.fishingop.loot;

import com.fishingop.drop.FishingContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

/** Ponte entre o LootContext da pesca e o contexto de rolagem do mod. */
public final class LootFishing {

    private LootFishing() {
    }

    public static FishingContext contextOf(LootContext ctx) {
        ItemStack rod = ctx.getParamOrNull(LootContextParams.TOOL);
        return new FishingContext(
                com.fishingop.drop.RodLuck.levelOf(rod),
                luckOfTheSea(ctx.getLevel(), rod),
                ctx.getRandom());
    }

    public static int luckOfTheSea(ServerLevel level, @Nullable ItemStack rod) {
        if (rod == null || rod.isEmpty()) {
            return 0;
        }
        try {
            return EnchantmentHelper.getItemEnchantmentLevel(
                    level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                            .getHolderOrThrow(Enchantments.LUCK_OF_THE_SEA),
                    rod);
        } catch (Exception e) {
            return 0;
        }
    }

    @Nullable
    public static ServerPlayer playerOf(LootContext ctx) {
        Entity entity = ctx.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity instanceof FishingHook hook && hook.getPlayerOwner() instanceof ServerPlayer player) {
            return player;
        }
        return null;
    }
}
