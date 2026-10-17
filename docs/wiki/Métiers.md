# Métiers

🇫🇷 Français · [🇬🇧 English](EN-Jobs.md)

Fichier : `jobs.yml`. Menu : `/metier`.

## Principe

- **Un seul métier** par joueur, proposé après la classe, la race et les traits.
- Changement possible (`allow-change`, délai `change-cooldown-hours`, 24 h par défaut). Le niveau et la spécialisation de chaque métier sont **conservés** si le joueur revient à un ancien métier.
- Un métier donne des stats et effets, de l'**XP de métier**, et réserve des **actions** à ses membres.

Métiers par défaut : Mineur, Forgeron, **Joaillier**, Bûcheron, Fermier, Alchimiste, Enchanteur, Pêcheur, Dresseur.

## Niveaux de métier

```yaml
leveling:
  max-level: 50
  base-xp: 50       # XP requise = base-xp × niveau ^ exponent
  exponent: 1.5
```

L'XP s'obtient en faisant son métier (`xp-sources`). Aucun gain pour un bloc posé par un joueur (anti-abus).

| Action | Cible | XP donnée |
|---|---|---|
| `BREAK` | Bloc cassé | xp |
| `PLACE` | Bloc posé | xp |
| `CRAFT` | Objet fabriqué | xp par fabrication |
| `SMELT` | Objet retiré d'un four | xp × objets |
| `ENCHANT` | Table d'enchantement | xp × niveaux dépensés |
| `BREW` | Alambic | xp × potions |
| `FISH`, `BREED`, `TAME` | Pêche, élevage, apprivoisement | xp |
| `REFINE` | Taille d'une gemme (cible = id de la gemme) | xp |
| `INTERACT_BLOCK` | Utiliser un bloc (restriction uniquement) | — |
| `USE_ITEM` | Clic droit avec un objet : poudre d'os, houe… (restriction uniquement) | — |

Cibles : noms de matériaux ou d'entités, `*` = joker, `(A|B)` = A ou B. Sans `targets` : toutes les cibles.

## Actions réservées et recettes débloquées

```yaml
restrictions:
  - action: CRAFT
    targets: ["DIAMOND_(SWORD|AXE|PICKAXE|SHOVEL|HOE|HELMET|CHESTPLATE|LEGGINGS|BOOTS)", "MACE"]
    jobs: [forgeron]
    min-level: 15                       # recette débloquée au niveau 15
    display: "Équipement en diamant, masse"   # texte court dans /metier
    message: "<red>Seul un forgeron confirmé sait faire ça."   # optionnel
```

Exemples par défaut :

| Métier | Niv. 1 | Débloqué ensuite |
|---|---|---|
| Mineur | Fer, or, lapis, redstone | Diamant/émeraude (10), débris antiques (25) |
| Forgeron | Équipement en fer, bouclier, enclume | Or + arbalète (5), diamant + masse (15), table de forgeron (30) |
| Bûcheron | Bois sombres et exotiques | Tiges du Nether (10) |
| Fermier | Élevage, citrouilles, melons, planter des cultures, poudre d'os et labourer | Verrues du Nether (10) |
| Alchimiste / Enchanteur / Pêcheur / Dresseur | Alambic / table d'enchantement / pêche / apprivoiser | — |

Permission `cykclasses.bypass.jobs` : ignore les restrictions.

## Spécialisations (sous-métiers)

Au niveau indiqué (20 par défaut), le joueur choisit **une spécialisation** pour son métier, depuis le bouton du menu `/metier`. Le choix est **définitif** pour ce métier (admin : `resetspec`).

| Métier | Spécialisation | Bonus |
|---|---|---|
| Mineur | **Prospecteur** | +3 % de chance de gemme brute sur les minerais, XP ×1,1 |
| Mineur | **Foreur** | Célérité II, XP ×1,3 sur la pierre et les minerais |
| Forgeron | **Armurier** | Armures et boucliers : +10 niveaux de rareté, stats +15 %, XP ×1,25 |
| Forgeron | **Fabricant d'armes** | Épées, haches, masses : +10 niveaux de rareté, stats +15 % |
| Forgeron | **Facteur d'arcs** | Arcs et arbalètes : +12 niveaux de rareté, stats +20 %, XP ×1,5 |
| Joaillier | **Lapidaire** | +12 % de pureté sur chaque gemme taillée |
| Joaillier | **Orfèvre** | +12 niveaux pour le tirage de rareté des gemmes |

```yaml
jobs:
  forgeron:
    specializations:
      armurier:
        name: "<gray><bold>Armurier"
        icon: IRON_CHESTPLATE
        min-level: 20
        description: ["<gray>Spécialiste des armures et des boucliers."]
        stats: {armor: 1}            # stats permanentes
        effects: []                  # effets permanents
        bonuses:
          - targets: ["*_HELMET", "*_CHESTPLATE", "*_LEGGINGS", "*_BOOTS", "SHIELD"]
            rarity-levels: 10        # niveaux ajoutés au tirage de rareté (forge et taille)
            power: 0.15              # stats des objets forgés +15 %
            xp-multiplier: 1.25      # XP de métier sur ces cibles
            purity: 0                # pureté ajoutée aux gemmes taillées
            gem-chance: 0            # chance de gemme brute ajoutée (minage)
```

`targets: []` = le bonus s'applique à toutes les cibles du métier.

## Ce qu'on ne peut pas faire

- Pas de second métier, ni deux spécialisations sur le même métier.
- Les restrictions portent sur des matériaux et entités vanilla (pas sur des objets custom CraftEngine — utiliser [Objets](Objets.md) pour ça).
- L'élevage est refusé avant que la nourriture soit consommée : un joueur sans le métier ne perd pas ses carottes ou son blé.
- Pas d'XP de métier pour les actions faites par des machines (redstone, fermes automatiques sans joueur).
