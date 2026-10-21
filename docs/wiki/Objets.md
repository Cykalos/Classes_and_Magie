# Objets

🇫🇷 Français · [🇬🇧 English](EN-Items.md)

## Kits de départ (`objets/kits.yml`)

Donnés au choix de la classe, et de la sous-classe si un kit porte son id.

```yaml
give-once: true      # un kit par classe, même après un reset

kits:
  guerrier:
    items:
      - item: "minecraft:iron_sword"
        name: "<red>Épée de recrue"
        lore: ["<gray>Fidèle compagne des premiers combats."]
        slot: 0
        enchants: {sharpness: 1}
      - item: "minecraft:shield"
        slot: OFFHAND
      - item: "craftengine:monpack:epee_runique"
      - item: "mythic:EpeeRecrue"
      - item: "executableitems:grimoire"
    commands:
      - "eco give %player% 100"
```

Options : `amount`, `slot` (`HEAD`, `CHEST`, `LEGS`, `FEET`, `OFFHAND` ou 0-35), `name`, `lore`, `enchants`, `unbreakable`, `item-model` (vanilla uniquement).

### Objets custom reconnus (via [UniItem](https://github.com/ProjectUnified/UniItem))

| Plugin | Format |
|---|---|
| Vanilla | `minecraft:iron_sword` |
| CraftEngine | `craftengine:pack:id` |
| ExecutableItems | `executableitems:id` |
| MythicMobs / MythicCrucible | `mythic:Nom` |
| ItemsAdder · Oraxen · Nexo | `itemsadder:ns:id` · `oraxen:id` · `nexo:id` |
| MMOItems | `mmoitems:TYPE:ID` |

La liste des préfixes disponibles est affichée dans la console au démarrage.

Admin : `/classe admin kit <joueur> [classe]` redonne un kit.

## Restrictions d'objets (`objets/items.yml`)

Les stats des objets custom restent gérées par leur plugin (CraftEngine, MythicCrucible…). Ici on décide **qui** peut s'en servir.

```yaml
rules:
  excalibur:
    match:                              # au moins un critère
      materials: ["NETHERITE_SWORD"]
      custom-ids: ["mythic:Excalibur", "craftengine:monpack:excalibur"]
      name-contains: "Excalibur"
      lore-contains: "Lame sacrée"
    conditions:                         # toutes doivent être remplies
      classes: [guerrier, paladin]
      races: [humain, elfe]
      jobs: [forgeron]
      min-level: 30
      attributes: {force: 20}
    block: [ATTACK, WEAR]               # par défaut : tous les usages
    message: "<red>Seul un paladin digne peut manier Excalibur."
```

| Usage bloqué | Effet |
|---|---|
| `ATTACK` | Frapper avec |
| `SHOOT` | Tirer à l'arc / l'arbalète |
| `USE` | Clic droit |
| `WEAR` | Porter en armure ou en main secondaire (retiré automatiquement) |

Sans `message`, les conditions manquantes sont affichées. Permission `cykclasses.bypass.items` : ignore toutes les règles.

## Ce qu'on ne peut pas faire

- Pas de **masquage** des objets interdits (ils restent visibles) : la restriction bloque l'usage, pas l'affichage.
- Pas de création d'objets custom : utiliser CraftEngine, MythicCrucible ou ExecutableItems, puis les référencer ici.
