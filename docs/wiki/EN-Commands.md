# Commands, permissions and API

[🇫🇷 Français](Commandes.md) · 🇬🇧 English

Every command has an English name and a French name; both work whatever the `language` setting. The French names are listed at the end of the page.

## Players

| Command | Description |
|---|---|
| `/class` | Class menu (character menu once the class is chosen) |
| `/class info [player]` | Profile: class, level, mana, stats |
| `/class choose <class>` | Choose a class (id or displayed name) |
| `/class subclass [id]` | Subclass menu or choice |
| `/class skills` | Skill list |
| `/class reset` | Reset the class (delay) |
| `/skill <id\|number>` | Cast a skill |
| `/race` · `/traits [reroll]` · `/weaknesses [reroll]` | Race, traits, weaknesses |
| `/job` | Job, levels, recipes, specialization |
| `/tree [reset]` · `/stats [reset]` | Skill tree, attributes |
| `/party …` · `/gc` · `/global` | Party and chat |
| `/quests` (`/class quests`) | Quest journal |
| `/quests start\|abandon\|track <id>` | Start, abandon, track a quest |
| `/quests tracker` · `/quests tutorial` | Sidebar, tutorial |
| `/titles` (`/class titles`) · `/titles equip <id>` · `/titles unequip` | Titles |

## Administration (`/class admin …`)

| Subcommand | Effect |
|---|---|
| `setlevel <player> <level>` · `addxp <player> <xp>` | Class level |
| `setclass <player> <class\|subclass>` · `reset <player>` | Class |
| `mana <player>` | Fills mana and clears cooldowns |
| `setrace <player> <race>` · `resetrace <player>` · `resettraits <player>` | Character |
| `setjob <player> [job]` · `jobxp <player> <xp>` · `setjoblevel <player> <lvl>` · `resetspec <player>` | Job |
| `addpoints <player> <skills\|stats> <n>` · `resettree <player>` · `resetstats <player>` | Points |
| `weakness <player> <reset\|add\|remove> [id]` | Weaknesses |
| `kit <player> [class]` | Gives a kit again |
| `gem <player> <id> [n] [rarity] [purity]` | Raw gem (without rarity) or cut gem |
| `forge <player> <rarity>` | Forges the item in hand |
| `releaseunique <id>` | Makes a unique item forgeable again |
| `quest <player> <start\|complete\|reset> <id>` · `quest <player> resetall` | Quests (see [Quests](EN-Quests.md#administration)) |
| `title <player> <give\|take\|reset> [id]` · `title release <id>` | Titles (see [Titles](EN-Titles.md#administration)) |
| `reload` | Reloads the configuration |

## French command names

| English | French |
|---|---|
| `/class` · `choose` · `subclass` · `skills` · `quests` · `titles` | `/classe` · `choisir` · `sous-classe` · `competences` · `quetes` · `titres` |
| `/skill` · `/party` · `/job` · `/tree` · `/weaknesses` | `/competence` · `/groupe` · `/metier` · `/arbre` · `/faiblesses` |
| `/quests` `start` · `abandon` · `track` · `tracker` · `tutorial` | `/quetes` `commencer` · `abandonner` · `suivre` · `panneau` · `tutoriel` |
| `/titles` `equip` · `unequip` | `/titres` `equiper` · `retirer` |
| `/party` `create` · `invite` · `accept` · `deny` · `leave` · `kick` · `lead` · `disband` | `/groupe` `creer` · `inviter` · `accepter` · `refuser` · `quitter` · `expulser` · `chef` · `dissoudre` |
| `reroll` | `relancer` |
| admin `weakness` · `gem` · `releaseunique` · `quest` · `title release` | `faiblesse` · `gemme` · `libererunique` · `quete` · `titre liberer` |

## Permissions

| Permission | Default |
|---|---|
| `cykclasses.use`, `cykclasses.reset` | everyone |
| `cykclasses.admin` | op |
| `cykclasses.bypass.items`, `cykclasses.bypass.jobs` | nobody |
| `cykclasses.chat.spy` | nobody |

## Placeholders (PlaceholderAPI)

`%cykclasses_class%`, `%cykclasses_class_id%`, `%cykclasses_subclass%`, `%cykclasses_race%`, `%cykclasses_job%`, `%cykclasses_traits%`, `%cykclasses_weaknesses%`, `%cykclasses_weaknesses_count%`, `%cykclasses_level%`, `%cykclasses_xp%`, `%cykclasses_xp_required%`, `%cykclasses_xp_percent%`, `%cykclasses_mana%`, `%cykclasses_max_mana%`, `%cykclasses_skill_points%`, `%cykclasses_attribute_points%`, `%cykclasses_party_chat%`, `%cykclasses_display%`, `%cykclasses_title%`, `%cykclasses_title_id%`, `%cykclasses_titles_count%`, `%cykclasses_titles_total%`, `%cykclasses_quests_completed%`, `%cykclasses_quests_active%`, `%cykclasses_quests_total%`, `%cykclasses_quest_tracked%`

## Java API

```java
RpgClass rc = CykClassesAPI.getClass(player);
CykClassesAPI.addXp(player, 50);

// New skill type, then usable in classes.yml (type: MY_SPELL)
CykClassesAPI.registerSkillType("MY_SPELL", ctx -> {
    ctx.player().sendMessage("Abracadabra!");
    return true; // true = cost and cooldown applied
});
```

Events: `PlayerClassChangeEvent` (cancellable), `PlayerClassLevelUpEvent`, `SkillCastEvent` (cancellable, mana cost can be changed).
