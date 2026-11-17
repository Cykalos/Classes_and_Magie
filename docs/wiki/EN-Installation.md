# Installation

[🇫🇷 Français](Installation.md) · 🇬🇧 English

## Requirements

- **Paper 26.1.x or 26.2** server (both are checked at build time).
- **Java 25**.
- No required dependency. SQLite is downloaded automatically by Paper if that storage is chosen.

## Optional plugins

| Plugin | Use |
|---|---|
| PlaceholderAPI | `%cykclasses_*%` placeholders (scoreboard, tab, chat) |
| MythicMobs | Mob names/levels in the HUD, XP per mob, `MYTHIC` skills, `mythic:` items |
| CraftEngine, ExecutableItems, ItemsAdder, Oraxen, Nexo, MMOItems | Custom items in kits and restrictions (through UniItem) |

## Steps

1. Download the latest `Classes_and_Magie-x.y.z.jar` from the [Releases](https://github.com/Cykalos/Classes_and_Magie/releases).
2. Put it in `plugins/` and start the server.
3. Edit the files in `plugins/Classes_and_Magie/`, then run `/class admin reload`.

## Configuration files

Files are organised by theme in `plugins/Classes_and_Magie/`:

| Folder / file | Role |
|---|---|
| `config.yml` | Language, modules, storage (`YAML`, `JSON`, `SQLITE`), XP curve, XP per monster, mana, skill mode, chat, parties, quests |
| `classes/classes.yml` | Classes, subclasses, stats, weaknesses, conditions, skills |
| `classes/skilltree.yml` · `classes/attributes.yml` | Skill tree and attributes |
| `character/races.yml` · `character/traits.yml` | Races and traits |
| `character/weaknesses.yml` | Imposed and random weaknesses |
| `character/titles.yml` | [Titles](EN-Titles.md): rarities, conditions, effects, display |
| `jobs/jobs.yml` | Jobs, job XP, specializations, reserved actions |
| `jobs/forge.yml` | Forged item rarities, random stats, unique items |
| `jobs/gems.yml` | Raw gems, cutting (jewelcrafting), rarities and purity |
| `items/kits.yml` · `items/items.yml` | Starter kits, item restrictions |
| `display/hud.yml` | Floating damage, target health, sidebar (party and quests), XP bars |
| `display/messages.yml` | Every text ([MiniMessage](https://docs.advntr.dev/minimessage/format.html)) |
| `quests/` | [Quests](EN-Quests.md): every `.yml` file in the folder is read |
| `data/` | Player data (`players/` or `data.db`), unique items, traits and titles, backups. Do not edit by hand. |

Every message can be changed in `messages.yml`, and every name (classes, races, jobs, items…) can be changed in its YAML file.

## Language

```yaml
# config.yml
language: en   # en (English) or fr (French)
```

- The language decides the **files created by the plugin** (texts, classes, races, quests, titles…) and the default value of any key missing from them.
- A **new server** starts in English. A server installed with a version before 1.12 gets `language: fr` automatically and stays in French.
- In French, the folders are named `personnage/`, `metiers/`, `objets/`, `affichage/`, `quetes/` and `donnees/` (instead of `character/`, `jobs/`, `items/`, `display/`, `quests/` and `data/`). A folder that already exists **always keeps its name**.
- Existing files are **never replaced**. To switch an existing server to the other language: set `language`, delete or rename the files to regenerate (for example `display/messages.yml`), then restart. Customised files can be kept: only missing keys use the new language.
- Ids (classes, races, jobs, skills, quests, titles) are **the same in both languages**: player data and commands keep working.
- Commands work in both languages (`/class` or `/classe`, `/quests` or `/quetes`…), see [Commands](EN-Commands.md).

## Modules

Each part of the plugin can be disabled in `config.yml`:

```yaml
modules:
  races: true
  traits: true
  weaknesses: true
  jobs: true
  skill-tree: true
  attributes: true
  parties: true
  forge: true
  gems: true
  kits: true
  item-restrictions: true
  quests: true
  titles: true
```

A disabled module disappears from the game: its creation step is skipped, its button is removed from the `/class` menu, its command and menu answer "feature disabled" and it no longer affects stats. Weaknesses written directly in a class or a race stay active, since they are part of the class or race. Player data is kept: enabling the module again brings it back. Only exception: an already forged item keeps its stats even if the forge is disabled.

## Updating

- New keys of `gems.yml` and `forge.yml` take their **default value** when they are missing from the server's file.
- If the server's `jobs.yml` predates job levels or jewelcrafting, it is **backed up** as `data/sauvegardes/jobs-ancien-<date>.yml` and replaced by the new version. Personal changes must be copied back from the backup.
- Player data (class, levels, jobs…) is kept.
- Since version 1.9, files are organised in folders (`classes/`, `character/`, `jobs/`, `items/`, `display/`, `data/`). The files of an older installation, all at the root, are **moved automatically** on first start, without loss: configuration, player data (`playerdata/` becomes `data/joueurs/`), SQLite database and backups.
- Since version 1.5, the configuration folder is named `plugins/Classes_and_Magie/`. The old `plugins/CykClasses/` folder is renamed automatically on first start, with all configuration and data.
