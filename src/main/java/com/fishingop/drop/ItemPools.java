package com.fishingop.drop;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.fishingop.FishingOp;
import com.fishingop.config.FishingOpConfig;
import com.fishingop.config.ConfigManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pool de itens montada dinamicamente a partir do registro: qualquer item dos
 * namespaces configurados entra sozinho, menos os da blacklist. Assim itens
 * novos do Cobblemon (ou do Mega Showdown) aparecem sem precisar de update.
 */
public final class ItemPools {

    private static volatile Snapshot snapshot;

    private ItemPools() {
    }

    public record Snapshot(Map<Tier, List<Item>> byTier, Map<Tier, List<Item>> pokeballsByTier, int total) {
    }

    public static void invalidate() {
        snapshot = null;
    }

    public static Snapshot get() {
        Snapshot local = snapshot;
        if (local == null) {
            synchronized (ItemPools.class) {
                local = snapshot;
                if (local == null) {
                    local = build();
                    snapshot = local;
                }
            }
        }
        return local;
    }

    private static Snapshot build() {
        FishingOpConfig cfg = ConfigManager.get();

        List<String> namespaces = cfg.itemNamespaces.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toList();
        List<IdPattern> blacklist = cfg.blacklist.stream().map(IdPattern::of).toList();

        // Regras por tier, avaliadas do mais raro para o menos raro.
        Map<Tier, List<IdPattern>> rules = new EnumMap<>(Tier.class);
        for (Tier tier : Tier.values()) {
            List<String> raw = cfg.tierRules.get(tier.name());
            rules.put(tier, raw == null ? List.of() : raw.stream().map(IdPattern::of).toList());
        }

        Map<Tier, List<Item>> byTier = new EnumMap<>(Tier.class);
        Map<Tier, List<Item>> ballsByTier = new EnumMap<>(Tier.class);
        for (Tier tier : Tier.values()) {
            byTier.put(tier, new ArrayList<>());
            ballsByTier.put(tier, new ArrayList<>());
        }

        int total = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!namespaces.contains(id.getNamespace())) {
                continue;
            }
            if (matchesAny(blacklist, id)) {
                continue;
            }
            Tier tier = classify(rules, id);
            byTier.get(tier).add(item);
            if (item instanceof PokeBallItem) {
                ballsByTier.get(tier).add(item);
            }
            total++;
        }

        FishingOp.LOGGER.info("Pool montada: {} itens ({}).", total,
                String.join(", ", java.util.Arrays.stream(Tier.values())
                        .map(t -> t.name() + "=" + byTier.get(t).size()).toList()));

        return new Snapshot(byTier, ballsByTier, total);
    }

    private static Tier classify(Map<Tier, List<IdPattern>> rules, ResourceLocation id) {
        Tier[] order = {Tier.LEGENDARY, Tier.EPIC, Tier.RARE, Tier.UNCOMMON};
        for (Tier tier : order) {
            if (matchesAny(rules.get(tier), id)) {
                return tier;
            }
        }
        return Tier.COMMON;
    }

    private static boolean matchesAny(List<IdPattern> patterns, ResourceLocation id) {
        for (IdPattern p : patterns) {
            if (p.matches(id)) {
                return true;
            }
        }
        return false;
    }
}
