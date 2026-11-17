package fr.cykalos.cykclasses.model;

import fr.cykalos.cykclasses.data.PlayerProfile;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

/**
 * Condition to meet to unlock a subclass.
 *
 * <ul>
 *     <li>LEVEL: minimum level ({@code amount})</li>
 *     <li>KILLS: kill {@code amount} entities ({@code target} = entity type, {@code any} or {@code player})</li>
 *     <li>SKILL_USES: use a skill ({@code target} = skill id, or {@code any})</li>
 *     <li>COUNTER: internal counter (blocks_mined, damage_dealt, damage_taken, healing_done, mana_spent)</li>
 *     <li>PERMISSION: have the {@code target} permission</li>
 * </ul>
 */
public final class Requirement {

    public enum Type { LEVEL, KILLS, SKILL_USES, COUNTER, PERMISSION }

    public final Type type;
    public final String target;
    public final double amount;
    public final String description;

    public Requirement(Type type, String target, double amount, String description) {
        this.type = type;
        this.target = target;
        this.amount = amount;
        this.description = description;
    }

    public static Requirement load(Map<?, ?> raw) {
        Object t = raw.get("type");
        if (t == null) {
            return null;
        }
        Type type;
        try {
            type = Type.valueOf(String.valueOf(t).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
        String target = raw.containsKey("target") ? String.valueOf(raw.get("target")).toLowerCase(Locale.ROOT) : "any";
        double amount = raw.get("amount") instanceof Number n ? n.doubleValue() : 1;
        String description = raw.containsKey("description") ? String.valueOf(raw.get("description")) : defaultDescription(type, target, amount);
        return new Requirement(type, target, amount, description);
    }

    private static String L(String key) {
        return fr.cykalos.cykclasses.config.Messages.text(key);
    }

    private static String defaultDescription(Type type, String target, double amount) {
        long a = (long) amount;
        return switch (type) {
            case LEVEL -> L("labels.req-level").replace("<amount>", String.valueOf(a));
            case KILLS -> L("labels.req-kills").replace("<amount>", String.valueOf(a))
                    .replace("<target>", "any".equals(target) ? L("labels.any-creatures") : target);
            case SKILL_USES -> L("labels.req-skill-uses").replace("<amount>", String.valueOf(a))
                    .replace("<target>", "any".equals(target) ? L("labels.any-skills") : target);
            case COUNTER -> target + ": " + a;
            case PERMISSION -> L("labels.req-permission");
        };
    }

    public double progress(PlayerProfile profile, Player player) {
        return switch (type) {
            case LEVEL -> profile.getLevel();
            case KILLS -> "any".equals(target) ? profile.totalKills() : profile.getKills(target);
            case SKILL_USES -> "any".equals(target) ? profile.totalSkillUses() : profile.getSkillUses(target);
            case COUNTER -> profile.getCounter(target);
            case PERMISSION -> player != null && player.hasPermission(target) ? 1 : 0;
        };
    }

    public double goal() {
        return type == Type.PERMISSION ? 1 : amount;
    }

    public boolean isMet(PlayerProfile profile, Player player) {
        return progress(profile, player) >= goal();
    }
}
