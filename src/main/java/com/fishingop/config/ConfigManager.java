package com.fishingop.config;

import com.fishingop.FishingOp;
import com.fishingop.drop.ItemPools;
import com.fishingop.legendary.LegendaryPool;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static FishingOpConfig config = new FishingOpConfig();

    private ConfigManager() {
    }

    public static FishingOpConfig get() {
        return config;
    }

    public static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(FishingOp.MOD_ID + ".json");
    }

    /** Recarrega do disco, escrevendo o padrão se o arquivo não existir. */
    public static void load() {
        Path file = path();
        try {
            if (Files.notExists(file)) {
                config = new FishingOpConfig();
                save();
            } else {
                try (Reader reader = Files.newBufferedReader(file)) {
                    FishingOpConfig parsed = GSON.fromJson(reader, FishingOpConfig.class);
                    config = parsed == null ? new FishingOpConfig() : parsed;
                }
                // Reescreve para que campos novos de versões futuras apareçam no arquivo.
                save();
            }
        } catch (IOException | JsonSyntaxException e) {
            FishingOp.LOGGER.error("Falha ao ler {} — usando os valores padrão.", file, e);
            config = new FishingOpConfig();
        }
        ItemPools.invalidate();
        LegendaryPool.invalidate();
    }

    public static void save() {
        Path file = path();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            FishingOp.LOGGER.error("Falha ao gravar {}", file, e);
        }
    }
}
