# Titles

[🇫🇷 Français](Titres.md) · 🇬🇧 English

File: `personnage/titles.yml`. Command: `/classe titres` or `/titres` (alias `/titles`), or the Titles button of the `/classe` menu.

Titles are **unlocked by the player's actions**: level reached, monsters killed, quests completed, distance travelled, Minecraft advancements… Only one title is equipped at a time. It is shown **above the head** and **in chat**, and can give **effects**. Some titles are purely honorary.

## Rarities

| Rarity | Feature |
|---|---|
| Common · Uncommon · Rare | — |
| Epic · Legendary | Announced to the whole server |
| **Unique** | A single player on the server can get it: the first one to meet the conditions |
| **Secret** | Shown as "???" in the menu until unlocked, hidden conditions |

Rarities are set in `titles.yml → rarities` (name, announcement, unique, secret); more can be added.

## Included titles

31 titles: progression (Novice, Veteran, Living legend…), combat (Hunter, Bane of the dead, Dragon slayer…), one title per class (Honed blade, Archmage, Lynx eye, Silent shadow, Hand of light), jobs and exploration (Relentless miner, Traveller, Explorer of worlds…), story (Guild member, Guild legend), 2 unique and 4 secret titles. The included texts are in French and can be renamed freely.

Titles based on totals (kills, mined blocks, statistics, advancements, playtime…) count **everything the player has already done**: a long-time player can unlock several of them right after the update.

## For players

- **Menu** `/titres`: every title, filtered by "Unlocked" / "To unlock". Each title shows its rarity, its effects (or "honorary title"), and for those still locked, the conditions with progress.
- Click an unlocked title: **equip** it; click again: **remove** it.
- `/titres equiper <id>` · `/titres retirer`.
- The first unlocked title is equipped automatically (`auto-equip-first`).

## Title format

```yaml
titles:
  tueur_de_dragons:
    name: "<light_purple>Dragon slayer"
    rarity: EPIC
    icon: DRAGON_HEAD
    description: ["<gray>The one who saw the guardian of the Heart fall."]
    unique: false            # default depends on the rarity
    hidden: false            # default depends on the rarity
    requirements:            # same conditions as quests
      level: 20
    unlock:                  # objectives to reach
      - {type: KILLS_TOTAL, targets: [ender_dragon], amount: 1, display: "Defeat the Ender Dragon"}
    stats: {melee-damage: 1.05}          # effects while equipped
    effects: ["night_vision:1"]          # permanent potion effects
    flaws:                               # conditional effects
      - {type: DAMAGE_DEALT, targets: [ENDERMAN], multiplier: 1.15, description: "+15% against Endermen"}
```

- `requirements`: `level`, `max-level`, `classes`, `races`, `jobs`, `job-level`, `quests`, `not-quests`, `permission`, `titles`, `checks` (see [Quests → Requirements](EN-Quests.md#requirements-requirements)).
- `unlock`: every objective type of [quests](EN-Quests.md#objectives). Without `unlock`, the title is not unlocked on its own: it is given by a quest (`rewards → titles`) or by an administrator.
- `stats` · `effects` · `flaws`: same keys as classes, races and [weaknesses](EN-Weaknesses.md) (conditions included: a title can give a bonus only at night, below layer 0…).

## Useful unlock objectives

| Type | Example |
|---|---|
| `LEVEL` · `JOB_LEVEL` | `{type: LEVEL, amount: 50}` |
| `KILLS_TOTAL` | `{type: KILLS_TOTAL, targets: [zombie, husk], amount: 1000}` (`mythic:name` supported) |
| `SKILL_USES_TOTAL` | `{type: SKILL_USES_TOTAL, targets: [boule_de_feu], amount: 500}` |
| `COUNTER` | `{type: COUNTER, targets: [blocks_mined], amount: 20000}` (`damage_dealt`, `damage_taken`, `healing_done`, `mana_spent`) |
| `STATISTIC` | `{type: STATISTIC, targets: [DEATHS], amount: 25}` · `MINE_BLOCK:DIAMOND_ORE` · `KILL_ENTITY:ZOMBIE` · `WALK_ONE_CM` |
| `ADVANCEMENT` | `{type: ADVANCEMENT, targets: ["minecraft:story/enter_the_nether"]}` |
| `PLAYTIME` | `{type: PLAYTIME, amount: 100}` (hours) |
| `QUEST` | `{type: QUEST, targets: [la_nuit_d_orage]}`; without `targets`: number of completed quests |
| `TITLES` | `{type: TITLES, amount: 15}`; `targets: ["rarity:EPIC"]` to count one rarity |
| `KILL` (with conditions) | `{type: KILL, amount: 300, conditions: {sneaking: true}}` (counts from when the title is added) |

For `QUEST` and `ADVANCEMENT`, `amount` is the number of listed items required (1 by default; set the total count to require all of them).

## Display

```yaml
display:
  enabled: true
  format: "<title>"            # <title>, <rarity>
  height: 0.55                 # above the name tag
  scale: 0.9
  background: default          # default, none or #AARRGGBB
  see-through: false
  hide-when-sneaking: true
  show-own: false              # does the player see their own title?

chat:
  format: "<dark_gray>«</dark_gray><title><dark_gray>»</dark_gray> "
```

- The title above the head is hidden when the player is invisible (potion, *Vanish* skill), in spectator mode, sneaking (configurable) or hidden by a vanish plugin.
- In chat, `<title>` is added to the `config.yml → chat` formats (already in the default formats). For a server that already has its `config.yml`, add `<title>` to `global-format` and `party-format`.
- Quests can require a title (`requirements → titles`) or give one (`rewards → titles`).

## Administration

- `/classe admin titre <player> give <id>`: gives a title.
- `/classe admin titre <player> take <id>`: removes a title (a unique title becomes available again).
- `/classe admin titre <player> reset`: removes all of the player's titles.
- `/classe admin titre liberer <id>`: releases a unique title; the former owner loses it (on their next login if offline).
- Placeholders: `%cykclasses_title%`, `%cykclasses_title_id%`, `%cykclasses_titles_count%`, `%cykclasses_titles_total%`.
- Unique title owners are stored in `donnees/titres-uniques.yml`.
- The module is disabled with `modules.titles: false`.

## What cannot be done

- Only one title equipped at a time.
- The title above the head is a floating display that follows the player: on a heavily loaded server, it can lag very slightly during fast movement.
- "Action" objectives (such as `KILL` with conditions) only count from the moment the title exists; totals (`KILLS_TOTAL`, `STATISTIC`…) also count the past.
