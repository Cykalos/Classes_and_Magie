# Quests

[🇫🇷 Français](Quêtes.md) · 🇬🇧 English

Folder: `quetes/` (every `.yml` file in the folder and its subfolders is read). Command: `/quetes` (aliases `/quests`, `/journal`).

**Every quest is optional**: a player can play without ever doing one.

## Categories

| Category | Role | Included by default |
|---|---|---|
| `TUTORIAL` | Tutorial, offered at the end of character creation | *First steps of an Awakened* (5 stages) |
| `MAIN` | Main story | 6 chapters, *The Shards of the Fracture* (levels 2 to 40) |
| `CLASS` | Quests reserved to a class | 2 per class (levels 3 and 15) |
| `SIDE` | Side quests, some repeatable | 5 (fishing, bounties, exploration…) |
| `SECRET` | Invisible until discovered | 4 (blood moon, abyss, ruins…) |

Included files: `tutoriel.yml`, `principale.yml`, `de-classe.yml`, `annexes.yml`, `secretes.yml`. They are only created if the `quetes/` folder does not exist: a deleted file does not come back. The included quest texts are in French and can be rewritten freely.

## For players

- **Tutorial**: at the end of character creation, a chat message offers to start it (`[Commencer]` / `[Plus tard]`). It stays available in the journal.
- **Journal** (`/quetes`, or the book at the top right of the `/classe` menu): one tab per category, plus an "In progress" tab. Each quest shows its level, description, state (in progress, available, locked, completed), objectives with progress, missing requirements and rewards.
  - Click an available quest: start it.
  - Click a quest in progress: track it (or stop tracking it) in the sidebar.
  - Shift + right click a quest in progress: abandon it.
- **Sidebar**: tracked quests and their objectives, updated live. In a party, the tracked quest is shown below the party members. `/quetes panneau` shows or hides it.
- **Secret quests**: they appear in the journal once discovered. The tab shows how many are left to discover.

## Quest format

```yaml
quests:
  la_porte_brulante:
    name: "<gold>V. The Burning Gate"
    category: MAIN                 # TUTORIAL, MAIN, CLASS, SIDE, SECRET
    level: 25                      # indicative level shown
    icon: BLAZE_ROD
    description: ["<gray>A passage to a world of flames."]
    start: AUTO                    # MANUAL (journal), AUTO (as soon as available), OFFER (offered)
    hidden: false                  # true by default for SECRET
    repeatable: false
    cooldown-hours: 0              # delay before doing it again
    abandonable: true
    requirements:
      level: 25
      quests: [la_nuit_des_eveilles]
    stages:
      - text: ["<gray>Text shown when the stage begins"]
        objectives:
          - {type: CONDITION, conditions: {dimensions: [NETHER]}, display: "Enter the Nether"}
      - objectives:
          - {type: KILL, targets: [BLAZE], amount: 6}
          - {type: COLLECT, targets: [BLAZE_ROD], amount: 4, take: true}
    rewards:
      xp: 2500
      skill-points: 1
      money: 300
      items:
        - {item: "minecraft:diamond", amount: 3}
    complete-text: ["<gray>Text shown at the end"]
```

Stages follow each other in order; every objective of a stage must be met to move on to the next one.

## Requirements (`requirements`)

| Key | Effect |
|---|---|
| `level` / `max-level` | Minimum / maximum class level |
| `classes` | Class or subclass (`[guerrier]` includes its subclasses) |
| `races` · `jobs` | Race or job |
| `job-level` | Level of the current job |
| `quests` | Quests that must be completed (quest chains) |
| `not-quests` | Quests that must not have been done (exclusive choices: join one side or the other) |
| `permission` | Required permission (quests unlocked by another plugin) |

With `quests.hide-other-classes: true` (config.yml), quests reserved to another class or race do not appear in the journal.

## Objectives

**Actions** (counted each time):

| Type | Counts… | `targets` |
|---|---|---|
| `KILL` | kills | entity types, `mythic:InternalName` |
| `BREAK` · `PLACE` | broken / placed blocks (not blocks placed by a player) | blocks |
| `CRAFT` · `SMELT` | crafts, smelted items | items |
| `FISH` · `BREED` · `TAME` | fishing, breeding, taming | fished items / animals |
| `ENCHANT` · `BREW` · `REFINE` | enchantments, potions, cut gems | items / gems |
| `CONSUME` | food and potions consumed | items |
| `SKILL` | skills cast | skill ids |
| `INTERACT_ENTITY` | right click on an entity (`name:` text of its name, for an NPC) | entity types |
| `INTERACT_BLOCK` | right click on a block | blocks |
| `COMMAND` | a typed command (aliases recognised) | commands without `/` |
| `DAMAGE` | damage dealt (`amount` = total) | entities hit |

**States** (checked every second):

| Type | Met when… |
|---|---|
| `LEVEL` · `JOB_LEVEL` | the level reaches `amount` |
| `CLASS` · `SUBCLASS` · `RACE` · `JOB` · `SPECIALIZATION` | the player has the class, race… listed in `targets` |
| `COLLECT` | the inventory holds `amount` items (`take: true` removes them at the end of the stage) |
| `REACH` | the player is within `radius` blocks of `x`, `y`, `z` in `world` |
| `BIOME` | the player is in one of the `targets` biomes |
| `CONDITION` | the `conditions` block is true (Nether, below layer 0, at night…) |
| `TREE_POINTS` · `ATTRIBUTE_POINTS` | the player has spent `amount` points |

**Common options**:

- `targets`: `*` is a wildcard (`"*_LOG"`, `"*DESERT*"`). Without `targets`, everything counts.
- `amount`: quantity (1 by default).
- `display`: text shown; without it, an automatic text (`messages.yml → quests.objective`).
- `conditions`: context of the action, with the same conditions as [weaknesses](EN-Weaknesses.md#conditions): `{time: NIGHT}`, `{holding: ["*_SWORD"]}`, `{sneaking: true}`, `{biomes: [DESERT]}`…
- `module`: required module (`skill-tree`, `attributes`, `jobs`, `parties`…). If that module is disabled, the objective is met automatically.

Example — kill 15 monsters with a sword, at night:

```yaml
- type: KILL
  targets: [ZOMBIE, SKELETON, SPIDER, CREEPER]
  amount: 15
  conditions: {time: NIGHT, holding: ["*_SWORD"]}
  display: "Kill monsters with a sword, at night"
```

## Secret quests

A `category: SECRET` (or `hidden: true`) quest is invisible until it is discovered. It starts on its own:

- **when its conditions are met** (`conditions:`), for example when entering the deep dark:
  ```yaml
  conditions: {biomes: [DEEP_DARK]}
  ```
- **or on a specific action** (`trigger:`), with the same options as an objective:
  ```yaml
  trigger:
    type: KILL
    targets: [ZOMBIE]
    conditions: {time: NIGHT, chance: 0.05}   # 5% chance, at night
  ```

For an NPC (villager renamed with a name tag, Citizens NPC…): `trigger: {type: INTERACT_ENTITY, targets: [VILLAGER], name: "Hermit"}`.

## Rewards

| Key | Effect |
|---|---|
| `xp` | Class XP |
| `job-xp` | XP for the current job |
| `skill-points` · `attribute-points` | Extra skill tree and attribute points |
| `money` | Money (requires **Vault** and an economy plugin) |
| `items` | Items, in the same format as `kits.yml` (CraftEngine, MythicMobs… custom items included) |
| `commands` | Console commands (`%player%` = player name): permissions, ranks, other plugins |
| `text` | Extra lines in the journal's reward list |

Rewards depend on each quest: the included quests give more when their level is higher.

## Settings (`config.yml → quests`)

```yaml
quests:
  offer-tutorial: true      # offer the tutorial at the end of character creation
  tutorial: tutoriel        # id of the tutorial quest
  max-active: 0             # quests in progress at the same time (0 = unlimited)
  max-tracked: 3            # quests tracked in the sidebar
  auto-track: true          # track a quest as soon as it starts
  hide-other-classes: true
  progress-actionbar: true  # progress in the action bar
  check-interval-ticks: 20
```

The sidebar is set in `hud.yml → quest-tracker` (title, lines, symbols, party display). Every text is in `messages.yml → quests`. The whole module is disabled with `modules.quests: false`.

## Administration

- `/quetes admin <player> start <id>`: starts a quest (without checking requirements).
- `/quetes admin <player> complete <id>`: completes it immediately, rewards included.
- `/quetes admin <player> reset <id>`: erases it (in progress and completed).
- `/quetes admin <player> resetall`: erases all of the player's quests and offers the tutorial again.
- Placeholders: `%cykclasses_quests_completed%`, `%cykclasses_quests_active%`, `%cykclasses_quests_total%`, `%cykclasses_quest_tracked%`.

## What cannot be done

- No built-in NPCs: dialogues are shown in chat. An NPC from another plugin can be used as a target (`INTERACT_ENTITY` with `name`).
- No dialogue choices with several answers (use `not-quests` for branches).
- Objective types are predefined (about thirty), but they combine with conditions.
- The sidebar replaces another plugin's scoreboard while quests are tracked (hide it with `/quetes panneau` or `quest-tracker.enabled: false`).
- Player titles are not handled yet (planned for a future version).
