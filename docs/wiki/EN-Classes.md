# Classes

[🇫🇷 Français](Classes.md) · 🇬🇧 English

File: `classes/classes.yml`. The top of the file documents every available key.

## Default classes and subclasses

| Class | Skills | Subclasses (unlocked at level 20 + conditions) |
|---|---|---|
| **Warrior** (Guerrier) | Charge, War cry, Whirlwind | **Berserker** (200 kills, 5,000 damage dealt) · **Paladin** |
| **Mage** | Arcane bolt, Blink, Mana shield | **Pyromancer** (300 spells cast) · **Cryomancer** |
| **Archer** | Piercing arrow, Volley, Disengage | **Hunter** (300 kills) · **Ranger** |
| **Assassin** | Shadow step, Vanish, Poison blade | **Shadow** · **Duelist** |
| **Cleric** (Clerc) | Heal, Blessing, Purify | **Priest** (1,500 health healed) · **Druid** |

A subclass keeps the skills of its class and adds 2 or 3. Choosing a subclass is final (except with `/classe reset`).

## Stats

| Key | Effect | Type |
|---|---|---|
| `health` / `health-per-level` | Bonus health (half-hearts) | additive |
| `mana` / `mana-per-level` | Maximum mana | additive |
| `mana-regen` / `mana-regen-per-level` | Mana per second | additive |
| `melee-damage`, `ranged-damage`, `magic-damage` | Damage (1.2 = +20%) | multiplier |
| `damage-taken` | Damage taken (0.8 = −20%) | multiplier |
| `speed`, `attack-speed` | Speed (+0.1 = +10%) | additive |
| `armor` | Armor points | additive |
| `crit-chance`, `crit-multiplier` | Melee critical hits | additive |

These keys are the same everywhere: races, traits, jobs, attributes, skill tree, forge, gems.

## Weaknesses (`flaws`)

Weaknesses specific to a class are written directly in its `flaws` key. To impose a reusable weakness from `weaknesses.yml`, add `weaknesses: [id]` (see [Weaknesses](EN-Weaknesses.md)).

| Type | Parameters | Example |
|---|---|---|
| `DAMAGE_TAKEN` | `causes`, `damagers`, `multiplier` | Mage: +30% fire damage; +25% against zombies |
| `DAMAGE_DEALT` | `kinds` (MELEE, RANGED, SKILL), `targets`, `multiplier` | −15% ranged damage |
| `FORBIDDEN_ARMOR` | `materials` | Mage: no iron armor or better |
| `WEAPON_PENALTY` | `materials`, `multiplier` | Cleric: −40% with a sword |
| `WATER_WEAKNESS` | — | Slow and weak in water |
| `SUNLIGHT_WEAKNESS` | — | Assassin: weak in full sunlight |
| `HUNGER` | `multiplier` | Warrior: faster hunger |
| `HEALING_REDUCTION` | `multiplier` | Reduced healing received |
| `DARKNESS_MANA_BLOCK` | — | No regeneration in the dark |
| `MANA_REGEN` | `multiplier` (0 = blocked) | Mana twice as slow |
| `EFFECT` | `effects` (`"slowness:1"`) | Slowed in water |
| `PERIODIC_DAMAGE` | `damage`, `interval` (s), `fire-ticks` | Burns in the sun like a vampire |

**Every weakness accepts conditions**: it only applies when all of them are true.

```yaml
flaws:
  - type: PERIODIC_DAMAGE
    damage: 1
    interval: 3
    fire-ticks: 60
    conditions: {time: DAY, under-sky: true, weather: [CLEAR]}
    description: "Burns in the sun"
```

Available conditions: `time`, `weather`, `dimensions`, `worlds`, `biomes`, `light-below`, `light-above`, `under-sky`, `in-water`, `in-rain`, `in-lava`, `health-below`, `health-above`, `y-below`, `y-above`, `sneaking`, `sprinting`, `gliding`, `holding`, `wearing`, `permission`, `chance`. Details on the [Weaknesses](EN-Weaknesses.md#conditions) page.

`inherit-flaws: false` on a subclass so it does not take over the parent's weaknesses.

## Subclass conditions (`requirements`)

| Type | Parameters |
|---|---|
| `LEVEL` | `amount` |
| `KILLS` | `target` (`zombie`, `player`, `any`…), `amount` |
| `SKILL_USES` | `target` (skill id or `any`), `amount` |
| `COUNTER` | `target`: `blocks_mined`, `damage_dealt`, `damage_taken`, `healing_done`, `mana_spent` |
| `PERMISSION` | `target`: permission node |

The `/classe sous-classe` menu shows the progress of each condition.

## Example: adding a class

```yaml
classes:
  necromancer:
    name: "<dark_purple><bold>Necromancer"
    icon: WITHER_SKELETON_SKULL
    description: ["<gray>Commands the dead."]
    stats:
      health: -2
      mana: 120
      mana-regen: 2.5
      magic-damage: 1.15
    flaws:
      - type: SUNLIGHT_WEAKNESS
        description: "Weakened in full sunlight"
    skills:
      drain:
        type: ARCANE_BOLT
        name: "<dark_purple>Drain"
        icon: WITHER_ROSE
        mana: 20
        cooldown: 4
        params: {damage: 6}
    subclasses:
      lich:
        name: "<dark_purple><bold>Lich"
        icon: SKELETON_SKULL
        requirements:
          - {type: LEVEL, amount: 25}
          - {type: KILLS, target: any, amount: 500}
```

## What cannot be done

- No multiclassing: a player has **one** class and at most **one** subclass.
- No sub-subclass (a subclass cannot have its own subclasses).
- A new skill **type** requires Java (see [Commands → API](EN-Commands.md)) or MythicMobs (`MYTHIC` type).
