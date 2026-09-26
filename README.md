# Cobblemon Fishing OP

Mod de Minecraft **1.21.1 / Fabric** inspirado no vídeo *"Pokemon Minecraft, But Fishing Is OP!"*:
pescar passa a dar itens do Cobblemon aleatórios e overpowered, incluindo um item raro que
invoca um Pokémon lendário.

- **Mod id:** `fishingop`
- **Dependências obrigatórias:** Fabric API, [Cobblemon 1.8.1](https://modrinth.com/mod/cobblemon) (+ fabric-language-kotlin, que o Cobblemon já traz)
- **Opcional:** [Cobblemon: Mega Showdown](https://modrinth.com/mod/mega-showdown) — se estiver instalado, os itens
  (mega stones, Z-crystals, plates, tera shards…) entram na pool e os lendários dele entram no Invocador, sem configurar nada.

## Mecânicas

### 1. Pesca OP
| Vara | Comportamento |
|---|---|
| Poké Rod (todas as 48 variações) | A cada fisgada que renderia um Pokémon, **70%** (configurável) de virar item OP; nos outros 30% o Pokémon vem normal. O ramo de lixo/tesouro da loot table vira sempre item OP. |
| Vara de pesca vanilla | **100%** dos drops viram itens do Cobblemon (substitui peixe, lixo e tesouro). |

A pool é montada **do registro em runtime**: todo item dos namespaces em `itemNamespaces`
(`cobblemon` e `mega_showdown` por padrão) entra sozinho, menos o que casar com a `blacklist`.
Itens novos de updates entram sem precisar mexer no mod.

### 2. Tiers
`Comum · Incomum · Raro · Épico · Lendário`, com pesos configuráveis. O que não casar com nenhuma
regra cai em Comum. Master Ball, Ability Patch, Rare Candy, Exp. Candy XL, Master Rod, Legend Plate
e afins ficam no topo.

> O Cobblemon 1.8.1 **não tem Bottle Cap nem Gold Bottle Cap** — esses itens não existem no registro
> dele, então o topo do tier Lendário usa os equivalentes acima.

Outras rolagens:
- **Bônus de pokébola** — rolagem extra e independente (padrão 25%) que garante uma pokébola a mais.
  Usa os mesmos pesos de tier, então a Master Ball continua rara dentro dela.
- **Duplicação** — 30% de vir x2–x4, 5% de vir x8–x16, sempre respeitando o stack máximo.
  Drops Raro+ ganham +15% nas duas chances.
- **Luck of the Sea** — aumenta o peso de Raro/Épico/Lendário, a chance de duplicação e a do Invocador.

### 3. Invocador Lendário (`fishingop:legendary_summoner`)
Drop do tier Lendário com chance própria (padrão **0,5% por pesca**). Clicando com o botão direito
ele é consumido e spawna um Pokémon selvagem aleatório à frente do jogador, com som, partículas e
anúncio no chat.

A pool são todas as espécies **implementadas** com label `legendary`, `mythical`, `ultra_beast` ou
`paradox`, menos a `speciesBlacklist` (padrão `["arceus"]`, pra ele ficar como chefe final).
No Cobblemon 1.8.1 puro isso dá 22 espécies; com o Mega Showdown instalado passa de 35.

### 4. A pokébola da Poké Rod aumenta a sorte
Cada variação de rod é um item próprio no registro do Cobblemon, e o datapack
`data/cobblemon/pokerods/<rod>.json` diz qual pokébola ela usa. O mod agrupa isso em 6 níveis:

| Nível | Varas |
|---|---|
| 0 | vara de pesca vanilla |
| 1 | Poké, Premier, Heal, Friend, Love, Sport, Citrine, Verdant, Azure, Roseate, Slate (+ versões *ancient*) |
| 2 | Great, Net, Dive, Nest, Repeat, Timer, Lure, Fast, Heavy (+ *ancient*) |
| 3 | Ultra, Dusk, Quick, Luxury, Level, Moon (+ *ancient*) |
| 4 | Beast, Dream, Cherish, Park, Safari, Ancient Origin |
| 5 | Master |

Cada nível aplica multiplicador nos pesos de Raro/Épico/Lendário, bônus na duplicação e
multiplicador na chance do Invocador. Soma com Luck of the Sea. A vara mostra
`Sorte de pesca: Nível X` no tooltip, colorido pelo nível.

Distribuição real (200k pescas simuladas, sem encantamento):

| Vara | Comum | Incomum | Raro | Épico | Lendário | Invocador |
|---|---|---|---|---|---|---|
| Vanilla (nível 0) | 59,7% | 26,0% | 9,9% | 3,1% | 1,3% | 0,51% |
| Poké Ball (nível 1) | 57,5% | 25,0% | 12,1% | 3,8% | 1,5% | 0,59% |
| Ultra Ball (nível 3) | 52,2% | 22,7% | 17,3% | 5,6% | 2,2% | 0,82% |
| Master Ball (nível 5) | 41,8% | 18,0% | 27,6% | 8,9% | 3,7% | 1,53% |

### 5. Feedback visual
Drops Raro+ mostram action bar colorida por tier + som. Lendário ganha título. O Invocador ganha
título grande, som épico e anúncio no chat do servidor.

## Comandos (op, nível 2)

```
/fishingop reload
/fishingop simulate <quantidade> [pokébola]
/fishingop give summoner [quantidade]
```

`simulate` roda N pescas e imprime a distribuição por tier, a taxa do Invocador, os bônus de
pokébola e os itens mais frequentes. O argumento `pokébola` aceita `vanilla`, o nome da bola
(`master_ball`) ou o da vara (`cobblemon:master_rod`):

```
/fishingop simulate 1000 poke_ball
/fishingop simulate 1000 master_ball
```

## Config

Arquivo `config/fishingop.json`, criado no primeiro boot. `/fishingop reload` recarrega tudo
e remonta as pools.

| Campo | O que faz |
|---|---|
| `pokeRodItemChance` | Chance (0–1) de a fisgada da Poké Rod virar item em vez de Pokémon. Padrão `0.70`. |
| `vanillaRodAlwaysItems` | `false` devolve o loot vanilla pra vara normal. **Precisa de `/reload` além do `/fishingop reload`**, porque loot tables só recarregam com o datapack. |
| `itemNamespaces` | Namespaces varridos no registro. Padrão `["cobblemon", "mega_showdown"]`. |
| `blacklist` | Ids fora da pool. Aceita curinga: `cobblemon:potted_*`. |
| `tierWeights` | Peso base de cada tier. Maior = mais comum. |
| `tierRules` | Ids/padrões por tier. Avaliado de `LEGENDARY` para `UNCOMMON`; o que sobra vira `COMMON`. |
| `pokeballBonus.chance` | Rolagem extra da pokébola (padrão `0.25`), com bônus por nível de rod e por Luck of the Sea. |
| `duplication` | `smallChance`/`smallMin`/`smallMax`, `bigChance`/`bigMin`/`bigMax` e `rarePlusBonus`. |
| `luckOfTheSea` | Quanto cada nível do encantamento soma nos tiers, na duplicação e no Invocador. |
| `rodLuck.levels` | Mapa `id da vara -> nível 0–5`. Vara desconhecida entra como nível 1. |
| `rodLuck.tierMultiplier` / `duplicationBonus` / `summonerMultiplier` | Listas indexadas pelo nível. |
| `summoner.chance` | Chance por pesca do Invocador. Padrão `0.005`. |
| `summoner.pokemonLevel` | Nível do lendário invocado. Padrão `70`. |
| `summoner.labels` | Labels aceitas na pool de invocação. |
| `summoner.speciesBlacklist` | Espécies banidas. Padrão `["arceus"]`. |
| `feedback` | `minTier` do aviso, `sounds`, `titles`, `broadcastSummonerDrop`. |

### Exemplos rápidos

```jsonc
// Mais itens, menos Pokémon
"pokeRodItemChance": 0.90

// Invocador em 2% por pesca
"summoner": { "chance": 0.02, ... }

// Master Ball ainda mais dominante
"rodLuck": { "tierMultiplier": [1.0, 1.25, 1.6, 2.0, 2.8, 8.0], ... }

// Só itens do Cobblemon, ignorando o Mega Showdown
"itemNamespaces": ["cobblemon"]
```

## Build

```bash
./gradlew build      # jar em build/libs/cobblemon-fishing-op-1.0.0.jar
./gradlew runClient  # cliente de dev com o Cobblemon já carregado
```

O build precisa alcançar o Maven do Cobblemon
(`https://maven.impactdev.net/repository/development/` ou `https://artefacts.cobblemon.com/releases`)
e `runClient` precisa do CDN de assets da Mojang (`resources.download.minecraft.net`).

## Notas de implementação

- **Sem Mixin.** O ramo "Pokémon" usa `CobblemonEvents.BOBBER_SPAWN_POKEMON_PRE`, que é cancelável;
  o ramo "item" das duas varas usa `LootTableEvents.REPLACE` da Fabric API nas tabelas
  `cobblemon:fishing/pokerod` e `minecraft:gameplay/fishing`.
- Ao cancelar `BOBBER_SPAWN_POKEMON_PRE`, o Cobblemon 1.8.1 sai de `retrieve()` antes do `discard()`
  e o bobber ficaria preso na água; o mod descarta o bobber por conta própria.
- O `build.gradle` aplica o plugin `org.jetbrains.kotlin.jvm` mesmo o mod sendo 100% Java. Não é
  enfeite: é ele que liga o remap das anotações `@Metadata` do jar do Cobblemon no Loom. Sem isso o
  `kotlin-reflect` do Cobblemon procura `net.minecraft.class_2960` (nome intermediary) em dev e o
  jogo nem sobe. É a mesma solução que o Mega Showdown usa.
- O tooltip de sorte da vara é client-side e lê o `config/fishingop.json` **do cliente**. Num servidor
  dedicado, copie a config pro cliente se quiser que o número bata.
