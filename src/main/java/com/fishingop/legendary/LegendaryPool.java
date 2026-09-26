package com.fishingop.legendary;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Species;
import com.fishingop.FishingOp;
import com.fishingop.config.ConfigManager;
import com.fishingop.config.FishingOpConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pool de espécies do Invocador. Montada a partir do registro de espécies do
 * Cobblemon, então addons que injetam espécies (o Mega Showdown, por exemplo,
 * escreve em data/cobblemon/species) entram sozinhos.
 */
public final class LegendaryPool {

    private static volatile List<Species> cached;

    private LegendaryPool() {
    }

    public static void invalidate() {
        cached = null;
    }

    public static List<Species> get() {
        List<Species> local = cached;
        if (local == null) {
            synchronized (LegendaryPool.class) {
                local = cached;
                if (local == null) {
                    local = build();
                    cached = local;
                }
            }
        }
        return local;
    }

    private static List<Species> build() {
        FishingOpConfig.Summoner cfg = ConfigManager.get().summoner;

        Set<String> wanted = cfg.labels.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Set<String> banned = cfg.speciesBlacklist.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        List<Species> result = new ArrayList<>();
        for (Species species : PokemonSpecies.getImplemented()) {
            boolean labelled = species.getLabels().stream()
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .anyMatch(wanted::contains);
            if (!labelled) {
                continue;
            }
            String id = species.getResourceIdentifier().toString().toLowerCase(Locale.ROOT);
            String path = species.getResourceIdentifier().getPath().toLowerCase(Locale.ROOT);
            if (banned.contains(id) || banned.contains(path)) {
                continue;
            }
            result.add(species);
        }

        FishingOp.LOGGER.info("Pool do Invocador Lendário: {} espécies implementadas.", result.size());
        return List.copyOf(result);
    }
}
