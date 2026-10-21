# Quêtes

🇫🇷 Français · [🇬🇧 English](EN-Quests.md)

Dossier : `quetes/` (tous les fichiers `.yml` du dossier et de ses sous-dossiers sont lus). Commande : `/quetes` (alias `/quests`, `/journal`).

**Toutes les quêtes sont facultatives** : un joueur peut jouer sans jamais en faire.

## Les catégories

| Catégorie | Rôle | Fournies par défaut |
|---|---|---|
| `TUTORIAL` | Tutoriel, proposé à la fin de la création du personnage | *Premiers pas d'un Éveillé* (5 étapes) |
| `MAIN` | Histoire principale | 6 chapitres, *Les Éclats de la Fracture* (niveaux 2 à 40) |
| `CLASS` | Quêtes réservées à une classe | 2 par classe (niveaux 3 et 15) |
| `SIDE` | Quêtes annexes, certaines répétables | 5 (pêche, primes, exploration…) |
| `SECRET` | Invisibles tant qu'elles ne sont pas découvertes | 4 (lune sanglante, abysses, ruines…) |

Les fichiers fournis : `tutoriel.yml`, `principale.yml`, `de-classe.yml`, `annexes.yml`, `secretes.yml`. Ils ne sont créés que si le dossier `quetes/` n'existe pas : un fichier supprimé ne revient pas.

## Pour les joueurs

- **Tutoriel** : à la fin de la création du personnage, un message propose de le commencer (`[Commencer]` / `[Plus tard]`). Il reste disponible dans le journal.
- **Journal** (`/quetes`, ou le livre en haut à droite du menu `/classe`) : un onglet par catégorie, plus un onglet « En cours ». Chaque quête affiche son niveau, sa description, son état (en cours, disponible, verrouillée, terminée), ses objectifs avec la progression, les conditions manquantes et les récompenses.
  - Clic sur une quête disponible : la commencer.
  - Clic sur une quête en cours : la suivre (ou ne plus la suivre) dans le panneau latéral.
  - Maj + clic droit sur une quête en cours : l'abandonner.
- **Panneau latéral** : les quêtes suivies et leurs objectifs, mis à jour en direct. En groupe, la quête suivie s'affiche sous les membres du groupe. `/quetes panneau` l'affiche ou le masque.
- **Quêtes secrètes** : elles apparaissent dans le journal une fois découvertes. L'onglet indique combien il en reste à découvrir.

## Format d'une quête

```yaml
quests:
  la_porte_brulante:
    name: "<gold>V. La Porte Brûlante"
    category: MAIN                 # TUTORIAL, MAIN, CLASS, SIDE, SECRET
    level: 25                      # niveau indicatif affiché
    icon: BLAZE_ROD
    description: ["<gray>Un passage vers un monde de flammes."]
    start: AUTO                    # MANUAL (journal), AUTO (dès que disponible), OFFER (proposée)
    hidden: false                  # true par défaut pour SECRET
    repeatable: false
    cooldown-hours: 0              # délai avant de la refaire
    abandonable: true
    requirements:
      level: 25
      quests: [la_nuit_des_eveilles]
    stages:
      - text: ["<gray>Texte affiché au début de l'étape"]
        objectives:
          - {type: CONDITION, conditions: {dimensions: [NETHER]}, display: "Entrer dans le Nether"}
      - objectives:
          - {type: KILL, targets: [BLAZE], amount: 6}
          - {type: COLLECT, targets: [BLAZE_ROD], amount: 4, take: true}
    rewards:
      xp: 2500
      skill-points: 1
      money: 300
      items:
        - {item: "minecraft:diamond", amount: 3}
    complete-text: ["<gray>Texte affiché à la fin"]
```

Les étapes s'enchaînent dans l'ordre ; tous les objectifs d'une étape doivent être remplis pour passer à la suivante.

## Conditions (`requirements`)

| Clé | Effet |
|---|---|
| `level` / `max-level` | Niveau de classe minimum / maximum |
| `classes` | Classe ou sous-classe (`[guerrier]` inclut ses sous-classes) |
| `races` · `jobs` | Race ou métier |
| `job-level` | Niveau du métier actuel |
| `quests` | Quêtes à avoir terminées (chaînes de quêtes) |
| `not-quests` | Quêtes à ne pas avoir faites (choix exclusifs : rejoindre un camp ou l'autre) |
| `permission` | Permission requise (quêtes débloquées par un autre plugin) |

Avec `quests.hide-other-classes: true` (config.yml), les quêtes réservées à une autre classe ou une autre race n'apparaissent pas dans le journal.

## Objectifs

**Actions** (comptées à chaque fois) :

| Type | Compte… | `targets` |
|---|---|---|
| `KILL` | les éliminations | types d'entités, `mythic:NomInterne` |
| `BREAK` · `PLACE` | les blocs cassés / posés (pas les blocs posés par un joueur) | blocs |
| `CRAFT` · `SMELT` | les fabrications, les objets cuits | objets |
| `FISH` · `BREED` · `TAME` | pêche, élevage, apprivoisement | objets pêchés / animaux |
| `ENCHANT` · `BREW` · `REFINE` | enchantements, potions, gemmes taillées | objets / gemmes |
| `CONSUME` | nourriture et potions consommées | objets |
| `SKILL` | les compétences lancées | ids de compétences |
| `INTERACT_ENTITY` | clic droit sur une entité (`name:` texte du nom, pour un PNJ) | types d'entités |
| `INTERACT_BLOCK` | clic droit sur un bloc | blocs |
| `COMMAND` | une commande tapée (alias reconnus) | commandes sans `/` |
| `DAMAGE` | les dégâts infligés (`amount` = total) | entités touchées |

**États** (vérifiés chaque seconde) :

| Type | Rempli quand… |
|---|---|
| `LEVEL` · `JOB_LEVEL` | le niveau atteint `amount` |
| `CLASS` · `SUBCLASS` · `RACE` · `JOB` · `SPECIALIZATION` | le joueur a la classe, la race… listée dans `targets` |
| `COLLECT` | l'inventaire contient `amount` objets (`take: true` les retire à la fin de l'étape) |
| `REACH` | le joueur est à moins de `radius` blocs de `x`, `y`, `z` dans `world` |
| `BIOME` | le joueur est dans un des biomes de `targets` |
| `CONDITION` | le bloc `conditions` est vrai (Nether, sous la couche 0, de nuit…) |
| `TREE_POINTS` · `ATTRIBUTE_POINTS` | le joueur a dépensé `amount` points |

**Options communes** :

- `targets` : `*` est un joker (`"*_LOG"`, `"*DESERT*"`). Sans `targets`, tout compte.
- `amount` : quantité (1 par défaut).
- `display` : texte affiché ; sans lui, un texte automatique (`messages.yml → quests.objective`).
- `conditions` : contexte de l'action, avec les mêmes conditions que les [faiblesses](Faiblesses.md#conditions) : `{time: NIGHT}`, `{holding: ["*_SWORD"]}`, `{sneaking: true}`, `{biomes: [DESERT]}`…
- `module` : module requis (`skill-tree`, `attributes`, `jobs`, `parties`…). Si ce module est désactivé, l'objectif est validé d'office.

Exemple — éliminer 15 monstres à l'épée, de nuit :

```yaml
- type: KILL
  targets: [ZOMBIE, SKELETON, SPIDER, CREEPER]
  amount: 15
  conditions: {time: NIGHT, holding: ["*_SWORD"]}
  display: "Éliminer des monstres à l'épée, de nuit"
```

## Quêtes secrètes

Une quête `category: SECRET` (ou `hidden: true`) est invisible jusqu'à sa découverte. Elle démarre seule :

- **quand ses conditions sont réunies** (`conditions:`), par exemple en entrant dans les profondeurs obscures :
  ```yaml
  conditions: {biomes: [DEEP_DARK]}
  ```
- **ou sur une action précise** (`trigger:`), avec les mêmes options qu'un objectif :
  ```yaml
  trigger:
    type: KILL
    targets: [ZOMBIE]
    conditions: {time: NIGHT, chance: 0.05}   # 5 % de chances, de nuit
  ```

Pour un PNJ (villageois renommé avec une étiquette, PNJ Citizens…) : `trigger: {type: INTERACT_ENTITY, targets: [VILLAGER], name: "Ermite"}`.

## Récompenses

| Clé | Effet |
|---|---|
| `xp` | XP de classe |
| `job-xp` | XP du métier actuel |
| `skill-points` · `attribute-points` | Points d'arbre et d'attributs en plus |
| `money` | Argent (nécessite **Vault** et un plugin d'économie) |
| `items` | Objets, au même format que `kits.yml` (objets custom CraftEngine, MythicMobs… compris) |
| `commands` | Commandes console (`%player%` = nom du joueur) : permissions, grades, autres plugins |
| `text` | Lignes en plus dans la liste des récompenses du journal |

Les récompenses dépendent de chaque quête : les quêtes fournies donnent plus quand leur niveau est élevé.

## Réglages (`config.yml → quests`)

```yaml
quests:
  offer-tutorial: true      # proposer le tutoriel à la fin de la création
  tutorial: tutoriel        # id de la quête de tutoriel
  max-active: 0             # quêtes en cours en même temps (0 = illimité)
  max-tracked: 3            # quêtes suivies dans le panneau
  auto-track: true          # suivre une quête dès qu'elle commence
  hide-other-classes: true
  progress-actionbar: true  # progression dans la barre d'action
  check-interval-ticks: 20
```

Le panneau latéral se règle dans `hud.yml → quest-tracker` (titre, lignes, symboles, affichage en groupe). Tous les textes sont dans `messages.yml → quests`. Le module entier se désactive avec `modules.quests: false`.

## Administration

- `/quetes admin <joueur> start <id>` : démarre une quête (sans vérifier les conditions).
- `/quetes admin <joueur> complete <id>` : la termine immédiatement, récompenses comprises.
- `/quetes admin <joueur> reset <id>` : l'efface (en cours et terminée).
- `/quetes admin <joueur> resetall` : efface toutes les quêtes du joueur et propose de nouveau le tutoriel.
- Placeholders : `%cykclasses_quests_completed%`, `%cykclasses_quests_active%`, `%cykclasses_quests_total%`, `%cykclasses_quest_tracked%`.

## Ce qu'on ne peut pas faire

- Pas de PNJ intégré : les dialogues s'affichent dans le chat. Un PNJ d'un autre plugin peut servir de cible (`INTERACT_ENTITY` avec `name`).
- Pas de choix de dialogue avec plusieurs réponses (utiliser `not-quests` pour des embranchements).
- Les types d'objectifs sont prédéfinis (une trentaine), mais se combinent avec les conditions.
- Le panneau latéral remplace le scoreboard d'un autre plugin tant que des quêtes sont suivies (le masquer avec `/quetes panneau` ou `quest-tracker.enabled: false`).
- Les titres de joueur ne sont pas encore gérés (prévus pour une prochaine version).
