# Races and traits

[🇫🇷 Français](Races-et-traits.md) · 🇬🇧 English

## Races (`races.yml`)

Chosen right after the class. Default races: **Human** (Humain), **Elf** (Elfe), **Dwarf** (Nain), **Orc**, **Tiefling** (Tieffelin), **Felys** (Félys).

Each race has stats, weaknesses (`flaws`, and `weaknesses` to impose a weakness from `weaknesses.yml`), permanent effects, recommended classes and **class penalties**:

| Race | Penalty when playing… |
|---|---|
| Human | — (versatile) |
| Elf | Warrior, Berserker |
| Dwarf | Mage, Archer, Assassin |
| Orc | Mage, Cleric, Priest |
| Tiefling | Cleric, Paladin, Cryomancer |
| Felys | Warrior, Paladin |

```yaml
races:
  nain:
    name: "<gold><bold>Dwarf"
    icon: IRON_PICKAXE
    stats: {health: 4, damage-taken: 0.95, speed: -0.05}
    effects: ["haste:1"]
    recommended-classes: [guerrier, clerc]
    class-penalties:
      mage:
        stats: {magic-damage: 0.85, mana: -20}
        description: "Magic resists dwarves"
```

`config.yml → character.race-can-change`: allows `/race` afterwards or not. `reset.keep-race`: keep the race on a class reset.

## Traits (`traits.yml`)

After the race, **3 traits** are rolled randomly (positive, negative or mixed).

```yaml
roll:
  count: 3
  max-negative: 1
  min-positive: 1
  unique-chance: 0.01   # chance to get a free unique trait IN ADDITION
  rerolls: 3            # rerolls per player (/traits relancer)
  rarity-weights: {COMMON: 60, UNCOMMON: 25, RARE: 10, EPIC: 3}
```

A trait has: `rarity`, `kind` (`POSITIVE`, `NEGATIVE`, `MIXED`), `stats`, `flaws`, `effects`, `conflicts` (traits never rolled together), `weight`.

### Unique traits

`rarity: UNIQUE`: **a single player on the whole server** can carry this trait. There can be as many as wanted (default: *Chosen of the gods*, *Legendary weapon master*, *Cursed immortal*). Each one has its own owner.

- A unique trait already taken is never rolled again.
- It becomes free again if its owner loses it (`/classe admin resettraits <player>`).

## What cannot be done

- Players cannot pick their traits manually (only reroll).
- A race has no active skills of its own (use `effects` or stats).
