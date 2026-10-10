package fr.cykalos.cykclasses.content;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Action réservée à certains métiers.
 * Actions : BREAK, PLACE, CRAFT, INTERACT_BLOCK, FISH, BREED, TAME, SMELT, ENCHANT, BREW.
 * {@code minLevel} : niveau de métier requis (recette débloquée à ce niveau).
 */
public record JobRule(Action action, List<Pattern> targets, List<String> jobs, String message, int minLevel, String display) {

    public enum Action { BREAK, PLACE, CRAFT, INTERACT_BLOCK, FISH, BREED, TAME, SMELT, ENCHANT, BREW, REFINE }

    public boolean matches(Material material) {
        return material != null && matches(material.name());
    }

    public boolean matches(EntityType type) {
        return type != null && matches(type.name());
    }

    public boolean matches(String name) {
        if (targets.isEmpty()) {
            return true;
        }
        String upper = name.toUpperCase(Locale.ROOT);
        for (Pattern p : targets) {
            if (p.matcher(upper).matches()) {
                return true;
            }
        }
        return false;
    }
}
