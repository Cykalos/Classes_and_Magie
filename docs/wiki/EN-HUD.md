# HUD

[🇫🇷 Français](HUD.md) · 🇬🇧 English

File: `hud.yml` (+ `config.yml → hud` for the action bar).

## Action bar

```yaml
hud:
  format: "<red>❤ <health>/<max_health></red>  <aqua>✦ <mana>/<max_mana></aqua>  <gold>Lv. <level></gold> <gray>(<xp_percent>%)</gray>"
  cast-mode-prefix: "<light_purple>✦ </light_purple>"   # shown in skill mode
```

## XP gained

A 5-second bossbar that adds up successive gains (`xp-display`, modes `BOSSBAR`, `ACTIONBAR`, `BOTH`, `NONE`). Job XP has its own bar (`job-xp-display`).

## Floating damage

```yaml
damage-indicators:
  visible-to: INVOLVED     # ALL, INVOLVED (attacker + victim), ATTACKER
  show-on-mobs: true
  show-on-players: true
  show-environmental: false
  formats:
    normal: "<#ff5555>-<damage>"
    critical: "<gold><bold>✦ -<damage> ✦</bold>"
    skill: "<#d68cff>-<damage>"
    player-hit: "<red>-<damage> ❤"
    heal: "<green>+<amount> ❤"
```

Damage from MythicMobs skills is also shown.

## Target health

`target-health.mode`: `BOSSBAR` (top of the screen), `ABOVE_HEAD` (above the target), `BOTH` or `NONE`.

- Name and **level of MythicMobs mobs** (`level-format`).
- Colour depending on remaining health (thresholds `medium-threshold`, `low-threshold`).
- Above the head: configurable health bar, visible to everyone or only to those who hit (`visible-to: DAMAGERS`).

## In a party

```yaml
party:
  share-with-party: true      # nearby allies see the damage and the targets' health bar
  no-bossbar-in-party: true   # in a party: bar above the head instead of the bossbar
  range: 48                   # maximum distance of allies
```

## Party board

`party-board`: title, lines per member (`<leader>`, `<name>`, `<class>`, `<level>`, `<bar>`, `<health>`, `<mana>`…), footer.

## What cannot be done

- The party board uses the sidebar scoreboard: it can conflict with another scoreboard plugin (disable one of them).
- No built-in resource-pack interface (bar textures): MiniMessage text only.
