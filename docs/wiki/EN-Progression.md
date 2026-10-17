# Progression

[🇫🇷 Français](Progression.md) · 🇬🇧 English

## Skill tree (`/arbre`, `skilltree.yml`)

One tree per base class + a **common** tree. Branches can be reserved to a subclass.

```yaml
trees:
  guerrier:
    title: "Warrior tree"
    nodes:
      charge_brutale:
        name: "<red>Brutal charge"
        icon: IRON_BOOTS
        x: 1          # column 0-8
        y: 0          # row 0-4
        max-rank: 3
        cost: 1
        requires: []
        required-level: 5
        required-subclass: berserker   # optional
        description: ["<gray>Stronger and more frequent charge."]
        stats: {melee-damage: 1.02}    # per rank
        skills:
          charge: {damage: 0.1, cooldown: -0.1, mana: -0.1}   # +10% damage, −10% cooldown, −10% mana per rank
```

## Attributes (`/stats`, `attributes.yml`)

Strength, Dexterity, Intelligence, Vitality, Wisdom, Agility, Endurance.

```yaml
attributes:
  force:
    name: "<red><bold>Strength"
    icon: IRON_SWORD
    max: 50
    per-point: {melee-damage: 1.01}   # +1% per point (multipliers stack)
```

Attributes are also used as **item conditions** (`items.yml → conditions.attributes`, see [Items](EN-Items.md)).

## Points and reset

```yaml
progression:
  starting-skill-points: 1
  skill-points-per-level: 1
  starting-attribute-points: 3
  attribute-points-per-level: 2
  tree-reset-cooldown-hours: 24
  attribute-reset-cooldown-hours: 24
```

`/arbre reset` and `/stats reset` give back every point (free, once per delay). Admin: `addpoints`, `resettree`, `resetstats`.

## What cannot be done

- A node cannot grant a new skill, only improve stats or an existing skill.
- No partial reset (a single node).
