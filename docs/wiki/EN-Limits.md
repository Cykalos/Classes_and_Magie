# Limits

[🇫🇷 Français](Limites.md) · 🇬🇧 English

What Classes and Magie **does not do** (or not yet), to avoid bad surprises.

## Character

- A single class and a single subclass; no multiclassing and no sub-subclass.
- 8 skills maximum in the hotbar.
- No automatically triggered passive skills: use stats, the skill tree or MythicMobs.
- Traits and weaknesses cannot be chosen, only rerolled (limited number).
- Weakness types and conditions are predefined (about a dozen types, about twenty conditions), but they combine freely.

## Jobs and crafting

- A single active job and a single specialization per job.
- Restrictions and forge on **vanilla** materials only.
- Once forged, an item keeps its rarity and stats; only gems can be added.
- A socketed gem is final; no fusion or recutting.
- A destroyed unique item (lava, cactus…) is not released automatically: `/classe admin libererunique <id>`.

## Quests

- No built-in NPCs and no multiple-choice dialogues (an NPC from another plugin can be used as a target).
- About forty predefined objective types, combinable with conditions.

## Titles

- Only one title equipped at a time.

## Items

- Forbidden items stay visible (no hiding).
- The plugin does not create custom items: it references those from CraftEngine, MythicCrucible, ExecutableItems…

## Technical

- Paper 26.x only (not Spigot, not Folia).
- The sidebar (party and quests) uses the scoreboard and can conflict with another scoreboard plugin.
- Parties do not survive a restart.
- New skill mechanics require Java or MythicMobs.
- Default content, menus and commands are in French (every text can be translated in `messages.yml`).
