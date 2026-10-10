package fr.cykalos.cykclasses.content;

import org.bukkit.Material;
import fr.cykalos.cykclasses.model.StatModifier;

import java.util.List;

/** Métier : un seul par joueur. Certaines actions sont réservées à certains métiers (voir {@link JobRule}). */
public record Job(String id, String name, Material icon, List<String> description, StatModifier modifiers,
                  List<PermanentEffect> effects, List<XpSource> xpSources) {

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
