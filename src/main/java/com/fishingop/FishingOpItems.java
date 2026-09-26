package com.fishingop;

import com.fishingop.item.LegendarySummonerItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class FishingOpItems {

    public static final Item LEGENDARY_SUMMONER = new LegendarySummonerItem(
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());

    private FishingOpItems() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, FishingOp.id("legendary_summoner"), LEGENDARY_SUMMONER);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.accept(LEGENDARY_SUMMONER));
    }
}
