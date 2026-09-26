package com.fishingop.command;

import com.fishingop.FishingOpItems;
import com.fishingop.config.ConfigManager;
import com.fishingop.drop.DropResult;
import com.fishingop.drop.DropRoller;
import com.fishingop.drop.FishingContext;
import com.fishingop.drop.ItemPools;
import com.fishingop.drop.RodLuck;
import com.fishingop.drop.Tier;
import com.fishingop.legendary.LegendaryPool;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FishingOpCommands {

    private static final int MAX_SIMULATION = 1_000_000;

    private FishingOpCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fishingop")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload")
                        .executes(FishingOpCommands::reload))
                .then(Commands.literal("simulate")
                        .then(Commands.argument("quantidade", IntegerArgumentType.integer(1, MAX_SIMULATION))
                                .executes(ctx -> simulate(ctx, 0, "vanilla"))
                                .then(Commands.argument("pokebola", StringArgumentType.string())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(ballSuggestions(), b))
                                        .executes(ctx -> {
                                            String ball = StringArgumentType.getString(ctx, "pokebola");
                                            Integer level = RodLuck.levelFromName(ball);
                                            if (level == null) {
                                                ctx.getSource().sendFailure(Component.translatable(
                                                        "fishingop.command.unknown_ball", ball));
                                                return 0;
                                            }
                                            return simulate(ctx, level, ball);
                                        }))))
                .then(Commands.literal("give")
                        .then(Commands.literal("summoner")
                                .executes(FishingOpCommands::giveSummoner)
                                .then(Commands.argument("quantidade", IntegerArgumentType.integer(1, 64))
                                        .executes(ctx -> giveSummoner(ctx,
                                                IntegerArgumentType.getInteger(ctx, "quantidade")))))));
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ConfigManager.load();
        ItemPools.Snapshot pools = ItemPools.get();
        int species = LegendaryPool.get().size();
        ctx.getSource().sendSuccess(() -> Component.translatable("fishingop.command.reloaded",
                pools.total(), species).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int giveSummoner(CommandContext<CommandSourceStack> ctx) {
        return giveSummoner(ctx, 1);
    }

    private static int giveSummoner(CommandContext<CommandSourceStack> ctx, int count) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.translatable("fishingop.command.players_only"));
            return 0;
        }
        for (int i = 0; i < count; i++) {
            ItemStack stack = new ItemStack(FishingOpItems.LEGENDARY_SUMMONER);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        final int given = count;
        ctx.getSource().sendSuccess(() -> Component.translatable("fishingop.command.gave_summoner", given)
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int simulate(CommandContext<CommandSourceStack> ctx, int rodLuckLevel, String label) {
        int runs = IntegerArgumentType.getInteger(ctx, "quantidade");
        CommandSourceStack source = ctx.getSource();
        RandomSource random = RandomSource.create();
        FishingContext fishing = new FishingContext(rodLuckLevel, 0, random);

        Map<Tier, Integer> tierCounts = new EnumMap<>(Tier.class);
        for (Tier tier : Tier.values()) {
            tierCounts.put(tier, 0);
        }
        Map<String, Integer> itemCounts = new HashMap<>();
        int summoners = 0;
        int bonusBalls = 0;
        long totalItems = 0;

        for (int i = 0; i < runs; i++) {
            DropResult result = DropRoller.rollMain(fishing);
            if (result.isEmpty()) {
                continue;
            }
            tierCounts.merge(result.tier(), 1, Integer::sum);
            totalItems += result.stack().getCount();
            if (result.summoner()) {
                summoners++;
            }
            itemCounts.merge(idOf(result.stack()), 1, Integer::sum);

            if (DropRoller.rollPokeballBonus(fishing)) {
                ItemStack bonus = DropRoller.rollBonusPokeball(fishing);
                if (!bonus.isEmpty()) {
                    bonusBalls++;
                    totalItems += bonus.getCount();
                }
            }
        }

        source.sendSuccess(() -> Component.literal("── Fishing OP · " + runs + " pescas · "
                        + label + " (nível " + rodLuckLevel + ") ──")
                .withStyle(RodLuck.color(rodLuckLevel), ChatFormatting.BOLD), false);

        for (Tier tier : Tier.values()) {
            int count = tierCounts.get(tier);
            double pct = runs == 0 ? 0 : (count * 100.0 / runs);
            source.sendSuccess(() -> Component.literal(String.format("  %-10s %7d  %6.2f%%",
                    tier.name(), count, pct)).withStyle(tier.color), false);
        }

        final int finalSummoners = summoners;
        final int finalBonus = bonusBalls;
        final long finalTotal = totalItems;
        source.sendSuccess(() -> Component.literal(String.format(
                        "  Invocador: %d (%.3f%%)  ·  bônus de pokébola: %d  ·  itens no total: %d",
                        finalSummoners, runs == 0 ? 0 : finalSummoners * 100.0 / runs, finalBonus, finalTotal))
                .withStyle(ChatFormatting.GRAY), false);

        List<Map.Entry<String, Integer>> top = new ArrayList<>(itemCounts.entrySet());
        top.sort(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed());
        int shown = Math.min(5, top.size());
        if (shown > 0) {
            source.sendSuccess(() -> Component.literal("  Mais frequentes:").withStyle(ChatFormatting.DARK_GRAY),
                    false);
            for (int i = 0; i < shown; i++) {
                Map.Entry<String, Integer> entry = top.get(i);
                source.sendSuccess(() -> Component.literal("    " + entry.getKey() + " ×" + entry.getValue())
                        .withStyle(ChatFormatting.DARK_GRAY), false);
            }
        }
        return 1;
    }

    private static String idOf(ItemStack stack) {
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.toString();
    }

    private static List<String> ballSuggestions() {
        List<String> out = new ArrayList<>();
        out.add("vanilla");
        for (String rodId : ConfigManager.get().rodLuck.levels.keySet()) {
            out.add(rodId.endsWith("_rod")
                    ? rodId.substring(0, rodId.length() - "_rod".length()) + "_ball"
                    : rodId);
        }
        return out;
    }
}
