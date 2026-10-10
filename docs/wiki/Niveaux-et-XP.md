# Niveaux et XP

## Niveaux de classe

`config.yml → leveling` :

```yaml
leveling:
  max-level: 100
  base-xp: 100        # XP requise = base-xp × niveau ^ exponent
  exponent: 1.5
  default-mob-xp: 5
  player-kill-xp: 50
  ignore-passive-mobs: true
  mob-xp:
    zombie: 10
    warden: 500
    ender_dragon: 2000
```

| Niveau | XP pour passer au suivant |
|---|---|
| 1 | 100 |
| 10 | 3 162 |
| 20 | 8 944 |
| 50 | 35 355 |

Chaque niveau donne des **points d'arbre** et des **points d'attributs** (`config.yml → progression`), et les stats `*-per-level` de la classe.

## MythicMobs

```yaml
mythicmobs:
  default-xp: 15
  xp-per-level: 2       # par niveau du mob au-delà de 1
  mob-xp:
    ExempleBoss: 500
```

## En groupe

- **Partagée** : l'XP est divisée entre les membres proches (40 blocs), +10 % par membre supplémentaire.
- **Égale** : chaque membre proche reçoit l'XP complète.

## Affichage

Bossbar de 5 s qui additionne les gains successifs (`hud.yml → xp-display`). Les niveaux de métier ont leur propre barre jaune (`job-xp-display`).

## Ce qu'on ne peut pas faire

- Pas d'XP de classe pour les actions de métier (le métier a sa propre XP).
- Pas de perte d'XP à la mort.
