package fr.cykalos.cykclasses.model;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Un défaut de classe, configurable dans classes.yml.
 */
public final class Flaw {

    public enum Type {
        /** Dégâts reçus multipliés pour certaines causes (FIRE, FALL, MAGIC...). */
        DAMAGE_TAKEN,
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
        /** Mana ne régénère pas la nuit / dans l'obscurité. */
        DARKNESS_MANA_BLOCK
    }

    public final Type type;
    public final String description;
    public final double multiplier;
    public final List<String> causes;
    public final List<Pattern> materials;

    private Flaw(Type type, String description, double multiplier, List<String> causes, List<Pattern> materials) {
        this.type = type;
        this.description = description;
        this.multiplier = multiplier;
        this.causes = causes;
        this.materials = materials;
    }

    @SuppressWarnings("unchecked")
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
        if (raw.get("causes") instanceof List<?> list) {
            for (Object o : list) {
                causes.add(String.valueOf(o).toUpperCase(Locale.ROOT));
            }
        }
        List<Pattern> materials = new ArrayList<>();
        if (raw.get("materials") instanceof List<?> list) {
            for (Object o : list) {
                materials.add(Pattern.compile(String.valueOf(o).toUpperCase(Locale.ROOT).replace("*", ".*")));
            }
        }
        return new Flaw(type, description, multiplier, causes, materials);
    }

    public static Flaw load(ConfigurationSection section) {
        return section == null ? null : load(section.getValues(false));
    }

    public boolean matches(Material material) {
        if (material == null) {
            return false;
        }
        String name = material.name();
        for (Pattern p : materials) {
            if (p.matcher(name).matches()) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesCause(String cause) {
        return causes.isEmpty() || causes.contains(cause.toUpperCase(Locale.ROOT));
    }
}
