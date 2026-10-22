# Titres

🇫🇷 Français · [🇬🇧 English](EN-Titles.md)

Fichier : `personnage/titles.yml`. Commande : `/titres` (alias `/titles`), ou le bouton « Titres » du menu `/classe`.

Les titres se **débloquent selon les actions du joueur** : niveau atteint, monstres éliminés, quêtes terminées, distance parcourue, progrès Minecraft… Un seul titre est équipé à la fois. Il s'affiche **au-dessus de la tête** et **dans le chat**, et peut donner des **effets**. Certains titres sont purement honorifiques.

## Raretés

| Rareté | Particularité |
|---|---|
| Commun · Peu commun · Rare | — |
| Épique · Légendaire | Annoncés à tout le serveur |
| **Unique** | Un seul joueur du serveur peut l'obtenir : le premier qui remplit les conditions |
| **Secret** | Affiché « ??? » dans le menu tant qu'il n'est pas débloqué, conditions cachées |

Les raretés se configurent dans `titles.yml → rarities` (nom, annonce, unique, secret) ; il est possible d'en ajouter.

## Titres fournis

31 titres : progression (Novice, Vétéran, Légende vivante…), combat (Chasseur, Fléau des morts, Tueur de dragons…), un titre par classe (Lame affûtée, Archimage, Œil de lynx, Ombre silencieuse, Main de lumière), métiers et exploration (Mineur acharné, Voyageur, Explorateur des mondes…), histoire (Membre de la Guilde, Légende de la Guilde), 2 uniques et 4 secrets.

Les titres basés sur des totaux (éliminations, blocs minés, statistiques, progrès, temps de jeu…) prennent en compte **tout ce que le joueur a déjà fait** : un joueur ancien peut en débloquer plusieurs dès la mise à jour.

## Pour les joueurs

- **Menu** `/titres` : tous les titres, filtrés par « Débloqués » / « À débloquer ». Chaque titre indique sa rareté, ses effets (ou « titre honorifique »), et pour ceux qui restent à débloquer, les conditions avec la progression.
- Clic sur un titre débloqué : l'**équiper** ; nouveau clic : le **retirer**.
- `/titres equiper <id>` · `/titres retirer`.
- Le premier titre débloqué est équipé automatiquement (`auto-equip-first`).

## Format d'un titre

```yaml
titles:
  tueur_de_dragons:
    name: "<light_purple>Tueur de dragons"
    rarity: EPIC
    icon: DRAGON_HEAD
    description: ["<gray>Celui qui a vu tomber le gardien du Cœur."]
    unique: false            # par défaut selon la rareté
    hidden: false            # par défaut selon la rareté
    requirements:            # mêmes conditions que les quêtes
      level: 20
    unlock:                  # objectifs à atteindre
      - {type: KILLS_TOTAL, targets: [ender_dragon], amount: 1, display: "Vaincre l'Ender Dragon"}
    stats: {melee-damage: 1.05}          # effets quand il est équipé
    effects: ["night_vision:1"]          # effets de potion permanents
    flaws:                               # effets conditionnels
      - {type: DAMAGE_DEALT, targets: [ENDERMAN], multiplier: 1.15, description: "+15% contre les Endermen"}
```

- `requirements` : `level`, `max-level`, `classes`, `races`, `jobs`, `job-level`, `quests`, `not-quests`, `permission`, `titles`, `checks` (voir [Quêtes → Conditions](Quêtes.md#conditions-requirements)).
- `unlock` : tous les types d'objectifs des [quêtes](Quêtes.md#objectifs). Sans `unlock`, le titre ne se débloque pas tout seul : il est donné par une quête (`rewards → titles`) ou par un administrateur.
- `stats` · `effects` · `flaws` : mêmes clés que les classes, races et [faiblesses](Faiblesses.md) (y compris les conditions : un titre peut donner un bonus seulement la nuit, sous la couche 0…).

## Objectifs de déblocage utiles

| Type | Exemple |
|---|---|
| `LEVEL` · `JOB_LEVEL` | `{type: LEVEL, amount: 50}` |
| `KILLS_TOTAL` | `{type: KILLS_TOTAL, targets: [zombie, husk], amount: 1000}` (`mythic:nom` possible) |
| `SKILL_USES_TOTAL` | `{type: SKILL_USES_TOTAL, targets: [boule_de_feu], amount: 500}` |
| `COUNTER` | `{type: COUNTER, targets: [blocks_mined], amount: 20000}` (`damage_dealt`, `damage_taken`, `healing_done`, `mana_spent`) |
| `STATISTIC` | `{type: STATISTIC, targets: [DEATHS], amount: 25}` · `MINE_BLOCK:DIAMOND_ORE` · `KILL_ENTITY:ZOMBIE` · `WALK_ONE_CM` |
| `ADVANCEMENT` | `{type: ADVANCEMENT, targets: ["minecraft:story/enter_the_nether"]}` |
| `PLAYTIME` | `{type: PLAYTIME, amount: 100}` (heures) |
| `QUEST` | `{type: QUEST, targets: [la_nuit_d_orage]}` ; sans `targets` : nombre de quêtes terminées |
| `TITLES` | `{type: TITLES, amount: 15}` ; `targets: ["rarity:EPIC"]` pour compter une rareté |
| `KILL` (avec conditions) | `{type: KILL, amount: 300, conditions: {sneaking: true}}` (compte à partir de l'ajout du titre) |

Pour `QUEST` et `ADVANCEMENT`, `amount` est le nombre d'éléments de la liste à avoir (1 par défaut ; mettre le nombre total pour les exiger tous).

## Affichage

```yaml
display:
  enabled: true
  format: "<title>"            # <title>, <rarity>
  height: 0.55                 # au-dessus du pseudo
  scale: 0.9
  background: default          # default, none ou #AARRGGBB
  see-through: false
  hide-when-sneaking: true
  show-own: false              # le joueur voit-il son propre titre ?

chat:
  format: "<dark_gray>«</dark_gray><title><dark_gray>»</dark_gray> "
```

- Le titre au-dessus de la tête est masqué quand le joueur est invisible (potion, compétence *Disparition*), en spectateur, accroupi (réglable) ou caché par un plugin de vanish.
- Dans le chat, `<title>` s'ajoute aux formats de `config.yml → chat` (déjà présent dans les formats par défaut). Pour un serveur qui a déjà son `config.yml`, il suffit d'ajouter `<title>` dans `global-format` et `party-format`.
- Les quêtes peuvent exiger un titre (`requirements → titles`) ou en donner un (`rewards → titles`).

## Administration

- `/titres admin <joueur> give <id>` : donne un titre.
- `/titres admin <joueur> take <id>` : retire un titre (un titre unique redevient disponible).
- `/titres admin <joueur> reset` : retire tous les titres du joueur.
- `/titres admin liberer <id>` : libère un titre unique ; l'ancien porteur le perd (à sa prochaine connexion s'il est absent).
- Placeholders : `%cykclasses_title%`, `%cykclasses_title_id%`, `%cykclasses_titles_count%`, `%cykclasses_titles_total%`.
- Les porteurs des titres uniques sont enregistrés dans `donnees/titres-uniques.yml`.
- Le module se désactive avec `modules.titles: false`.

## Ce qu'on ne peut pas faire

- Un seul titre équipé à la fois.
- Le titre au-dessus de la tête est un affichage flottant qui suit le joueur : sur un serveur très chargé, il peut avoir un très léger décalage pendant les déplacements rapides.
- Les objectifs « action » (comme `KILL` avec conditions) ne comptent qu'à partir du moment où le titre existe ; les totaux (`KILLS_TOTAL`, `STATISTIC`…) comptent aussi le passé.
