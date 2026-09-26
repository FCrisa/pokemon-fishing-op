package com.fishingop.drop;

import com.cobblemon.mod.common.item.interactive.PokerodItem;
import com.fishingop.config.FishingOpConfig;
import com.fishingop.config.ConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Traduz a vara usada num "nível de sorte de pesca" de 0 a 5. A pokébola de
 * cada Poké Rod é o que define o nível — cada variação de rod é um item próprio
 * no registro do Cobblemon ({@code cobblemon:master_rod} etc.).
 */
public final class RodLuck {

    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 5;

    private static final ChatFormatting[] COLORS = {
            ChatFormatting.GRAY,
            ChatFormatting.WHITE,
            ChatFormatting.GREEN,
            ChatFormatting.AQUA,
            ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.GOLD
    };

    private RodLuck() {
    }

    public static boolean isPokeRod(ItemStack stack) {
        return stack != null && stack.getItem() instanceof PokerodItem;
    }

    /** Nível da vara segurada. Vara vanilla (ou qualquer outra coisa) é 0. */
    public static int levelOf(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof PokerodItem)) {
            return 0;
        }
        return levelOfItem(stack.getItem());
    }

    public static int levelOfItem(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        Integer level = ConfigManager.get().rodLuck.levels.get(id.toString());
        // Uma Poké Rod desconhecida (mod novo, datapack) entra como nível 1.
        return clamp(level == null ? 1 : level);
    }

    /** Resolve o nível a partir do id de uma rod OU de uma pokébola. */
    public static Integer levelFromName(String raw) {
        String name = raw.trim().toLowerCase(java.util.Locale.ROOT);
        if (name.equals("vanilla") || name.equals("none") || name.equals("nenhuma")) {
            return 0;
        }
        FishingOpConfig cfg = ConfigManager.get();
        String qualified = name.contains(":") ? name : "cobblemon:" + name;

        Integer direct = cfg.rodLuck.levels.get(qualified);
        if (direct != null) {
            return clamp(direct);
        }
        // Aceita o nome da pokébola: cobblemon:master_ball -> cobblemon:master_rod
        if (qualified.endsWith("_ball")) {
            String asRod = qualified.substring(0, qualified.length() - "_ball".length()) + "_rod";
            Integer viaBall = cfg.rodLuck.levels.get(asRod);
            if (viaBall != null) {
                return clamp(viaBall);
            }
            if (BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(asRod))) {
                return 1;
            }
        }
        if (qualified.endsWith("_rod") && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(qualified))) {
            return 1;
        }
        return null;
    }

    public static double tierMultiplier(int level) {
        return fromList(ConfigManager.get().rodLuck.tierMultiplier, level, 1.0);
    }

    public static double duplicationBonus(int level) {
        return fromList(ConfigManager.get().rodLuck.duplicationBonus, level, 0.0);
    }

    public static double summonerMultiplier(int level) {
        return fromList(ConfigManager.get().rodLuck.summonerMultiplier, level, 1.0);
    }

    public static ChatFormatting color(int level) {
        return COLORS[clamp(level)];
    }

    private static double fromList(List<Double> list, int level, double fallback) {
        int i = clamp(level);
        if (list == null || list.isEmpty()) {
            return fallback;
        }
        return list.get(Math.min(i, list.size() - 1));
    }

    private static int clamp(int level) {
        return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
    }
}
