package com.fishingop.loot;

import com.fishingop.drop.DropRoller;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;

/** Rolagem extra: se passar, a pool do bônus de pokébola é sorteada. */
public final class PokeballBonusCondition implements LootItemCondition {

    public static final PokeballBonusCondition INSTANCE = new PokeballBonusCondition();
    public static final MapCodec<PokeballBonusCondition> CODEC = MapCodec.unit(() -> INSTANCE);

    private PokeballBonusCondition() {
    }

    @Override
    public LootItemConditionType getType() {
        return FishingOpLoot.POKEBALL_BONUS_TYPE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.TOOL);
    }

    @Override
    public boolean test(LootContext ctx) {
        return DropRoller.rollPokeballBonus(LootFishing.contextOf(ctx));
    }
}
