# Forge

🇫🇷 Français · [🇬🇧 English](EN-Forge.md)

Fichier : `metiers/forge.yml`.

## Principe

Quand un joueur ayant un métier listé dans `crafters` fabrique un objet ciblé, l'objet reçoit :

- une **rareté**,
- des **stats aléatoires**, positives ou négatives,
- des **emplacements de gemmes**,
- la signature du forgeron.

Plus le niveau de métier est haut, plus les raretés élevées sont probables. Les objets de qualité se fabriquent un par un (pas de Maj + clic). Les stats s'appliquent quand l'objet est **équipé** (arme en main, armure portée, bouclier en main secondaire).

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

## Raretés

Chance relative = `max(min-weight, weight + weight-per-level × niveau)`.

| Rareté | Niveau min. | Stats | Emplacements | Puissance | Chance qu'une stat soit négative |
|---|---|---|---|---|---|
| Commun | — | 0-1 | 0 | ×0,6 | 50 % |
| Peu commun | — | 1-2 | 1 | ×0,8 | 35 % |
| Rare | 5 | 2-3 | 1 | ×1,0 | 25 % |
| Épique | 15 | 3-4 | 2 | ×1,25 | 15 % |
| Légendaire | 25 | 4-5 | 3 | ×1,5 | 5 % |
| **Unique** | 30 | fixes | fixes | — | — |

La spécialisation du forgeron ajoute des niveaux au tirage (`rarity-levels`) et renforce les stats (`power`) pour son domaine.

## Stats possibles

`stat-pool` liste les stats par type d'objet (`weapon`, `ranged`, `armor`) avec un intervalle `[min, max]` avant le multiplicateur de rareté. Une stat négative vaut la valeur tirée × `negative-scale` (0,6) en moins.

```yaml
stat-pool:
  weapon:
    melee-damage: [0.02, 0.08]
    crit-chance: [0.01, 0.05]
    health: [1, 3]
```

## Objets uniques

Chaque objet unique ne peut être forgé **qu'une seule fois sur tout le serveur**. Quand le tirage donne « Unique », un objet unique encore libre et compatible avec le matériau est choisi. S'il n'en reste aucun, l'objet devient Légendaire.

```yaml
uniques:
  excalibur:
    name: "<gold><bold>Excalibur"
    materials: ["*_SWORD"]
    lore: ["<gray>L'épée du roi légendaire."]
    sockets: 3
    stats: {melee-damage: 1.2, crit-chance: 0.1, health: 4}
```

L'historique est dans `forge-uniques.yml` (qui l'a forgé, quand). `/classe admin libererunique <id>` le rend de nouveau disponible (par exemple si l'objet a été détruit). L'ancien exemplaire, s'il existe encore, **perd son pouvoir** : il se brise dès qu'il se trouve dans l'inventaire d'un joueur. Il n'y a donc jamais deux exemplaires actifs.

Un objet forgé amélioré en netherite à la table de forgeron garde sa rareté, ses stats et ses gemmes, et son nom est mis à jour.

Admin : `/classe admin forge <joueur> <rareté>` applique une rareté à l'objet tenu en main (pour tester).

## Ce qu'on ne peut pas faire

- Pas de forge sur des objets custom (CraftEngine, MythicCrucible…) : seulement les matériaux vanilla ciblés.
- Pas d'amélioration d'un objet déjà forgé (sa rareté et ses stats sont fixes ; seules les gemmes s'ajoutent).
- Le plugin ne peut pas empêcher la destruction d'un objet unique (lave, cactus…) ; il faut alors le libérer à la main.
- Un exemplaire libéré rangé dans un coffre ne se brise qu'au moment où un joueur le récupère.
