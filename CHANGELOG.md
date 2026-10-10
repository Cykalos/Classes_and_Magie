# Changelog

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

