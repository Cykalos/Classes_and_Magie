# Items

[🇫🇷 Français](Objets.md) · 🇬🇧 English

## Starter kits (`objets/kits.yml`)

Given when the class is chosen, and the subclass if a kit has its id.

```yaml
give-once: true      # one kit per class, even after a reset

kits:
  guerrier:
    items:
      - item: "minecraft:iron_sword"
        name: "<red>Recruit's sword"
        lore: ["<gray>Faithful companion of the first fights."]
        slot: 0
        enchants: {sharpness: 1}
      - item: "minecraft:shield"
        slot: OFFHAND
      - item: "craftengine:mypack:runic_sword"
      - item: "mythic:RecruitSword"
      - item: "executableitems:grimoire"
    commands:
      - "eco give %player% 100"
```

Options: `amount`, `slot` (`HEAD`, `CHEST`, `LEGS`, `FEET`, `OFFHAND` or 0-35), `name`, `lore`, `enchants`, `unbreakable`, `item-model` (vanilla only).

### Supported custom items (through [UniItem](https://github.com/ProjectUnified/UniItem))

| Plugin | Format |
|---|---|
| Vanilla | `minecraft:iron_sword` |
| CraftEngine | `craftengine:pack:id` |
| ExecutableItems | `executableitems:id` |
| MythicMobs / MythicCrucible | `mythic:Name` |
| ItemsAdder · Oraxen · Nexo | `itemsadder:ns:id` · `oraxen:id` · `nexo:id` |
| MMOItems | `mmoitems:TYPE:ID` |

The list of available prefixes is printed in the console at startup.

Admin: `/classe admin kit <player> [class]` gives a kit again.

## Item restrictions (`objets/items.yml`)

Custom item stats stay handled by their plugin (CraftEngine, MythicCrucible…). Here you decide **who** can use them.

```yaml
rules:
  excalibur:
    match:                              # at least one criterion
      materials: ["NETHERITE_SWORD"]
      custom-ids: ["mythic:Excalibur", "craftengine:mypack:excalibur"]
      name-contains: "Excalibur"
      lore-contains: "Sacred blade"
    conditions:                         # all must be met
      classes: [guerrier, paladin]
      races: [humain, elfe]
      jobs: [forgeron]
      min-level: 30
      attributes: {force: 20}
    block: [ATTACK, WEAR]               # default: every use
    message: "<red>Only a worthy paladin can wield Excalibur."
```

| Blocked use | Effect |
|---|---|
| `ATTACK` | Hitting with it |
| `SHOOT` | Shooting with a bow / crossbow |
| `USE` | Right click |
| `WEAR` | Wearing as armor or in the off hand (removed automatically) |

Without `message`, the missing conditions are shown. Permission `cykclasses.bypass.items`: ignores every rule.

## What cannot be done

- Forbidden items are not **hidden** (they stay visible): the restriction blocks use, not display.
- No creation of custom items: use CraftEngine, MythicCrucible or ExecutableItems, then reference them here.
