package fr.cykalos.cykclasses.model;

import fr.cykalos.cykclasses.content.PermanentEffect;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A flaw (class, race, trait, weakness…). Every type accepts a "conditions" block
 * (see {@link FlawCondition}): the flaw only applies when the conditions are met.
 */
public final class Flaw {

    public enum Type {
        /** Damage taken multiplied (causes: FIRE, FALL, MAGIC…; damagers: ZOMBIE, *SKELETON, PLAYER, mythic:Name). */
        DAMAGE_TAKEN,
        /** Damage dealt multiplied (targets: entity types; kinds: MELEE, RANGED, SKILL). */
        DAMAGE_DEALT,
        /** Items forbidden in armour / off-hand slots: they are removed. */
        FORBIDDEN_ARMOR,
        /** Penalised weapons: damage multiplied while the item is held. */
        WEAPON_PENALTY,
        /** Weakened and slowed in water. */
        WATER_WEAKNESS,
        /** Weakened in full sunlight. */
        SUNLIGHT_WEAKNESS,
        /** Faster hunger (exhaustion multiplier). */
        HUNGER,
        /** Reduced healing received (multiplier). */
        HEALING_REDUCTION,
        /** No mana regeneration in the dark. */
        DARKNESS_MANA_BLOCK,
        /** Mana regeneration multiplied (0 = blocked). */
        MANA_REGEN,
        /** Potion effects applied while the conditions are met (effects: ["slowness:1"]). */
        EFFECT,
        /** Periodic damage while the conditions are met (damage, interval in seconds, fire-ticks). */
        PERIODIC_DAMAGE
    }

    public enum Kind { MELEE, RANGED, SKILL }

    public final Type type;
    public final String description;
    public final double multiplier;
    public final List<String> causes;
    public final List<Pattern> materials;
    /** DAMAGE_TAKEN: who deals the damage. */
    public final List<Pattern> damagers;
    /** DAMAGE_DEALT: who receives the damage. */
    public final List<Pattern> targets;
    /** DAMAGE_DEALT: attack kind. */
    public final List<Kind> kinds;
    /** EFFECT: applied effects. */
    public final List<PermanentEffect> effects;
    /** PERIODIC_DAMAGE: damage per trigger (half-hearts). */
    public final double damage;
    /** PERIODIC_DAMAGE: interval in seconds. */
    public final double interval;
    /** PERIODIC_DAMAGE: fire ticks added. */
    public final int fireTicks;
    public final FlawCondition condition;

    private Flaw(Type type, String description, double multiplier, List<String> causes, List<Pattern> materials,
                 List<Pattern> damagers, List<Pattern> targets, List<Kind> kinds, List<PermanentEffect> effects,
                 double damage, double interval, int fireTicks, FlawCondition condition) {
        this.type = type;
        this.description = description;
        this.multiplier = multiplier;
        this.causes = causes;
        this.materials = materials;
        this.damagers = damagers;
        this.targets = targets;
        this.kinds = kinds;
        this.effects = effects;
        this.damage = damage;
        this.interval = interval;
        this.fireTicks = fireTicks;
        this.condition = condition;
    }

    public static Flaw load(Map<?, ?> raw) {
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
        String description = raw.containsKey("description") ? String.valueOf(raw.get("description")) : type.name();
        double multiplier = raw.get("multiplier") instanceof Number n ? n.doubleValue() : 1.0;
        List<String> causes = new ArrayList<>();
        FlawCondition.strings(raw.get("causes")).forEach(s -> causes.add(s.toUpperCase(Locale.ROOT)));
        List<Kind> kinds = new ArrayList<>();
        for (String s : FlawCondition.strings(raw.get("kinds"))) {
            try {
                kinds.add(Kind.valueOf(s.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        double damage = raw.get("damage") instanceof Number n ? n.doubleValue() : 1.0;
        double interval = raw.get("interval") instanceof Number n ? Math.max(1, n.doubleValue()) : 2.0;
        int fireTicks = raw.get("fire-ticks") instanceof Number n ? n.intValue() : 0;
        return new Flaw(type, description, multiplier, causes,
                FlawCondition.patterns(raw.get("materials")),
                FlawCondition.patterns(raw.get("damagers")),
                FlawCondition.patterns(raw.get("targets")),
                kinds,
                PermanentEffect.parse(FlawCondition.strings(raw.get("effects"))),
                damage, interval, fireTicks,
                FlawCondition.load(raw.get("conditions")));
    }

    public static Flaw load(ConfigurationSection section) {
        return section == null ? null : load(section.getValues(false));
    }

    /** Are the conditions met for this player? */
    public boolean active(Player player) {
        return condition.test(player);
    }

    public boolean matches(Material material) {
        return material != null && matchesName(materials, material.name());
    }

    public boolean matchesCause(String cause) {
        return causes.isEmpty() || causes.contains(cause.toUpperCase(Locale.ROOT));
    }

    /** Empty = all; otherwise the entity name (ZOMBIE) or "MYTHIC:Name" must match. */
    public static boolean matchesName(List<Pattern> patterns, String... names) {
        if (patterns.isEmpty()) {
            return true;
        }
        for (String name : names) {
            if (name == null) {
                continue;
            }
            String upper = name.toUpperCase(Locale.ROOT);
            for (Pattern p : patterns) {
                if (p.matcher(upper).matches()) {
                    return true;
                }
            }
        }
        return false;
    }
}
