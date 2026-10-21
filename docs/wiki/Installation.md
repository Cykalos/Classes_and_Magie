# Installation

🇫🇷 Français · [🇬🇧 English](EN-Installation.md)

## Prérequis

- Serveur **Paper 26.1.x ou 26.2** (testé à la compilation sur les deux).
- **Java 25**.
- Aucune dépendance obligatoire. SQLite est téléchargé automatiquement par Paper si ce stockage est choisi.

## Plugins optionnels

| Plugin | Utilisation |
|---|---|
| PlaceholderAPI | Placeholders `%cykclasses_*%` (scoreboard, tab, chat) |
| MythicMobs | Noms/niveaux des mobs dans le HUD, XP par mob, compétences de type `MYTHIC`, objets `mythic:` |
| CraftEngine, ExecutableItems, ItemsAdder, Oraxen, Nexo, MMOItems | Objets custom dans les kits et les restrictions (via UniItem) |

## Étapes

1. Télécharger le dernier `Classes_and_Magie-x.y.z.jar` dans les [Releases](https://github.com/Cykalos/Classes_and_Magie/releases).
2. Le placer dans `plugins/` et démarrer le serveur.
3. Modifier les fichiers de `plugins/Classes_and_Magie/`, puis `/classe admin reload`.

## Fichiers de configuration

Les fichiers sont rangés par thème dans `plugins/Classes_and_Magie/` :

| Dossier / fichier | Rôle |
|---|---|
| `config.yml` | Modules, stockage (`YAML`, `JSON`, `SQLITE`), courbe d'XP, XP par monstre, mana, mode compétences, chat, groupes, quêtes |
| `classes/classes.yml` | Classes, sous-classes, stats, faiblesses, conditions, compétences |
| `classes/skilltree.yml` · `classes/attributes.yml` | Arbre de compétences et attributs |
| `personnage/races.yml` · `personnage/traits.yml` | Races et traits |
| `personnage/weaknesses.yml` | Faiblesses imposées et aléatoires |
| `personnage/titles.yml` | [Titres](Titres.md) : raretés, conditions, effets, affichage |
| `metiers/jobs.yml` | Métiers, XP de métier, spécialisations, actions réservées |
| `metiers/forge.yml` | Raretés des objets forgés, stats aléatoires, objets uniques |
| `metiers/gems.yml` | Gemmes brutes, taille (joaillerie), raretés et pureté |
| `objets/kits.yml` · `objets/items.yml` | Kits de départ, restrictions d'objets |
| `affichage/hud.yml` | Dégâts flottants, PV des cibles, panneau latéral (groupe et quêtes), barres d'XP |
| `affichage/messages.yml` | Tous les textes ([MiniMessage](https://docs.advntr.dev/minimessage/format.html)) |
| `quetes/` | [Quêtes](Quêtes.md) : tous les fichiers `.yml` du dossier sont lus |
| `donnees/` | Données des joueurs (`joueurs/` ou `data.db`), objets, traits et titres uniques, sauvegardes. À ne pas modifier à la main. |

## Modules

Chaque partie du plugin peut être désactivée dans `config.yml` :

```yaml
modules:
  races: true
  traits: true
  weaknesses: true
  jobs: true
  skill-tree: true
  attributes: true
  parties: true
  forge: true
  gems: true
  kits: true
  item-restrictions: true
  quests: true
  titles: true
```

Un module désactivé disparaît du jeu : son étape de création est sautée, son bouton disparaît du menu `/classe`, sa commande et son menu répondent « fonctionnalité désactivée » et il n'a plus d'effet sur les stats. Les faiblesses écrites directement dans une classe ou une race restent actives, puisqu'elles font partie de la classe ou de la race. Les données des joueurs sont conservées, il suffit de le réactiver pour les retrouver. Seule exception : un objet déjà forgé garde ses stats même si la forge est désactivée.

## Mise à jour

- Les nouvelles clés de `gems.yml` et `forge.yml` prennent leur **valeur par défaut** si elles manquent dans le fichier du serveur.
- Si le `jobs.yml` du serveur date d'avant les niveaux de métier ou la joaillerie, il est **sauvegardé** en `donnees/sauvegardes/jobs-ancien-<date>.yml` et remplacé par la nouvelle version. Les modifications personnelles sont à reporter depuis la sauvegarde.
- Les données des joueurs (classe, niveaux, métiers…) sont conservées.
- Depuis la version 1.9, les fichiers sont rangés dans des dossiers (`classes/`, `personnage/`, `metiers/`, `objets/`, `affichage/`, `donnees/`). Les fichiers d'une ancienne installation, tous à la racine, sont **déplacés automatiquement** au premier démarrage, sans perte : configuration, données des joueurs (`playerdata/` devient `donnees/joueurs/`), base SQLite et sauvegardes.
- Depuis la version 1.5, le dossier de configuration s'appelle `plugins/Classes_and_Magie/`. L'ancien dossier `plugins/CykClasses/` est renommé automatiquement au premier démarrage, avec toute la configuration et les données.
