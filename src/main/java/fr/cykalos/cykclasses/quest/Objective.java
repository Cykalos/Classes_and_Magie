package fr.cykalos.cykclasses.quest;

import fr.cykalos.cykclasses.model.FlawCondition;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Objective of a quest stage.
 * <p>
 * "Event" objectives (counted on each action): KILL, BREAK, PLACE, CRAFT, SMELT, FISH, BREED, TAME,
 * ENCHANT, BREW, REFINE, CONSUME, SKILL, INTERACT_ENTITY, INTERACT_BLOCK, COMMAND, DAMAGE.
 * <br>
 * "State" objectives (checked every second): LEVEL, JOB_LEVEL, CLASS, SUBCLASS, RACE, JOB, SPECIALIZATION,
 * COLLECT, REACH, BIOME, CONDITION, TREE_POINTS, ATTRIBUTE_POINTS, QUEST, TITLES, KILLS_TOTAL, SKILL_USES_TOTAL,
 * COUNTER, STATISTIC, ADVANCEMENT, PLAYTIME (totals since the player first joined: they also count the past).
 */
public final class Objective {

    public enum Type {
        KILL, BREAK, PLACE, CRAFT, SMELT, FISH, BREED, TAME, ENCHANT, BREW, REFINE, CONSUME, SKILL,
        INTERACT_ENTITY, INTERACT_BLOCK, COMMAND, DAMAGE,
        LEVEL, JOB_LEVEL, CLASS, SUBCLASS, RACE, JOB, SPECIALIZATION, COLLECT, REACH, BIOME, CONDITION,
        TREE_POINTS, ATTRIBUTE_POINTS,
        QUEST, TITLES, KILLS_TOTAL, SKILL_USES_TOTAL, COUNTER, STATISTIC, ADVANCEMENT, PLAYTIME;

        public boolean isState() {
            return ordinal() >= LEVEL.ordinal();
        }
    }

    public final Type type;
    public final List<String> targets;
    private final List<Pattern> patterns;
    public final double amount;
    public final String display;
    /** Name (contained text) of an entity to target: INTERACT_ENTITY, KILL. */
    public final String entityName;
    /** Extra conditions: context of the action (KILL at night…) or a CONDITION objective. */
    public final FlawCondition conditions;
    /** COLLECT: remove the items from the inventory when the stage is completed. */
    public final boolean take;
    /** REACH: world, coordinates and radius (without coordinates: being in the world is enough). */
    public final String world;
    public final Double x, y, z;
    public final double radius;
    /** Required module (config.yml → modules): objective met automatically if the module is disabled. */
    public final String module;

    private Objective(Type type, List<String> targets, double amount, String display, String entityName,
                      FlawCondition conditions, boolean take, String world, Double x, Double y, Double z,
                      double radius, String module) {
        this.type = type;
        this.targets = targets;
        this.patterns = FlawConditionPatterns.of(targets);
        this.amount = amount;
        this.display = display;
        this.entityName = entityName;
        this.conditions = conditions;
        this.take = take;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.module = module;
    }

    public static Objective load(Map<?, ?> m, String where) {
        Type type;
        try {
            type = Type.valueOf(String.valueOf(m.get("type")).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(where + " : type d'objectif inconnu « " + m.get("type") + " »");
        }
        List<String> targets = new ArrayList<>();
        Object t = m.containsKey("targets") ? m.get("targets") : m.get("target");
        if (t instanceof List<?> l) {
            l.forEach(o -> targets.add(String.valueOf(o)));
        } else if (t != null) {
            targets.add(String.valueOf(t));
        }
        double amount = m.get("amount") instanceof Number n ? n.doubleValue() : 1;
        return new Objective(type, targets, Math.max(amount, 0), str(m, "display"), str(m, "name"),
                FlawCondition.load(m.get("conditions")), Boolean.TRUE.equals(m.get("take")),
                str(m, "world"), num(m, "x"), num(m, "y"), num(m, "z"),
                m.get("radius") instanceof Number n ? n.doubleValue() : 5, str(m, "module"));
    }

    public static Objective load(ConfigurationSection s, String where) {
        return s == null ? null : load(s.getValues(false), where);
    }

    /** True if the target matches (empty list = everything matches). Case-insensitive, * = wildcard. */
    public boolean matches(String target) {
        if (patterns.isEmpty()) {
            return true;
        }
        if (target == null) {
            return false;
        }
        String up = target.toUpperCase(Locale.ROOT);
        for (Pattern p : patterns) {
            if (p.matcher(up).matches()) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesAny(Iterable<String> candidates) {
        if (patterns.isEmpty()) {
            return true;
        }
        for (String c : candidates) {
            if (matches(c)) {
                return true;
            }
        }
        return false;
    }

    private static String str(Map<?, ?> m, String key) {
        return m.get(key) == null ? null : String.valueOf(m.get(key));
    }

    private static Double num(Map<?, ?> m, String key) {
        return m.get(key) instanceof Number n ? n.doubleValue() : null;
    }

    /** Compiles patterns such as "*_SWORD", "mythic:Boss"… into regular expressions. */
    static final class FlawConditionPatterns {
        static List<Pattern> of(List<String> raw) {
            List<Pattern> out = new ArrayList<>();
            for (String s : raw) {
                out.add(Pattern.compile(Pattern.quote(s.toUpperCase(Locale.ROOT)).replace("*", "\\E.*\\Q")));
            }
            return out;
        }
    }
}
