package com.fishingop;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.fishing.BobberSpawnPokemonEvent;
import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity;
import com.fishingop.config.ConfigManager;
import com.fishingop.drop.DropResult;
import com.fishingop.drop.DropRoller;
import com.fishingop.drop.FishingContext;
import com.fishingop.drop.RodLuck;
import com.fishingop.feedback.DropFeedback;
import com.fishingop.loot.LootFishing;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Ramo "Pokémon" da Poké Rod. O Cobblemon expõe BOBBER_SPAWN_POKEMON_PRE como
 * evento cancelável, então dá pra trocar o Pokémon por um item sem Mixin.
 */
public final class CobblemonFishingHooks {

    private CobblemonFishingHooks() {
    }

    public static void register() {
        CobblemonEvents.BOBBER_SPAWN_POKEMON_PRE.subscribe(Priority.NORMAL,
                (Consumer<BobberSpawnPokemonEvent.Pre>) CobblemonFishingHooks::onBobberSpawnPokemon);
    }

    private static void onBobberSpawnPokemon(BobberSpawnPokemonEvent.Pre event) {
        PokeRodFishingBobberEntity bobber = event.getBobber();
        if (!(bobber.level() instanceof ServerLevel level)) {
            return;
        }
        Player owner = bobber.getPlayerOwner();
        if (!(owner instanceof ServerPlayer player)) {
            return;
        }

        RandomSource random = level.getRandom();
        if (random.nextDouble() >= ConfigManager.get().pokeRodItemChance) {
            return; // Deixa o Cobblemon spawnar o Pokémon normalmente.
        }

        ItemStack rod = event.getRod();
        FishingContext ctx = new FishingContext(
                RodLuck.levelOf(rod), LootFishing.luckOfTheSea(level, rod), random);

        DropResult result = DropRoller.rollMain(ctx);
        if (result.isEmpty()) {
            return; // Sem drop para oferecer: o Pokémon segue normalmente.
        }

        event.cancel();

        dropTowardsPlayer(level, bobber, player, result.stack());
        if (DropRoller.rollPokeballBonus(ctx)) {
            ItemStack bonus = DropRoller.rollBonusPokeball(ctx);
            if (!bonus.isEmpty()) {
                dropTowardsPlayer(level, bobber, player, bonus);
            }
        }
        DropFeedback.announce(player, result);

        level.addFreshEntity(new ExperienceOrb(level, player.getX(), player.getY() + 0.5,
                player.getZ() + 0.5, random.nextInt(6) + 1));

        // O Cobblemon faz `return 0` antes do discard() quando o evento é
        // cancelado, então o bobber ficaria preso na água. Removemos aqui.
        bobber.discard();
    }

    private static void dropTowardsPlayer(ServerLevel level, PokeRodFishingBobberEntity bobber,
                                          ServerPlayer player, ItemStack stack) {
        ItemEntity item = new ItemEntity(level, bobber.getX(), bobber.getY(), bobber.getZ(), stack);
        double dx = player.getX() - bobber.getX();
        double dy = player.getY() - bobber.getY();
        double dz = player.getZ() - bobber.getZ();
        item.setDeltaMovement(dx * 0.1,
                dy * 0.1 + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08,
                dz * 0.1);
        level.addFreshEntity(item);
    }
}
