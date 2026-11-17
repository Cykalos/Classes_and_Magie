package fr.cykalos.cykclasses.content;

import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.StatModifier;

import java.util.List;

/**
 * Randomly rolled character trait.
 */
public record Trait(String id, String name, List<String> description, Rarity rarity, Kind kind, double weight,
                    StatModifier modifiers, List<Flaw> flaws, List<PermanentEffect> effects, List<String> conflicts) {

    public enum Kind { POSITIVE, NEGATIVE, MIXED }

    public enum Rarity {
        COMMON, UNCOMMON, RARE, EPIC, UNIQUE;

        /** Display name from messages.yml → labels.rarity.*. */
        public String label() {
            return fr.cykalos.cykclasses.config.Messages.text("labels.rarity." + name().toLowerCase(java.util.Locale.ROOT));
        }
    }

    public boolean unique() {
        return rarity == Rarity.UNIQUE;
    }
}
