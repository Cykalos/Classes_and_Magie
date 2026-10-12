# Classes

Fichier : `classes.yml`. Le haut du fichier documente toutes les clés disponibles.

## Classes et sous-classes par défaut

| Classe | Compétences | Sous-classes (débloquées au niveau 20 + conditions) |
|---|---|---|
| **Guerrier** | Charge, Cri de guerre, Tourbillon | **Berserker** (200 éliminations, 5 000 dégâts infligés) · **Paladin** |
| **Mage** | Projectile arcanique, Transfert, Bouclier de mana | **Pyromancien** (300 sorts lancés) · **Cryomancien** |
| **Archer** | Flèche perçante, Volée, Bond arrière | **Chasseur** (300 éliminations) · **Rôdeur** |
| **Assassin** | Pas de l'ombre, Disparition, Lame empoisonnée | **Ombre** · **Duelliste** |
| **Clerc** | Soin, Bénédiction, Purification | **Prêtre** (1 500 PV soignés) · **Druide** |

Une sous-classe garde les compétences de sa classe et en ajoute 2 ou 3. Le choix d'une sous-classe est définitif (sauf `/classe reset`).

## Statistiques

| Clé | Effet | Type |
|---|---|---|
| `health` / `health-per-level` | PV bonus (demi-cœurs) | additif |
| `mana` / `mana-per-level` | Mana maximum | additif |
| `mana-regen` / `mana-regen-per-level` | Mana par seconde | additif |
| `melee-damage`, `ranged-damage`, `magic-damage` | Dégâts (1.2 = +20 %) | multiplicateur |
| `damage-taken` | Dégâts subis (0.8 = −20 %) | multiplicateur |
| `speed`, `attack-speed` | Vitesse (+0.1 = +10 %) | additif |
| `armor` | Points d'armure | additif |
| `crit-chance`, `crit-multiplier` | Coups critiques de mêlée | additif |

Ces clés sont les mêmes partout : races, traits, métiers, attributs, arbre, forge, gemmes.

## Faiblesses (`flaws`)

Les faiblesses propres à une classe s'écrivent directement dans sa clé `flaws`. Pour imposer une faiblesse réutilisable de `weaknesses.yml`, ajouter `weaknesses: [id]` (voir [Faiblesses](Faiblesses.md)).


| Type | Paramètres | Exemple |
|---|---|---|
| `DAMAGE_TAKEN` | `causes`, `damagers`, `multiplier` | Mage : +30 % de dégâts de feu ; +25 % contre les zombies |
| `DAMAGE_DEALT` | `kinds` (MELEE, RANGED, SKILL), `targets`, `multiplier` | −15 % de dégâts à distance |
| `FORBIDDEN_ARMOR` | `materials` | Mage : pas d'armure en fer ou plus |
| `WEAPON_PENALTY` | `materials`, `multiplier` | Clerc : −40 % avec une épée |
| `WATER_WEAKNESS` | — | Lent et faible dans l'eau |
| `SUNLIGHT_WEAKNESS` | — | Assassin : faible en plein soleil |
| `HUNGER` | `multiplier` | Guerrier : faim accrue |
| `HEALING_REDUCTION` | `multiplier` | Soins reçus réduits |
| `DARKNESS_MANA_BLOCK` | — | Pas de régénération dans le noir |
| `MANA_REGEN` | `multiplier` (0 = bloquée) | Mana deux fois plus lent |
| `EFFECT` | `effects` (`"slowness:1"`) | Ralenti dans l'eau |
| `PERIODIC_DAMAGE` | `damage`, `interval` (s), `fire-ticks` | Brûle au soleil comme un vampire |

**Toutes les faiblesses acceptent des conditions** : ils ne s'appliquent que lorsqu'elles sont toutes vraies.

```yaml
flaws:
  - type: PERIODIC_DAMAGE
    damage: 1
    interval: 3
    fire-ticks: 60
    conditions: {time: DAY, under-sky: true, weather: [CLEAR]}
    description: "Brûle au soleil"
```

Conditions disponibles : `time`, `weather`, `dimensions`, `worlds`, `biomes`, `light-below`, `light-above`, `under-sky`, `in-water`, `in-rain`, `in-lava`, `health-below`, `health-above`, `y-below`, `y-above`, `sneaking`, `sprinting`, `gliding`, `holding`, `wearing`, `permission`, `chance`. Détail sur la page [Faiblesses](Faiblesses.md#conditions).

`inherit-flaws: false` sur une sous-classe pour ne pas reprendre les faiblesses du parent.

## Conditions des sous-classes (`requirements`)

| Type | Paramètres |
|---|---|
| `LEVEL` | `amount` |
| `KILLS` | `target` (`zombie`, `player`, `any`…), `amount` |
| `SKILL_USES` | `target` (id de compétence ou `any`), `amount` |
| `COUNTER` | `target` : `blocks_mined`, `damage_dealt`, `damage_taken`, `healing_done`, `mana_spent` |
| `PERMISSION` | `target` : nœud de permission |

Le menu `/classe sous-classe` affiche la progression de chaque condition.

## Exemple : ajouter une classe

```yaml
classes:
  necromancien:
    name: "<dark_purple><bold>Nécromancien"
    icon: WITHER_SKELETON_SKULL
    description: ["<gray>Commande aux morts."]
    stats:
      health: -2
      mana: 120
      mana-regen: 2.5
      magic-damage: 1.15
    flaws:
      - type: SUNLIGHT_WEAKNESS
        description: "Affaibli en plein soleil"
    skills:
      drain:
        type: ARCANE_BOLT
        name: "<dark_purple>Drain"
        icon: WITHER_ROSE
        mana: 20
        cooldown: 4
        params: {damage: 6}
    subclasses:
      liche:
        name: "<dark_purple><bold>Liche"
        icon: SKELETON_SKULL
        requirements:
          - {type: LEVEL, amount: 25}
          - {type: KILLS, target: any, amount: 500}
```

## Ce qu'on ne peut pas faire

- Pas de multi-classe : un joueur a **une** classe et au plus **une** sous-classe.
- Pas de sous-sous-classe (une sous-classe ne peut pas avoir ses propres sous-classes).
- Un nouveau **type** de compétence demande du Java (voir [Commandes → API](Commandes.md)) ou MythicMobs (type `MYTHIC`).
