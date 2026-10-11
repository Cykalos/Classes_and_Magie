# Groupes et chat

## Créer un groupe

`/groupe` ouvre le menu de création, où le chef fixe les **règles** (elles ne changent plus ensuite) :

| Règle | Choix |
|---|---|
| **XP** | *Partagée* : divisée entre les membres proches, +10 % par membre · *Égale* : chacun reçoit l'XP complète |
| **Dégâts alliés** | Activés : les coups et compétences offensives (Charge…) touchent aussi les alliés · Désactivés |

Les soins et bonus ne touchent **que les membres du groupe** (ou soi-même sans groupe), quelle que soit la règle.

## Commandes

`/groupe inviter|accepter|refuser|quitter|expulser|chef|dissoudre|info`

```yaml
party:
  max-size: 5
  invite-timeout-seconds: 60
  members-can-invite: false
  disband-when-alone: true
  xp-share-range: 40
  shared-bonus-per-member: 0.1
  default-xp-mode: SHARED      # valeur proposée dans le menu
  default-friendly-fire: true
```

Un **panneau latéral** affiche chaque membre : classe, niveau, PV, mana (`hud.yml → party-board`).

## Chat

| Commande | Effet |
|---|---|
| `/gc` | Bascule le chat du joueur sur le groupe |
| `/global` | Revient au chat public |
| `/gc <message>` · `/global <message>` | Un seul message, sans changer de mode |

La classe et le niveau s'affichent devant le nom :

```yaml
chat:
  format-enabled: true
  global-format: "<class_tag> <white><name></white><dark_gray> » </dark_gray><gray><message>"
  party-format: "<dark_aqua>[Groupe]</dark_aqua> <class_tag> <aqua><name></aqua><dark_gray> » </dark_gray><white><message>"
  class-tag: "<dark_gray>[</dark_gray><class> <gray>Niv. <level></gray><dark_gray>]</dark_gray>"
```

Si un autre plugin gère le chat (EssentialsChat, LPC…), passer `format-enabled: false` et utiliser les placeholders `%cykclasses_class%`, `%cykclasses_level%`, `%cykclasses_display%`.

Permission `cykclasses.chat.spy` : voir les chats de groupe.

## Ce qu'on ne peut pas faire

- Pas de modification des règles après la création (dissoudre et recréer).
- Un joueur n'appartient qu'à un groupe à la fois.
- Pas de groupes persistants après un redémarrage du serveur.
