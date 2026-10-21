<p align="center"><img src="icon.png" width="128" alt="Classes and Magie"></p>

# Classes and Magie — Wiki

🇫🇷 Français · [🇬🇧 English](EN-Home.md)

**Classes and Magie** est un plugin **Paper 26.x** (Java 25) de jeu de rôle : classes, sous-classes, compétences et mana, races, traits, arbre de compétences, attributs, métiers avec spécialisations, forge à raretés, gemmes à tailler et incruster, groupes, quêtes, HUD.

> Ce wiki est généré automatiquement depuis le dossier `docs/wiki` du dépôt. Les corrections et ajouts se proposent par pull request sur ce dossier.

## Parcours d'un joueur

1. **Classe** (`/classe`) : Guerrier, Mage, Archer, Assassin, Clerc → kit de départ.
2. **Race** (`/race`) : bonus, faiblesses, et malus si la race ne convient pas à la classe.
3. **Traits** (`/traits`) : 3 traits tirés au hasard, relances limitées, traits uniques.
4. **Faiblesses** (`/faiblesses`) : imposées par la race ou la classe, ou tirées au hasard selon elles.
5. **Métier** (`/metier`) : un seul métier, qui débloque des actions et des recettes par niveau.
6. Progression : niveaux de classe, **sous-classe**, **arbre** (`/arbre`), **attributs** (`/stats`), **spécialisation** de métier au niveau 20.
7. **Quêtes** (`/quetes`), toutes facultatives : tutoriel proposé à la fin de la création, histoire principale, quêtes de classe, annexes et secrètes.

Les étapes de création ne peuvent pas être sautées en fermant le menu (il se rouvre), et les traits et faiblesses tirés restent à valider après une déconnexion. Fermer le menu du métier revient à « choisir plus tard ». Un joueur mort pendant la création retrouve l'étape en cours à sa réapparition. Désactivable avec `force-class-selection: false`.

Pendant la création, le joueur est **protégé** : il ne subit aucun dégât, les monstres l'ignorent et il ne peut pas attaquer (`creation-protection: false` pour désactiver).

## Pages

| Page | Contenu |
|---|---|
| [Installation](Installation.md) | Prérequis, plugins compatibles, fichiers de configuration |
| [Classes](Classes.md) | Classes, sous-classes, stats, faiblesses, conditions |
| [Compétences et mana](Compétences-et-mana.md) | Barre de compétences, mana, types de compétences, MythicMobs |
| [Niveaux et XP](Niveaux-et-XP.md) | Courbe d'XP, XP par monstre |
| [Races et traits](Races-et-traits.md) | Races, malus de classe, traits et traits uniques |
| [Faiblesses](Faiblesses.md) | Toutes les faiblesses : imposées, aléatoires, conditions |
| [Progression](Progression.md) | Arbre de compétences et attributs |
| [Quêtes](Quêtes.md) | Tutoriel, histoire, quêtes de classe, annexes et secrètes, journal, panneau de suivi |
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

`classe (+ sous-classe) → race → malus race/classe → traits → faiblesses → métier → spécialisation → attributs → arbre → équipement forgé et gemmes`

Les valeurs « additives » (PV, mana, armure…) s'ajoutent ; les multiplicateurs (dégâts, dégâts subis) se multiplient.
