# Parties and chat

[🇫🇷 Français](Groupes-et-chat.md) · 🇬🇧 English

## Creating a party

`/groupe` (alias `/party`) opens the creation menu, where the leader sets the **rules** (they cannot be changed afterwards):

| Rule | Choices |
|---|---|
| **XP** | *Shared*: split between nearby members, +10% per member · *Equal*: everyone gets the full XP |
| **Friendly fire** | Enabled: hits and offensive skills (Charge…) also affect allies · Disabled |

Heals and buffs **only affect party members** (or yourself without a party), whatever the rule.

## Commands

`/groupe inviter|accepter|refuser|quitter|expulser|chef|dissoudre|info` (invite, accept, decline, leave, kick, leader, disband, info)

With `disband-when-alone: true` (default), a party with a single member left is disbanded: kicking the only other member therefore disbands the party.

```yaml
party:
  max-size: 5
  invite-timeout-seconds: 60
  members-can-invite: false
  disband-when-alone: true
  xp-share-range: 40
  shared-bonus-per-member: 0.1
  default-xp-mode: SHARED      # value suggested in the menu
  default-friendly-fire: true
```

A **side board** shows each member: class, level, health, mana (`hud.yml → party-board`).

## Chat

| Command | Effect |
|---|---|
| `/gc` | Switches the player's chat to the party |
| `/global` | Goes back to public chat |
| `/gc <message>` · `/global <message>` | A single message, without changing mode |

Class and level are shown before the name:

```yaml
chat:
  format-enabled: true
  global-format: "<class_tag> <white><name></white><dark_gray> » </dark_gray><gray><message>"
  party-format: "<dark_aqua>[Party]</dark_aqua> <class_tag> <aqua><name></aqua><dark_gray> » </dark_gray><white><message>"
  class-tag: "<dark_gray>[</dark_gray><class> <gray>Lv. <level></gray><dark_gray>]</dark_gray>"
```

If another plugin handles chat (EssentialsChat, LPC…), set `format-enabled: false` and use the `%cykclasses_class%`, `%cykclasses_level%`, `%cykclasses_display%` placeholders.

Permission `cykclasses.chat.spy`: see party chats.

## What cannot be done

- Rules cannot be changed after creation (disband and create again).
- A player belongs to one party at a time.
- Parties do not persist after a server restart.
