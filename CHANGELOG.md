# Changelog

Each version is described in English first, then in French (🇫🇷 En français).
Chaque version est décrite en anglais, puis en français (🇫🇷 En français).

## [1.9.0](https://github.com/Cykalos/Classes_and_Magie/compare/v1.8.2...v1.9.0) (2026-10-21)

### ✨ New features

- quest system and configuration files organised in folders ([`f570110`](https://github.com/Cykalos/Classes_and_Magie/commit/f570110cb3fd50fecba8a029cfcba0062bd5b0e2))
  - Optional quests: tutorial offered at the end of character creation, 6-chapter main story, 2 quests per class, side quests (some repeatable) and secret quests discovered under conditions or on a specific action
  - Quest journal (/quetes, book in the /classe menu): tabs per category, objectives with progress, missing requirements and rewards; click to start or track, Shift + right click to abandon
  - Sidebar quest tracker, shared with the party board (hud.yml → quest-tracker, /quetes panneau to hide it)
  - About thirty objective types (kill, gather, craft, fish, cast a skill, collect items, explore a biome, reach a place or a level…) with conditions (night, item in hand, sneaking…)
  - Rewards: class and job XP, skill tree and attribute points, money (Vault), items (custom items included), commands
  - Configuration files organised by theme: classes/, personnage/, metiers/, objets/, affichage/, quetes/, donnees/; files from older versions are moved automatically on first start
  - Admin commands /quetes admin, placeholders %cykclasses_quests_completed%, %cykclasses_quests_active%, %cykclasses_quests_total%, %cykclasses_quest_tracked%, new quests module
  - CI starts a real Paper server to check that the plugin enables and loads its quests

### 🇫🇷 En français

#### ✨ Nouveautés

- système de quêtes et fichiers de configuration rangés dans des dossiers
  - Quêtes facultatives : tutoriel proposé à la fin de la création du personnage, histoire principale en 6 chapitres, 2 quêtes par classe, quêtes annexes (dont des répétables) et quêtes secrètes découvertes sous conditions ou sur une action précise
  - Journal des quêtes (/quetes, livre dans le menu /classe) : onglets par catégorie, objectifs avec progression, conditions manquantes et récompenses ; clic pour commencer ou suivre, Maj + clic droit pour abandonner
  - Panneau latéral de suivi des quêtes, partagé avec le panneau du groupe (hud.yml → quest-tracker, /quetes panneau pour le masquer)
  - Une trentaine de types d'objectifs (éliminer, récolter, fabriquer, pêcher, lancer une compétence, rassembler des objets, explorer un biome, atteindre un lieu ou un niveau…) avec des conditions (de nuit, objet en main, accroupi…)
  - Récompenses : XP de classe et de métier, points d'arbre et d'attributs, argent (Vault), objets (custom compris), commandes
  - Fichiers de configuration rangés par thème : classes/, personnage/, metiers/, objets/, affichage/, quetes/, donnees/ ; les fichiers des anciennes versions sont déplacés automatiquement au premier démarrage
  - Commandes d'administration /quetes admin, placeholders %cykclasses_quests_completed%, %cykclasses_quests_active%, %cykclasses_quests_total%, %cykclasses_quest_tracked%, nouveau module quests
  - La CI démarre un vrai serveur Paper pour vérifier que le plugin s'active et charge ses quêtes


## [1.8.2](https://github.com/Cykalos/Classes_and_Magie/compare/v1.8.1...v1.8.2) (2026-10-19)

### 🐛 Fixes

- shadow step stuns its target so it cannot strike back instantly ([`b829542`](https://github.com/Cykalos/Classes_and_Magie/commit/b829542be1f0b71db2576ea450e266d94b54bb30))
  - After the teleport, the target is stunned for 1 s: it cannot attack or target anyone and is heavily slowed (new stun parameter, in seconds)
  - Fall distance is reset after the teleport

### 🇫🇷 En français

#### 🐛 Corrections

- Pas de l'ombre étourdit sa cible, qui ne peut plus riposter aussitôt
  - Après la téléportation, la cible est étourdie 1 s : elle ne peut ni attaquer ni cibler et elle est fortement ralentie (nouveau paramètre stun, en secondes)
  - La hauteur de chute est remise à zéro après la téléportation


## [1.8.1](https://github.com/Cykalos/Classes_and_Magie/compare/v1.8.0...v1.8.1) (2026-10-19)

### 🐛 Fixes

- character creation resumes after death, protection during creation ([`c7c3a94`](https://github.com/Cykalos/Classes_and_Magie/commit/c7c3a94c39041725c2cb8bd1ef97c8a010fa08e7))
  - A player who dies during character creation gets the current step back on respawn
  - Menus closed by the game (teleport, unloaded world) open again
  - New creation-protection option (config.yml, on by default): no damage, ignored by monsters, cannot attack until the character is created

### 📚 Documentation

- bilingual README, wiki and changelog [skip release] ([`8340709`](https://github.com/Cykalos/Classes_and_Magie/commit/83407095fb1d671862e71f10bac92ba56b3b0a57))
  - English README (README.en.md) with language links on both READMEs
  - English wiki pages (EN-*) with a bilingual sidebar, footer and home links
  - CHANGELOG entries 1.0.0 to 1.8.0 in English then French
  - Release notes generated in English and French (FR: line in the commit body)
  - New CHANGELOG entries inserted after the file header

### 🇫🇷 En français

#### 🐛 Corrections

- la création du personnage reprend après une mort, protection pendant la création
  - Un joueur mort pendant la création du personnage retrouve l'étape en cours à sa réapparition
  - Les menus fermés par le jeu (téléportation, monde déchargé) se rouvrent
  - Nouvelle option creation-protection (config.yml, activée par défaut) : aucun dégât, ignoré par les monstres, ne peut pas attaquer tant que le personnage n'est pas créé

#### 📚 Documentation

- bilingual README, wiki and changelog [skip release]
  - English README (README.en.md) with language links on both READMEs
  - English wiki pages (EN-*) with a bilingual sidebar, footer and home links
  - CHANGELOG entries 1.0.0 to 1.8.0 in English then French
  - Release notes generated in English and French (FR: line in the commit body)
  - New CHANGELOG entries inserted after the file header


## [1.8.0](https://github.com/Cykalos/Classes_and_Magie/compare/v1.7.0...v1.8.0) (2026-10-15)

### ✨ New features

- fixes from the acceptance tests before the stable release ([`a878c3a`](https://github.com/Cykalos/Classes_and_Magie/commit/a878c3a9806f901c16a39bdf97ce7f76e9bb22eb))
  - Character creation: closing a required menu (Esc / E) reopens it; rolled traits and weaknesses must be confirmed, even after a disconnect; closing the job menu means "choose later"
  - Menus: stat labels take the colour of their value ("Melee +25%" fully green)
  - Skill mode: slots without a skill are filled (picked-up loot goes to the inventory)
  - Unique items: a released copy (libererunique) breaks, so two copies are never active
  - Netherite upgrade: the forged item's name is updated
  - Gems: socketing restricted to the jeweler (gems.yml → socketing.jobs)
  - Jobs: a refused breeding no longer consumes the food; new USE_ITEM action (bone meal, hoe…); crops, bone meal and tilling reserved to the farmer by default
  - Disabled modules: buttons removed from the /classe menu and menus blocked; Weaknesses button added to the /classe menu
  - Parties: floating damage and target health bars shared with nearby allies, no target bossbar while in a party (hud.yml → party)
  - Placeholders %cykclasses_weaknesses% (all weaknesses) and %cykclasses_weaknesses_count%

### 🇫🇷 En français

#### ✨ Nouveautés

- corrections de la recette avant la version stable
  - Création du personnage : fermer un menu obligatoire (Échap / E) le rouvre ; les traits et faiblesses tirés doivent être validés, même après une déconnexion ; fermer le menu du métier = « choisir plus tard »
  - Menus : libellés de stats colorés comme leur valeur (Mêlée +25 % en vert)
  - Mode compétences : les slots sans compétence sont remplis (le butin va dans l'inventaire)
  - Objets uniques : un exemplaire libéré (libererunique) se brise, jamais deux exemplaires actifs
  - Amélioration en netherite : le nom de l'objet forgé est mis à jour
  - Gemmes : incrustation réservée au joaillier (gems.yml → socketing.jobs)
  - Métiers : l'élevage refusé ne consomme plus la nourriture ; nouvelle action USE_ITEM (poudre d'os, houe…) ; cultures, poudre d'os et labour réservés au fermier par défaut
  - Modules désactivés : boutons retirés du menu /classe et menus inaccessibles ; bouton Faiblesses ajouté au menu /classe
  - Groupes : dégâts flottants et barre de vie des cibles partagés avec les alliés proches, pas de bossbar de cible en groupe (hud.yml → party)
  - Placeholders %cykclasses_weaknesses% (toutes les faiblesses) et %cykclasses_weaknesses_count%


## [1.7.0](https://github.com/Cykalos/Classes_and_Magie/compare/v1.6.0...v1.7.0) (2026-10-12)

### ✨ New features

- unified weaknesses — a single menu and a single name for players ([`e00d5c9`](https://github.com/Cykalos/Classes_and_Magie/commit/e00d5c9ae5431f2602a2ffc1851cc2359312bf6c))
  - /faiblesses groups every weakness of the character with its origin: class, race (and class penalty), traits, imposed and randomly rolled weaknesses
  - "Défauts" (flaws) becomes "Faiblesses" (weaknesses) everywhere in the menus (classes and races show their weaknesses and the imposed ones under a single title)
  - Impose a weakness directly in races.yml or classes.yml: weaknesses: [id] (subclasses inherit those of their class, unless inherit-flaws: false)
  - Default imposed weaknesses: Elf → Claustrophobic, Dwarf → Vertigo, Orc → Poor marksman, Tiefling → Infernal blood, Felys → Sensitive to cold
  - Exclusions also apply to imposed weaknesses, and the default exclusions avoid duplicates with class and race weaknesses
  - Flaws already written in classes.yml / races.yml / traits.yml (flaws key) work as before

### 🇫🇷 En français

#### ✨ Nouveautés

- faiblesses unifiées — un seul menu et un seul nom pour les joueurs
  - /faiblesses regroupe toutes les faiblesses du personnage avec leur origine : classe, race (et malus de classe), traits, faiblesses imposées et tirées au hasard
  - « Défauts » devient « Faiblesses » partout dans les menus (classes et races affichent leurs faiblesses et celles imposées sous un seul titre)
  - Imposer une faiblesse directement dans races.yml ou classes.yml : weaknesses: [id] (les sous-classes héritent de celles de leur classe, sauf inherit-flaws: false)
  - Faiblesses imposées par défaut : Elfe → Claustrophobe, Nain → Vertige, Orc → Mauvais tireur, Tieffelin → Sang infernal, Félys → Frileux
  - Les exclusions s'appliquent aussi aux faiblesses imposées, et les exclusions par défaut évitent les doublons avec les faiblesses des classes et des races
  - Les défauts déjà écrits dans classes.yml / races.yml / traits.yml (clé flaws) fonctionnent comme avant


## [1.6.0](https://github.com/Cykalos/Classes_and_Magie/compare/v1.5.0...v1.6.0) (2026-10-12)

### ✨ New features

- weaknesses, conditional flaws and toggleable modules ([`7186bee`](https://github.com/Cykalos/Classes_and_Magie/commit/7186bee752a16b5216a1a15bc0882ae343f18d22))
  - Weaknesses (weaknesses.yml, /faiblesses): imposed on a race, class or subclass, or randomly rolled with per-race and per-class odds (races, classes, exclusions, multipliers), rerolls, menu during character creation
  - 15 weaknesses included: Hydrophobic, Photosensitive, Vulnerable to fire, Sensitive to cold, Brittle bones, Prey of the dead, Fear of the dark, Unstable mana, Poor marksman, Panic, Vertigo, Claustrophobic, Anaemia, Moon-cursed, Infernal blood
  - Conditional flaws: every flaw (classes, races, traits, weaknesses) accepts conditions — time of day, weather, dimension, world, biome, light, open sky, water, rain, lava, health, altitude, sneaking, sprinting, elytra, held or worn item, permission, chance
  - New flaw types: DAMAGE_DEALT, MANA_REGEN, EFFECT, PERIODIC_DAMAGE; DAMAGE_TAKEN can be filtered by attacker (entity types and MythicMobs mobs)
  - Toggleable modules in config.yml: races, traits, weaknesses, jobs, skill tree, attributes, parties, forge, gems, kits, item restrictions
  - /classe admin faiblesse <player> <reset|add|remove> [id], placeholder %cykclasses_weaknesses%
  - Wiki: Weaknesses page, conditional flaws, modules

### 📚 Documentation

- Modrinth description — customisation and limits explained ([`be3e34a`](https://github.com/Cykalos/Classes_and_Magie/commit/be3e34aea391c59c6290ff3585cbe3b1fe45ee67))

### 🇫🇷 En français

#### ✨ Nouveautés

- faiblesses, défauts conditionnels et modules activables
  - Faiblesses (weaknesses.yml, /faiblesses) : imposées à une race, une classe ou une sous-classe, ou tirées au hasard avec des chances par race et par classe (races, classes, exclusions, multiplicateurs), relances, menu pendant la création
  - 15 faiblesses fournies : Hydrophobe, Photosensible, Vulnérable au feu, Frileux, Os fragiles, Proie des morts, Peur du noir, Mana instable, Mauvais tireur, Panique, Vertige, Claustrophobe, Anémie, Maudit par la lune, Sang infernal
  - Défauts conditionnels : tous les défauts (classes, races, traits, faiblesses) acceptent des conditions — moment de la journée, météo, dimension, monde, biome, lumière, ciel, eau, pluie, lave, PV, altitude, accroupi, sprint, élytres, objet tenu ou porté, permission, chance
  - Nouveaux types de défauts : DAMAGE_DEALT, MANA_REGEN, EFFECT, PERIODIC_DAMAGE ; DAMAGE_TAKEN filtrable par attaquant (types d'entités et mobs MythicMobs)
  - Modules activables dans config.yml : races, traits, faiblesses, métiers, arbre, attributs, groupes, forge, gemmes, kits, restrictions d'objets
  - /classe admin faiblesse <joueur> <reset|add|remove> [id], placeholder %cykclasses_weaknesses%
  - Wiki : page Faiblesses, défauts conditionnels, modules

#### 📚 Documentation

- description Modrinth — personnalisation et limites expliquées


## [1.5.0](https://github.com/Cykalos/Classes_and_Magie/compare/v1.4.2...v1.5.0) (2026-10-11)

### ✨ New features

- the plugin is now named Classes_and_Magie ([`b2f88c1`](https://github.com/Cykalos/Classes_and_Magie/commit/b2f88c1cbe372f46148434e8dbe2e1ad6032ee80))
  - Configuration folder plugins/Classes_and_Magie: the old plugins/CykClasses folder is renamed automatically on first start, configuration and data included
  - .jar file named Classes_and_Magie-X.Y.Z.jar
  - Commands, permissions (cykclasses.*) and placeholders (%cykclasses_*%) unchanged

### 📚 Documentation

- project icon in the README and the wiki ([`742b3bf`](https://github.com/Cykalos/Classes_and_Magie/commit/742b3bf436422fa39f1c64aeab5b992436f3ae13))
- GPL-3.0-only licence stated in the README ([`a5b2640`](https://github.com/Cykalos/Classes_and_Magie/commit/a5b2640f6c2f7b6733f8c2d9a768be220f7d4d72))

### 🔧 CI

- automatic publishing to Modrinth on every release ([`ce830c3`](https://github.com/Cykalos/Classes_and_Magie/commit/ce830c32ecbbf11826dd8ba8ce408f5c08c84171))
- "Classes and Magie X.Y.Z.jar" label on the release file ([`9022a26`](https://github.com/Cykalos/Classes_and_Magie/commit/9022a264f90698701b7f330e260ee974392b9b96))

### 🇫🇷 En français

#### ✨ Nouveautés

- le plugin s'appelle désormais Classes_and_Magie
  - Dossier de configuration plugins/Classes_and_Magie : l'ancien dossier plugins/CykClasses est renommé automatiquement au premier démarrage, configuration et données comprises
  - Fichier .jar nommé Classes_and_Magie-X.Y.Z.jar
  - Commandes, permissions (cykclasses.*) et placeholders (%cykclasses_*%) inchangés

#### 📚 Documentation

- icône du projet dans le README et le wiki
- licence GPL-3.0-only précisée dans le README

#### 🔧 CI

- publication automatique sur Modrinth à chaque release
- libellé « Classes and Magie X.Y.Z.jar » sur le fichier des releases


## [1.4.2](https://github.com/Cykalos/Classes_and_Magie/compare/v1.4.1...v1.4.2) (2026-10-11)

### 🐛 Fixes

- .jar renamed "Classes and Magie X.Y.Z.jar", documentation written for every reader ([`d766573`](https://github.com/Cykalos/Classes_and_Magie/commit/d76657380305e4de321a7d05a3f49a26dbd293f8))
  - The .jar is now named "Classes and Magie X.Y.Z.jar" (the internal plugin name stays CykClasses, configuration and data unchanged)
  - README and wiki rewritten for every reader
  - Project description for Modrinth (docs/modrinth.md)

### 🇫🇷 En français

#### 🐛 Corrections

- fichier .jar renommé « Classes and Magie X.Y.Z.jar », documentation pour tous les publics
  - Le .jar s'appelle maintenant « Classes and Magie X.Y.Z.jar » (le nom interne du plugin reste CykClasses, configuration et données inchangées)
  - README et wiki réécrits sans tutoiement, pour tous les lecteurs
  - Description du projet pour Modrinth (docs/modrinth.md)


## [1.4.1](https://github.com/Cykalos/Classes_and_Magie/compare/v1.4.0...v1.4.1) (2026-10-10)

### 🐛 Fixes

- missing messages after a plugin update ([`a5418ce`](https://github.com/Cykalos/Classes_and_Magie/commit/a5418ce6040763a2eb22394f7d241e6c2efa42a1))

### 🔧 Maintenance

- links to the repository renamed Classes_and_Magie ([`a60df32`](https://github.com/Cykalos/Classes_and_Magie/commit/a60df32a21eaa0884ec821e6381a05d5334b1222))

### 🇫🇷 En français

#### 🐛 Corrections

- messages manquants après une mise à jour du plugin

#### 🔧 Maintenance

- liens vers le dépôt renommé Classes_and_Magie


## [1.4.0](https://github.com/Cykalos/Classes_and_Magie/compare/v1.3.0...v1.4.0) (2026-10-10)

### ✨ New features

- job specializations, jewelcrafting and wiki ([`28e1574`](https://github.com/Cykalos/Classes_and_Magie/commit/28e1574848a6098809982d256223e0678f3cc01c))
  - Specializations (sub-jobs) at level 20: Prospector, Driller, Armorsmith, Weaponsmith, Bowyer, Lapidary, Goldsmith, with rarity, power, purity, gem chance and XP bonuses
  - New Jeweler job: gems are found raw and cut at the stonecutter; rarity (Common → Perfect) and purity depend on the job level
  - Jeweler's workshop: rarity chances and purity range shown, cut one at a time or a whole stack
  - Reworked menus: dark frame, profile headers, centred buttons
  - Old jobs.yml backed up automatically; new gems.yml and forge.yml keys get default values
  - GitHub wiki published automatically from docs/wiki

### 🇫🇷 En français

#### ✨ Nouveautés

- spécialisations de métier, joaillerie et wiki
  - Spécialisations (sous-métiers) au niveau 20 : Prospecteur, Foreur, Armurier, Fabricant d'armes, Facteur d'arcs, Lapidaire, Orfèvre, avec bonus de rareté, puissance, pureté, chance de gemme et XP
  - Nouveau métier Joaillier : les gemmes sont trouvées brutes et taillées au tailleur de pierre ; rareté (Commune → Parfaite) et pureté selon le niveau du métier
  - Atelier du joaillier : chances de rareté et fourchette de pureté affichées, taille à l'unité ou en lot
  - Menus retravaillés : cadre sombre, en-têtes de profil, boutons centrés
  - jobs.yml ancien sauvegardé automatiquement ; nouvelles clés de gems.yml et forge.yml avec valeurs par défaut
  - Wiki GitHub publié automatiquement depuis docs/wiki


## [1.3.0](https://github.com/Cykalos/Classes_and_Magie/compare/v1.2.0...v1.3.0) (2026-10-10)

### ✨ New features

- job levels, forge with rarities and unique items, gems ([`e5c738e`](https://github.com/Cykalos/Classes_and_Magie/commit/e5c738ec0c8e188535379555519ab5d9b7c9b4c0))
  - job levels and XP (up to level 50), job XP bar
  - job recipes and actions unlocked by level (min-level)
  - forge: Common to Legendary rarity depending on the blacksmith's level, random positive or negative stats
  - unique items: a single copy of each on the whole server (forge-uniques.yml)
  - gems found while mining, socketed by drag and drop onto forged items
  - forged equipment stats applied while it is worn
  - "Niv." (level) shown before the level in chat and on the party board
  - easier-to-read job menu (short text per action, required level)
  - "Your current job" / "Your current race" labels
  - old jobs.yml backed up and replaced automatically by the new format

### 🇫🇷 En français

#### ✨ Nouveautés

- niveaux de métier, forge avec raretés et objets uniques, gemmes
  - niveaux et XP de métier (jusqu'au niveau 50), barre d'XP de métier
  - recettes et actions de métier débloquées par niveau (min-level)
  - forge : rareté Commun à Légendaire selon le niveau du forgeron, stats aléatoires positives ou négatives
  - objets uniques : un seul exemplaire de chacun sur tout le serveur (forge-uniques.yml)
  - gemmes trouvées en minant, incrustées par glisser-déposer sur les objets forgés
  - stats de l'équipement forgé appliquées quand il est porté
  - « Niv. » devant le niveau dans le chat et le panneau du groupe
  - menu des métiers plus lisible (texte court par action, niveau requis)
  - libellés « Ton métier actuel » / « Ta race actuelle »
  - ancien jobs.yml sauvegardé et remplacé automatiquement par le nouveau format


## [1.2.0](https://github.com/Cykalos/Classes_and_Magie/compare/v1.1.0...v1.2.0) (2026-10-10)

### ✨ New features

- races, traits, jobs, skill tree, attributes, kits and party chat ([`9279ad9`](https://github.com/Cykalos/Classes_and_Magie/commit/9279ad9e40da6b33d42997b891cfd260605485b2))
  - fixes the HUD (health, mana, level, XP) hidden in skill mode
  - fixes the 21/20 health display (inconsistent rounding of the maximum)
  - races with bonuses, flaws, effects and penalties when the race does not suit the class
  - random positive / negative / mixed traits, unique traits (a single player), configurable rerolls
  - one job per player, reserved actions (ores, forge, brewing stand, enchanting, fishing…)
  - skill tree per class with subclass branches and a common tree
  - attribute points (Strength, Dexterity, Intelligence, Vitality, Wisdom, Agility, Endurance)
  - configurable starter kits, CraftEngine / ExecutableItems / MythicMobs items through UniItem
  - item restrictions by class, race, job, level and attributes
  - /gc switches chat to the party, /global goes back to public chat
  - class and level in chat and on the party board

### 🇫🇷 En français

#### ✨ Nouveautés

- races, traits, métiers, arbre de compétences, attributs, kits et chat de groupe
  - corrige le HUD (PV, mana, niveau, XP) caché en mode compétences
  - corrige l'affichage 21/20 des PV (arrondi incohérent du maximum)
  - races avec bonus, défauts, effets et malus si la race ne convient pas à la classe
  - traits aléatoires positifs / négatifs / mixtes, traits uniques (un seul joueur), relances configurables
  - métier unique par joueur, actions réservées (minerais, forge, alambic, enchantement, pêche...)
  - arbre de compétences par classe avec branches de sous-classe et arbre commun
  - points d'attributs (Force, Dextérité, Intelligence, Vitalité, Sagesse, Agilité, Endurance)
  - kits de départ configurables, objets CraftEngine / ExecutableItems / MythicMobs via UniItem
  - restrictions d'objets par classe, race, métier, niveau et attributs
  - /gc bascule le chat sur le groupe, /global revient au chat public
  - classe et niveau dans le chat et le panneau du groupe


## [1.1.0](https://github.com/Cykalos/Classes_and_Magie/compare/v1.0.0...v1.1.0) (2026-10-10)

### ✨ New features

- icon skill bar, combat HUD, parties and MythicMobs ([`6aed4b9`](https://github.com/Cykalos/Classes_and_Magie/commit/6aed4b90f3366f43788efa8ab0d8123ad38fb33e))
  - fixes key 1 doing nothing when the hand was already on slot 1
  - F shows the skills as icons in the hotbar (slots 1-8), the weapon stays in hand
  - customisable icons: icon, icon-model (resource pack) or custom-model-data
  - cooldown shown on the icons, items saved even after a crash or a death
  - XP gained shown for 5 s in a bossbar, with stacking (hud.yml)
  - floating normal / critical / skill / healing damage on mobs and players
  - target health in a bossbar and above its head, configurable in hud.yml
  - party system: XP rules (shared or equal) and friendly fire set at creation
  - heals and buffs reserved to party members, party health board, /gc
  - MythicMobs: mob names and levels, XP per mob, MYTHIC skill type

### 🇫🇷 En français

#### ✨ Nouveautés

- barre de compétences en icônes, HUD de combat, groupes et MythicMobs
  - corrige la touche 1 qui ne lançait rien quand la main était déjà sur le slot 1
  - F affiche les compétences en icônes dans la barre (slots 1-8), l'arme reste en main
  - icônes personnalisables : icon, icon-model (resource pack) ou custom-model-data
  - recharge visible sur les icônes, objets sauvegardés même en cas de crash ou de mort
  - XP gagnée affichée 5 s en bossbar avec cumul (hud.yml)
  - dégâts flottants normaux / critiques / compétences / soins sur mobs et joueurs
  - PV de la cible en bossbar et au-dessus de la tête, configurables dans hud.yml
  - système de groupe : règles d'XP (partagée ou égale) et de dégâts alliés fixées à la création
  - soins et bonus réservés aux membres du groupe, panneau des PV du groupe, /gc
  - MythicMobs : noms et niveaux des mobs, XP par mob, compétences de type MYTHIC


## 1.0.0 (2026-10-10)

### ✨ New features

- first version of CykClasses ([`7bbeb1b`](https://github.com/Cykalos/Classes_and_Magie/commit/7bbeb1b1f4a6952b17416faaf3ecba5b49814138))
  - 5 classes (Warrior, Mage, Archer, Assassin, Cleric) and 10 unlockable subclasses
  - 36 configurable skills, skill mode (F then 1-9) and /competence
  - mana with regeneration, stats per class and per level, flaws
  - levels and XP, class / subclass / skill menus
  - YAML, JSON or SQLite storage, PlaceholderAPI, Java API and events
  - GitHub Actions CI and automatic releases with a changelog

### 🔧 CI

- in-house versioning and changelog, Paper 26.1 and 26.2 builds ([`c80c413`](https://github.com/Cykalos/Classes_and_Magie/commit/c80c413b1c37b8909d73d9b4e7406ff5e6a5aafb))
  - Conventional Commits version calculation (first release as 1.0.0)
  - changelog grouped by type, with commit details
  - Paper 26.1.2 / 26.2 build matrix and errors reported as annotations
  - updated actions (checkout v5, setup-java v5, setup-gradle v5)
  - Dependabot limited to GitHub Actions
- removal of the v0.0.1 test release ([`e6e41dd`](https://github.com/Cykalos/Classes_and_Magie/commit/e6e41dde2e3a74b3f68ca349e067e43b89399a7c))

### 🇫🇷 En français

#### ✨ Nouveautés

- première version de CykClasses
  - 5 classes (Guerrier, Mage, Archer, Assassin, Clerc) et 10 sous-classes à débloquer
  - 36 compétences configurables, mode compétences (F puis 1-9) et /competence
  - mana avec régénération, statistiques par classe et par niveau, défauts
  - niveaux et XP, menus de classes / sous-classes / compétences
  - stockage YAML, JSON ou SQLite, PlaceholderAPI, API Java et événements
  - CI GitHub Actions et releases automatiques avec changelog

#### 🔧 CI

- versionnage et changelog maison, compilation Paper 26.1 et 26.2
  - calcul de version Conventional Commits (première release en 1.0.0)
  - changelog en français groupé par type, avec le détail des commits
  - matrice de compilation Paper 26.1.2 / 26.2 et erreurs remontées en annotations
  - actions mises à jour (checkout v5, setup-java v5, setup-gradle v5)
  - Dependabot limité aux GitHub Actions
- suppression de la release de test v0.0.1
