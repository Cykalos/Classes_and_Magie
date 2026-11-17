package fr.cykalos.cykclasses.content;

import java.util.Map;

/** Skill tree of a class (or shared by every class: id "commun"). */
public record SkillTree(String classId, String title, Map<String, TreeNode> nodes) {
}
