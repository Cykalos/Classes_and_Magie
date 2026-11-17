# Commandes, permissions et API

🇫🇷 Français · [🇬🇧 English](EN-Commands.md)

## Joueurs

| Commande | Description |
|---|---|
| `/classe` | Menu des classes |
| `/classe info [joueur]` | Profil : classe, niveau, mana, stats |
| `/classe choisir <classe>` | Choisir sa classe |
| `/classe sous-classe [id]` | Menu ou choix de sous-classe |
| `/classe competences` | Liste des compétences |
| `/classe reset` | Réinitialiser sa classe (délai) |
| `/competence <id\|n°>` | Lancer une compétence |
| `/race` · `/traits [relancer]` · `/faiblesses [relancer]` | Race, traits, faiblesses |
| `/metier` | Métier, niveaux, recettes, spécialisation |
| `/arbre [reset]` · `/stats [reset]` | Arbre de compétences, attributs |
| `/groupe …` · `/gc` · `/global` | Groupe et chat |
| `/classe quetes` (`/quetes`) | Journal des quêtes |
| `/classe titres` (`/titres`) · `titres equiper <id>` · `titres retirer` | Titres |
| `/quetes commencer\|abandonner\|suivre <id>` | Gérer une quête |
| `/quetes panneau` · `/quetes tutoriel` | Panneau de suivi, tutoriel |

## Administration (`/classe admin …`)

| Sous-commande | Effet |
|---|---|
| `setlevel <joueur> <niveau>` · `addxp <joueur> <xp>` | Niveau de classe |
| `setclass <joueur> <classe\|sous-classe>` · `reset <joueur>` | Classe |
| `mana <joueur>` | Remplit le mana et vide les recharges |
| `setrace <joueur> <race>` · `resetrace <joueur>` · `resettraits <joueur>` | Personnage |
| `setjob <joueur> [métier]` · `jobxp <joueur> <xp>` · `setjoblevel <joueur> <niv>` · `resetspec <joueur>` | Métier |
| `addpoints <joueur> <competences\|stats> <n>` · `resettree <joueur>` · `resetstats <joueur>` | Points |
| `faiblesse <joueur> <reset\|add\|remove> [id]` | Faiblesses |
| `kit <joueur> [classe]` | Redonne un kit |
| `gemme <joueur> <id> [n] [rareté] [pureté]` | Gemme brute (sans rareté) ou taillée |
| `forge <joueur> <rareté>` | Forge l'objet en main |
| `libererunique <id>` | Rend un objet unique de nouveau forgeable |
| `reload` | Recharge la configuration |

Titres : `/classe admin titre <joueur> <give|take|reset> [id]` et `/classe admin titre liberer <id>` (voir [Titres](Titres.md#administration)).

Quêtes : `/classe admin quete <joueur> <start|complete|reset> <id>` et `/classe admin quete <joueur> resetall` (voir [Quêtes](Quêtes.md#administration)).

## Noms anglais des commandes

Chaque commande a aussi un nom anglais, utilisable quelle que soit la langue choisie dans `config.yml` :

| Français | Anglais |
|---|---|
| `/classe` · `choisir` · `sous-classe` · `competences` · `quetes` · `titres` | `/class` · `choose` · `subclass` · `skills` · `quests` · `titles` |
| `/competence` · `/groupe` · `/metier` · `/arbre` · `/faiblesses` | `/skill` · `/party` · `/job` · `/tree` · `/weaknesses` |
| `/quetes` `commencer` · `abandonner` · `suivre` · `panneau` · `tutoriel` | `/quests` `start` · `abandon` · `track` · `tracker` · `tutorial` |
| `/titres` `equiper` · `retirer` | `/titles` `equip` · `unequip` |
| `/groupe` `creer` · `inviter` · `accepter` · `refuser` · `quitter` · `expulser` · `chef` · `dissoudre` | `/party` `create` · `invite` · `accept` · `deny` · `leave` · `kick` · `lead` · `disband` |
| `relancer` | `reroll` |
| admin `faiblesse` · `gemme` · `libererunique` · `quete` · `titre liberer` | `weakness` · `gem` · `releaseunique` · `quest` · `title release` |

`/classe choisir` accepte aussi le nom affiché de la classe (`/classe choisir Guerrier`).

## Permissions

| Permission | Par défaut |
|---|---|
| `cykclasses.use`, `cykclasses.reset` | tout le monde |
| `cykclasses.admin` | op |
| `cykclasses.bypass.items`, `cykclasses.bypass.jobs` | personne |
| `cykclasses.chat.spy` | personne |

## Placeholders (PlaceholderAPI)

`%cykclasses_class%`, `%cykclasses_class_id%`, `%cykclasses_subclass%`, `%cykclasses_race%`, `%cykclasses_job%`, `%cykclasses_traits%`, `%cykclasses_weaknesses%`, `%cykclasses_weaknesses_count%`, `%cykclasses_level%`, `%cykclasses_xp%`, `%cykclasses_xp_required%`, `%cykclasses_xp_percent%`, `%cykclasses_mana%`, `%cykclasses_max_mana%`, `%cykclasses_skill_points%`, `%cykclasses_attribute_points%`, `%cykclasses_party_chat%`, `%cykclasses_display%`, `%cykclasses_title%`, `%cykclasses_title_id%`, `%cykclasses_titles_count%`, `%cykclasses_titles_total%`, `%cykclasses_quests_completed%`, `%cykclasses_quests_active%`, `%cykclasses_quests_total%`, `%cykclasses_quest_tracked%`

## API Java

```java
RpgClass rc = CykClassesAPI.getClass(player);
CykClassesAPI.addXp(player, 50);

// Nouveau type de compétence, utilisable ensuite dans classes.yml (type: MON_SORT)
CykClassesAPI.registerSkillType("MON_SORT", ctx -> {
    ctx.player().sendMessage("Abracadabra !");
    return true; // true = coût et recharge appliqués
});
```

Événements : `PlayerClassChangeEvent` (annulable), `PlayerClassLevelUpEvent`, `SkillCastEvent` (annulable, coût en mana modifiable).
