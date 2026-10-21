# Gems and jewelcrafting

[🇫🇷 Français](Gemmes-et-joaillerie.md) · 🇬🇧 English

File: `metiers/gems.yml`.

## A gem's journey

1. **Found raw** by a miner (ores not placed by a player).
2. **Cut** by a **jeweler**: right click a **stonecutter** with a raw gem in hand → the workshop opens.
3. Cutting gives a **rarity** and a **purity** that multiply its stats.
4. **Socketed** by a jeweler into a forged item that has a free socket: pick up the gem with the mouse and click the item. This is final.

The jobs allowed to socket gems are set in `gems.yml`:

```yaml
socketing:
  jobs: [joaillier]   # empty = everyone
```

A raw gem cannot be socketed.

## Finding raw gems

```yaml
drops:
  jobs: [mineur]
  targets: ["*_ORE", "ANCIENT_DEBRIS"]
  base-chance: 0.01             # 1% per block
  chance-per-job-level: 0.001   # +0.1% per job level
```

At level 30: 1% + 3% = 4% per ore, +3% with the **Prospector** specialization. The rolled gem depends on its `weight`.

## The jeweler's workshop

- Top: the **chance of each rarity** and the **purity range** at the current level.
- Centre: the raw gems from the inventory, grouped by type.
- **Click**: cut one gem. **Shift + click**: cut the whole stock of that type.
- Bottom: the last gem obtained.

Each cut gem gives jeweler XP (`REFINE` in `jobs.yml`).

## Cutting rarity

| Rarity | Min. level | Power |
|---|---|---|
| Common | — | ×0.8 |
| Refined | — | ×1.0 |
| Rare | 5 | ×1.3 |
| Epic | 15 | ×1.6 |
| Legendary | 25 | ×1.8 |
| **Perfect** | 35 | ×2.0 (always 100% purity) |

Same chance rules as the forge: `max(min-weight, weight + weight-per-level × level)`. The **Goldsmith** specialization adds 12 levels to the roll.

## Purity

```yaml
refining:
  station: STONECUTTER
  jobs: [joaillier]
  purity:
    min: 35
    max: 80
    per-level: 0.8      # purity = random(min, max) + 0.8 × level, capped at 100
  purity-min-factor: 0.5
  always-pure: [PARFAITE]
```

**Final multiplier** = rarity power × (0.5 + 0.5 × purity).

| Example | Calculation | Multiplier |
|---|---|---|
| Rare at 50% | 1.3 × 0.75 | ×0.98 |
| Epic at 80% | 1.6 × 0.9 | ×1.44 |
| Perfect | 2.0 × 1.0 | ×2.0 |

An Epic Ruby (+4% melee damage) at 80% therefore gives +5.8%. The **Lapidary** specialization adds 12% purity.

## Defining a gem

```yaml
gems:
  rubis:
    name: "<red>Ruby"
    material: RED_DYE
    item-model: "mypack:gems/ruby"     # optional
    weight: 30
    description: ["<gray>A blood-red stone."]
    applies-to: [WEAPON]               # WEAPON, RANGED, ARMOR
    stats: {melee-damage: 1.04}
```

- `raw-name`: name of a raw gem (`"<gray>Raw <gem>"`).
- `raw-model-suffix`: suffix added to `item-model` for the raw look (e.g. `_raw`).

Default gems: Ruby (Rubis), Sapphire (Saphir), Emerald (Émeraude), Topaz (Topaze), Onyx, Hawk's eye (Œil de faucon), Pure diamond (Diamant pur).

## Admin commands

- `/classe admin gemme <player> <id> [amount]`: **raw** gem.
- `/classe admin gemme <player> <id> <amount> <rarity> [purity]`: **cut** gem.

## What cannot be done

- A socketed gem cannot be removed.
- No gem fusion and no recutting of an already cut gem.
- Gems can only be socketed into **forged** items (which have sockets).
