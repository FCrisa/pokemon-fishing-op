package com.fishingop.client;

import com.fishingop.drop.RodLuck;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class FishingOpClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!RodLuck.isPokeRod(stack)) {
                return;
            }
            int level = RodLuck.levelOf(stack);
            lines.add(Component.translatable("fishingop.tooltip.rod_luck",
                            Component.literal(String.valueOf(level)).withStyle(RodLuck.color(level)))
                    .withStyle(ChatFormatting.GRAY));
        });
    }
}
