# Jobs

[🇫🇷 Français](Métiers.md) · 🇬🇧 English

File: `metiers/jobs.yml`. Menu: `/metier` (alias `/job`).

## Principle

- **A single job** per player, offered after the class, race and traits.
- It can be changed (`allow-change`, delay `change-cooldown-hours`, 24 h by default). The level and specialization of each job are **kept** if the player goes back to a former job.
- A job gives stats and effects, **job XP**, and reserves **actions** to its members.

Default jobs: Miner (Mineur), Blacksmith (Forgeron), **Jeweler** (Joaillier), Lumberjack (Bûcheron), Farmer (Fermier), Alchemist (Alchimiste), Enchanter (Enchanteur), Fisher (Pêcheur), Tamer (Dresseur).

## Job levels

```yaml
leveling:
  max-level: 50
  base-xp: 50       # required XP = base-xp × level ^ exponent
  exponent: 1.5
```

XP is earned by doing the job (`xp-sources`). No XP for a block placed by a player (anti-abuse).

| Action | Target | XP given |
|---|---|---|
| `BREAK` | Broken block | xp |
| `PLACE` | Placed block | xp |
| `CRAFT` | Crafted item | xp per craft |
| `SMELT` | Item taken from a furnace | xp × items |
| `ENCHANT` | Enchanting table | xp × levels spent |
| `BREW` | Brewing stand | xp × potions |
| `FISH`, `BREED`, `TAME` | Fishing, breeding, taming | xp |
| `REFINE` | Cutting a gem (target = gem id) | xp |
| `INTERACT_BLOCK` | Using a block (restriction only) | — |
| `USE_ITEM` | Right click with an item: bone meal, hoe… (restriction only) | — |

Targets: material or entity names, `*` = wildcard, `(A|B)` = A or B. Without `targets`: every target.

## Reserved actions and unlocked recipes

```yaml
restrictions:
  - action: CRAFT
    targets: ["DIAMOND_(SWORD|AXE|PICKAXE|SHOVEL|HOE|HELMET|CHESTPLATE|LEGGINGS|BOOTS)", "MACE"]
    jobs: [forgeron]
    min-level: 15                       # recipe unlocked at level 15
    display: "Diamond gear, mace"       # short text in /metier
    message: "<red>Only a skilled blacksmith can do that."   # optional
```

Default examples:

| Job | Level 1 | Unlocked later |
|---|---|---|
| Miner | Iron, gold, lapis, redstone | Diamond/emerald (10), ancient debris (25) |
| Blacksmith | Iron gear, shield, anvil | Gold + crossbow (5), diamond + mace (15), smithing table (30) |
| Lumberjack | Dark and exotic woods | Nether stems (10) |
| Farmer | Breeding, pumpkins, melons, planting crops, bone meal and tilling | Nether wart (10) |
| Alchemist / Enchanter / Fisher / Tamer | Brewing stand / enchanting table / fishing / taming | — |

Permission `cykclasses.bypass.jobs`: ignores the restrictions.

## Specializations (sub-jobs)

At the configured level (20 by default), the player chooses **one specialization** for their job, from the button in the `/metier` menu. The choice is **final** for that job (admin: `resetspec`).

| Job | Specialization | Bonus |
|---|---|---|
| Miner | **Prospector** | +3% raw gem chance on ores, XP ×1.1 |
| Miner | **Driller** | Haste II, XP ×1.3 on stone and ores |
| Blacksmith | **Armorsmith** | Armor and shields: +10 rarity levels, stats +15%, XP ×1.25 |
| Blacksmith | **Weaponsmith** | Swords, axes, maces: +10 rarity levels, stats +15% |
| Blacksmith | **Bowyer** | Bows and crossbows: +12 rarity levels, stats +20%, XP ×1.5 |
| Jeweler | **Lapidary** | +12% purity on each cut gem |
| Jeweler | **Goldsmith** | +12 levels for the gem rarity roll |

```yaml
jobs:
  forgeron:
    specializations:
      armurier:
        name: "<gray><bold>Armorsmith"
        icon: IRON_CHESTPLATE
        min-level: 20
        description: ["<gray>Armor and shield specialist."]
        stats: {armor: 1}            # permanent stats
        effects: []                  # permanent effects
        bonuses:
          - targets: ["*_HELMET", "*_CHESTPLATE", "*_LEGGINGS", "*_BOOTS", "SHIELD"]
            rarity-levels: 10        # levels added to the rarity roll (forge and cutting)
            power: 0.15              # forged item stats +15%
            xp-multiplier: 1.25      # job XP on these targets
            purity: 0                # purity added to cut gems
            gem-chance: 0            # raw gem chance added (mining)
```

`targets: []` = the bonus applies to every target of the job.

## What cannot be done

- No second job, and no two specializations on the same job.
- Restrictions apply to vanilla materials and entities (not to CraftEngine custom items — use [Items](EN-Items.md) for that).
- Breeding is refused before the food is consumed: a player without the job does not lose their carrots or wheat.
- No job XP for actions done by machines (redstone, automatic farms without a player).
