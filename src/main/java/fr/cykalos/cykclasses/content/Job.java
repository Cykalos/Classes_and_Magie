package fr.cykalos.cykclasses.content;

import org.bukkit.Material;
import fr.cykalos.cykclasses.model.StatModifier;

import java.util.List;

/** Job: only one per player. Some actions are reserved to some jobs (see {@link JobRule}). */
public record Job(String id, String name, Material icon, List<String> description, StatModifier modifiers,
                  List<PermanentEffect> effects, List<XpSource> xpSources, java.util.Map<String, Specialization> specializations) {

    /**
     * Specialization (sub-job) chosen from a given job level.
     */
    public record Specialization(String id, String name, Material icon, List<String> description, int minLevel,
                                 StatModifier modifiers, List<PermanentEffect> effects, List<Bonus> bonuses) {
    }

    /**
     * Bonus of a specialization on some targets (crafted materials, or gem ids).
     * rarityLevels: job levels added to the rarity roll · power: +X% stat strength
     * xpMultiplier: job XP multiplier · purity: purity points added (gems)
     * gemChance: chance added to find a gem while mining
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

    /** Sum of the bonuses that apply to a target. */
    public record BonusTotal(int rarityLevels, double power, double xpMultiplier, double purity, double gemChance) {
        public static final BonusTotal NONE = new BonusTotal(0, 0, 1, 0, 0);
    }

    /** Job XP source: an action on some targets gives {@code xp}. */
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
