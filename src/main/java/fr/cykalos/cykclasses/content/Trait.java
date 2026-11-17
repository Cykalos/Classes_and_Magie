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
        COMMON("<white>Commun", "<white>Common"), UNCOMMON("<green>Peu commun", "<green>Uncommon"), RARE("<aqua>Rare", "<aqua>Rare"),
        EPIC("<light_purple>Épique", "<light_purple>Epic"), UNIQUE("<gold><bold>UNIQUE</bold>", "<gold><bold>UNIQUE</bold>");

        private final String fr;
        private final String en;

        Rarity(String fr, String en) {
            this.fr = fr;
            this.en = en;
        }

        /** Label in the plugin language. */
        public String label() {
            return fr.cykalos.cykclasses.config.Lang.tr(fr, en);
        }
    }

    public boolean unique() {
        return rarity == Rarity.UNIQUE;
    }
}
