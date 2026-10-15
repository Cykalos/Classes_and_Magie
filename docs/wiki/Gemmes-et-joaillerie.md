# Gemmes et joaillerie

Fichier : `gems.yml`.

## Le parcours d'une gemme

1. **Trouvée brute** par un mineur (minerais non posés par un joueur).
2. **Taillée** par un joueur **joaillier** : clic droit sur un **tailleur de pierre** avec une gemme brute en main → l'atelier s'ouvre.
3. La taille donne une **rareté** et une **pureté** qui multiplient ses stats.
4. **Incrustée** par un joaillier dans un objet forgé qui a un emplacement libre : prendre la gemme avec la souris et cliquer sur l'objet. C'est définitif.

Les métiers autorisés à incruster se règlent dans `gems.yml` :

```yaml
socketing:
  jobs: [joaillier]   # vide = tout le monde
```

Une gemme brute ne peut pas être incrustée.

## Trouver des gemmes brutes

```yaml
drops:
  jobs: [mineur]
  targets: ["*_ORE", "ANCIENT_DEBRIS"]
  base-chance: 0.01             # 1 % par bloc
  chance-per-job-level: 0.001   # +0,1 % par niveau de métier
```

Au niveau 30 : 1 % + 3 % = 4 % par minerai, +3 % avec la spécialisation **Prospecteur**. La gemme tirée dépend de son `weight`.

## L'atelier du joaillier

- En haut : les **chances de chaque rareté** et la **fourchette de pureté** au niveau actuel.
- Au centre : les gemmes brutes de l'inventaire, regroupées par type.
- **Clic** : taille une gemme. **Maj + clic** : taille tout le stock de ce type.
- En bas : la dernière gemme obtenue.

Chaque gemme taillée donne de l'XP de joaillier (`REFINE` dans `jobs.yml`).

## Rareté de taille

| Rareté | Niveau min. | Puissance |
|---|---|---|
| Commune | — | ×0,8 |
| Raffinée | — | ×1,0 |
| Rare | 5 | ×1,3 |
| Épique | 15 | ×1,6 |
| Légendaire | 25 | ×1,8 |
| **Parfaite** | 35 | ×2,0 (toujours 100 % de pureté) |

Mêmes règles de chance que la forge : `max(min-weight, weight + weight-per-level × niveau)`. La spécialisation **Orfèvre** ajoute 12 niveaux au tirage.

## Pureté

```yaml
refining:
  station: STONECUTTER
  jobs: [joaillier]
  purity:
    min: 35
    max: 80
    per-level: 0.8      # pureté = aléatoire(min, max) + 0,8 × niveau, plafonnée à 100
  purity-min-factor: 0.5
  always-pure: [PARFAITE]
```

**Multiplicateur final** = puissance de la rareté × (0,5 + 0,5 × pureté).

| Exemple | Calcul | Multiplicateur |
|---|---|---|
| Rare à 50 % | 1,3 × 0,75 | ×0,98 |
| Épique à 80 % | 1,6 × 0,9 | ×1,44 |
| Parfaite | 2,0 × 1,0 | ×2,0 |

Un Rubis (+4 % de dégâts de mêlée) Épique à 80 % donne donc +5,8 %. La spécialisation **Lapidaire** ajoute 12 % de pureté.

## Définir une gemme

```yaml
gems:
  rubis:
    name: "<red>Rubis"
    material: RED_DYE
    item-model: "monpack:gems/rubis"   # optionnel
    weight: 30
    description: ["<gray>Une pierre rouge sang."]
    applies-to: [WEAPON]               # WEAPON, RANGED, ARMOR
    stats: {melee-damage: 1.04}
```

- `raw-name` : nom d'une gemme brute (`"<gray><gem> brut"`).
- `raw-model-suffix` : suffixe ajouté à `item-model` pour l'apparence brute (ex. `_brut`).

Gemmes par défaut : Rubis, Saphir, Émeraude, Topaze, Onyx, Œil de faucon, Diamant pur.

## Commandes admin

- `/classe admin gemme <joueur> <id> [nombre]` : gemme **brute**.
- `/classe admin gemme <joueur> <id> <nombre> <rareté> [pureté]` : gemme **taillée**.

## Ce qu'on ne peut pas faire

- Une gemme incrustée ne se retire pas.
- Pas de fusion de gemmes ni de retaille d'une gemme déjà taillée.
- Les gemmes ne s'incrustent que dans des objets **forgés** (qui ont des emplacements).
