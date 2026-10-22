# Commands, permissions and API

[🇫🇷 Français](Commandes.md) · 🇬🇧 English

Commands are in French. Main English aliases: `/class` (`/classe`), `/skill` (`/competence`), `/party` (`/groupe`), `/weaknesses` (`/faiblesses`), `/job` (`/metier`), `/skilltree` (`/arbre`), `/quests` (`/quetes`), `/titles` (`/titres`).

## Players

| Command | Description |
|---|---|
| `/classe` | Class menu |
| `/classe info [player]` | Profile: class, level, mana, stats |
| `/classe choisir <class>` | Choose a class |
| `/classe sous-classe [id]` | Subclass menu or choice |
| `/classe competences` | Skill list |
| `/classe reset` | Reset the class (delay) |
| `/competence <id\|n°>` | Cast a skill |
| `/race` · `/traits [relancer]` · `/faiblesses [relancer]` | Race, traits, weaknesses (`relancer` = reroll) |
| `/metier` | Job, levels, recipes, specialization |
| `/arbre [reset]` · `/stats [reset]` | Skill tree, attributes |
| `/groupe …` · `/gc` · `/global` | Party and chat |
| `/classe quetes` (`/quetes`, `/quests`) | Quest journal |
| `/classe titres` (`/titres`, `/titles`) · `titres equiper <id>` · `titres retirer` | Titles |
| `/quetes commencer\|abandonner\|suivre <id>` | Start, abandon, track a quest |
| `/quetes panneau` · `/quetes tutoriel` | Sidebar, tutorial |

## Administration (`/classe admin …`)

| Subcommand | Effect |
|---|---|
| `setlevel <player> <level>` · `addxp <player> <xp>` | Class level |
| `setclass <player> <class\|subclass>` · `reset <player>` | Class |
| `mana <player>` | Fills mana and clears cooldowns |
| `setrace <player> <race>` · `resetrace <player>` · `resettraits <player>` | Character |
| `setjob <player> [job]` · `jobxp <player> <xp>` · `setjoblevel <player> <lvl>` · `resetspec <player>` | Job |
| `addpoints <player> <competences\|stats> <n>` · `resettree <player>` · `resetstats <player>` | Points |
| `faiblesse <player> <reset\|add\|remove> [id]` | Weaknesses |
| `kit <player> [class]` | Gives a kit again |
| `gemme <player> <id> [n] [rarity] [purity]` | Raw gem (without rarity) or cut gem |
| `forge <player> <rarity>` | Forges the item in hand |
| `libererunique <id>` | Makes a unique item forgeable again |
| `reload` | Reloads the configuration |

Titles: `/classe admin titre <player> <give|take|reset> [id]` and `/classe admin titre liberer <id>` (see [Titles](EN-Titles.md#administration)).

Quests: `/classe admin quete <player> <start|complete|reset> <id>` and `/classe admin quete <player> resetall` (see [Quests](EN-Quests.md#administration)).

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
