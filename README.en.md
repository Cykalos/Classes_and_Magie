<p align="center"><img src="docs/images/icon.png" width="160" alt="Classes and Magie"></p>

# Classes and Magie

[🇫🇷 Français](README.md) · 🇬🇧 English

**Classes and Magie** is an RPG class plugin for **Paper 26.x** (Minecraft Java Edition): classes, unlockable subclasses, unique skills, stats, weaknesses and mana.

> The default content (class names, menus, messages) and the commands are in French, with English aliases for the main commands. Every text can be translated in `messages.yml`, and every class, race, job or item can be renamed in the YAML files.

## Features

- **5 playable classes**, each with its own stats, weaknesses and 3 skills:
  Warrior (Guerrier), Mage, Archer, Assassin, Cleric (Clerc).
- **10 subclasses** unlocked by conditions (level, kills, skill usage, damage dealt, healing done…):

  | Class | Subclasses |
  |---|---|
  | Warrior | Berserker · Paladin |
  | Mage | Pyromancer · Cryomancer |
  | Archer | Hunter · Ranger |
  | Assassin | Shadow · Duelist |
  | Cleric | Priest · Druid |

- **36 different skills** (charge, fireball, meteor shower, frost nova, shadow step, parry, hunter's mark, roots…). Every value can be tuned in `classes.yml`.
- **Mana** with per-class regeneration, reduced in combat.
- **Stats**: health, mana, regeneration, damage multipliers (melee / ranged / magic), damage taken, speed, armor, attack speed, critical hits — with per-level growth.
- **Weaknesses**: vulnerabilities (fire, fall, magic…), forbidden armor/shields, penalised weapons, weakness in water or sunlight, faster hunger, reduced healing, no mana in the dark.
- **Levels and XP** earned by killing monsters (configurable XP per creature).
- **Menus**: class choice, subclasses with condition progress, skill list.
- **Storage** of your choice: `YAML`, `JSON` or `SQLITE`.
- **Parties** with XP and friendly-fire rules, damage and health **HUD**, **MythicMobs** integration.
- Optional **quests**: tutorial, main story, class quests, side and secret quests, journal and quest tracker.
- **PlaceholderAPI** (optional) and a **Java API** for other plugins.

## Casting a skill

1. Press **F** (swap hands): the hotbar shows the **skill icons** (slots 1 to 8) and the weapon stays in hand (slot 9).
2. Press **1 to 8** to cast the matching skill. The icon shows the cooldown (animation + remaining seconds), turns blue when mana is missing and grey when the skill is locked.
3. Press **F** again, click in the inventory or wait 15 s of inactivity to get the normal hotbar back. (Minecraft does not tell the server when players open their own inventory: the first click leaves the mode.)

Hotbar items are saved while in skill mode (even after a crash or a death). The mouse wheel also casts skills in this mode.

Icons can be customised per skill in `classes.yml`: `icon`, `icon-model` (resource pack model) or `custom-model-data`.

Alternatives: `/competence <id|number>` (alias `/skill`) or a click in `/classe competences`.

## Parties

`/groupe` (alias `/party`) opens the creation menu, where the **party rules** are set (they cannot be changed afterwards):

- **XP**: *shared* (split between nearby members, +10% per member) or *equal* (everyone gets the full XP of the kill).
- **Friendly fire**: enabled (offensive skills such as Charge and hits also affect allies) or disabled.

Heals and buffs (Heal, Blessing, War cry, Holy shield…) **only affect party members** (or the caster alone without a party). A side board shows the members' health.

`/groupe inviter|accepter|refuser|quitter|expulser|chef|dissoudre|info` (invite, accept, decline, leave, kick, leader, disband, info), `/gc <message>` for party chat.

## Character creation

Class → **race** → **traits** → **weaknesses** → **job**, each step opens automatically.

- **Races** (`races.yml`): Human, Elf, Dwarf, Orc, Tiefling, Felys. Bonuses, weaknesses, permanent effects, and **penalties when the race does not suit the class** (e.g. Dwarf mage, Orc priest).
- **Traits** (`traits.yml`): 3 randomly rolled traits, positive, negative or mixed, with rarity. **Unique traits**: a single player on the server can get each of them (1% chance). Limited rerolls (3 by default), `/traits relancer`.
- **Weaknesses** (`weaknesses.yml`): a single `/faiblesses` menu (alias `/weaknesses`) groups every weakness of the character (class, race, traits). Reusable weaknesses can be imposed on a race or a class (`weaknesses: [id]`), or rolled randomly with odds specific to each race and class. Vulnerabilities (fire, fall, undead…), penalties at night, in the rain, in some biomes or at low health, periodic damage.
- **Job** (`jobs.yml`): one per player (Miner, Blacksmith, Jeweler, Lumberjack, Farmer, Alchemist, Enchanter, Fisher, Tamer). Some actions are reserved: mining diamonds, crafting armor, using the brewing stand or the anvil, fishing…

## Progression

- **Skill tree** (`/arbre`, `skilltree.yml`): one tree per class with branches reserved to each subclass, plus a common tree. Nodes improve stats or a specific skill (power, cooldown, mana cost).
- **Attributes** (`/stats`, `attributes.yml`): Strength, Dexterity, Intelligence, Vitality, Wisdom, Agility, Endurance. Points earned at each level.
- Free reset every 24 h (`/arbre reset`, `/stats reset`).

## Jobs, forge and gems

- **Job levels** (`jobs.yml`): XP earned by doing the job (mining, forging, cutting gems, fishing, enchanting, brewing…), up to level 50. Recipes and actions unlock by level (e.g. diamond gear at blacksmith level 15).
- **Specializations**: at level 20, each job offers sub-jobs (Prospector/Driller, Armorsmith/Weaponsmith/Bowyer, Lapidary/Goldsmith) with bonuses in a specific field.
- **Forge** (`forge.yml`): items crafted by a blacksmith get a rarity (Common → Legendary, and Unique), random positive or negative stats, and gem sockets. The higher the job level, the better the quality. Each **unique** item exists only once on the server.
- **Gems and jewelcrafting** (`gems.yml`): found **raw** while mining, **cut** by a jeweler at the stonecutter (rarity Common → Perfect and purity depending on the jeweler's level), then socketed by clicking a forged item with the gem.

📖 **Full documentation with examples: [project wiki](https://github.com/Cykalos/Classes_and_Magie/wiki/EN-Home)** (in English and French).

## Quests

`/quetes` (alias `/quests`) opens the **journal**: quests in progress, available, locked and completed, by category. Every quest is **optional**.

- **Tutorial** offered at the end of character creation (accept or decline, can be started later from the journal).
- **Main story** in 6 chapters (*The Shards of the Fracture*), **2 quests per class**, **side quests** (some repeatable) and **secret quests** discovered under specific conditions (at night, in a biome, fishing in a storm…).
- **Sidebar** tracking the objectives, shared with the party board (`/quetes panneau` to hide it).
- Objectives: kill, gather, craft, fish, cast a skill, collect items, explore a biome, reach a place or a level… with conditions (at night, sword in hand, sneaking…).
- Rewards: class and job XP, skill tree and attribute points, money (Vault), items (custom items included), commands.
- `quetes/` folder: every `.yml` file is read; the full format is documented at the top of `tutoriel.yml` and on the [wiki](https://github.com/Cykalos/Classes_and_Magie/wiki/EN-Quests). The included quest texts are in French.

## Items

- **Starter kits** (`kits.yml`) given when choosing the class or the subclass.
- **Restrictions** (`items.yml`): class, race, job, level and attributes required to hit, shoot, use or wear an item.
- Custom items supported through [UniItem](https://github.com/ProjectUnified/UniItem): **CraftEngine**, **ExecutableItems**, **MythicMobs/MythicCrucible**, ItemsAdder, Oraxen, Nexo, MMOItems… (`craftengine:pack:id`, `executableitems:id`, `mythic:Name`).

## Chat

- `/gc` switches the player's chat to the party, `/global` goes back to public chat. `/gc <message>` and `/global <message>` for a single message.
- Class and level shown before the name (can be disabled if another plugin handles chat: `%cykclasses_*%` placeholders).

## HUD (`hud.yml`)

- **XP gained** in a bossbar for 5 s, with stacking of successive gains.
- **Floating damage** above mobs and players: normal, critical, skill, healing — formats, size, duration and visibility are configurable.
- **Target health** in a bossbar (colour depends on health) and/or above its head, with the name and level of MythicMobs mobs.

## MythicMobs (optional)

- Custom mob names and levels in the displays; damage from MythicMobs skills is shown.
- XP per mob (`mythicmobs.mob-xp` in `config.yml`, + bonus per level).
- `MYTHIC` class skills that cast a MythicMobs skill (`params: {skill: "MythicName"}`).

## Commands

| Command | Description |
|---|---|
| `/classe` (`/class`) | Class menu |
| `/classe info [player]` | Profile (class, level, mana, stats) |
| `/classe choisir <class>` | Choose a class |
| `/classe sous-classe [id]` | Subclass menu / choice |
| `/classe competences` | Skill list |
| `/classe reset` | Reset the class (configurable delay) |
| `/arbre` · `/stats` | Skill tree · attributes |
| `/race` · `/traits [relancer]` · `/faiblesses [relancer]` · `/metier` (`/job`) | Character |
| `/groupe` (`/party`) · `/gc` · `/global` | Party and chat |
| `/quetes` (`/quests`) `[commencer\|abandonner\|suivre\|panneau\|tutoriel]` | Quest journal and tracking |
| `/competence <id\|n°>` (`/skill`) | Cast a skill |
| `/classe admin …` | Administration (full list on the [wiki](https://github.com/Cykalos/Classes_and_Magie/wiki/EN-Commands)) |
| `/classe admin reload` | Reload the configuration |

Permissions: `cykclasses.use`, `cykclasses.reset` (everyone), `cykclasses.admin` (op).

## Placeholders

`%cykclasses_class%`, `%cykclasses_subclass%`, `%cykclasses_race%`, `%cykclasses_job%`, `%cykclasses_traits%`, `%cykclasses_weaknesses%`, `%cykclasses_weaknesses_count%`, `%cykclasses_skill_points%`, `%cykclasses_attribute_points%`, `%cykclasses_party_chat%`, `%cykclasses_display%`, `%cykclasses_class_id%`, `%cykclasses_level%`, `%cykclasses_xp%`, `%cykclasses_xp_required%`, `%cykclasses_xp_percent%`, `%cykclasses_mana%`, `%cykclasses_max_mana%`, `%cykclasses_quests_completed%`, `%cykclasses_quests_active%`, `%cykclasses_quests_total%`, `%cykclasses_quest_tracked%`

## Configuration

Files organised by theme in `plugins/Classes_and_Magie/` (files from an older version are moved automatically):

- `config.yml` — **modules** (enable or disable races, traits, weaknesses, jobs, skill tree, attributes, parties, forge, gems, kits, restrictions, quests), storage, XP curve, XP per monster, mana, skill mode, HUD, reset, quests.
- `classes/` — `classes.yml` (classes, subclasses, stats, weaknesses, conditions and skills; the top of the file documents every type), `skilltree.yml`, `attributes.yml`.
- `personnage/` — `races.yml`, `traits.yml`, `weaknesses.yml`.
- `metiers/` — `jobs.yml`, `forge.yml`, `gems.yml`.
- `objets/` — `kits.yml`, `items.yml`.
- `affichage/` — `hud.yml` (XP gained, floating damage, target health, party and quest sidebar), `messages.yml` (every text, in [MiniMessage](https://docs.advntr.dev/minimessage/format.html) format).
- `quetes/` — quests (every `.yml` file in the folder).
- `donnees/` — player and server data (do not edit by hand).

## API

```java
RpgClass rc = CykClassesAPI.getClass(player);
CykClassesAPI.addXp(player, 50);

// Register your own skill type, then use it in classes.yml (type: MY_SPELL)
CykClassesAPI.registerSkillType("MY_SPELL", ctx -> {
    ctx.player().sendMessage("Abracadabra!");
    return true; // true = cost and cooldown applied
});
```

Events: `PlayerClassChangeEvent` (cancellable), `PlayerClassLevelUpEvent`, `SkillCastEvent` (cancellable, mana cost can be changed).

## Building

Java 25 required.

```bash
./gradlew build
# -> build/libs/Classes_and_Magie-<version>.jar
```

## Releases

Every push to `main` automatically publishes a GitHub release with the `.jar` and a changelog generated from the commit messages ([Conventional Commits](https://www.conventionalcommits.org/)):
`feat:` → minor version, `fix:` → patch, `feat!:` → major version. Add `[skip release]` to the message to skip publishing.

The changelog is bilingual: the commit subject (in English) feeds the English part. For the French part, add a `FR: French summary` line to the commit body, followed by the French details (without it, the English text is reused):

```text
feat(jobs): new USE_ITEM action

- Bone meal and hoes can be reserved to a job
FR: nouvelle action USE_ITEM
- La poudre d'os et les houes peuvent être réservées à un métier
```

## Credits

Architecture inspired by open-source RPG plugins such as [Fabled (formerly ProSkillAPI)](https://github.com/magemonkeystudio/fabled) and [MMOCore](https://gitlab.com/phoenix-dvpmt/mmocore). Original code.

## License

[GPL-3.0-only](LICENSE) — © Cykalos.

The plugin is free software: anyone can use, modify and redistribute it, as long as the author credit is kept and any modified version is published under the same license, with its source code.
