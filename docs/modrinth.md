# Classes and Magie

**Un système RPG complet et entièrement configurable pour serveurs Paper 26.x : classes, sous-classes, compétences et mana, races, traits, métiers, forge, gemmes, quêtes et titres.**

Chaque joueur crée un vrai personnage : il choisit une classe, une race, reçoit des traits au hasard, apprend un métier, puis progresse en niveaux, en sous-classe et en spécialisation.

> **Tout est configurable.** Les classes, races, métiers et objets décrits ci-dessous sont un **contenu d'exemple prêt à jouer**. Modifiez-les, supprimez-les ou créez les vôtres dans des fichiers YAML commentés, sans écrire une ligne de code.

---

## ⚔️ Classes et compétences

Contenu fourni par défaut :

- **5 classes** : Guerrier, Mage, Archer, Assassin, Clerc, chacune avec ses statistiques, ses faiblesses et ses compétences.
- **10 sous-classes** débloquées par des conditions : niveau, éliminations, sorts lancés, dégâts infligés, soins prodigués…
- **36 compétences** : charge, boule de feu, pluie de météores, nova de givre, pas de l'ombre, parade, racines, jugement…
- **Barre de compétences** : la touche **F** remplace la barre d'objets par les icônes des compétences, les touches **1 à 8** les lancent. Recharge et mana visibles sur chaque icône, icônes personnalisables par resource pack.
- **Mana** avec régénération par classe, réduite en combat.

## 🧬 Création du personnage

- **6 races** (Humain, Elfe, Nain, Orc, Tieffelin, Félys) avec bonus, faiblesses, effets permanents et **malus** si la race ne convient pas à la classe.
- **Traits aléatoires** positifs, négatifs ou mixtes, avec relances limitées.
- **Traits uniques** : un seul joueur sur tout le serveur peut porter chacun d'eux.
- **Faiblesses** imposées par la race ou la classe, ou tirées au hasard selon elles : vulnérable au feu, proie des morts-vivants, maudit par la lune…

## 📈 Progression

- Niveaux et XP par monstre (barème configurable, MythicMobs compris).
- **Arbre de compétences** par classe, avec des branches réservées aux sous-classes.
- **Attributs** : Force, Dextérité, Intelligence, Vitalité, Sagesse, Agilité, Endurance.

## ⚒️ Métiers et artisanat

- **9 métiers**, un seul par joueur : Mineur, Forgeron, Joaillier, Bûcheron, Fermier, Alchimiste, Enchanteur, Pêcheur, Dresseur.
- Actions et **recettes débloquées par niveau** de métier (équipement en diamant, débris antiques, table de forgeron…).
- **Spécialisations** au niveau 20 : Prospecteur, Foreur, Armurier, Fabricant d'armes, Facteur d'arcs, Lapidaire, Orfèvre.
- **Forge** : les armes et armures fabriquées par un forgeron reçoivent une **rareté** (Commun → Légendaire), des **stats aléatoires** positives ou négatives et des emplacements de gemmes.
- **Objets uniques** : Excalibur, Égide… chacun ne peut être forgé **qu'une seule fois** sur le serveur.
- **Gemmes** trouvées brutes en minant, **taillées par un joaillier** (rareté et pureté selon son niveau), puis incrustées dans l'équipement.

## 📜 Quêtes

- **Toutes facultatives**, avec un **journal** (`/quetes`) : en cours, disponibles, verrouillées, terminées.
- **Tutoriel** proposé à la fin de la création du personnage.
- **Histoire principale** en 6 chapitres, **quêtes de classe**, **quêtes annexes** (répétables ou non) et **quêtes secrètes** qui se découvrent sous certaines conditions.
- **Panneau latéral** de suivi des objectifs.
- Une trentaine de types d'objectifs (éliminer, récolter, fabriquer, explorer un biome, rassembler des objets, lancer une compétence…) combinables avec des conditions (de nuit, épée en main…), et des récompenses au choix : XP, points, argent (Vault), objets, commandes.

## 🏅 Titres

- **31 titres** à débloquer selon les actions du joueur : niveau, éliminations, statistiques, progrès, temps de jeu, quêtes…
- Raretés de Commun à **Unique** (un seul joueur du serveur) et **Secret** (conditions cachées).
- Le titre équipé s'affiche **au-dessus de la tête** et dans le chat, et peut donner des effets… ou rester honorifique.

## 👥 Groupes, chat et HUD

- **Groupes** dont les règles sont fixées à la création : XP partagée ou égale, dégâts alliés ou non. Les soins ne touchent que les alliés.
- **Chat de groupe** (`/gc`), classe et niveau affichés dans le chat.
- **Dégâts flottants** (normaux, critiques, compétences, soins) et **PV de la cible** en bossbar ou au-dessus de la tête.
- Panneau latéral avec les PV et le mana des membres du groupe.

## 🛠️ Créez votre propre univers

Tout se règle dans des fichiers YAML commentés, rangés par dossier, avec des exemples :

| Fichier | Ce que vous pouvez créer ou modifier |
|---|---|
| `classes.yml` | Classes, sous-classes, stats, faiblesses, conditions de déblocage, compétences |
| `races.yml` · `traits.yml` | Races, malus selon la classe, traits et traits uniques |
| `weaknesses.yml` | Faiblesses imposées ou aléatoires, avec chances par race et par classe |
| `jobs.yml` | Métiers, spécialisations, actions réservées, recettes par niveau, XP |
| `forge.yml` · `gems.yml` | Raretés, stats aléatoires, objets uniques, gemmes, taille |
| `skilltree.yml` · `attributes.yml` | Arbres de compétences, attributs |
| `kits.yml` · `items.yml` | Kits de départ, restrictions d'objets (objets custom compris) |
| `hud.yml` · `messages.yml` | Affichages, et **tous les textes** (traduisibles dans n'importe quelle langue) |
| `quetes/` | Quêtes : tutoriel, histoire, classes, annexes, secrètes |
| `titles.yml` | Titres, raretés, conditions de déblocage, effets |

**Bon à savoir**

- Les compétences s'appuient sur une trentaine de **mécaniques fournies** (projectile, zone, soin, charge, téléportation, invisibilité…) entièrement réglables : dégâts, portée, durée, mana, recharge, icône. Pour une mécanique totalement nouvelle, utilisez une compétence **MythicMobs** (type `MYTHIC`, sans code) ou l'**API Java**.
- **Faiblesses conditionnelles** : chaque faiblesse peut ne s'appliquer que la nuit, sous la pluie, dans un biome, un monde, à PV bas, en altitude, avec un objet en main… (une vingtaine de conditions combinables).
- **Modules** : désactivez races, traits, faiblesses, métiers, arbre, attributs, groupes, forge, gemmes, kits, restrictions d'objets, quêtes ou titres dans `config.yml`.
- Les types de faiblesses, de conditions et d'actions de métier sont prédéfinis, mais se combinent librement.
- Une classe et une sous-classe par joueur, un seul métier, jusqu'à 8 compétences dans la barre.
- Les commandes sont en français, avec des alias anglais (`/class`, `/job`, `/party`, `/skill`).

## 🔌 Compatibilité

- **Paper 26.1.x et 26.2** (et Purpur), Java 25.
- **MythicMobs** (optionnel) : noms et niveaux des mobs, XP par mob, compétences MythicMobs utilisables comme compétences de classe.
- **PlaceholderAPI** (optionnel) : placeholders `%cykclasses_*%`.
- Objets custom dans les kits et les restrictions : **CraftEngine, ExecutableItems, MythicCrucible, ItemsAdder, Oraxen, Nexo, MMOItems**.
- Stockage **YAML, JSON ou SQLite**.
- **API Java** et événements pour les autres plugins.

## 📖 Documentation

Documentation complète en français et en anglais, avec exemples et limites : **[wiki](https://github.com/Cykalos/Classes_and_Magie/wiki)**.

Code source sous licence **GPL-3.0** : **[GitHub](https://github.com/Cykalos/Classes_and_Magie)** · Bugs et suggestions : **[Issues](https://github.com/Cykalos/Classes_and_Magie/issues)**.

---

## 🇬🇧 English

**A complete RPG system for Paper 26.x servers.** Players pick a **class** (5 classes, 10 unlockable subclasses, 36 skills cast from a dedicated hotbar with mana and cooldowns), a **race** (with class penalties), random **traits** (including server-unique ones), and a **job** (9 jobs with level-gated recipes and specializations). Blacksmiths **forge** gear with random rarity and stats, including one-of-a-kind **unique items**; jewelers **cut raw gems** with rarity and purity before they are socketed. Optional **quests** (tutorial, 6-chapter main story, class, side and secret quests) come with a journal and a sidebar tracker, and **titles** (from Common to Unique and Secret) unlocked by the player's actions are shown above the head, with or without effects. Also includes **parties** with custom rules, party chat, floating damage numbers and target health display, a **skill tree** and **attributes**.

**Fully configurable:** everything listed above is ready-to-play default content. Weaknesses can be imposed by race or class or rolled with per-race/per-class odds, every flaw can be conditional (night, rain, biome, world, low health, altitude, held item…), and each module can be turned off. Create your own classes, subclasses, races, traits, jobs, forge rarities, gems, kits and item rules in commented YAML files, without writing code. Skills are built from ~36 tunable mechanics (projectiles, AoE, heals, dashes, teleports…); brand-new mechanics can be added through **MythicMobs** skills or the Java API.

Compatible with **MythicMobs**, **PlaceholderAPI** and custom items from **CraftEngine, ExecutableItems, MythicCrucible, ItemsAdder, Oraxen, Nexo, MMOItems**. The default texts are in **French**; every message can be translated in `messages.yml`. Full documentation in English and French on the **[wiki](https://github.com/Cykalos/Classes_and_Magie/wiki/EN-Home)**. Commands are in French with English aliases (`/class`, `/job`, `/party`, `/skill`).
