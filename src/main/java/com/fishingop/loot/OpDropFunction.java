package com.fishingop.loot;

import com.fishingop.drop.DropResult;
import com.fishingop.drop.DropRoller;
import com.fishingop.drop.FishingContext;
import com.fishingop.feedback.DropFeedback;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Set;

/** Troca o item que a loot table ia dar por um drop OP do Cobblemon. */
public final class OpDropFunction implements LootItemFunction {

    public static final OpDropFunction MAIN = new OpDropFunction(Mode.MAIN);
    public static final OpDropFunction BONUS_POKEBALL = new OpDropFunction(Mode.BONUS_POKEBALL);

    public static final MapCodec<OpDropFunction> CODEC = MapCodec.unit(() -> MAIN);

    private enum Mode {
        MAIN,
        BONUS_POKEBALL
    }

    private final Mode mode;

    private OpDropFunction(Mode mode) {
        this.mode = mode;
    }

    @Override
    public LootItemFunctionType<? extends LootItemFunction> getType() {
        return FishingOpLoot.OP_DROP_TYPE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.TOOL, LootContextParams.THIS_ENTITY);
    }

    @Override
    public ItemStack apply(ItemStack original, LootContext ctx) {
        FishingContext fishing = LootFishing.contextOf(ctx);

        if (mode == Mode.BONUS_POKEBALL) {
            return DropRoller.rollBonusPokeball(fishing);
        }

        DropResult result = DropRoller.rollMain(fishing);
        if (result.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ServerPlayer player = LootFishing.playerOf(ctx);
        if (player != null) {
            DropFeedback.announce(player, result);
        }
        return result.stack();
    }
}
