package fr.cykalos.cykclasses.content;

import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.StatModifier;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

/**
 * Race: permanent bonuses/penalties, weaknesses, and penalties when the race does not suit the class.
 */
public record Race(String id, String name, Material icon, List<String> description, StatModifier modifiers,
                   List<Flaw> flaws, List<PermanentEffect> effects, List<String> recommended,
                   Map<String, Affinity> penalties) {

    /** Penalty applied when the race plays a class (or subclass) it is not suited for. */
    public record Affinity(String description, StatModifier modifiers, List<Flaw> flaws) {
    }

    /** Penalty for the effective class: the subclass takes priority, otherwise the base class. */
    public Affinity penaltyFor(String classId, String subclassId) {
        if (subclassId != null && penalties.containsKey(subclassId)) {
            return penalties.get(subclassId);
        }
        return classId == null ? null : penalties.get(classId);
    }
}
