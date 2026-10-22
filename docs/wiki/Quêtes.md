# Quêtes

🇫🇷 Français · [🇬🇧 English](EN-Quests.md)

Dossier : `quetes/` (tous les fichiers `.yml` du dossier et de ses sous-dossiers sont lus). Commande : `/quetes` (alias `/quests`, `/journal`).

**Toutes les quêtes sont facultatives** : un joueur peut jouer sans jamais en faire.

## Les catégories

| Catégorie | Rôle | Fournies par défaut |
|---|---|---|
| `TUTORIAL` | Tutoriel, proposé à la fin de la création du personnage | *Premiers pas* (5 étapes) |
| `MAIN` | Histoire principale | *La Guilde des Aventuriers* : 6 rangs, de Recrue à Légende (niveaux 2 à 45) |
| `CLASS` | Quêtes réservées à une classe | 2 par classe (niveaux 3 et 15) |
| `SIDE` | Quêtes annexes, certaines répétables | 5 (récoltes, concours de pêche, patrouille, cartographie…) |
| `SECRET` | Invisibles tant qu'elles ne sont pas découvertes | 4 (trésor englouti, nuit d'orage, jardin secret, chant des abeilles) |

Les fichiers fournis : `tutoriel.yml`, `principale.yml`, `de-classe.yml`, `annexes.yml`, `secretes.yml`. Ils ne sont créés que si le dossier `quetes/` n'existe pas : un fichier supprimé ne revient pas.

## Pour les joueurs

- **Tutoriel** : à la fin de la création du personnage, un message propose de le commencer (`[Commencer]` / `[Plus tard]`). Il reste disponible dans le journal.
- **Quêtes proposées** : quand une quête de l'histoire ou de classe devient disponible, elle est proposée dans le chat avec `[Accepter]` / `[Plus tard]`. Rien ne démarre sans l'accord du joueur, sauf les quêtes secrètes, qui se déclenchent quand on les découvre.
- **Journal** (`/quetes` ou `/classe quetes`, ou le bouton « Journal des quêtes » du menu `/classe`) : un onglet par catégorie, plus un onglet « En cours ». Chaque quête affiche son niveau, sa description, son état (en cours, disponible, verrouillée, terminée), ses objectifs avec la progression, les conditions manquantes et les récompenses.
  - Clic sur une quête disponible : la commencer.
  - Clic sur une quête en cours : la suivre (ou ne plus la suivre) dans le panneau latéral.
  - Maj + clic droit sur une quête en cours : l'abandonner. Une quête abandonnée ne redémarre pas toute seule : elle reste disponible dans le journal.
  - Une quête retirée du panneau n'y revient pas d'elle-même ; quand la quête suivie se termine, une autre quête en cours prend sa place.
- **Panneau latéral** : les quêtes suivies et leurs objectifs, mis à jour en direct. En groupe, la quête suivie s'affiche sous les membres du groupe. `/quetes panneau` l'affiche ou le masque.
- **Quêtes secrètes** : elles apparaissent dans le journal une fois découvertes. L'onglet indique combien il en reste à découvrir.

## Format d'une quête

```yaml
quests:
  guilde_platine:
    name: "<gold>Rang V — Platine"
    category: MAIN                 # TUTORIAL, MAIN, CLASS, SIDE, SECRET
    level: 30                      # niveau indicatif affiché
    icon: QUARTZ
    description: ["<gray>Une expédition dans le Nether."]
    start: OFFER                   # MANUAL (journal), OFFER (proposée dans le chat), AUTO (démarre seule*)
    hidden: false                  # true par défaut pour SECRET
    repeatable: false
    cooldown-hours: 0              # délai avant de la refaire
    abandonable: true
    requirements:
      level: 30
      quests: [guilde_or]
    stages:
      - text: ["<gray>Texte affiché au début de l'étape"]
        objectives:
          - {type: CONDITION, conditions: {dimensions: [NETHER]}, display: "Entrer dans le Nether"}
      - objectives:
          - {type: KILL, targets: [GHAST], amount: 2}
          - {type: COLLECT, targets: [QUARTZ], amount: 16, take: true}
    rewards:
      xp: 2500
      skill-points: 1
      money: 300
      items:
        - {item: "minecraft:diamond", amount: 3}
    complete-text: ["<gray>Texte affiché à la fin"]
```

\* Avec `quests.ask-before-start: true` (par défaut), une quête `AUTO` non secrète est elle aussi proposée au lieu de démarrer seule.

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
| `titles` | [Titres](Titres.md) à avoir débloqués |
| `checks` | Liste d'objectifs « état » à remplir : statistiques, progrès, totaux… (`- {type: ADVANCEMENT, targets: ["minecraft:story/enter_the_nether"]}`) |

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
| `COLLECT` | l'inventaire contient `amount` objets (`take: true` les retire à la fin de l'étape, avec un message « Objets remis ») |
| `REACH` | le joueur est à moins de `radius` blocs de `x`, `y`, `z` dans `world` |
| `BIOME` | le joueur est dans un des biomes de `targets` |
| `CONDITION` | le bloc `conditions` est vrai (Nether, sous la couche 0, de nuit…) |
| `TREE_POINTS` · `ATTRIBUTE_POINTS` | le joueur a dépensé `amount` points |
| `QUEST` | `amount` quêtes de `targets` sont terminées (sans `targets` : toutes quêtes confondues) |
| `TITLES` | `amount` titres débloqués (`targets` : ids ou `"rarity:EPIC"`) |
| `KILLS_TOTAL` · `SKILL_USES_TOTAL` | total d'éliminations / de compétences lancées depuis l'arrivée du joueur |
| `COUNTER` | compteur interne : `blocks_mined`, `damage_dealt`, `damage_taken`, `healing_done`, `mana_spent` |
| `STATISTIC` | statistique Minecraft : `DEATHS`, `WALK_ONE_CM`, `FISH_CAUGHT`, `MINE_BLOCK:DIAMOND_ORE`, `KILL_ENTITY:ZOMBIE`… |
| `ADVANCEMENT` | `amount` progrès de `targets` obtenus (`"minecraft:story/enter_the_nether"`) |
| `PLAYTIME` | `amount` heures de jeu |

Les totaux (`KILLS_TOTAL`, `COUNTER`, `STATISTIC`, `ADVANCEMENT`, `PLAYTIME`, `QUEST`) comptent aussi tout ce que le joueur a fait avant de commencer la quête.

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
| `titles` | [Titres](Titres.md) débloqués |
| `commands` | Commandes console (`%player%` = nom du joueur) : permissions, grades, autres plugins |
| `text` | Lignes en plus dans la liste des récompenses du journal |

Les récompenses dépendent de chaque quête : les quêtes fournies donnent plus quand leur niveau est élevé.

## Réglages (`config.yml → quests`)

```yaml
quests:
  offer-tutorial: true      # proposer le tutoriel à la fin de la création
  tutorial: tutoriel        # id de la quête de tutoriel
  ask-before-start: true    # proposer les quêtes au lieu de les démarrer seules (sauf secrètes)
  max-active: 0             # quêtes en cours en même temps (0 = illimité)
  max-tracked: 3            # quêtes suivies dans le panneau
  auto-track: true          # suivre une quête dès qu'elle commence
  hide-other-classes: true
  progress-actionbar: true  # progression dans la barre d'action
  check-interval-ticks: 20
```

Le panneau latéral se règle dans `hud.yml → quest-tracker` (titre, lignes, symboles, affichage en groupe). Tous les textes sont dans `messages.yml → quests`. Le module entier se désactive avec `modules.quests: false`.

## Administration

- `/classe admin quete <joueur> start <id>` : démarre une quête (sans vérifier les conditions).
- `/classe admin quete <joueur> complete <id>` : la termine immédiatement, récompenses comprises.
- `/classe admin quete <joueur> reset <id>` : l'efface (en cours et terminée).
- `/classe admin quete <joueur> resetall` : efface toutes les quêtes du joueur et propose de nouveau le tutoriel.
- Placeholders : `%cykclasses_quests_completed%`, `%cykclasses_quests_active%`, `%cykclasses_quests_total%`, `%cykclasses_quest_tracked%`.

## Ce qu'on ne peut pas faire

- Pas de PNJ intégré : les dialogues s'affichent dans le chat. Un PNJ d'un autre plugin peut servir de cible (`INTERACT_ENTITY` avec `name`).
- Pas de choix de dialogue avec plusieurs réponses (utiliser `not-quests` pour des embranchements).
- Les types d'objectifs sont prédéfinis (une quarantaine), mais se combinent avec les conditions.
- Le panneau latéral remplace le scoreboard d'un autre plugin tant que des quêtes sont suivies (le masquer avec `/quetes panneau` ou `quest-tracker.enabled: false`).
