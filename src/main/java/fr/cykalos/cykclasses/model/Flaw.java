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
 * Un défaut (classe, race, trait, faiblesse…). Tous les types acceptent un bloc « conditions »
 * (voir {@link FlawCondition}) : le défaut ne s'applique que lorsque les conditions sont remplies.
 */
public final class Flaw {

    public enum Type {
        /** Dégâts reçus multipliés (causes : FIRE, FALL, MAGIC… ; damagers : ZOMBIE, *SKELETON, PLAYER, mythic:Nom). */
        DAMAGE_TAKEN,
        /** Dégâts infligés multipliés (targets : types d'entités ; kinds : MELEE, RANGED, SKILL). */
        DAMAGE_DEALT,
        /** Objets interdits dans les slots d'armure / main secondaire : ils sont retirés. */
        FORBIDDEN_ARMOR,
        /** Armes pénalisées : dégâts multipliés quand l'objet est tenu. */
        WEAPON_PENALTY,
        /** Affaibli et ralenti dans l'eau. */
        WATER_WEAKNESS,
        /** Affaibli en plein soleil. */
        SUNLIGHT_WEAKNESS,
        /** Faim plus rapide (multiplicateur d'épuisement). */
        HUNGER,
        /** Soins reçus réduits (multiplicateur). */
        HEALING_REDUCTION,
        /** Pas de régénération de mana dans l'obscurité. */
        DARKNESS_MANA_BLOCK,
        /** Régénération de mana multipliée (0 = bloquée). */
        MANA_REGEN,
        /** Effets de potion appliqués tant que les conditions sont remplies (effects: ["slowness:1"]). */
        EFFECT,
        /** Dégâts réguliers tant que les conditions sont remplies (damage, interval en secondes, fire-ticks). */
        PERIODIC_DAMAGE
    }

    public enum Kind { MELEE, RANGED, SKILL }

    public final Type type;
    public final String description;
    public final double multiplier;
    public final List<String> causes;
    public final List<Pattern> materials;
    /** DAMAGE_TAKEN : qui inflige les dégâts. */
    public final List<Pattern> damagers;
    /** DAMAGE_DEALT : qui reçoit les dégâts. */
    public final List<Pattern> targets;
    /** DAMAGE_DEALT : type d'attaque. */
    public final List<Kind> kinds;
    /** EFFECT : effets appliqués. */
    public final List<PermanentEffect> effects;
    /** PERIODIC_DAMAGE : dégâts par déclenchement (demi-cœurs). */
    public final double damage;
    /** PERIODIC_DAMAGE : intervalle en secondes. */
    public final double interval;
    /** PERIODIC_DAMAGE : ticks de feu ajoutés. */
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

    /** Les conditions sont-elles remplies pour ce joueur ? */
    public boolean active(Player player) {
        return condition.test(player);
    }

    public boolean matches(Material material) {
        return material != null && matchesName(materials, material.name());
    }

    public boolean matchesCause(String cause) {
        return causes.isEmpty() || causes.contains(cause.toUpperCase(Locale.ROOT));
    }

    /** Vide = tous ; sinon le nom d'entité (ZOMBIE) ou « MYTHIC:Nom » doit correspondre. */
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
