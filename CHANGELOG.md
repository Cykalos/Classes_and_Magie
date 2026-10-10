# Changelog

## 1.0.0 (2026-10-10)

### ✨ Nouveautés

- première version de CykClasses ([`7bbeb1b`](https://github.com/Cykalos/CykClasses_and_Magie/commit/7bbeb1b1f4a6952b17416faaf3ecba5b49814138))
  - 5 classes (Guerrier, Mage, Archer, Assassin, Clerc) et 10 sous-classes à débloquer
  - 36 compétences configurables, mode compétences (F puis 1-9) et /competence
  - mana avec régénération, statistiques par classe et par niveau, défauts
  - niveaux et XP, menus de classes / sous-classes / compétences
  - stockage YAML, JSON ou SQLite, PlaceholderAPI, API Java et événements
  - CI GitHub Actions et releases automatiques avec changelog

### 🔧 CI

- versionnage et changelog maison, compilation Paper 26.1 et 26.2 ([`c80c413`](https://github.com/Cykalos/CykClasses_and_Magie/commit/c80c413b1c37b8909d73d9b4e7406ff5e6a5aafb))
  - calcul de version Conventional Commits (première release en 1.0.0)
  - changelog en français groupé par type, avec le détail des commits
  - matrice de compilation Paper 26.1.2 / 26.2 et erreurs remontées en annotations
  - actions mises à jour (checkout v5, setup-java v5, setup-gradle v5)
  - Dependabot limité aux GitHub Actions
- suppression de la release de test v0.0.1 [skip release] ([`e6e41dd`](https://github.com/Cykalos/CykClasses_and_Magie/commit/e6e41dde2e3a74b3f68ca349e067e43b89399a7c))

