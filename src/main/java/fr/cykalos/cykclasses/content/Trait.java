package fr.cykalos.cykclasses.content;

import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.StatModifier;

import java.util.List;

/**
 * Trait de personnage tiré au hasard.
 */
public record Trait(String id, String name, List<String> description, Rarity rarity, Kind kind, double weight,
                    StatModifier modifiers, List<Flaw> flaws, List<PermanentEffect> effects, List<String> conflicts) {

    public enum Kind { POSITIVE, NEGATIVE, MIXED }

    public enum Rarity {
        COMMON("<white>Commun"), UNCOMMON("<green>Peu commun"), RARE("<aqua>Rare"), EPIC("<light_purple>Épique"),
        UNIQUE("<gold><bold>UNIQUE</bold>");

        public final String label;

        Rarity(String label) {
            this.label = label;
        }
    }

    public boolean unique() {
        return rarity == Rarity.UNIQUE;
    }
}
