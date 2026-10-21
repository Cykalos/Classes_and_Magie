# Limites

🇫🇷 Français · [🇬🇧 English](EN-Limits.md)

Ce que Classes and Magie **ne fait pas** (ou pas encore), pour éviter les mauvaises surprises.

## Personnage

- Une seule classe et une seule sous-classe ; pas de multi-classe ni de sous-sous-classe.
- 8 compétences maximum dans la barre.
- Pas de compétences passives déclenchées automatiquement : passer par les stats, l'arbre ou MythicMobs.
- Les traits et faiblesses ne se choisissent pas, ils se relancent (nombre limité).
- Les types de faiblesses et de conditions sont prédéfinis (une douzaine de types, une vingtaine de conditions), mais se combinent librement.

## Métiers et artisanat

- Un seul métier actif et une seule spécialisation par métier.
- Restrictions et forge sur les matériaux **vanilla** uniquement.
- Une fois forgé, un objet garde sa rareté et ses stats ; seules les gemmes s'ajoutent.
- Une gemme incrustée est définitive ; pas de fusion ni de retaille.
- Un objet unique détruit (lave, cactus…) n'est pas libéré automatiquement : `/classe admin libererunique <id>`.

## Quêtes

- Pas de PNJ intégré ni de dialogues à choix multiples (un PNJ d'un autre plugin peut servir de cible).
- Une trentaine de types d'objectifs prédéfinis, combinables avec les conditions.
- Pas encore de titres de joueur.

## Objets

- Les objets interdits restent visibles (pas de masquage).
- Le plugin ne crée pas d'objets custom : il référence ceux de CraftEngine, MythicCrucible, ExecutableItems…

## Technique

- Paper 26.x uniquement (pas Spigot, pas Folia).
- Le panneau latéral (groupe et quêtes) utilise le scoreboard et peut entrer en conflit avec un autre plugin de scoreboard.
- Les groupes ne survivent pas à un redémarrage.
- Les nouvelles mécaniques de compétence demandent du Java ou MythicMobs.
