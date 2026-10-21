# Progression

🇫🇷 Français · [🇬🇧 English](EN-Progression.md)

## Arbre de compétences (`/arbre`, `classes/skilltree.yml`)

Un arbre par classe de base + un arbre **commun**. Les branches peuvent être réservées à une sous-classe.

```yaml
trees:
  guerrier:
    title: "Arbre du guerrier"
    nodes:
      charge_brutale:
        name: "<red>Charge brutale"
        icon: IRON_BOOTS
        x: 1          # colonne 0-8
        y: 0          # ligne 0-4
        max-rank: 3
        cost: 1
        requires: []
        required-level: 5
        required-subclass: berserker   # optionnel
        description: ["<gray>Charge plus forte et plus fréquente."]
        stats: {melee-damage: 1.02}    # par rang
        skills:
          charge: {damage: 0.1, cooldown: -0.1, mana: -0.1}   # +10 % dégâts, −10 % recharge, −10 % mana par rang
```

## Attributs (`/stats`, `classes/attributes.yml`)

Force, Dextérité, Intelligence, Vitalité, Sagesse, Agilité, Endurance.

```yaml
attributes:
  force:
    name: "<red><bold>Force"
    icon: IRON_SWORD
    max: 50
    per-point: {melee-damage: 1.01}   # +1 % par point (multiplicateurs cumulés)
```

Les attributs servent aussi de **conditions d'objets** (`items.yml → conditions.attributes`, voir [Objets](Objets.md)).

## Points et réinitialisation

```yaml
progression:
  starting-skill-points: 1
  skill-points-per-level: 1
  starting-attribute-points: 3
  attribute-points-per-level: 2
  tree-reset-cooldown-hours: 24
  attribute-reset-cooldown-hours: 24
```

`/arbre reset` et `/stats reset` rendent tous les points (gratuit, une fois par délai). Admin : `addpoints`, `resettree`, `resetstats`.

## Ce qu'on ne peut pas faire

- Un nœud ne peut pas donner une nouvelle compétence, seulement améliorer les stats ou une compétence existante.
- Pas de réinitialisation partielle (un seul nœud).
