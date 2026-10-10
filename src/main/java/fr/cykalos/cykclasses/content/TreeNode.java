package fr.cykalos.cykclasses.content;

import fr.cykalos.cykclasses.model.StatModifier;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

/**
 * Nœud d'arbre de compétences. Chaque rang coûte {@code cost} points et donne {@code modifiers}
 * et {@code skillMods} (cumulés par rang).
 */
public record TreeNode(String id, String name, Material icon, List<String> description, int x, int y, int maxRank,
                       int cost, List<String> requires, int requiredLevel, String requiredSubclass,
                       StatModifier modifiers, Map<String, SkillMod> skillMods) {
}
