# Faiblesses

Fichier : `weaknesses.yml`. Commande : `/faiblesses` (alias `/weaknesses`).

Les faiblesses (vulnérabilités) s'ajoutent aux défauts de classe et de race. Il en existe deux sortes :

- **Imposées** : toujours appliquées à une race, une classe ou une sous-classe.
- **Aléatoires** : tirées après les traits, avec des chances qui dépendent de la race et de la classe du joueur.

Le menu montre l'origine de chaque faiblesse, et le joueur peut relancer le tirage s'il lui reste des relances.

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

```yaml
predefined:
  races:
    demon: [sang_infernal]
  classes:
    pyromancien: [frileux]   # classes et sous-classes
```

Une faiblesse imposée n'est jamais tirée une seconde fois. Avec `weight: 0`, une faiblesse n'est **jamais** tirée au hasard : elle sert uniquement de faiblesse imposée.

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

Faiblesses fournies : Hydrophobe, Photosensible, Vulnérable au feu, Frileux, Os fragiles, Proie des morts, Peur du noir, Mana instable, Mauvais tireur, Panique, Vertige, Claustrophobe, Anémie, Maudit par la lune, Sang infernal (Tieffelin).

## Types de défauts

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

Chaque défaut peut avoir un bloc `conditions`. Il ne s'applique que si **toutes** sont vraies.

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

`DAMAGE_DEALT` avec un multiplicateur supérieur à 1 devient un **bonus** : les défauts servent aussi à créer des avantages conditionnels.

## Administration

- `/classe admin faiblesse <joueur> reset` : nouveau tirage au prochain passage.
- `/classe admin faiblesse <joueur> add <id>` / `remove <id>` : ajouter ou retirer une faiblesse précise.
- Placeholder : `%cykclasses_weaknesses%`.

## Ce qu'on ne peut pas faire

- Les faiblesses tirées ne changent pas automatiquement quand le joueur change de classe ou de race (utiliser `reset`). Les faiblesses imposées, elles, suivent toujours la race et la classe actuelles.
- Les conditions sont vérifiées chaque seconde pour les effets et dégâts réguliers, et à chaque coup pour les multiplicateurs de dégâts.
