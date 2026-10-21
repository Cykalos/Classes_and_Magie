# Weaknesses

[🇫🇷 Français](Faiblesses.md) · 🇬🇧 English

File: `personnage/weaknesses.yml`. Command: `/faiblesses` (alias `/weaknesses`).

For players, everything that penalises their character is a **weakness**. The `/faiblesses` menu groups them all, with their origin:

- **Class**, **race** and **traits**: written directly in `classes.yml`, `races.yml` or `traits.yml` (`flaws` key).
- **Imposed**: weaknesses from `weaknesses.yml` always applied to a race, a class or a subclass.
- **Randomly rolled**: after the traits, with odds that depend on the player's race and class. Players can reroll if they have rerolls left.

In the files, `flaws` (weakness written in place) and `weaknesses.yml` (reusable weakness) use exactly the same types and conditions.

## Roll

```yaml
roll:
  enabled: true      # false = imposed weaknesses only
  min: 1             # random number between min and max
  max: 2
  rerolls: 1         # rerolls per player (/faiblesses relancer)
  show-step: true    # menu during character creation (false = silent roll)
```

## Imposed weaknesses

Two ways, pick one:

**In the race or the class** (recommended, everything in one place):

```yaml
# races.yml
nain:
  weaknesses: [vertige]

# classes.yml (class or subclass; a subclass inherits its class's weaknesses, unless inherit-flaws: false)
pyromancien:
  weaknesses: [vulnerable_au_feu]
```

**Or in `weaknesses.yml`**:

```yaml
predefined:
  races:
    elfe: [claustrophobe]
  classes:
    pyromancien: [vulnerable_au_feu]
```

Imposed by default: Elf → *Claustrophobic*, Dwarf → *Vertigo*, Orc → *Poor marksman*, Tiefling → *Infernal blood*, Felys → *Sensitive to cold*. Humans have none.

An imposed weakness is never rolled a second time. Exclusions (`exclude-races`, `exclude-classes`) also apply to imposed weaknesses: an Orc warrior does not get *Poor marksman*, since the warrior is already penalised at range. With `weight: 0`, a weakness is **never** rolled randomly: it is only used as an imposed weakness.

## Defining a weakness

```yaml
weaknesses:
  vulnerable_au_feu:
    name: "<red>Vulnerable to fire"
    icon: BLAZE_POWDER
    severity: "<gold>Medium weakness"     # free text under the name
    description: ["<gray>Flames consume you faster."]
    weight: 10                            # relative chance
    races: []                             # empty = every race
    classes: []                           # empty = every class and subclass
    exclude-races: [demon]                # never for these races
    exclude-classes: [pyromancien]
    race-weights: {elfe: 2, felin: 2}     # twice as likely for these races
    class-weights: {cryomancien: 3}
    conflicts: []                         # never rolled together
    stats: {health: -2}                   # modifiers (same keys as classes.yml)
    effects: []                           # permanent effects
    flaws:
      - type: DAMAGE_TAKEN
        causes: [FIRE, FIRE_TICK, LAVA]
        multiplier: 1.3
        description: "+30% fire damage"
```

**Actual chance** = `weight` × race multiplier × class multiplier × subclass multiplier. If any of them is 0, the weakness cannot be rolled for that character.

Included weaknesses: Hydrophobic, Photosensitive, Vulnerable to fire, Sensitive to cold, Brittle bones, Prey of the dead, Fear of the dark, Unstable mana, Poor marksman, Panic, Vertigo, Claustrophobic, Anaemia, Moon-cursed, Infernal blood (Tiefling). Their exclusions avoid duplicates with the weaknesses already present in classes and races (a Felys never rolls *Hydrophobic*, it already hates water).

## Weakness types

| Type | Parameters | Effect |
|---|---|---|
| `DAMAGE_TAKEN` | `multiplier`, `causes`, `damagers` | Damage taken × multiplier |
| `DAMAGE_DEALT` | `multiplier`, `kinds`, `targets` | Damage dealt × multiplier |
| `HEALING_REDUCTION` | `multiplier` | Healing received × multiplier |
| `HUNGER` | `multiplier` | Hunger × multiplier |
| `MANA_REGEN` | `multiplier` | Mana regeneration × multiplier (0 = blocked) |
| `EFFECT` | `effects` | Potion effects while the conditions are true |
| `PERIODIC_DAMAGE` | `damage`, `interval`, `fire-ticks` | Periodic damage |
| `FORBIDDEN_ARMOR` | `materials` | Forbidden armor |
| `WEAPON_PENALTY` | `materials`, `multiplier` | Reduced damage with some weapons |
| `WATER_WEAKNESS`, `SUNLIGHT_WEAKNESS`, `DARKNESS_MANA_BLOCK` | — | Legacy shortcuts |

- `causes`: Minecraft damage causes (`FIRE`, `FIRE_TICK`, `LAVA`, `FALL`, `DROWNING`, `FREEZE`, `MAGIC`, `POISON`, `WITHER`, `ENTITY_ATTACK`, `PROJECTILE`, `ENTITY_EXPLOSION`…).
- `damagers` / `targets`: entity types (`ZOMBIE`, `"*SKELETON*"`, `PLAYER`) or MythicMobs mobs (`"mythic:InternalName"`).
- `kinds`: `MELEE`, `RANGED` (bow, crossbow), `SKILL` (skills).

## Conditions

Each weakness can have a `conditions` block. It only applies when **all** of them are true.

| Condition | Example | True when… |
|---|---|---|
| `time` | `NIGHT` | it is night (`DAY` / `NIGHT`) |
| `weather` | `[RAIN, THUNDER]` | the weather matches (`CLEAR`, `RAIN`, `THUNDER`) |
| `dimensions` | `[NETHER]` | `NORMAL`, `NETHER` or `THE_END` |
| `worlds` | `[world_pvp]` | the player is in one of these worlds |
| `biomes` | `["*DESERT*", BADLANDS]` | the biome matches |
| `light-below` / `light-above` | `7` | block light lower / higher |
| `under-sky` | `true` | under open sky (`false` = under cover) |
| `in-water` / `in-rain` / `in-lava` | `true` | in water, in the rain, in lava |
| `health-below` / `health-above` | `0.3` | health below / above 30% |
| `y-below` / `y-above` | `20` | altitude |
| `sneaking` / `sprinting` / `gliding` | `true` | sneaking, sprinting, flying with elytra |
| `holding` | `["*_SWORD"]` | item in main hand |
| `wearing` | `["GOLDEN_*"]` | at least one matching armor piece |
| `permission` | `"server.cursed"` | the player has this permission |
| `chance` | `0.25` | 25% chance at each check |

Example — a vampire that burns in the sun and hits harder at night:

```yaml
flaws:
  - type: PERIODIC_DAMAGE
    damage: 1
    interval: 3
    fire-ticks: 60
    conditions: {time: DAY, under-sky: true, weather: [CLEAR]}
    description: "Burns in the sun"
  - type: DAMAGE_DEALT
    multiplier: 1.2
    conditions: {time: NIGHT}
    description: "+20% damage at night"
```

`DAMAGE_DEALT` with a multiplier above 1 becomes a **bonus**: weaknesses can also be used to create conditional advantages.

## Administration

- `/classe admin faiblesse <player> reset`: new roll next time.
- `/classe admin faiblesse <player> add <id>` / `remove <id>`: add or remove a specific weakness.
- Placeholders: `%cykclasses_weaknesses%` (every weakness: class, race, traits, imposed, rolled) and `%cykclasses_weaknesses_count%`.

## What cannot be done

- Rolled weaknesses do not change automatically when the player changes class or race (use `reset`). Imposed weaknesses always follow the current race and class.
- Conditions are checked every second for effects and periodic damage, and on every hit for damage multipliers.
