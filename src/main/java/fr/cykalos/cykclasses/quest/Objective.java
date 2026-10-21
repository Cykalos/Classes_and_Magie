package fr.cykalos.cykclasses.quest;

import fr.cykalos.cykclasses.model.FlawCondition;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Objectif d'une étape de quête.
 * <p>
 * Objectifs « événements » (comptés à chaque action) : KILL, BREAK, PLACE, CRAFT, SMELT, FISH, BREED, TAME,
 * ENCHANT, BREW, REFINE, CONSUME, SKILL, INTERACT_ENTITY, INTERACT_BLOCK, COMMAND, DAMAGE.
 * <br>
 * Objectifs « état » (vérifiés chaque seconde) : LEVEL, JOB_LEVEL, CLASS, SUBCLASS, RACE, JOB, SPECIALIZATION,
 * COLLECT, REACH, BIOME, CONDITION, TREE_POINTS, ATTRIBUTE_POINTS.
 */
public final class Objective {

    public enum Type {
        KILL, BREAK, PLACE, CRAFT, SMELT, FISH, BREED, TAME, ENCHANT, BREW, REFINE, CONSUME, SKILL,
        INTERACT_ENTITY, INTERACT_BLOCK, COMMAND, DAMAGE,
        LEVEL, JOB_LEVEL, CLASS, SUBCLASS, RACE, JOB, SPECIALIZATION, COLLECT, REACH, BIOME, CONDITION,
        TREE_POINTS, ATTRIBUTE_POINTS;

        public boolean isState() {
            return ordinal() >= LEVEL.ordinal();
        }
    }

    public final Type type;
    public final List<String> targets;
    private final List<Pattern> patterns;
    public final double amount;
    public final String display;
    /** Nom (texte contenu) d'une entité à cibler : INTERACT_ENTITY, KILL. */
    public final String entityName;
    /** Conditions supplémentaires : contexte de l'action (KILL de nuit…) ou objectif CONDITION. */
    public final FlawCondition conditions;
    /** COLLECT : retirer les objets de l'inventaire quand l'étape est terminée. */
    public final boolean take;
    /** REACH : monde, coordonnées et rayon (sans coordonnées : être dans le monde suffit). */
    public final String world;
    public final Double x, y, z;
    public final double radius;
    /** Module requis (config.yml → modules) : objectif validé d'office si le module est désactivé. */
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

    /** Vrai si la cible correspond (liste vide = tout correspond). Insensible à la casse, * = joker. */
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

    /** Compilation des motifs « *_SWORD », « mythic:Boss »… en expressions régulières. */
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
