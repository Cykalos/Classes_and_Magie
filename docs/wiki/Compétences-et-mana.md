# Compétences et mana

🇫🇷 Français · [🇬🇧 English](EN-Skills-and-mana.md)

## Lancer une compétence

1. **F** (échange de main) : la barre d'objets affiche les **icônes des compétences** dans les slots 1 à 8. L'arme reste en main dans le slot 9.
2. **1 à 8** (ou la molette) lance la compétence. L'icône montre la recharge (secondes restantes), devient bleue si le mana manque, grise si elle n'est pas débloquée.
3. **F** à nouveau, un clic dans l'inventaire ou 15 s sans lancer : retour à la barre normale. Minecraft ne prévient pas le serveur à l'ouverture de l'inventaire du joueur : c'est le premier clic qui quitte le mode.

Les slots sans compétence sont remplis par une case grise (`casting.empty-slot-icon`), pour que le butin ramassé aille dans l'inventaire et pas dans la barre.

Les objets de la barre sont sauvegardés pendant le mode compétences, même en cas de mort ou de crash.

Autres façons : `/competence <id|numéro>`, ou un clic dans `/classe competences`.

Réglages dans `config.yml → casting` : `swap-hands-toggle`, `require-sneak` (F accroupi), `timeout-seconds`, `locked-icon`, `pvp`.

## Définir une compétence

```yaml
skills:
  charge:
    type: CHARGE            # implémentation
    name: "<red>Charge"
    description: ["<gray>Fonce et projette les ennemis."]
    icon: IRON_BOOTS
    icon-model: "monpack:skills/charge"   # modèle de resource pack (optionnel)
    custom-model-data: 1001               # alternative (optionnel)
    mana: 20
    health-cost: 0          # demi-cœurs
    cooldown: 8             # secondes
    unlock-level: 1
    params: {damage: 6, distance: 8}
```

8 compétences maximum par classe (sous-classe comprise), une par slot.

## Types disponibles

`ARCANE_BOLT`, `ARROW_RAIN`, `BEAR_TRAP`, `BLESSING`, `BLINK`, `BLOOD_RAGE`, `CHARGE`, `DISENGAGE`, `DIVINE_GRACE`, `ENTANGLING_ROOTS`, `EXECUTION`, `EXPLOSIVE_ARROW`, `FIREBALL`, `FLAME_AURA`, `FROST_NOVA`, `HEAL`, `HOLY_LIGHT`, `HOLY_SHIELD`, `HUNTERS_MARK`, `ICE_LANCE`, `JUDGMENT`, `LUNGE`, `MANA_SHIELD`, `METEOR_SHOWER`, `PARRY`, `PIERCING_ARROW`, `POISON_BLADE`, `PURIFY`, `SHADOW_STEP`, `SHADOW_VEIL`, `SHATTERING_BLOW`, `VANISH`, `VOLLEY`, `WAR_CRY`, `WHIRLWIND`, `WILD_SPIRIT`, `MYTHIC`.

Les soins et bonus (`HEAL`, `BLESSING`, `WAR_CRY`, `HOLY_SHIELD`…) ne touchent **que le lanceur et son groupe**. Les compétences offensives touchent les alliés seulement si le groupe a activé les dégâts alliés.

## Compétences MythicMobs

```yaml
  boule_ombre:
    type: MYTHIC
    name: "<dark_purple>Boule d'ombre"
    icon: ECHO_SHARD
    mana: 25
    cooldown: 8
    params: {skill: "MaCompetenceMythic", power: 1.0}
```

Le mana et la recharge sont gérés par Classes and Magie, l'effet par MythicMobs.

## Mana

- Maximum et régénération venant de la classe, du niveau, de la race, des traits, des attributs, de l'arbre, de l'équipement.
- `config.yml → mana` : `regen-interval-ticks`, `regen-in-combat`, `combat-regen-multiplier` (régénération réduite 5 s après un coup).
- La faiblesse `DARKNESS_MANA_BLOCK` coupe la régénération dans le noir.

## Flèches

Les flèches des compétences, et celles tirées à l'arc par les joueurs, touchent même si la cible vient de subir des dégâts. Dans Minecraft, elles rebondiraient sans faire de dégâts, par exemple sur un zombie qui brûle au soleil. Pour l'arc, l'option se règle dans `config.yml → combat.bow-ignores-hit-delay` (`false` = comportement de Minecraft). L'arbalète garde le comportement de Minecraft, à cause de l'enchantement Tir multiple.

## Ce qu'on ne peut pas faire

- Pas plus de 8 compétences dans la barre.
- Pas de combo ni de compétence passive déclenchée automatiquement (utiliser les stats, l'arbre ou MythicMobs).
- Les nouvelles mécaniques demandent du Java (`CykClassesAPI.registerSkillType`) ou MythicMobs.
