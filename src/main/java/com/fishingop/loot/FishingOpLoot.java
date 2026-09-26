package com.fishingop.loot;

import com.fishingop.FishingOp;
import com.fishingop.config.ConfigManager;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Set;

/**
 * Substitui as loot tables de pesca. Não usa Mixin: o Cobblemon não expõe
 * evento para o ramo "item" da Poké Rod, mas ambos os ramos passam por uma
 * loot table, e a Fabric API permite trocá-las.
 */
public final class FishingOpLoot {

    /** Loot table da Poké Rod (ver PokeRodFishingBobberEntity.LOOT_TABLE_ID). */
    public static final ResourceLocation POKEROD_TABLE =
            ResourceLocation.fromNamespaceAndPath("cobblemon", "fishing/pokerod");
    /** Loot table raiz da vara de pesca vanilla. */
    public static final ResourceLocation VANILLA_TABLE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "gameplay/fishing");

    public static LootItemFunctionType<OpDropFunction> OP_DROP_TYPE;
    public static LootItemConditionType POKEBALL_BONUS_TYPE;

    private static final Set<ResourceLocation> REPLACED = Set.of(POKEROD_TABLE, VANILLA_TABLE);

    private FishingOpLoot() {
    }

    public static void register() {
        OP_DROP_TYPE = Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, FishingOp.id("op_drop"),
                new LootItemFunctionType<>(OpDropFunction.CODEC));
        POKEBALL_BONUS_TYPE = Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE,
                FishingOp.id("pokeball_bonus"), new LootItemConditionType(PokeballBonusCondition.CODEC));

        LootTableEvents.REPLACE.register((key, original, source, registries) -> {
            ResourceLocation id = key.location();
            if (VANILLA_TABLE.equals(id) && !ConfigManager.get().vanillaRodAlwaysItems) {
                return null; // vara vanilla mantém o loot original
            }
            if (!REPLACED.contains(id)) {
                return null;
            }
            return opFishingTable(key);
        });
    }

    private static LootTable opFishingTable(ResourceKey<LootTable> key) {
        return LootTable.lootTable()
                .setParamSet(LootContextParamSets.FISHING)
                .setRandomSequence(key.location())
                // Drop principal: sempre um item OP.
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.STONE)
                                .apply(() -> OpDropFunction.MAIN)))
                // Bônus de pokébola: rolagem separada, só sai se a condição passar.
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(() -> PokeballBonusCondition.INSTANCE)
                        .add(LootItem.lootTableItem(Items.STONE)
                                .apply(() -> OpDropFunction.BONUS_POKEBALL)))
                .build();
    }
}
