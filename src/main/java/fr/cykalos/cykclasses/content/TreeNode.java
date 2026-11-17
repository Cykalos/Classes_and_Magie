package fr.cykalos.cykclasses.content;

import fr.cykalos.cykclasses.model.StatModifier;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

/**
 * Skill tree node. Each rank costs {@code cost} points and gives {@code modifiers}
 * and {@code skillMods} (stacked per rank).
 */
public record TreeNode(String id, String name, Material icon, List<String> description, int x, int y, int maxRank,
                       int cost, List<String> requires, int requiredLevel, String requiredSubclass,
                       StatModifier modifiers, Map<String, SkillMod> skillMods) {
}
