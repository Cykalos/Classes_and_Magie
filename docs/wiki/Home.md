<p align="center"><img src="icon.png" width="128" alt="Classes and Magie"></p>

# Classes and Magie — Wiki

**Classes and Magie** est un plugin **Paper 26.x** (Java 25) de jeu de rôle : classes, sous-classes, compétences et mana, races, traits, arbre de compétences, attributs, métiers avec spécialisations, forge à raretés, gemmes à tailler et incruster, groupes, HUD.

> Ce wiki est généré automatiquement depuis le dossier `docs/wiki` du dépôt. Les corrections et ajouts se proposent par pull request sur ce dossier.

## Parcours d'un joueur

1. **Classe** (`/classe`) : Guerrier, Mage, Archer, Assassin, Clerc → kit de départ.
2. **Race** (`/race`) : bonus, défauts, et malus si la race ne convient pas à la classe.
3. **Traits** (`/traits`) : 3 traits tirés au hasard, relances limitées, traits uniques.
4. **Métier** (`/metier`) : un seul métier, qui débloque des actions et des recettes par niveau.
5. Progression : niveaux de classe, **sous-classe**, **arbre** (`/arbre`), **attributs** (`/stats`), **spécialisation** de métier au niveau 20.

## Pages

| Page | Contenu |
|---|---|
| [Installation](Installation.md) | Prérequis, plugins compatibles, fichiers de configuration |
| [Classes](Classes.md) | Classes, sous-classes, stats, défauts, conditions |
| [Compétences et mana](Compétences-et-mana.md) | Barre de compétences, mana, types de compétences, MythicMobs |
| [Niveaux et XP](Niveaux-et-XP.md) | Courbe d'XP, XP par monstre |
| [Races et traits](Races-et-traits.md) | Races, malus de classe, traits et traits uniques |
| [Progression](Progression.md) | Arbre de compétences et attributs |
| [Métiers](Métiers.md) | Métiers, restrictions, niveaux, recettes, spécialisations |
| [Forge](Forge.md) | Raretés, stats aléatoires, objets uniques |
| [Gemmes et joaillerie](Gemmes-et-joaillerie.md) | Gemmes brutes, taille, rareté, pureté, incrustation |
| [Objets](Objets.md) | Kits de départ, restrictions d'objets, objets custom |
| [Groupes et chat](Groupes-et-chat.md) | Groupes, règles, chat de groupe |
| [HUD](HUD.md) | Dégâts flottants, PV des cibles, XP |
| [Commandes](Commandes.md) | Commandes, permissions, placeholders, API |
| [Limites](Limites.md) | Ce que le plugin ne fait pas |

## Ordre de calcul des statistiques

Les statistiques finales d'un joueur s'additionnent ou se multiplient dans cet ordre :

`classe (+ sous-classe) → race → malus race/classe → traits → métier → spécialisation → attributs → arbre → équipement forgé et gemmes`

Les valeurs « additives » (PV, mana, armure…) s'ajoutent ; les multiplicateurs (dégâts, dégâts subis) se multiplient.
