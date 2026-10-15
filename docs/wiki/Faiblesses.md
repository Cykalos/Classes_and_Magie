# Faiblesses

Fichier : `weaknesses.yml`. Commande : `/faiblesses` (alias `/weaknesses`).

Pour le joueur, tout ce qui pénalise son personnage est une **faiblesse**. Le menu `/faiblesses` les regroupe toutes, avec leur origine :

- **Classe**, **race** et **traits** : écrites directement dans `classes.yml`, `races.yml` ou `traits.yml` (clé `flaws`).
- **Imposées** : faiblesses de `weaknesses.yml` toujours appliquées à une race, une classe ou une sous-classe.
- **Tirées au hasard** : après les traits, avec des chances qui dépendent de la race et de la classe du joueur. Le joueur peut relancer le tirage s'il lui reste des relances.

Dans les fichiers, `flaws` (faiblesse écrite sur place) et `weaknesses.yml` (faiblesse réutilisable) utilisent exactement les mêmes types et les mêmes conditions.

## Tirage

```yaml
roll:
  enabled: true      # false = seulement les faiblesses imposées
  min: 1             # nombre tiré au hasard entre min et max
  max: 2
  rerolls: 1         # relances par joueur (/faiblesses relancer)
  show-step: true    # menu pendant la création (false = tirage silencieux)
```

## Faiblesses imposées

Deux façons de faire, au choix :

**Dans la race ou la classe** (recommandé, tout est au même endroit) :

```yaml
# races.yml
nain:
  weaknesses: [vertige]

# classes.yml (classe ou sous-classe ; une sous-classe hérite de celles de sa classe, sauf inherit-flaws: false)
pyromancien:
  weaknesses: [vulnerable_au_feu]
```

**Ou dans `weaknesses.yml`** :

```yaml
predefined:
  races:
    elfe: [claustrophobe]
  classes:
    pyromancien: [vulnerable_au_feu]
```

Imposées par défaut : Elfe → *Claustrophobe*, Nain → *Vertige*, Orc → *Mauvais tireur*, Tieffelin → *Sang infernal*, Félys → *Frileux*. L'Humain n'en a aucune.

Une faiblesse imposée n'est jamais tirée une seconde fois. Les exclusions (`exclude-races`, `exclude-classes`) s'appliquent aussi aux faiblesses imposées : un Orc guerrier n'a pas *Mauvais tireur*, puisque le guerrier est déjà pénalisé à distance. Avec `weight: 0`, une faiblesse n'est **jamais** tirée au hasard : elle sert uniquement de faiblesse imposée.

## Définir une faiblesse

```yaml
weaknesses:
  vulnerable_au_feu:
    name: "<red>Vulnérable au feu"
    icon: BLAZE_POWDER
    severity: "<gold>Faiblesse moyenne"   # texte libre sous le nom
    description: ["<gray>Les flammes te dévorent plus vite."]
    weight: 10                            # chance relative
    races: []                             # vide = toutes les races
    classes: []                           # vide = toutes (classes et sous-classes)
    exclude-races: [demon]                # jamais pour ces races
    exclude-classes: [pyromancien]
    race-weights: {elfe: 2, felin: 2}     # deux fois plus probable pour ces races
    class-weights: {cryomancien: 3}
    conflicts: []                         # jamais tirées ensemble
    stats: {health: -2}                   # modificateurs (mêmes clés que classes.yml)
    effects: []                           # effets permanents
    flaws:
      - type: DAMAGE_TAKEN
        causes: [FIRE, FIRE_TICK, LAVA]
        multiplier: 1.3
        description: "+30% de dégâts de feu"
```

**Chance réelle** = `weight` × multiplicateur de la race × multiplicateur de la classe × multiplicateur de la sous-classe. Si l'un vaut 0, la faiblesse ne peut pas être tirée pour ce personnage.

Faiblesses fournies : Hydrophobe, Photosensible, Vulnérable au feu, Frileux, Os fragiles, Proie des morts, Peur du noir, Mana instable, Mauvais tireur, Panique, Vertige, Claustrophobe, Anémie, Maudit par la lune, Sang infernal (Tieffelin). Leurs exclusions évitent les doublons avec les faiblesses déjà présentes dans les classes et les races (un Félys ne tire jamais *Hydrophobe*, il déteste déjà l'eau).

## Types de faiblesses

| Type | Paramètres | Effet |
|---|---|---|
| `DAMAGE_TAKEN` | `multiplier`, `causes`, `damagers` | Dégâts reçus × multiplicateur |
| `DAMAGE_DEALT` | `multiplier`, `kinds`, `targets` | Dégâts infligés × multiplicateur |
| `HEALING_REDUCTION` | `multiplier` | Soins reçus × multiplicateur |
| `HUNGER` | `multiplier` | Faim × multiplicateur |
| `MANA_REGEN` | `multiplier` | Régénération de mana × multiplicateur (0 = bloquée) |
| `EFFECT` | `effects` | Effets de potion tant que les conditions sont vraies |
| `PERIODIC_DAMAGE` | `damage`, `interval`, `fire-ticks` | Dégâts réguliers |
| `FORBIDDEN_ARMOR` | `materials` | Armures interdites |
| `WEAPON_PENALTY` | `materials`, `multiplier` | Dégâts réduits avec certaines armes |
| `WATER_WEAKNESS`, `SUNLIGHT_WEAKNESS`, `DARKNESS_MANA_BLOCK` | — | Raccourcis historiques |

- `causes` : causes de dégâts Minecraft (`FIRE`, `FIRE_TICK`, `LAVA`, `FALL`, `DROWNING`, `FREEZE`, `MAGIC`, `POISON`, `WITHER`, `ENTITY_ATTACK`, `PROJECTILE`, `ENTITY_EXPLOSION`…).
- `damagers` / `targets` : types d'entités (`ZOMBIE`, `"*SKELETON*"`, `PLAYER`) ou mobs MythicMobs (`"mythic:NomInterne"`).
- `kinds` : `MELEE` (corps à corps), `RANGED` (arc, arbalète), `SKILL` (compétences).

## Conditions

Chaque faiblesse peut avoir un bloc `conditions`. Il ne s'applique que si **toutes** sont vraies.

| Condition | Exemple | Vrai quand… |
|---|---|---|
| `time` | `NIGHT` | il fait nuit (`DAY` / `NIGHT`) |
| `weather` | `[RAIN, THUNDER]` | la météo correspond (`CLEAR`, `RAIN`, `THUNDER`) |
| `dimensions` | `[NETHER]` | `NORMAL`, `NETHER` ou `THE_END` |
| `worlds` | `[world_pvp]` | le joueur est dans un de ces mondes |
| `biomes` | `["*DESERT*", BADLANDS]` | le biome correspond |
| `light-below` / `light-above` | `7` | lumière du bloc inférieure / supérieure |
| `under-sky` | `true` | à ciel ouvert (`false` = à couvert) |
| `in-water` / `in-rain` / `in-lava` | `true` | dans l'eau, sous la pluie, dans la lave |
| `health-below` / `health-above` | `0.3` | PV sous / au-dessus de 30 % |
| `y-below` / `y-above` | `20` | altitude |
| `sneaking` / `sprinting` / `gliding` | `true` | accroupi, en sprint, en élytres |
| `holding` | `["*_SWORD"]` | objet en main principale |
| `wearing` | `["GOLDEN_*"]` | au moins une pièce d'armure correspondante |
| `permission` | `"serveur.maudit"` | le joueur a cette permission |
| `chance` | `0.25` | 25 % de chances à chaque vérification |

Exemple — un vampire qui brûle au soleil et frappe plus fort la nuit :

```yaml
flaws:
  - type: PERIODIC_DAMAGE
    damage: 1
    interval: 3
    fire-ticks: 60
    conditions: {time: DAY, under-sky: true, weather: [CLEAR]}
    description: "Brûle au soleil"
  - type: DAMAGE_DEALT
    multiplier: 1.2
    conditions: {time: NIGHT}
    description: "+20% de dégâts la nuit"
```

`DAMAGE_DEALT` avec un multiplicateur supérieur à 1 devient un **bonus** : les faiblesses servent aussi à créer des avantages conditionnels.

## Administration

- `/classe admin faiblesse <joueur> reset` : nouveau tirage au prochain passage.
- `/classe admin faiblesse <joueur> add <id>` / `remove <id>` : ajouter ou retirer une faiblesse précise.
- Placeholders : `%cykclasses_weaknesses%` (toutes les faiblesses : classe, race, traits, imposées, tirées) et `%cykclasses_weaknesses_count%`.

## Ce qu'on ne peut pas faire

- Les faiblesses tirées ne changent pas automatiquement quand le joueur change de classe ou de race (utiliser `reset`). Les faiblesses imposées, elles, suivent toujours la race et la classe actuelles.
- Les conditions sont vérifiées chaque seconde pour les effets et dégâts réguliers, et à chaque coup pour les multiplicateurs de dégâts.
