package fr.cykalos.cykclasses.content;

import java.util.Map;

/** Arbre de compétences d'une classe (ou commun à toutes les classes : id « commun »). */
public record SkillTree(String classId, String title, Map<String, TreeNode> nodes) {
}
