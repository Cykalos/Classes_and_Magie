# Levels and XP

[🇫🇷 Français](Niveaux-et-XP.md) · 🇬🇧 English

## Class levels

`config.yml → leveling`:

```yaml
leveling:
  max-level: 100
  base-xp: 100        # required XP = base-xp × level ^ exponent
  exponent: 1.5
  default-mob-xp: 5
  player-kill-xp: 50
  ignore-passive-mobs: true
  mob-xp:
    zombie: 10
    warden: 500
    ender_dragon: 2000
```

| Level | XP to reach the next one |
|---|---|
| 1 | 100 |
| 10 | 3,162 |
| 20 | 8,944 |
| 50 | 35,355 |

Each level gives **skill tree points** and **attribute points** (`config.yml → progression`), plus the class's `*-per-level` stats.

## MythicMobs

```yaml
mythicmobs:
  default-xp: 15
  xp-per-level: 2       # per mob level above 1
  mob-xp:
    ExampleBoss: 500
```

## In a party

- **Shared**: XP is split between nearby members (40 blocks), +10% per extra member.
- **Equal**: each nearby member gets the full XP.

## Display

A 5-second bossbar that adds up successive gains (`hud.yml → xp-display`). Job levels have their own yellow bar (`job-xp-display`).

## What cannot be done

- No class XP for job actions (the job has its own XP).
- No XP loss on death.
