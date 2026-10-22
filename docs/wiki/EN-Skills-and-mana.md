# Skills and mana

[🇫🇷 Français](Compétences-et-mana.md) · 🇬🇧 English

## Casting a skill

1. **F** (swap hands): the hotbar shows the **skill icons** in slots 1 to 8. The weapon stays in hand in slot 9.
2. **1 to 8** (or the mouse wheel) casts the skill. The icon shows the cooldown (remaining seconds), turns blue when mana is missing, grey when the skill is locked.
3. **F** again, a click in the inventory or 15 s without casting: back to the normal hotbar. Minecraft does not tell the server when players open their own inventory: the first click leaves the mode.

Slots without a skill are filled with a grey square (`casting.empty-slot-icon`), so picked-up loot goes to the inventory and not to the hotbar.

Hotbar items are saved while in skill mode, even after a death or a crash.

Other ways: `/competence <id|number>` (alias `/skill`), or a click in `/classe competences`.

Settings in `config.yml → casting`: `swap-hands-toggle`, `require-sneak` (F while sneaking), `timeout-seconds`, `locked-icon`, `pvp`.

## Defining a skill

```yaml
skills:
  charge:
    type: CHARGE            # implementation
    name: "<red>Charge"
    description: ["<gray>Rushes forward and knocks enemies back."]
    icon: IRON_BOOTS
    icon-model: "mypack:skills/charge"    # resource pack model (optional)
    custom-model-data: 1001               # alternative (optional)
    mana: 20
    health-cost: 0          # half-hearts
    cooldown: 8             # seconds
    unlock-level: 1
    params: {damage: 6, distance: 8}
```

8 skills maximum per class (subclass included), one per slot.

## Available types

`ARCANE_BOLT`, `ARROW_RAIN`, `BEAR_TRAP`, `BLESSING`, `BLINK`, `BLOOD_RAGE`, `CHARGE`, `DISENGAGE`, `DIVINE_GRACE`, `ENTANGLING_ROOTS`, `EXECUTION`, `EXPLOSIVE_ARROW`, `FIREBALL`, `FLAME_AURA`, `FROST_NOVA`, `HEAL`, `HOLY_LIGHT`, `HOLY_SHIELD`, `HUNTERS_MARK`, `ICE_LANCE`, `JUDGMENT`, `LUNGE`, `MANA_SHIELD`, `METEOR_SHOWER`, `PARRY`, `PIERCING_ARROW`, `POISON_BLADE`, `PURIFY`, `SHADOW_STEP`, `SHADOW_VEIL`, `SHATTERING_BLOW`, `VANISH`, `VOLLEY`, `WAR_CRY`, `WHIRLWIND`, `WILD_SPIRIT`, `MYTHIC`.

Heals and buffs (`HEAL`, `BLESSING`, `WAR_CRY`, `HOLY_SHIELD`…) **only affect the caster and their party**. Offensive skills hit allies only if the party has enabled friendly fire.

## MythicMobs skills

```yaml
  shadow_orb:
    type: MYTHIC
    name: "<dark_purple>Shadow orb"
    icon: ECHO_SHARD
    mana: 25
    cooldown: 8
    params: {skill: "MyMythicSkill", power: 1.0}
```

Mana and cooldown are handled by Classes and Magie, the effect by MythicMobs.

## Mana

- Maximum and regeneration come from the class, level, race, traits, attributes, skill tree and equipment.
- `config.yml → mana`: `regen-interval-ticks`, `regen-in-combat`, `combat-regen-multiplier` (regeneration reduced for 5 s after a hit).
- The `DARKNESS_MANA_BLOCK` weakness stops regeneration in the dark.

## Arrows

Skill arrows, and arrows shot with a bow by players, hit even if the target has just taken damage. In Minecraft they would bounce off without damage, for example on a zombie burning in the sun. For bows, this is set in `config.yml → combat.bow-ignores-hit-delay` (`false` = Minecraft behaviour). Crossbows keep the Minecraft behaviour because of the Multishot enchantment.

## What cannot be done

- No more than 8 skills in the hotbar.
- No combos or automatically triggered passive skills (use stats, the skill tree or MythicMobs).
- New mechanics require Java (`CykClassesAPI.registerSkillType`) or MythicMobs.
