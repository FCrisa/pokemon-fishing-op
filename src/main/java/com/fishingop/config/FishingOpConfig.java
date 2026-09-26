package com.fishingop.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Espelho 1:1 do config/fishingop.json. Todos os valores aqui são os padrões
 * usados quando o arquivo não existe ou quando um campo é omitido.
 */
public class FishingOpConfig {

    /** Chance de uma fisgada da Poké Rod que renderia um Pokémon virar item OP. */
    public double pokeRodItemChance = 0.70;

    /** Vara de pesca vanilla: 100% dos drops viram itens do Cobblemon. */
    public boolean vanillaRodAlwaysItems = true;

    /** Namespaces varridos no registro de itens para montar a pool. */
    public List<String> itemNamespaces = new ArrayList<>(List.of("cobblemon", "mega_showdown"));

    /** Itens excluídos da pool. Aceita id exato ou curinga com '*'. */
    public List<String> blacklist = new ArrayList<>(List.of(
            // técnicos / debug / sem uso fora de creative
            "cobblemon:npc_editor",
            "cobblemon:technical_machine",
            "cobblemon:pokedex_black",
            "cobblemon:pokedex_blue",
            "cobblemon:pokedex_green",
            "cobblemon:pokedex_pink",
            "cobblemon:pokedex_red",
            "cobblemon:pokedex_white",
            "cobblemon:pokedex_yellow",
            "cobblemon:habitat_block",
            "cobblemon:damaged_monitor",
            "cobblemon:monitor",
            "cobblemon:peat_block",
            // blocos decorativos em massa só poluem a pool
            "cobblemon:potted_*",
            "cobblemon:*_budding_*",
            "cobblemon:deepslate_*_ore",
            "cobblemon:nether_fire_stone_ore",
            "cobblemon:terracotta_sun_stone_ore",
            "cobblemon:dripstone_moon_stone_ore"
    ));

    /** Pesos base de cada tier. Quanto maior, mais comum. */
    public Map<String, Double> tierWeights = new LinkedHashMap<>(Map.of(
            "COMMON", 600.0,
            "UNCOMMON", 260.0,
            "RARE", 100.0,
            "EPIC", 32.0,
            "LEGENDARY", 8.0
    ));

    /**
     * Classificação dos itens. Avaliada de LEGENDARY para UNCOMMON; o primeiro
     * padrão que casar decide o tier. O que não casar cai em COMMON.
     */
    public Map<String, List<String>> tierRules = defaultTierRules();

    public PokeballBonus pokeballBonus = new PokeballBonus();
    public Duplication duplication = new Duplication();
    public LuckOfTheSea luckOfTheSea = new LuckOfTheSea();
    public RodLuckConfig rodLuck = new RodLuckConfig();
    public Summoner summoner = new Summoner();
    public Feedback feedback = new Feedback();

    public static class PokeballBonus {
        /** Rolagem extra e independente: se passar, garante uma pokébola a mais. */
        public double chance = 0.25;
        /** Bônus somado por nível de sorte da rod. */
        public double bonusPerRodLuckLevel = 0.03;
        /** Bônus somado por nível de Luck of the Sea. */
        public double bonusPerLuckOfTheSea = 0.02;
    }

    public static class Duplication {
        public double smallChance = 0.30;
        public int smallMin = 2;
        public int smallMax = 4;
        public double bigChance = 0.05;
        public int bigMin = 8;
        public int bigMax = 16;
        /** Somado às duas chances acima quando o drop é Raro ou melhor. */
        public double rarePlusBonus = 0.15;
    }

    public static class LuckOfTheSea {
        /** Multiplicador somado (1 + n*x) aos pesos de RARE/EPIC/LEGENDARY. */
        public double rareTierMultiplierPerLevel = 0.35;
        public double duplicationBonusPerLevel = 0.03;
        public double summonerMultiplierPerLevel = 0.25;
    }

    public static class RodLuckConfig {
        /** Nível de sorte por item de vara. A vara vanilla é sempre nível 0. */
        public Map<String, Integer> levels = defaultRodLevels();
        /** Multiplicador dos pesos de RARE/EPIC/LEGENDARY, índice = nível. */
        public List<Double> tierMultiplier = new ArrayList<>(List.of(1.0, 1.25, 1.6, 2.0, 2.8, 4.0));
        /** Bônus na chance de duplicação, índice = nível. */
        public List<Double> duplicationBonus = new ArrayList<>(List.of(0.0, 0.02, 0.04, 0.06, 0.08, 0.10));
        /** Multiplicador da chance do Invocador Lendário, índice = nível. */
        public List<Double> summonerMultiplier = new ArrayList<>(List.of(1.0, 1.15, 1.35, 1.6, 2.2, 3.0));
    }

    public static class Summoner {
        /** Chance por pesca de o drop ser o Invocador Lendário. */
        public double chance = 0.005;
        /** Nível do Pokémon invocado. */
        public int pokemonLevel = 70;
        /** Labels aceitas para entrar na pool de invocação. */
        public List<String> labels = new ArrayList<>(List.of("legendary", "mythical", "ultra_beast", "paradox"));
        /** Espécies banidas da invocação (nome do registro, sem namespace). */
        public List<String> speciesBlacklist = new ArrayList<>(List.of("arceus"));
        /** Distância à frente do jogador onde o Pokémon aparece. */
        public double spawnDistance = 4.0;
    }

    public static class Feedback {
        /** Tier mínimo que dispara título/action bar. */
        public String minTier = "RARE";
        public boolean sounds = true;
        public boolean titles = true;
        /** Anúncio no chat do servidor quando alguém pesca o Invocador. */
        public boolean broadcastSummonerDrop = true;
    }

    private static Map<String, List<String>> defaultTierRules() {
        Map<String, List<String>> m = new LinkedHashMap<>();

        m.put("LEGENDARY", new ArrayList<>(List.of(
                "cobblemon:master_ball",
                "cobblemon:master_rod",
                "cobblemon:cherish_ball",
                "cobblemon:cherish_rod",
                "cobblemon:beast_ball",
                "cobblemon:beast_rod",
                "cobblemon:ancient_origin_ball",
                "cobblemon:ancient_origin_rod",
                "cobblemon:ability_patch",
                "cobblemon:rare_candy",
                "cobblemon:exp_candy_xl",
                "cobblemon:pp_max",
                // Mega Showdown
                "mega_showdown:legend_plate",
                "mega_showdown:ultranecrozium_z",
                "mega_showdown:omni_ring",
                "mega_showdown:wishing_star",
                "mega_showdown:star_core",
                "mega_showdown:rusted_sword",
                "mega_showdown:rusted_shield",
                "mega_showdown:griseous_core",
                "mega_showdown:adamant_crystal",
                "mega_showdown:lustrous_globe",
                "mega_showdown:reins_of_unity",
                "mega_showdown:prison_bottle",
                "mega_showdown:dna_splicer",
                "mega_showdown:n_solarizer",
                "mega_showdown:n_lunarizer",
                "mega_showdown:reveal_glass",
                "mega_showdown:soul_dew",
                "mega_showdown:zygarde_cube",
                "mega_showdown:tera_orb",
                "mega_showdown:dynamax_band",
                "mega_showdown:z_power_ring"
        )));

        m.put("EPIC", new ArrayList<>(List.of(
                "cobblemon:ability_capsule",
                "cobblemon:ability_shield",
                "cobblemon:exp_candy_l",
                "cobblemon:lucky_egg",
                "cobblemon:max_revive",
                "cobblemon:full_restore",
                "cobblemon:clear_amulet",
                "cobblemon:covert_cloak",
                "cobblemon:loaded_dice",
                "cobblemon:destiny_knot",
                "cobblemon:eviolite",
                "cobblemon:focus_sash",
                "cobblemon:life_orb",
                "cobblemon:choice_band",
                "cobblemon:choice_scarf",
                "cobblemon:choice_specs",
                "cobblemon:assault_vest",
                "cobblemon:heavy_duty_boots",
                "cobblemon:leftovers",
                "cobblemon:rocky_helmet",
                "cobblemon:weakness_policy",
                "cobblemon:upgrade",
                "cobblemon:dubious_disc",
                "cobblemon:protector",
                "cobblemon:electirizer",
                "cobblemon:magmarizer",
                "cobblemon:prism_scale",
                "cobblemon:reaper_cloth",
                "cobblemon:razor_claw",
                "cobblemon:razor_fang",
                "cobblemon:sachet",
                "cobblemon:whipped_dream",
                "cobblemon:metal_alloy",
                "cobblemon:auspicious_armor",
                "cobblemon:malicious_armor",
                "cobblemon:syrupy_apple",
                "cobblemon:tart_apple",
                "cobblemon:sweet_apple",
                "cobblemon:black_augurite",
                "cobblemon:galarica_cuff",
                "cobblemon:galarica_wreath",
                "cobblemon:link_cable",
                "cobblemon:scroll_of_darkness",
                "cobblemon:scroll_of_waters",
                "cobblemon:dream_ball",
                "cobblemon:dream_rod",
                "cobblemon:safari_ball",
                "cobblemon:park_ball",
                "cobblemon:ancient_*_ball",
                "cobblemon:ancient_*_rod",
                // Mega Showdown: mega stones, Z-crystals e parafernália de gimmick
                "mega_showdown:*ite",
                "mega_showdown:*ite_x",
                "mega_showdown:*ite_y",
                "mega_showdown:*ite_z",
                "mega_showdown:*ium_z",
                "mega_showdown:*_z",
                "mega_showdown:mega_bracelet*",
                "mega_showdown:mega_ring",
                "mega_showdown:mega_stone",
                "mega_showdown:keystone",
                "mega_showdown:z_ring*",
                "mega_showdown:*_z_power_ring",
                "mega_showdown:*s_z_ring",
                "mega_showdown:red_orb",
                "mega_showdown:blue_orb",
                "mega_showdown:adamant_orb",
                "mega_showdown:lustrous_orb",
                "mega_showdown:griseous_orb",
                "mega_showdown:cornerstone_mask",
                "mega_showdown:hearthflame_mask",
                "mega_showdown:wellspring_mask",
                "mega_showdown:booster_energy",
                "mega_showdown:rotom_catalogue",
                "mega_showdown:pika_case",
                "mega_showdown:ash_cap",
                "mega_showdown:max_soup",
                "mega_showdown:sweet_max_soup",
                "mega_showdown:dynamax_candy",
                "mega_showdown:meltan"
        )));

        m.put("RARE", new ArrayList<>(List.of(
                "cobblemon:ultra_ball",
                "cobblemon:ultra_rod",
                "cobblemon:quick_ball",
                "cobblemon:dusk_ball",
                "cobblemon:timer_ball",
                "cobblemon:repeat_ball",
                "cobblemon:luxury_ball",
                "cobblemon:level_ball",
                "cobblemon:moon_ball",
                "cobblemon:love_ball",
                "cobblemon:heavy_ball",
                "cobblemon:fast_ball",
                "cobblemon:friend_ball",
                "cobblemon:lure_ball",
                "cobblemon:heal_ball",
                "cobblemon:dive_ball",
                "cobblemon:net_ball",
                "cobblemon:nest_ball",
                "cobblemon:sport_ball",
                "cobblemon:*_rod",
                "cobblemon:*_stone",
                "cobblemon:oval_stone",
                "cobblemon:exp_candy_m",
                "cobblemon:pp_up",
                "cobblemon:hp_up",
                "cobblemon:protein",
                "cobblemon:iron",
                "cobblemon:calcium",
                "cobblemon:zinc",
                "cobblemon:carbos",
                "cobblemon:max_potion",
                "cobblemon:max_elixir",
                "cobblemon:max_ether",
                "cobblemon:revive",
                "cobblemon:*_mint",
                "cobblemon:*_mochi",
                "cobblemon:*_feather",
                "cobblemon:*_candy",
                "cobblemon:*_gem",
                "cobblemon:exp_share",
                "cobblemon:metal_coat",
                "cobblemon:kings_rock",
                "cobblemon:deep_sea_scale",
                "cobblemon:deep_sea_tooth",
                "cobblemon:dragon_scale",
                "cobblemon:power_*",
                "cobblemon:soothe_bell",
                "cobblemon:smoke_ball",
                "cobblemon:shell_bell",
                "cobblemon:muscle_band",
                "cobblemon:wise_glasses",
                "cobblemon:expert_belt",
                "cobblemon:scope_lens",
                "cobblemon:wide_lens",
                "cobblemon:zoom_lens",
                "cobblemon:quick_claw",
                "cobblemon:bright_powder",
                "cobblemon:focus_band",
                "cobblemon:mirror_herb",
                "cobblemon:punching_glove",
                "cobblemon:protective_pads",
                "cobblemon:throat_spray",
                "cobblemon:room_service",
                "cobblemon:eject_pack",
                "cobblemon:eject_button",
                "cobblemon:red_card",
                "cobblemon:blunder_policy",
                "cobblemon:utility_umbrella",
                "cobblemon:terrain_extender",
                "cobblemon:damp_rock",
                "cobblemon:heat_rock",
                "cobblemon:icy_rock",
                "cobblemon:smooth_rock",
                "cobblemon:light_clay",
                "cobblemon:binding_band",
                "cobblemon:grip_claw",
                "cobblemon:float_stone",
                "cobblemon:iron_ball",
                "cobblemon:lagging_tail",
                "cobblemon:sticky_barb",
                "cobblemon:toxic_orb",
                "cobblemon:flame_orb",
                "cobblemon:black_sludge",
                "cobblemon:shed_shell",
                "cobblemon:metal_powder",
                "cobblemon:quick_powder",
                "cobblemon:light_ball",
                "cobblemon:absorb_bulb",
                "cobblemon:cell_battery",
                "cobblemon:luminous_moss",
                "cobblemon:snowball",
                "cobblemon:*_seed",
                "cobblemon:fossilized_*",
                "cobblemon:*_fossil",
                "cobblemon:old_amber_fossil",
                "cobblemon:relic_coin",
                "cobblemon:blank_tm",
                "cobblemon:medicinal_leek",
                "cobblemon:*_sweet",
                "cobblemon:*_sherd",
                "cobblemon:masterpiece_teacup",
                "cobblemon:unremarkable_teacup",
                "cobblemon:chipped_pot",
                "cobblemon:cracked_pot",
                // Mega Showdown
                "mega_showdown:*_tera_shard",
                "mega_showdown:*_memory",
                "mega_showdown:*_plate",
                "mega_showdown:*_drive",
                "mega_showdown:tera_pouch_*",
                "mega_showdown:tera_pouch",
                "mega_showdown:*_nectar",
                "mega_showdown:adrenaline_orb",
                "mega_showdown:zygarde_cell",
                "mega_showdown:zygarde_core",
                "mega_showdown:max_honey",
                "mega_showdown:max_mushroom",
                "mega_showdown:sparkling_stone_*",
                "mega_showdown:blank_z",
                "mega_showdown:*_mega_*",
                "mega_showdown:*_glove",
                "mega_showdown:*_glasses",
                "mega_showdown:*_anchor",
                "mega_showdown:*_bracelet",
                "mega_showdown:*_pendant",
                "mega_showdown:*_ring"
        )));

        m.put("UNCOMMON", new ArrayList<>(List.of(
                "cobblemon:great_ball",
                "cobblemon:premier_ball",
                "cobblemon:hyper_potion",
                "cobblemon:super_potion",
                "cobblemon:ether",
                "cobblemon:elixir",
                "cobblemon:full_heal",
                "cobblemon:exp_candy_s",
                "cobblemon:*_apricorn",
                "cobblemon:*_apricorn_seed",
                "cobblemon:*_mulch",
                "cobblemon:mulch_base",
                "cobblemon:aprijuice_*",
                "cobblemon:*_tumblestone",
                "cobblemon:tumblestone",
                "cobblemon:remedy",
                "cobblemon:fine_remedy",
                "cobblemon:superb_remedy",
                "cobblemon:medicinal_brew",
                "cobblemon:revival_herb",
                "cobblemon:galarica_nuts",
                "cobblemon:pokerod_smithing_template",
                "cobblemon:poke_bait"
        )));

        return m;
    }

    private static Map<String, Integer> defaultRodLevels() {
        Map<String, Integer> m = new LinkedHashMap<>();
        // Nível 1 — bolas básicas
        for (String s : new String[]{"poke_rod", "premier_rod", "heal_rod", "friend_rod", "love_rod",
                "sport_rod", "citrine_rod", "verdant_rod", "azure_rod", "roseate_rod", "slate_rod",
                "ancient_poke_rod", "ancient_citrine_rod", "ancient_verdant_rod", "ancient_azure_rod",
                "ancient_roseate_rod", "ancient_slate_rod", "ancient_ivory_rod"}) {
            m.put("cobblemon:" + s, 1);
        }
        // Nível 2 — Great Ball e especiais comuns
        for (String s : new String[]{"great_rod", "net_rod", "dive_rod", "nest_rod", "repeat_rod",
                "timer_rod", "lure_rod", "fast_rod", "heavy_rod", "ancient_great_rod",
                "ancient_feather_rod", "ancient_wing_rod", "ancient_heavy_rod"}) {
            m.put("cobblemon:" + s, 2);
        }
        // Nível 3 — Ultra Ball e especiais fortes
        for (String s : new String[]{"ultra_rod", "dusk_rod", "quick_rod", "luxury_rod", "level_rod",
                "moon_rod", "ancient_ultra_rod", "ancient_jet_rod", "ancient_leaden_rod",
                "ancient_gigaton_rod"}) {
            m.put("cobblemon:" + s, 3);
        }
        // Nível 4 — bolas raras
        for (String s : new String[]{"beast_rod", "dream_rod", "cherish_rod", "park_rod", "safari_rod",
                "ancient_origin_rod"}) {
            m.put("cobblemon:" + s, 4);
        }
        // Nível 5 — Master Ball
        m.put("cobblemon:master_rod", 5);
        return m;
    }
}
