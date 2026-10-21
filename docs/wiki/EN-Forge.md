# Forge

[🇫🇷 Français](Forge.md) · 🇬🇧 English

File: `metiers/forge.yml`.

## Principle

When a player whose job is listed in `crafters` crafts a targeted item, the item gets:

- a **rarity**,
- **random stats**, positive or negative,
- **gem sockets**,
- the blacksmith's signature.

The higher the job level, the more likely the high rarities. Quality items are crafted one at a time (no Shift + click). Stats apply when the item is **equipped** (weapon in hand, armor worn, shield in off hand).

```yaml
crafters:
  forgeron:
    targets:
      - "(IRON|GOLDEN|DIAMOND|NETHERITE|CHAINMAIL)_(SWORD|AXE|HELMET|CHESTPLATE|LEGGINGS|BOOTS)"
      - "SHIELD"
      - "BOW"
      - "CROSSBOW"
      - "MACE"
```

## Rarities

Relative chance = `max(min-weight, weight + weight-per-level × level)`.

| Rarity | Min. level | Stats | Sockets | Power | Chance for a stat to be negative |
|---|---|---|---|---|---|
| Common | — | 0-1 | 0 | ×0.6 | 50% |
| Uncommon | — | 1-2 | 1 | ×0.8 | 35% |
| Rare | 5 | 2-3 | 1 | ×1.0 | 25% |
| Epic | 15 | 3-4 | 2 | ×1.25 | 15% |
| Legendary | 25 | 4-5 | 3 | ×1.5 | 5% |
| **Unique** | 30 | fixed | fixed | — | — |

The blacksmith's specialization adds levels to the roll (`rarity-levels`) and strengthens stats (`power`) in its field.

## Possible stats

`stat-pool` lists the stats per item type (`weapon`, `ranged`, `armor`) with a `[min, max]` range before the rarity multiplier. A negative stat is the rolled value × `negative-scale` (0.6), subtracted.

```yaml
stat-pool:
  weapon:
    melee-damage: [0.02, 0.08]
    crit-chance: [0.01, 0.05]
    health: [1, 3]
```

## Unique items

Each unique item can be forged **only once on the whole server**. When the roll gives "Unique", a unique item that is still free and compatible with the material is chosen. If none is left, the item becomes Legendary.

```yaml
uniques:
  excalibur:
    name: "<gold><bold>Excalibur"
    materials: ["*_SWORD"]
    lore: ["<gray>The sword of the legendary king."]
    sockets: 3
    stats: {melee-damage: 1.2, crit-chance: 0.1, health: 4}
```

The history is stored in `forge-uniques.yml` (who forged it, when). `/classe admin libererunique <id>` makes it available again (for example if the item was destroyed). The old copy, if it still exists, **loses its power**: it breaks as soon as it is in a player's inventory. So there are never two active copies.

A forged item upgraded to netherite at the smithing table keeps its rarity, stats and gems, and its name is updated.

Admin: `/classe admin forge <player> <rarity>` applies a rarity to the item held in hand (for testing).

## What cannot be done

- No forging of custom items (CraftEngine, MythicCrucible…): only targeted vanilla materials.
- No upgrading of an already forged item (its rarity and stats are fixed; only gems can be added).
- The plugin cannot prevent a unique item from being destroyed (lava, cactus…); it must then be released manually.
- A released copy stored in a chest only breaks when a player picks it up.
