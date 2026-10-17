# Races et traits

🇫🇷 Français · [🇬🇧 English](EN-Races-and-traits.md)

## Races (`races.yml`)

Choisie juste après la classe. Par défaut : **Humain**, **Elfe**, **Nain**, **Orc**, **Tieffelin**, **Félys**.

Chaque race a des stats, des faiblesses (`flaws`, et `weaknesses` pour imposer une faiblesse de `weaknesses.yml`), des effets permanents, des classes recommandées et des **malus de classe** :

| Race | Malus si elle joue… |
|---|---|
| Humain | — (polyvalent) |
| Elfe | Guerrier, Berserker |
| Nain | Mage, Archer, Assassin |
| Orc | Mage, Clerc, Prêtre |
| Tieffelin | Clerc, Paladin, Cryomancien |
| Félys | Guerrier, Paladin |

```yaml
races:
  nain:
    name: "<gold><bold>Nain"
    icon: IRON_PICKAXE
    stats: {health: 4, damage-taken: 0.95, speed: -0.05}
    effects: ["haste:1"]
    recommended-classes: [guerrier, clerc]
    class-penalties:
      mage:
        stats: {magic-damage: 0.85, mana: -20}
        description: "La magie résiste aux nains"
```

`config.yml → character.race-can-change` : autorise ou non `/race` après coup. `reset.keep-race` : garder la race lors d'un reset de classe.

## Traits (`traits.yml`)

Après la race, **3 traits** sont tirés au hasard (positifs, négatifs ou mixtes).

```yaml
roll:
  count: 3
  max-negative: 1
  min-positive: 1
  unique-chance: 0.01   # chance d'obtenir EN PLUS un trait unique libre
  rerolls: 3            # relances par joueur (/traits relancer)
  rarity-weights: {COMMON: 60, UNCOMMON: 25, RARE: 10, EPIC: 3}
```

Un trait : `rarity`, `kind` (`POSITIVE`, `NEGATIVE`, `MIXED`), `stats`, `flaws`, `effects`, `conflicts` (traits jamais tirés ensemble), `weight`.

### Traits uniques

`rarity: UNIQUE` : **un seul joueur sur tout le serveur** peut porter ce trait. Il peut y en avoir autant que souhaité (par défaut : *Élu des dieux*, *Maître d'armes légendaire*, *Maudit immortel*). Chacun a son propre propriétaire.

- Un trait unique déjà pris n'est plus jamais tiré.
- Il redevient libre si son porteur le perd (`/classe admin resettraits <joueur>`).

## Ce qu'on ne peut pas faire

- Pas de choix manuel des traits par le joueur (seulement des relances).
- Une race n'a pas de compétences actives propres (utiliser `effects` ou les stats).
