package fr.cykalos.cykclasses.model;

import fr.cykalos.cykclasses.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Compétence telle que déclarée dans classes.yml.
 * Le {@code type} pointe vers l'implémentation Java ; les {@code params} règlent ses valeurs.
 */
public final class SkillDefinition {

    public final String id;
    public final String type;
    public final String name;
    public final List<String> description;
    public final Material icon;
    public final double manaCost;
    public final double cooldown;
    public final int unlockLevel;
    /** Coût en PV (en demi-cœurs) au lieu ou en plus du mana. */
    public final double healthCost;
    private final Map<String, Object> params;

    public SkillDefinition(String id, String type, String name, List<String> description, Material icon,
                           double manaCost, double cooldown, int unlockLevel, double healthCost,
                           Map<String, Object> params) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.unlockLevel = unlockLevel;
        this.healthCost = healthCost;
        this.params = params;
    }

    public static SkillDefinition load(String id, ConfigurationSection s) {
        String type = s.getString("type", id).toUpperCase(Locale.ROOT);
        Map<String, Object> params = new HashMap<>();
        ConfigurationSection p = s.getConfigurationSection("params");
        if (p != null) {
            params.putAll(p.getValues(false));
        }
        return new SkillDefinition(
                id.toLowerCase(Locale.ROOT),
                type,
                s.getString("name", id),
                s.getStringList("description"),
                ItemBuilder.material(s.getString("icon"), Material.BLAZE_POWDER),
                s.getDouble("mana", 0),
                s.getDouble("cooldown", 5),
                s.getInt("unlock-level", 1),
                s.getDouble("health-cost", 0),
                params
        );
    }

    public double d(String key, double def) {
        Object o = params.get(key);
        return o instanceof Number n ? n.doubleValue() : def;
    }

    public int i(String key, int def) {
        Object o = params.get(key);
        return o instanceof Number n ? n.intValue() : def;
    }

    public boolean b(String key, boolean def) {
        Object o = params.get(key);
        return o instanceof Boolean bool ? bool : def;
    }

    public Map<String, Object> params() {
        return Collections.unmodifiableMap(params);
    }
}
