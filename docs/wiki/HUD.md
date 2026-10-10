# HUD

Fichier : `hud.yml` (+ `config.yml → hud` pour la barre d'action).

## Barre d'action

```yaml
hud:
  format: "<red>❤ <health>/<max_health></red>  <aqua>✦ <mana>/<max_mana></aqua>  <gold>Niv. <level></gold> <gray>(<xp_percent>%)</gray>"
  cast-mode-prefix: "<light_purple>✦ </light_purple>"   # affiché en mode compétences
```

## XP gagnée

Bossbar de 5 s, qui additionne les gains successifs (`xp-display`, modes `BOSSBAR`, `ACTIONBAR`, `BOTH`, `NONE`). L'XP de métier a sa propre barre (`job-xp-display`).

## Dégâts flottants

```yaml
damage-indicators:
  visible-to: INVOLVED     # ALL, INVOLVED (attaquant + victime), ATTACKER
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

Les dégâts des compétences MythicMobs sont aussi affichés.

## PV de la cible

`target-health.mode` : `BOSSBAR` (haut de l'écran), `ABOVE_HEAD` (au-dessus de la cible), `BOTH` ou `NONE`.

- Nom et **niveau des mobs MythicMobs** (`level-format`).
- Couleur selon les PV restants (seuils `medium-threshold`, `low-threshold`).
- Au-dessus de la tête : barre de vie configurable, visible par tous ou seulement par ceux qui ont frappé (`visible-to: DAMAGERS`).

## Panneau du groupe

`party-board` : titre, lignes par membre (`<leader>`, `<name>`, `<class>`, `<level>`, `<bar>`, `<health>`, `<mana>`…), pied de page.

## Ce qu'on ne peut pas faire

- Le panneau du groupe utilise le scoreboard latéral : il peut entrer en conflit avec un autre plugin de scoreboard (désactiver l'un des deux).
- Pas d'interface graphique par resource pack (textures de barres) intégrée : seulement du texte MiniMessage.
