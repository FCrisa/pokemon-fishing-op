package com.fishingop.feedback;

import com.fishingop.config.ConfigManager;
import com.fishingop.config.FishingOpConfig;
import com.fishingop.drop.DropResult;
import com.fishingop.drop.Tier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Título, action bar e som — é o que dá o "pop" pro vídeo. */
public final class DropFeedback {

    private DropFeedback() {
    }

    public static void announce(ServerPlayer player, DropResult result) {
        FishingOpConfig.Feedback cfg = ConfigManager.get().feedback;
        Tier minTier = Tier.parse(cfg.minTier, Tier.RARE);

        if (result.summoner()) {
            announceSummonerDrop(player, cfg);
            return;
        }
        if (!result.tier().atLeast(minTier)) {
            return;
        }

        MutableComponent label = Component.translatable("fishingop.drop.caught",
                        result.stack().getHoverName().copy().withStyle(result.tier().color),
                        Component.translatable(result.tier().translationKey()).withStyle(result.tier().color))
                .withStyle(ChatFormatting.WHITE);

        if (cfg.titles) {
            player.sendSystemMessage(label, true);
            if (result.tier() == Tier.LEGENDARY) {
                sendTitle(player,
                        Component.translatable(result.tier().translationKey())
                                .withStyle(result.tier().color, ChatFormatting.BOLD),
                        result.stack().getHoverName().copy().withStyle(ChatFormatting.WHITE),
                        5, 45, 15);
            }
        }
        if (cfg.sounds) {
            playTierSound(player, result.tier());
        }
    }

    private static void announceSummonerDrop(ServerPlayer player, FishingOpConfig.Feedback cfg) {
        if (cfg.titles) {
            sendTitle(player,
                    Component.translatable("fishingop.summoner.title")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    Component.translatable("fishingop.summoner.subtitle").withStyle(ChatFormatting.YELLOW),
                    5, 70, 20);
        }
        if (cfg.sounds) {
            play(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
            play(player, SoundEvents.END_PORTAL_SPAWN, 0.6F, 1.6F);
        }
        if (cfg.broadcastSummonerDrop) {
            Component message = Component.translatable("fishingop.summoner.found", player.getDisplayName())
                    .withStyle(ChatFormatting.GOLD);
            player.server.getPlayerList().broadcastSystemMessage(message, false);
        }
    }

    private static void playTierSound(ServerPlayer player, Tier tier) {
        switch (tier) {
            case LEGENDARY -> {
                play(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.2F);
                play(player, SoundEvents.PLAYER_LEVELUP, 0.7F, 1.8F);
            }
            case EPIC -> play(player, SoundEvents.PLAYER_LEVELUP, 0.7F, 1.6F);
            case RARE -> play(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.6F);
            default -> play(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.6F, 1.2F);
        }
    }

    private static void play(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void sendTitle(ServerPlayer player, Component title, Component subtitle,
                                  int fadeIn, int stay, int fadeOut) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
    }
}
