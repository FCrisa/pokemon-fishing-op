package com.fishingop.legendary;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Species;
import com.fishingop.FishingOp;
import com.fishingop.config.ConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class LegendarySummons {

    private LegendarySummons() {
    }

    /** Invoca um lendário aleatório à frente do jogador. false se a pool estiver vazia. */
    public static boolean summon(ServerPlayer player) {
        List<Species> pool = LegendaryPool.get();
        if (pool.isEmpty()) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        Species species = pool.get(level.getRandom().nextInt(pool.size()));
        int pokemonLevel = Math.max(1, Math.min(100, ConfigManager.get().summoner.pokemonLevel));

        PokemonEntity entity;
        try {
            PokemonProperties properties = PokemonProperties.Companion.parse(
                    "species=\"" + species.getResourceIdentifier() + "\" level=" + pokemonLevel);
            entity = properties.createEntity(level);
        } catch (Exception e) {
            FishingOp.LOGGER.error("Não consegui criar a entidade de {}", species.getResourceIdentifier(), e);
            return false;
        }

        Vec3 spawn = findSpawnPos(player, level);
        entity.moveTo(spawn.x, spawn.y, spawn.z, wrapDegrees(player.getYRot() + 180.0F), 0.0F);
        try {
            entity.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(spawn)),
                    MobSpawnType.COMMAND, null);
        } catch (Exception e) {
            FishingOp.LOGGER.warn("finalizeSpawn falhou para {}, seguindo mesmo assim.",
                    species.getResourceIdentifier(), e);
        }
        if (!level.addFreshEntity(entity)) {
            return false;
        }

        playEffects(level, spawn);
        announce(player, species);
        return true;
    }

    private static Vec3 findSpawnPos(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0E-4) {
            flat = new Vec3(0, 0, 1);
        }
        double distance = ConfigManager.get().summoner.spawnDistance;
        Vec3 target = player.position().add(flat.normalize().scale(distance));

        BlockPos base = BlockPos.containing(target);
        for (int dy = 2; dy >= -3; dy--) {
            BlockPos candidate = base.offset(0, dy, 0);
            if (level.getBlockState(candidate).isAir()
                    && level.getBlockState(candidate.above()).isAir()
                    && !level.getBlockState(candidate.below()).isAir()) {
                return Vec3.atBottomCenterOf(candidate);
            }
        }
        return new Vec3(target.x, player.getY(), target.z);
    }

    private static void playEffects(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.x, pos.y + 1.0, pos.z, 120, 0.6, 1.0, 0.6, 0.35);
        level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1.0, pos.z, 60, 0.5, 0.9, 0.5, 0.12);
        level.sendParticles(ParticleTypes.FLASH, pos.x, pos.y + 1.2, pos.z, 3, 0.1, 0.1, 0.1, 0.0);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.9F, 1.4F);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.7F, 1.1F);
    }

    private static void announce(ServerPlayer player, Species species) {
        Component message = Component.translatable("fishingop.summoner.summoned",
                        player.getDisplayName(),
                        species.getTranslatedName().copy().withStyle(ChatFormatting.GOLD))
                .withStyle(ChatFormatting.LIGHT_PURPLE);
        player.server.getPlayerList().broadcastSystemMessage(message, false);
    }

    private static float wrapDegrees(double degrees) {
        double d = degrees % 360.0;
        if (d >= 180.0) {
            d -= 360.0;
        }
        if (d < -180.0) {
            d += 360.0;
        }
        return (float) d;
    }
}
