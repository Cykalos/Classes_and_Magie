package fr.cykalos.cykclasses.content;

import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.StatModifier;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

/**
 * Weakness / vulnerability (weaknesses.yml): rolled randomly depending on race and class, or imposed.
 *
 * @param races          races that can receive it (empty = all)
 * @param classes        classes or subclasses that can receive it (empty = all)
 * @param excludeRaces   races that never receive it
 * @param excludeClasses classes or subclasses that never receive it
 * @param raceWeights    chance multiplier per race (0 = never)
 * @param classWeights   chance multiplier per class or subclass (0 = never)
 */
public record Weakness(String id, String name, Material icon, List<String> description, String severity, double weight,
                       List<String> races, List<String> classes, List<String> excludeRaces, List<String> excludeClasses,
                       Map<String, Double> raceWeights, Map<String, Double> classWeights,
                       StatModifier modifiers, List<Flaw> flaws, List<PermanentEffect> effects, List<String> conflicts) {

    /** Relative chance for this character (0 = impossible). */
    public double weightFor(String raceId, String classId, String subclassId) {
        if (!races.isEmpty() && (raceId == null || !races.contains(raceId))) {
            return 0;
        }
        if (!classes.isEmpty() && !classes.contains(classId) && (subclassId == null || !classes.contains(subclassId))) {
            return 0;
        }
        if (raceId != null && excludeRaces.contains(raceId)) {
            return 0;
        }
        if (excludeClasses.contains(classId) || (subclassId != null && excludeClasses.contains(subclassId))) {
            return 0;
        }
        double w = Math.max(0, weight);
        if (raceId != null) {
            w *= raceWeights.getOrDefault(raceId, 1.0);
        }
        w *= classWeights.getOrDefault(classId, 1.0);
        if (subclassId != null) {
            w *= classWeights.getOrDefault(subclassId, 1.0);
        }
        return w;
    }
}
