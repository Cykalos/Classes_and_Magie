# CykClasses

Plugin **Paper 26.x** (Minecraft Java Edition) de classes RPG : classes, sous-classes à débloquer, compétences uniques, statistiques, défauts et mana.

## Fonctionnalités

- **5 classes** jouables, chacune avec ses statistiques, ses défauts et 3 compétences :
  Guerrier, Mage, Archer, Assassin, Clerc.
- **10 sous-classes** débloquées par des conditions (niveau, éliminations, utilisation de compétences, dégâts infligés, soins prodigués…) :

  | Classe | Sous-classes |
  |---|---|
  | Guerrier | Berserker · Paladin |
  | Mage | Pyromancien · Cryomancien |
  | Archer | Chasseur · Rôdeur |
  | Assassin | Ombre · Duelliste |
  | Clerc | Prêtre · Druide |

- **36 compétences** différentes (charge, boule de feu, pluie de météores, nova de givre, pas de l'ombre, parade, marque du chasseur, racines…). Toutes les valeurs sont réglables dans `classes.yml`.
- **Mana** avec régénération par classe, réduite en combat.
- **Statistiques** : PV, mana, régénération, multiplicateurs de dégâts (mêlée / distance / magie), dégâts subis, vitesse, armure, vitesse d'attaque, coups critiques — avec progression par niveau.
- **Défauts** : vulnérabilités (feu, chute, magie…), armures/boucliers interdits, armes pénalisées, faiblesse dans l'eau ou au soleil, faim accrue, soins réduits, pas de mana dans le noir.
- **Niveaux et XP** gagnés en tuant des monstres (barème par créature configurable).
- **Menus** : choix de classe, sous-classes avec progression des conditions, liste des compétences.
- **Stockage** au choix : `YAML`, `JSON` ou `SQLITE`.
- **PlaceholderAPI** (optionnel) et **API Java** pour les autres plugins.

## Lancer une compétence

1. Appuie sur **F** (échange de main) pour passer en *mode compétences*.
2. Appuie sur **1 à 9** pour lancer la compétence correspondante (l'objet tenu ne change pas).
3. **F** à nouveau (ou 8 s d'inactivité) pour quitter.

Alternatives : `/competence <id|numéro>` (pratique pour les macros) ou clic dans `/classe competences`.

## Commandes

| Commande | Description |
|---|---|
| `/classe` | Menu des classes |
| `/classe info [joueur]` | Profil (classe, niveau, mana, stats) |
| `/classe choisir <classe>` | Choisir sa classe |
| `/classe sous-classe [id]` | Menu / choix de sous-classe |
| `/classe competences` | Liste des compétences |
| `/classe reset` | Réinitialiser sa classe (délai configurable) |
| `/competence <id\|n°>` | Lancer une compétence |
| `/classe admin setlevel\|addxp\|setclass\|reset\|mana <joueur> …` | Administration |
| `/classe admin reload` | Recharger la configuration |

Permissions : `cykclasses.use`, `cykclasses.reset` (tout le monde), `cykclasses.admin` (op).

## Placeholders

`%cykclasses_class%`, `%cykclasses_subclass%`, `%cykclasses_display%`, `%cykclasses_class_id%`, `%cykclasses_level%`, `%cykclasses_xp%`, `%cykclasses_xp_required%`, `%cykclasses_xp_percent%`, `%cykclasses_mana%`, `%cykclasses_max_mana%`

## Configuration

- `config.yml` — stockage, courbe d'XP, XP par monstre, mana, mode compétences, HUD, reset.
- `classes.yml` — classes, sous-classes, stats, défauts, conditions et compétences (le haut du fichier documente tous les types disponibles).
- `messages.yml` — tous les textes au format [MiniMessage](https://docs.advntr.dev/minimessage/format.html).

## API

```java
RpgClass rc = CykClassesAPI.getClass(player);
CykClassesAPI.addXp(player, 50);

// Ajouter son propre type de compétence, utilisable ensuite dans classes.yml (type: MON_SORT)
CykClassesAPI.registerSkillType("MON_SORT", ctx -> {
    ctx.player().sendMessage("Abracadabra !");
    return true; // true = coût et recharge appliqués
});
```

Événements : `PlayerClassChangeEvent` (annulable), `PlayerClassLevelUpEvent`, `SkillCastEvent` (annulable, coût en mana modifiable).

## Compilation

Java 25 requis.

```bash
./gradlew build
# -> build/libs/CykClasses-<version>.jar
```

## Releases

Chaque push sur `main` publie automatiquement une release GitHub avec le `.jar` et le changelog généré à partir des messages de commit ([Conventional Commits](https://www.conventionalcommits.org/fr/)) :
`feat:` → version mineure, `fix:` → correctif, `feat!:` → version majeure. Ajouter `[skip release]` au message pour ne pas publier.

## Crédits

Architecture inspirée de plugins RPG open source comme [Fabled (ex-ProSkillAPI)](https://github.com/magemonkeystudio/fabled) et [MMOCore](https://gitlab.com/phoenix-dvpmt/mmocore). Code original.

## Licence

[GPL-3.0](LICENSE)
