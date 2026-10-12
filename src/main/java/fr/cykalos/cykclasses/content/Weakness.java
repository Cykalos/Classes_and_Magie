package fr.cykalos.cykclasses.content;

import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.StatModifier;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

/**
 * Faiblesse / vulnérabilité (weaknesses.yml) : tirée au hasard selon la race et la classe, ou imposée.
 *
 * @param races          races qui peuvent la recevoir (vide = toutes)
 * @param classes        classes ou sous-classes qui peuvent la recevoir (vide = toutes)
 * @param excludeRaces   races qui ne la reçoivent jamais
 * @param excludeClasses classes ou sous-classes qui ne la reçoivent jamais
 * @param raceWeights    multiplicateur de chance par race (0 = jamais)
 * @param classWeights   multiplicateur de chance par classe ou sous-classe (0 = jamais)
 */
public record Weakness(String id, String name, Material icon, List<String> description, String severity, double weight,
                       List<String> races, List<String> classes, List<String> excludeRaces, List<String> excludeClasses,
                       Map<String, Double> raceWeights, Map<String, Double> classWeights,
                       StatModifier modifiers, List<Flaw> flaws, List<PermanentEffect> effects, List<String> conflicts) {

    /** Chance relative pour ce personnage (0 = impossible). */
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
