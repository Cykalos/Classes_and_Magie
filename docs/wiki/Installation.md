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

| Fichier | Rôle |
|---|---|
| `config.yml` | Stockage (`YAML`, `JSON`, `SQLITE`), courbe d'XP, XP par monstre, mana, mode compétences, chat, groupes |
| `classes.yml` | Classes, sous-classes, stats, faiblesses, conditions, compétences |
| `races.yml` · `traits.yml` | Races et traits |
| `weaknesses.yml` | Faiblesses imposées et aléatoires |
| `skilltree.yml` · `attributes.yml` | Arbre de compétences et attributs |
| `jobs.yml` | Métiers, XP de métier, spécialisations, actions réservées |
| `forge.yml` | Raretés des objets forgés, stats aléatoires, objets uniques |
| `gems.yml` | Gemmes brutes, taille (joaillerie), raretés et pureté |
| `kits.yml` · `items.yml` | Kits de départ, restrictions d'objets |
| `hud.yml` | Dégâts flottants, PV des cibles, panneau du groupe, barres d'XP |
| `messages.yml` | Tous les textes ([MiniMessage](https://docs.advntr.dev/minimessage/format.html)) |

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
```

Un module désactivé disparaît du jeu : son étape de création est sautée, son bouton disparaît du menu `/classe`, sa commande et son menu répondent « fonctionnalité désactivée » et il n'a plus d'effet sur les stats. Les faiblesses écrites directement dans une classe ou une race restent actives, puisqu'elles font partie de la classe ou de la race. Les données des joueurs sont conservées, il suffit de le réactiver pour les retrouver. Seule exception : un objet déjà forgé garde ses stats même si la forge est désactivée.

## Mise à jour

- Les nouvelles clés de `gems.yml` et `forge.yml` prennent leur **valeur par défaut** si elles manquent dans le fichier du serveur.
- Si le `jobs.yml` du serveur date d'avant les niveaux de métier ou la joaillerie, il est **sauvegardé** en `jobs-ancien-<date>.yml` et remplacé par la nouvelle version. Les modifications personnelles sont à reporter depuis la sauvegarde.
- Les données des joueurs (classe, niveaux, métiers…) sont conservées.
- Depuis la version 1.5, le dossier de configuration s'appelle `plugins/Classes_and_Magie/`. L'ancien dossier `plugins/CykClasses/` est renommé automatiquement au premier démarrage, avec toute la configuration et les données.
