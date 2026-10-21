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
3. Edit the files in `plugins/Classes_and_Magie/`, then run `/classe admin reload`.

## Configuration files

Files are organised by theme in `plugins/Classes_and_Magie/`:

| Folder / file | Role |
|---|---|
| `config.yml` | Modules, storage (`YAML`, `JSON`, `SQLITE`), XP curve, XP per monster, mana, skill mode, chat, parties, quests |
| `classes/classes.yml` | Classes, subclasses, stats, weaknesses, conditions, skills |
| `classes/skilltree.yml` · `classes/attributes.yml` | Skill tree and attributes |
| `personnage/races.yml` · `personnage/traits.yml` | Races and traits |
| `personnage/weaknesses.yml` | Imposed and random weaknesses |
| `metiers/jobs.yml` | Jobs, job XP, specializations, reserved actions |
| `metiers/forge.yml` | Forged item rarities, random stats, unique items |
| `metiers/gems.yml` | Raw gems, cutting (jewelcrafting), rarities and purity |
| `objets/kits.yml` · `objets/items.yml` | Starter kits, item restrictions |
| `affichage/hud.yml` | Floating damage, target health, sidebar (party and quests), XP bars |
| `affichage/messages.yml` | Every text ([MiniMessage](https://docs.advntr.dev/minimessage/format.html)) |
| `quetes/` | [Quests](EN-Quests.md): every `.yml` file in the folder is read |
| `donnees/` | Player data (`joueurs/` or `data.db`), unique items and traits, backups. Do not edit by hand. |

The default content and texts are in French. Every message can be translated in `messages.yml`, and every name (classes, races, jobs, items…) can be changed in its YAML file.

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
```

A disabled module disappears from the game: its creation step is skipped, its button is removed from the `/classe` menu, its command and menu answer "feature disabled" and it no longer affects stats. Weaknesses written directly in a class or a race stay active, since they are part of the class or race. Player data is kept: enabling the module again brings it back. Only exception: an already forged item keeps its stats even if the forge is disabled.

## Updating

- New keys of `gems.yml` and `forge.yml` take their **default value** when they are missing from the server's file.
- If the server's `jobs.yml` predates job levels or jewelcrafting, it is **backed up** as `donnees/sauvegardes/jobs-ancien-<date>.yml` and replaced by the new version. Personal changes must be copied back from the backup.
- Player data (class, levels, jobs…) is kept.
- Since version 1.9, files are organised in folders (`classes/`, `personnage/`, `metiers/`, `objets/`, `affichage/`, `donnees/`). The files of an older installation, all at the root, are **moved automatically** on first start, without loss: configuration, player data (`playerdata/` becomes `donnees/joueurs/`), SQLite database and backups.
- Since version 1.5, the configuration folder is named `plugins/Classes_and_Magie/`. The old `plugins/CykClasses/` folder is renamed automatically on first start, with all configuration and data.
