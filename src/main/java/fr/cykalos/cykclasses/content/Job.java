package fr.cykalos.cykclasses.content;

import org.bukkit.Material;
import fr.cykalos.cykclasses.model.StatModifier;

import java.util.List;

/** Métier : un seul par joueur. Certaines actions sont réservées à certains métiers (voir {@link JobRule}). */
public record Job(String id, String name, Material icon, List<String> description, StatModifier modifiers,
                  List<PermanentEffect> effects, List<XpSource> xpSources, java.util.Map<String, Specialization> specializations) {

    /**
     * Spécialisation (sous-métier) choisie à partir d'un certain niveau de métier.
     */
    public record Specialization(String id, String name, Material icon, List<String> description, int minLevel,
                                 StatModifier modifiers, List<PermanentEffect> effects, List<Bonus> bonuses) {
    }

    /**
     * Bonus d'une spécialisation sur certaines cibles (matériaux fabriqués, ou identifiants de gemmes).
     * rarityLevels : niveaux de métier ajoutés pour le tirage de rareté · power : +X % de force des stats
     * xpMultiplier : multiplicateur d'XP de métier · purity : points de pureté ajoutés (gemmes)
     * gemChance : chance ajoutée de trouver une gemme en minant
     */
    public record Bonus(List<java.util.regex.Pattern> targets, int rarityLevels, double power, double xpMultiplier,
                        double purity, double gemChance) {

        public boolean matches(String name) {
            if (targets.isEmpty()) {
                return true;
            }
            String upper = name.toUpperCase(java.util.Locale.ROOT);
            for (var p : targets) {
                if (p.matcher(upper).matches()) {
                    return true;
                }
            }
            return false;
        }
    }

    /** Somme des bonus qui s'appliquent à une cible. */
    public record BonusTotal(int rarityLevels, double power, double xpMultiplier, double purity, double gemChance) {
        public static final BonusTotal NONE = new BonusTotal(0, 0, 1, 0, 0);
    }

    /** Source d'XP de métier : une action sur certaines cibles rapporte {@code xp}. */
    public record XpSource(JobRule.Action action, List<java.util.regex.Pattern> targets, double xp) {

        public boolean matches(String name) {
            if (targets.isEmpty()) {
                return true;
            }
            String upper = name.toUpperCase(java.util.Locale.ROOT);
            for (var p : targets) {
                if (p.matcher(upper).matches()) {
                    return true;
                }
            }
            return false;
        }
    }
}
