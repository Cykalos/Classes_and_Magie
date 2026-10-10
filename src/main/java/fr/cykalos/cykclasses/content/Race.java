package fr.cykalos.cykclasses.content;

import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.StatModifier;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

/**
 * Race : bonus/malus permanents, défauts, et pénalités si la race ne convient pas à la classe.
 */
public record Race(String id, String name, Material icon, List<String> description, StatModifier modifiers,
                   List<Flaw> flaws, List<PermanentEffect> effects, List<String> recommended,
                   Map<String, Affinity> penalties) {

    /** Pénalité appliquée quand la race joue une classe (ou sous-classe) pour laquelle elle n'est pas faite. */
    public record Affinity(String description, StatModifier modifiers, List<Flaw> flaws) {
    }

    /** Pénalité pour la classe effective : la sous-classe a priorité, sinon la classe de base. */
    public Affinity penaltyFor(String classId, String subclassId) {
        if (subclassId != null && penalties.containsKey(subclassId)) {
            return penalties.get(subclassId);
        }
        return classId == null ? null : penalties.get(classId);
    }
}
