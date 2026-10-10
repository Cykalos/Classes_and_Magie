# CykClasses

Plugin **Paper 26.x** (Minecraft Java Edition) de classes RPG : classes, sous-classes à débloquer, compétences uniques, statistiques, défauts et mana.

## Fonctionnalités

- **5 classes** jouables, chacune avec ses statistiques, ses défauts et 3 compétences :
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
- **Défauts** : vulnérabilités (feu, chute, magie…), armures/boucliers interdits, armes pénalisées, faiblesse dans l'eau ou au soleil, faim accrue, soins réduits, pas de mana dans le noir.
- **Niveaux et XP** gagnés en tuant des monstres (barème par créature configurable).
- **Menus** : choix de classe, sous-classes avec progression des conditions, liste des compétences.
- **Stockage** au choix : `YAML`, `JSON` ou `SQLITE`.
- **Groupes** avec règles d'XP et de dégâts alliés, **HUD** de dégâts et de PV, intégration **MythicMobs**.
- **PlaceholderAPI** (optionnel) et **API Java** pour les autres plugins.

## Lancer une compétence

1. Appuie sur **F** (échange de main) : ta barre d'objets affiche les **icônes de tes compétences** (slots 1 à 8) et ton arme reste en main (slot 9).
2. Appuie sur **1 à 8** pour lancer la compétence correspondante. L'icône affiche la recharge (animation + secondes restantes), passe en bleu si le mana manque, en gris si elle n'est pas débloquée.
3. **F** à nouveau, ouvrir l'inventaire ou 15 s d'inactivité pour retrouver ta barre normale.

Tes objets sont sauvegardés pendant le mode compétences (même en cas de crash ou de mort). La molette lance aussi les compétences dans ce mode.

Icônes personnalisables par compétence dans `classes.yml` : `icon`, `icon-model` (modèle de resource pack) ou `custom-model-data`.

Alternatives : `/competence <id|numéro>` ou clic dans `/classe competences`.

## Groupes

`/groupe` ouvre le menu de création, où l'on fixe les **règles du groupe** (elles ne changent plus ensuite) :

- **XP** : *partagée* (divisée entre les membres proches, +10 % par membre) ou *égale* (chacun reçoit l'XP complète de l'élimination).
- **Dégâts alliés** : activés (les compétences offensives comme Charge et les coups touchent aussi les alliés) ou désactivés.

Les soins et bonus (Soin, Bénédiction, Cri de guerre, Bouclier sacré…) ne touchent **que les membres du groupe** (ou toi seul sans groupe). Un panneau latéral affiche les PV des membres.

`/groupe inviter|accepter|refuser|quitter|expulser|chef|dissoudre|info`, `/gc <message>` pour le chat de groupe.

## Création du personnage

Classe → **race** → **traits** → **métier**, chaque étape s'ouvre automatiquement.

- **Races** (`races.yml`) : Humain, Elfe, Nain, Orc, Tieffelin, Félys. Bonus, défauts, effets permanents, et **malus si la race n'est pas faite pour la classe** (ex. Nain mage, Orc prêtre).
- **Traits** (`traits.yml`) : 3 traits tirés au hasard, positifs, négatifs ou mixtes, avec rareté. **Traits uniques** : un seul joueur du serveur peut les obtenir (1 % de chance). Relances limitées (3 par défaut), `/traits relancer`.
- **Métier** (`jobs.yml`) : un seul par joueur (Mineur, Forgeron, Bûcheron, Fermier, Alchimiste, Enchanteur, Pêcheur, Dresseur). Certaines actions sont réservées : miner le diamant, fabriquer des armures, utiliser l'alambic, l'enclume, pêcher…

## Progression

- **Arbre de compétences** (`/arbre`, `skilltree.yml`) : un arbre par classe avec des branches réservées à chaque sous-classe, plus un arbre commun. Les nœuds améliorent les stats ou une compétence précise (puissance, recharge, coût en mana).
- **Attributs** (`/stats`, `attributes.yml`) : Force, Dextérité, Intelligence, Vitalité, Sagesse, Agilité, Endurance. Points gagnés à chaque niveau.
- Réinitialisation gratuite toutes les 24 h (`/arbre reset`, `/stats reset`).

## Métiers, forge et gemmes

- **Niveaux de métier** (`jobs.yml`) : XP gagnée en faisant son métier (miner, forger, pêcher, enchanter, distiller…), jusqu'au niveau 50. Les recettes et actions se débloquent par niveau (ex. équipement en diamant au niveau 15 du forgeron).
- **Forge** (`forge.yml`) : les objets fabriqués par un forgeron reçoivent une rareté (Commun → Légendaire, et Unique), des stats aléatoires positives ou négatives, et des emplacements de gemmes. Plus le métier est haut, meilleure est la qualité. Chaque objet **unique** n'existe qu'une fois sur le serveur.
- **Gemmes** (`gems.yml`) : trouvées en minant, à incruster en cliquant avec la gemme sur un objet forgé.

Documentation complète avec exemples : voir la doc partagée du projet.

## Objets

- **Kits de départ** (`kits.yml`) donnés au choix de la classe ou de la sous-classe.
- **Restrictions** (`items.yml`) : classe, race, métier, niveau et attributs requis pour frapper, tirer, utiliser ou porter un objet.
- Objets custom reconnus via [UniItem](https://github.com/ProjectUnified/UniItem) : **CraftEngine**, **ExecutableItems**, **MythicMobs/MythicCrucible**, ItemsAdder, Oraxen, Nexo, MMOItems… (`craftengine:pack:id`, `executableitems:id`, `mythic:Nom`).

## Chat

- `/gc` bascule ton chat sur le groupe, `/global` revient au chat public. `/gc <message>` et `/global <message>` pour un seul message.
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
| `/race` · `/traits [relancer]` · `/metier` | Personnage |
| `/groupe` · `/gc` · `/global` | Groupe et chat |
| `/competence <id\|n°>` | Lancer une compétence |
| `/classe admin setlevel\|addxp\|setclass\|reset\|mana <joueur> …` | Administration |
| `/classe admin reload` | Recharger la configuration |

Permissions : `cykclasses.use`, `cykclasses.reset` (tout le monde), `cykclasses.admin` (op).

## Placeholders

`%cykclasses_class%`, `%cykclasses_subclass%`, `%cykclasses_race%`, `%cykclasses_job%`, `%cykclasses_traits%`, `%cykclasses_skill_points%`, `%cykclasses_attribute_points%`, `%cykclasses_party_chat%`, `%cykclasses_display%`, `%cykclasses_class_id%`, `%cykclasses_level%`, `%cykclasses_xp%`, `%cykclasses_xp_required%`, `%cykclasses_xp_percent%`, `%cykclasses_mana%`, `%cykclasses_max_mana%`

## Configuration

- `config.yml` — stockage, courbe d'XP, XP par monstre, mana, mode compétences, HUD, reset.
- `classes.yml` — classes, sous-classes, stats, défauts, conditions et compétences (le haut du fichier documente tous les types disponibles).
- `hud.yml` — XP gagnée, dégâts flottants, PV des cibles, panneau du groupe.
- `messages.yml` — tous les textes au format [MiniMessage](https://docs.advntr.dev/minimessage/format.html).

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
# -> build/libs/CykClasses-<version>.jar
```

## Releases

Chaque push sur `main` publie automatiquement une release GitHub avec le `.jar` et le changelog généré à partir des messages de commit ([Conventional Commits](https://www.conventionalcommits.org/fr/)) :
`feat:` → version mineure, `fix:` → correctif, `feat!:` → version majeure. Ajouter `[skip release]` au message pour ne pas publier.

## Crédits

Architecture inspirée de plugins RPG open source comme [Fabled (ex-ProSkillAPI)](https://github.com/magemonkeystudio/fabled) et [MMOCore](https://gitlab.com/phoenix-dvpmt/mmocore). Code original.

## Licence

[GPL-3.0](LICENSE)
