# Installation

## Prérequis

- Serveur **Paper 26.1.x ou 26.2** (testé à la compilation sur les deux).
- **Java 25**.
- Aucune dépendance obligatoire. SQLite est téléchargé automatiquement par Paper si tu choisis ce stockage.

## Plugins optionnels

| Plugin | Utilisation |
|---|---|
| PlaceholderAPI | Placeholders `%cykclasses_*%` (scoreboard, tab, chat) |
| MythicMobs | Noms/niveaux des mobs dans le HUD, XP par mob, compétences de type `MYTHIC`, objets `mythic:` |
| CraftEngine, ExecutableItems, ItemsAdder, Oraxen, Nexo, MMOItems | Objets custom dans les kits et les restrictions (via UniItem) |

## Étapes

1. Télécharge le dernier `CykClasses-x.y.z.jar` dans les [Releases](https://github.com/Cykalos/CykClasses_and_Magie/releases).
2. Place-le dans `plugins/` et démarre le serveur.
3. Modifie les fichiers de `plugins/CykClasses/`, puis `/classe admin reload`.

## Fichiers de configuration

| Fichier | Rôle |
|---|---|
| `config.yml` | Stockage (`YAML`, `JSON`, `SQLITE`), courbe d'XP, XP par monstre, mana, mode compétences, chat, groupes |
| `classes.yml` | Classes, sous-classes, stats, défauts, conditions, compétences |
| `races.yml` · `traits.yml` | Races et traits |
| `skilltree.yml` · `attributes.yml` | Arbre de compétences et attributs |
| `jobs.yml` | Métiers, XP de métier, spécialisations, actions réservées |
| `forge.yml` | Raretés des objets forgés, stats aléatoires, objets uniques |
| `gems.yml` | Gemmes brutes, taille (joaillerie), raretés et pureté |
| `kits.yml` · `items.yml` | Kits de départ, restrictions d'objets |
| `hud.yml` | Dégâts flottants, PV des cibles, panneau du groupe, barres d'XP |
| `messages.yml` | Tous les textes ([MiniMessage](https://docs.advntr.dev/minimessage/format.html)) |

## Mise à jour

- Les nouvelles clés de `gems.yml` et `forge.yml` prennent leur **valeur par défaut** si elles manquent dans ton fichier.
- Si ton `jobs.yml` date d'avant les niveaux de métier ou la joaillerie, il est **sauvegardé** en `jobs-ancien-<date>.yml` et remplacé par la nouvelle version. Reporte tes modifications depuis la sauvegarde.
- Les données des joueurs (classe, niveaux, métiers…) sont conservées.
