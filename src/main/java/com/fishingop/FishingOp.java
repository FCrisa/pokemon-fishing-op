package com.fishingop;

import com.fishingop.command.FishingOpCommands;
import com.fishingop.config.ConfigManager;
import com.fishingop.drop.ItemPools;
import com.fishingop.legendary.LegendaryPool;
import com.fishingop.loot.FishingOpLoot;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FishingOp implements ModInitializer {

    public static final String MOD_ID = "fishingop";
    public static final Logger LOGGER = LoggerFactory.getLogger("Cobblemon Fishing OP");

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ConfigManager.load();
        FishingOpItems.register();
        FishingOpLoot.register();
        CobblemonFishingHooks.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                FishingOpCommands.register(dispatcher));

        // As pools dependem dos registros e dos dados do Cobblemon, então só são
        // montadas sob demanda; aqui apenas garantimos que começam limpas.
        ItemPools.invalidate();
        LegendaryPool.invalidate();

        LOGGER.info("Cobblemon Fishing OP carregado. Pescar agora é OP.");
    }
}
