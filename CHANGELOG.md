# Changelog

## [1.6.0](https://github.com/Cykalos/Classes_and_Magie/compare/v1.5.0...v1.6.0) (2026-10-12)

### ✨ Nouveautés

- faiblesses, défauts conditionnels et modules activables ([`7186bee`](https://github.com/Cykalos/Classes_and_Magie/commit/7186bee752a16b5216a1a15bc0882ae343f18d22))
  - Faiblesses (weaknesses.yml, /faiblesses) : imposées à une race, une classe ou une sous-classe, ou tirées au hasard avec des chances par race et par classe (races, classes, exclusions, multiplicateurs), relances, menu pendant la création
  - 15 faiblesses fournies : Hydrophobe, Photosensible, Vulnérable au feu, Frileux, Os fragiles, Proie des morts, Peur du noir, Mana instable, Mauvais tireur, Panique, Vertige, Claustrophobe, Anémie, Maudit par la lune, Sang infernal
  - Défauts conditionnels : tous les défauts (classes, races, traits, faiblesses) acceptent des conditions — moment de la journée, météo, dimension, monde, biome, lumière, ciel, eau, pluie, lave, PV, altitude, accroupi, sprint, élytres, objet tenu ou porté, permission, chance
  - Nouveaux types de défauts : DAMAGE_DEALT, MANA_REGEN, EFFECT, PERIODIC_DAMAGE ; DAMAGE_TAKEN filtrable par attaquant (types d'entités et mobs MythicMobs)
  - Modules activables dans config.yml : races, traits, faiblesses, métiers, arbre, attributs, groupes, forge, gemmes, kits, restrictions d'objets
  - /classe admin faiblesse <joueur> <reset|add|remove> [id], placeholder %cykclasses_weaknesses%
  - Wiki : page Faiblesses, défauts conditionnels, modules

### 📚 Documentation

- description Modrinth — personnalisation et limites expliquées [skip release] ([`be3e34a`](https://github.com/Cykalos/Classes_and_Magie/commit/be3e34aea391c59c6290ff3585cbe3b1fe45ee67))


## [1.5.0](https://github.com/Cykalos/Classes_and_Magie/compare/v1.4.2...v1.5.0) (2026-10-11)

### ✨ Nouveautés

- le plugin s'appelle désormais Classes_and_Magie ([`b2f88c1`](https://github.com/Cykalos/Classes_and_Magie/commit/b2f88c1cbe372f46148434e8dbe2e1ad6032ee80))
  - Dossier de configuration plugins/Classes_and_Magie : l'ancien dossier plugins/CykClasses est renommé automatiquement au premier démarrage, configuration et données comprises
  - Fichier .jar nommé Classes_and_Magie-X.Y.Z.jar
  - Commandes, permissions (cykclasses.*) et placeholders (%cykclasses_*%) inchangés

### 📚 Documentation

- icône du projet dans le README et le wiki [skip release] ([`742b3bf`](https://github.com/Cykalos/Classes_and_Magie/commit/742b3bf436422fa39f1c64aeab5b992436f3ae13))
- licence GPL-3.0-only précisée dans le README [skip release] ([`a5b2640`](https://github.com/Cykalos/Classes_and_Magie/commit/a5b2640f6c2f7b6733f8c2d9a768be220f7d4d72))

### 🔧 CI

- publication automatique sur Modrinth à chaque release [skip release] ([`ce830c3`](https://github.com/Cykalos/Classes_and_Magie/commit/ce830c32ecbbf11826dd8ba8ce408f5c08c84171))
- libellé « Classes and Magie X.Y.Z.jar » sur le fichier des releases [skip release] ([`9022a26`](https://github.com/Cykalos/Classes_and_Magie/commit/9022a264f90698701b7f330e260ee974392b9b96))


## [1.4.2](https://github.com/Cykalos/Classes_and_Magie/compare/v1.4.1...v1.4.2) (2026-10-11)

### 🐛 Corrections

- fichier .jar renommé « Classes and Magie X.Y.Z.jar », documentation pour tous les publics ([`d766573`](https://github.com/Cykalos/Classes_and_Magie/commit/d76657380305e4de321a7d05a3f49a26dbd293f8))
  - Le .jar s'appelle maintenant « Classes and Magie X.Y.Z.jar » (le nom interne du plugin reste CykClasses, configuration et données inchangées)
  - README et wiki réécrits sans tutoiement, pour tous les lecteurs
  - Description du projet pour Modrinth (docs/modrinth.md)


## [1.4.1](https://github.com/Cykalos/Classes_and_Magie/compare/v1.4.0...v1.4.1) (2026-10-10)

### 🐛 Corrections

- messages manquants après une mise à jour du plugin ([`a5418ce`](https://github.com/Cykalos/Classes_and_Magie/commit/a5418ce6040763a2eb22394f7d241e6c2efa42a1))

### 🔧 Maintenance

- liens vers le dépôt renommé Classes_and_Magie [skip release] ([`a60df32`](https://github.com/Cykalos/Classes_and_Magie/commit/a60df32a21eaa0884ec821e6381a05d5334b1222))


## [1.4.0](https://github.com/Cykalos/Classes_and_Magie/compare/v1.3.0...v1.4.0) (2026-10-10)

### ✨ Nouveautés

- spécialisations de métier, joaillerie et wiki ([`28e1574`](https://github.com/Cykalos/Classes_and_Magie/commit/28e1574848a6098809982d256223e0678f3cc01c))
  - Spécialisations (sous-métiers) au niveau 20 : Prospecteur, Foreur, Armurier, Fabricant d'armes, Facteur d'arcs, Lapidaire, Orfèvre, avec bonus de rareté, puissance, pureté, chance de gemme et XP
  - Nouveau métier Joaillier : les gemmes sont trouvées brutes et taillées au tailleur de pierre ; rareté (Commune → Parfaite) et pureté selon le niveau du métier
  - Atelier du joaillier : chances de rareté et fourchette de pureté affichées, taille à l'unité ou en lot
  - Menus retravaillés : cadre sombre, en-têtes de profil, boutons centrés
  - jobs.yml ancien sauvegardé automatiquement ; nouvelles clés de gems.yml et forge.yml avec valeurs par défaut
  - Wiki GitHub publié automatiquement depuis docs/wiki


## [1.3.0](https://github.com/Cykalos/Classes_and_Magie/compare/v1.2.0...v1.3.0) (2026-10-10)

### ✨ Nouveautés

- niveaux de métier, forge avec raretés et objets uniques, gemmes ([`e5c738e`](https://github.com/Cykalos/Classes_and_Magie/commit/e5c738ec0c8e188535379555519ab5d9b7c9b4c0))
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

### ✨ Nouveautés

- races, traits, métiers, arbre de compétences, attributs, kits et chat de groupe ([`9279ad9`](https://github.com/Cykalos/Classes_and_Magie/commit/9279ad9e40da6b33d42997b891cfd260605485b2))
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

### ✨ Nouveautés

- barre de compétences en icônes, HUD de combat, groupes et MythicMobs ([`6aed4b9`](https://github.com/Cykalos/Classes_and_Magie/commit/6aed4b90f3366f43788efa8ab0d8123ad38fb33e))
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

### ✨ Nouveautés

- première version de CykClasses ([`7bbeb1b`](https://github.com/Cykalos/Classes_and_Magie/commit/7bbeb1b1f4a6952b17416faaf3ecba5b49814138))
  - 5 classes (Guerrier, Mage, Archer, Assassin, Clerc) et 10 sous-classes à débloquer
  - 36 compétences configurables, mode compétences (F puis 1-9) et /competence
  - mana avec régénération, statistiques par classe et par niveau, défauts
  - niveaux et XP, menus de classes / sous-classes / compétences
  - stockage YAML, JSON ou SQLite, PlaceholderAPI, API Java et événements
  - CI GitHub Actions et releases automatiques avec changelog

### 🔧 CI

- versionnage et changelog maison, compilation Paper 26.1 et 26.2 ([`c80c413`](https://github.com/Cykalos/Classes_and_Magie/commit/c80c413b1c37b8909d73d9b4e7406ff5e6a5aafb))
  - calcul de version Conventional Commits (première release en 1.0.0)
  - changelog en français groupé par type, avec le détail des commits
  - matrice de compilation Paper 26.1.2 / 26.2 et erreurs remontées en annotations
  - actions mises à jour (checkout v5, setup-java v5, setup-gradle v5)
  - Dependabot limité aux GitHub Actions
- suppression de la release de test v0.0.1 [skip release] ([`e6e41dd`](https://github.com/Cykalos/Classes_and_Magie/commit/e6e41dde2e3a74b3f68ca349e067e43b89399a7c))

