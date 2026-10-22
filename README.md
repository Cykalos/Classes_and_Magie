<p align="center"><img src="docs/images/icon.png" width="160" alt="Classes and Magie"></p>

# Classes and Magie

🇫🇷 Français · [🇬🇧 English](README.en.md)

**Classes and Magie** est un plugin **Paper 26.x** (Minecraft Java Edition) de classes RPG : classes, sous-classes à débloquer, compétences uniques, statistiques, faiblesses et mana.

## Fonctionnalités

- **5 classes** jouables, chacune avec ses statistiques, ses faiblesses et 3 compétences :
  Guerrier, Mage, Archer, Assassin, Clerc.
- **10 sous-classes** débloquées par des conditions (niveau, éliminations, utilisation de compétences, dégâts infligés, soins prodigués…) :

  | Classe | Sous-classes |
  |---|---|
  | Guerrier | Berserker · Paladin |
  | Mage | Pyromancien · Cryomancien |
  | Archer | Chasseur · Rôdeur |
  | Assassin | Ombre · Duelliste |
  | Clerc | Prêtre · Druide |

- **36 compétences** différentes (charge, boule de feu, pluie de météores, nova de givre, pas de l'ombre, parade, marque du chasseur, racines…). Toutes les valeurs sont réglables dans `classes.yml`.
- **Mana** avec régénération par classe, réduite en combat.
- **Statistiques** : PV, mana, régénération, multiplicateurs de dégâts (mêlée / distance / magie), dégâts subis, vitesse, armure, vitesse d'attaque, coups critiques — avec progression par niveau.
- **Faiblesses** : vulnérabilités (feu, chute, magie…), armures/boucliers interdits, armes pénalisées, faiblesse dans l'eau ou au soleil, faim accrue, soins réduits, pas de mana dans le noir.
- **Niveaux et XP** gagnés en tuant des monstres (barème par créature configurable).
- **Menus** : choix de classe, sous-classes avec progression des conditions, liste des compétences.
- **Stockage** au choix : `YAML`, `JSON` ou `SQLITE`.
- **Groupes** avec règles d'XP et de dégâts alliés, **HUD** de dégâts et de PV, intégration **MythicMobs**.
- **Titres** à débloquer selon les actions du joueur, de Commun à Unique et Secret, affichés au-dessus de la tête et dans le chat, avec ou sans effets.
- **Quêtes** facultatives : tutoriel, histoire principale, quêtes de classe, annexes et secrètes, journal et panneau de suivi.
- **PlaceholderAPI** (optionnel) et **API Java** pour les autres plugins.

## Lancer une compétence

1. Appuyer sur **F** (échange de main) : la barre d'objets affiche les **icônes des compétences** (slots 1 à 8) et l'arme reste en main (slot 9).
2. Appuyer sur **1 à 8** pour lancer la compétence correspondante. L'icône affiche la recharge (animation + secondes restantes), passe en bleu si le mana manque, en gris si elle n'est pas débloquée.
3. **F** à nouveau, un clic dans l'inventaire ou 15 s d'inactivité pour retrouver la barre normale. (Minecraft ne prévient pas le serveur quand un joueur ouvre son propre inventaire : c'est le premier clic qui quitte le mode.)

Les objets de la barre sont sauvegardés pendant le mode compétences (même en cas de crash ou de mort). La molette lance aussi les compétences dans ce mode.

Icônes personnalisables par compétence dans `classes.yml` : `icon`, `icon-model` (modèle de resource pack) ou `custom-model-data`.

Alternatives : `/competence <id|numéro>` ou clic dans `/classe competences`.

## Groupes

`/groupe` ouvre le menu de création, où l'on fixe les **règles du groupe** (elles ne changent plus ensuite) :

- **XP** : *partagée* (divisée entre les membres proches, +10 % par membre) ou *égale* (chacun reçoit l'XP complète de l'élimination).
- **Dégâts alliés** : activés (les compétences offensives comme Charge et les coups touchent aussi les alliés) ou désactivés.

Les soins et bonus (Soin, Bénédiction, Cri de guerre, Bouclier sacré…) ne touchent **que les membres du groupe** (ou le lanceur seul, sans groupe). Un panneau latéral affiche les PV des membres.

`/groupe inviter|accepter|refuser|quitter|expulser|chef|dissoudre|info`, `/gc <message>` pour le chat de groupe.

## Création du personnage

Classe → **race** → **traits** → **faiblesses** → **métier**, chaque étape s'ouvre automatiquement.

- **Races** (`races.yml`) : Humain, Elfe, Nain, Orc, Tieffelin, Félys. Bonus, faiblesses, effets permanents, et **malus si la race n'est pas faite pour la classe** (ex. Nain mage, Orc prêtre).
- **Traits** (`traits.yml`) : 3 traits tirés au hasard, positifs, négatifs ou mixtes, avec rareté. **Traits uniques** : un seul joueur du serveur peut les obtenir (1 % de chance). Relances limitées (3 par défaut), `/traits relancer`.
- **Faiblesses** (`weaknesses.yml`) : un seul menu `/faiblesses` regroupe toutes les faiblesses du personnage (classe, race, traits). Des faiblesses réutilisables peuvent être imposées à une race ou une classe (`weaknesses: [id]`), ou tirées au hasard avec des chances propres à chaque race et classe. Vulnérabilités (feu, chute, morts-vivants…), malus de nuit, sous la pluie, dans certains biomes ou à PV bas, dégâts réguliers. `/faiblesses`.
- **Métier** (`jobs.yml`) : un seul par joueur (Mineur, Forgeron, Joaillier, Bûcheron, Fermier, Alchimiste, Enchanteur, Pêcheur, Dresseur). Certaines actions sont réservées : miner le diamant, fabriquer des armures, utiliser l'alambic, l'enclume, pêcher…

## Progression

- **Arbre de compétences** (`/arbre`, `skilltree.yml`) : un arbre par classe avec des branches réservées à chaque sous-classe, plus un arbre commun. Les nœuds améliorent les stats ou une compétence précise (puissance, recharge, coût en mana).
- **Attributs** (`/stats`, `attributes.yml`) : Force, Dextérité, Intelligence, Vitalité, Sagesse, Agilité, Endurance. Points gagnés à chaque niveau.
- Réinitialisation gratuite toutes les 24 h (`/arbre reset`, `/stats reset`).

## Métiers, forge et gemmes

- **Niveaux de métier** (`jobs.yml`) : XP gagnée en faisant son métier (miner, forger, tailler des gemmes, pêcher, enchanter, distiller…), jusqu'au niveau 50. Les recettes et actions se débloquent par niveau (ex. équipement en diamant au niveau 15 du forgeron).
- **Spécialisations** : au niveau 20, chaque métier propose des sous-métiers (Prospecteur/Foreur, Armurier/Fabricant d'armes/Facteur d'arcs, Lapidaire/Orfèvre) avec des bonus sur un domaine précis.
- **Forge** (`forge.yml`) : les objets fabriqués par un forgeron reçoivent une rareté (Commun → Légendaire, et Unique), des stats aléatoires positives ou négatives, et des emplacements de gemmes. Plus le métier est haut, meilleure est la qualité. Chaque objet **unique** n'existe qu'une fois sur le serveur.
- **Gemmes et joaillerie** (`gems.yml`) : trouvées **brutes** en minant, **taillées** par un joaillier au tailleur de pierre (rareté Commune → Parfaite et pureté selon son niveau), puis incrustées en cliquant avec la gemme sur un objet forgé.

📖 **Documentation complète avec exemples : [wiki du projet](https://github.com/Cykalos/Classes_and_Magie/wiki)** (en français et en anglais).

## Quêtes

`/quetes` ouvre le **journal** : quêtes en cours, disponibles, verrouillées et terminées, par catégorie. Toutes les quêtes sont **facultatives**.

- **Tutoriel** proposé à la fin de la création du personnage (accepter ou refuser, relançable depuis le journal).
- **Histoire principale d'exemple** en 6 rangs (*La Guilde des Aventuriers*), **2 quêtes par classe**, **quêtes annexes** (dont des répétables) et **quêtes secrètes** qui se découvrent sous certaines conditions (de nuit, dans un biome, en pêchant sous l'orage…).
- **Panneau latéral** de suivi des objectifs, partagé avec le panneau du groupe (`/quetes panneau` pour le masquer).
- Objectifs : éliminer, récolter, fabriquer, pêcher, lancer une compétence, rassembler des objets, explorer un biome, atteindre un lieu, un niveau… avec des conditions (de nuit, épée en main, accroupi…).
- Récompenses : XP de classe et de métier, points d'arbre et d'attributs, argent (Vault), objets (custom compris), commandes.
- Dossier `quetes/` : chaque fichier `.yml` est lu, le format complet est documenté en haut de `tutoriel.yml` et sur le [wiki](https://github.com/Cykalos/Classes_and_Magie/wiki/Quêtes).

## Titres

`/titres` ouvre la collection de titres. Un seul titre est équipé : il s'affiche **au-dessus de la tête** et **dans le chat**, et peut donner des **effets** (stats, effets de potion, bonus conditionnels) ou rester honorifique.

- **31 titres fournis** : progression, combat, un par classe, métiers et exploration, histoire, **uniques** (un seul joueur du serveur) et **secrets** (conditions cachées).
- Débloqués par : niveau, éliminations, compétences lancées, blocs minés, dégâts subis, soins, statistiques Minecraft (morts, distance, poissons…), progrès, temps de jeu, quêtes terminées… Les totaux comptent aussi ce que le joueur a déjà fait.
- Les quêtes peuvent exiger ou donner un titre. Tout se règle dans `personnage/titles.yml` ([wiki](https://github.com/Cykalos/Classes_and_Magie/wiki/Titres)).

## Objets

- **Kits de départ** (`kits.yml`) donnés au choix de la classe ou de la sous-classe.
- **Restrictions** (`items.yml`) : classe, race, métier, niveau et attributs requis pour frapper, tirer, utiliser ou porter un objet.
- Objets custom reconnus via [UniItem](https://github.com/ProjectUnified/UniItem) : **CraftEngine**, **ExecutableItems**, **MythicMobs/MythicCrucible**, ItemsAdder, Oraxen, Nexo, MMOItems… (`craftengine:pack:id`, `executableitems:id`, `mythic:Nom`).

## Chat

- `/gc` bascule le chat du joueur sur le groupe, `/global` revient au chat public. `/gc <message>` et `/global <message>` pour un seul message.
- Classe et niveau affichés devant le nom (désactivable si un autre plugin gère le chat : placeholders `%cykclasses_*%`).

## HUD (`hud.yml`)

- **XP gagnée** en bossbar pendant 5 s, avec cumul des gains successifs.
- **Dégâts flottants** au-dessus des mobs et des joueurs : normaux, critiques, compétences, soins — formats, taille, durée et visibilité réglables.
- **PV de la cible** en bossbar (couleur selon les PV) et/ou au-dessus de sa tête, avec le nom et le niveau des mobs MythicMobs.

## MythicMobs (optionnel)

- Noms et niveaux des mobs custom dans les affichages ; dégâts des compétences MythicMobs affichés.
- XP par mob (`mythicmobs.mob-xp` dans `config.yml`, + bonus par niveau).
- Compétences de classe de type `MYTHIC` qui lancent une compétence MythicMobs (`params: {skill: "NomMythic"}`).

## Commandes

| Commande | Description |
|---|---|
| `/classe` | Menu des classes |
| `/classe info [joueur]` | Profil (classe, niveau, mana, stats) |
| `/classe choisir <classe>` | Choisir sa classe |
| `/classe sous-classe [id]` | Menu / choix de sous-classe |
| `/classe competences` | Liste des compétences |
| `/classe reset` | Réinitialiser sa classe (délai configurable) |
| `/arbre` · `/stats` | Arbre de compétences · attributs |
| `/race` · `/traits [relancer]` · `/faiblesses [relancer]` · `/metier` | Personnage |
| `/groupe` · `/gc` · `/global` | Groupe et chat |
| `/quetes [commencer\|abandonner\|suivre\|panneau\|tutoriel]` | Journal et suivi des quêtes |
| `/titres [equiper <id>\|retirer]` | Titres |
| `/competence <id\|n°>` | Lancer une compétence |
| `/classe admin …` | Administration (liste complète sur le [wiki](https://github.com/Cykalos/Classes_and_Magie/wiki/Commandes)) |
| `/classe admin reload` | Recharger la configuration |

Permissions : `cykclasses.use`, `cykclasses.reset` (tout le monde), `cykclasses.admin` (op).

## Placeholders

`%cykclasses_class%`, `%cykclasses_subclass%`, `%cykclasses_race%`, `%cykclasses_job%`, `%cykclasses_traits%`, `%cykclasses_weaknesses%`, `%cykclasses_weaknesses_count%`, `%cykclasses_skill_points%`, `%cykclasses_attribute_points%`, `%cykclasses_party_chat%`, `%cykclasses_display%`, `%cykclasses_class_id%`, `%cykclasses_level%`, `%cykclasses_xp%`, `%cykclasses_xp_required%`, `%cykclasses_xp_percent%`, `%cykclasses_mana%`, `%cykclasses_max_mana%`, `%cykclasses_title%`, `%cykclasses_titles_count%`, `%cykclasses_quests_completed%`, `%cykclasses_quests_active%`, `%cykclasses_quests_total%`, `%cykclasses_quest_tracked%`

## Configuration

Fichiers rangés par thème dans `plugins/Classes_and_Magie/` (ceux d'une ancienne version sont déplacés automatiquement) :

- `config.yml` — **modules** (activer ou désactiver races, traits, faiblesses, métiers, arbre, attributs, groupes, forge, gemmes, kits, restrictions, quêtes, titres), stockage, courbe d'XP, XP par monstre, mana, mode compétences, HUD, reset, quêtes.
- `classes/` — `classes.yml` (classes, sous-classes, stats, faiblesses, conditions et compétences ; le haut du fichier documente tous les types), `skilltree.yml`, `attributes.yml`.
- `personnage/` — `races.yml`, `traits.yml`, `weaknesses.yml`, `titles.yml`.
- `metiers/` — `jobs.yml`, `forge.yml`, `gems.yml`.
- `objets/` — `kits.yml`, `items.yml`.
- `affichage/` — `hud.yml` (XP gagnée, dégâts flottants, PV des cibles, panneau du groupe et des quêtes), `messages.yml` (tous les textes au format [MiniMessage](https://docs.advntr.dev/minimessage/format.html)).
- `quetes/` — les quêtes (tous les fichiers `.yml` du dossier).
- `donnees/` — données des joueurs et du serveur (à ne pas modifier à la main).

## API

```java
RpgClass rc = CykClassesAPI.getClass(player);
CykClassesAPI.addXp(player, 50);

// Ajouter son propre type de compétence, utilisable ensuite dans classes.yml (type: MON_SORT)
CykClassesAPI.registerSkillType("MON_SORT", ctx -> {
    ctx.player().sendMessage("Abracadabra !");
    return true; // true = coût et recharge appliqués
});
```

Événements : `PlayerClassChangeEvent` (annulable), `PlayerClassLevelUpEvent`, `SkillCastEvent` (annulable, coût en mana modifiable).

## Compilation

Java 25 requis.

```bash
./gradlew build
# -> build/libs/Classes_and_Magie-<version>.jar
```

## Releases

Chaque push sur `main` publie automatiquement une release GitHub avec le `.jar` et le changelog généré à partir des messages de commit ([Conventional Commits](https://www.conventionalcommits.org/fr/)) :
`feat:` → version mineure, `fix:` → correctif, `feat!:` → version majeure. Ajouter `[skip release]` au message pour ne pas publier.

Le changelog est bilingue : le sujet du commit (en anglais) alimente la partie anglaise. Pour la partie française, ajouter dans le corps du commit une ligne `FR: résumé en français` suivie des détails en français (sans elle, le texte anglais est repris) :

```text
feat(jobs): new USE_ITEM action

- Bone meal and hoes can be reserved to a job
FR: nouvelle action USE_ITEM
- La poudre d'os et les houes peuvent être réservées à un métier
```

## Crédits

Architecture inspirée de plugins RPG open source comme [Fabled (ex-ProSkillAPI)](https://github.com/magemonkeystudio/fabled) et [MMOCore](https://gitlab.com/phoenix-dvpmt/mmocore). Code original.

## Licence

[GPL-3.0-only](LICENSE) — © Cykalos.

Le plugin est libre et gratuit : chacun peut l'utiliser, le modifier et le redistribuer, à condition de conserver la mention de l'auteur et de publier toute version modifiée sous la même licence, avec son code source.
