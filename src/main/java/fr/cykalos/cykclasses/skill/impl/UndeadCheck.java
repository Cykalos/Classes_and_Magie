package fr.cykalos.cykclasses.skill.impl;

import org.bukkit.entity.Entity;

import java.util.Locale;
import java.util.Set;

/** Simple undead detection (independent of version tags). */
final class UndeadCheck {

    private static final Set<String> UNDEAD = Set.of(
            "zombie", "zombie_villager", "husk", "drowned", "skeleton", "stray", "wither_skeleton",
            "bogged", "phantom", "zombified_piglin", "zoglin", "wither", "skeleton_horse", "zombie_horse",
            "parched", "camel_husk");

    private UndeadCheck() {
    }

    static boolean isUndead(Entity entity) {
        return UNDEAD.contains(entity.getType().getKey().getKey().toLowerCase(Locale.ROOT));
    }
}
