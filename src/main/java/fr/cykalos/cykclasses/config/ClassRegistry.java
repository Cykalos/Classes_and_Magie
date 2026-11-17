package fr.cykalos.cykclasses.config;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.model.SkillDefinition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Loads classes and subclasses from classes.yml.
 */
public final class ClassRegistry {

    private final CykClasses plugin;
    private final Map<String, RpgClass> baseClasses = new LinkedHashMap<>();
    private final Map<String, RpgClass> allClasses = new LinkedHashMap<>();

    public ClassRegistry(CykClasses plugin) {
        this.plugin = plugin;
    }

    public void load() {
        baseClasses.clear();
        allClasses.clear();
        YamlConfiguration yaml = ConfigFiles.load(plugin, "classes.yml");
        ConfigurationSection root = yaml.getConfigurationSection("classes");
        if (root == null) {
            plugin.getLogger().severe("classes.yml has no 'classes' section!");
            return;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(key);
            if (s == null) {
                continue;
            }
            RpgClass rc = RpgClass.load(key, s, null, plugin.getLogger());
            baseClasses.put(rc.id, rc);
            allClasses.put(rc.id, rc);
            for (RpgClass sub : rc.subclasses().values()) {
                if (allClasses.containsKey(sub.id)) {
                    plugin.getLogger().warning("Duplicate subclass id: " + sub.id);
                }
                allClasses.put(sub.id, sub);
            }
        }
        // Checks that every skill type exists.
        for (RpgClass rc : allClasses.values()) {
            for (SkillDefinition def : rc.ownSkills().values()) {
                if (!plugin.skills().hasType(def.type)) {
                    plugin.getLogger().warning("Skill '" + def.id + "' (" + rc.id + "): unknown type " + def.type);
                }
            }
        }
    }

    public RpgClass get(String id) {
        return id == null ? null : allClasses.get(id.toLowerCase(Locale.ROOT));
    }

    /** Class by id or by displayed name ("guerrier" or "Warrior"), ignoring case and accents. */
    public RpgClass find(String input) {
        RpgClass rc = get(input);
        if (rc != null || input == null) {
            return rc;
        }
        String wanted = fold(input);
        for (RpgClass c : allClasses.values()) {
            if (fold(fr.cykalos.cykclasses.util.Text.plain(c.name)).equals(wanted)) {
                return c;
            }
        }
        return null;
    }

    private static String fold(String s) {
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('_', ' ').trim().toLowerCase(Locale.ROOT);
    }

    public Collection<RpgClass> baseClasses() {
        return baseClasses.values();
    }

    public Collection<RpgClass> all() {
        return allClasses.values();
    }

    /** The player's effective class: their subclass if they have one, otherwise their base class. */
    public RpgClass effective(PlayerProfile profile) {
        if (profile == null || !profile.hasClass()) {
            return null;
        }
        RpgClass sub = get(profile.getSubclassId());
        if (sub != null) {
            return sub;
        }
        return get(profile.getClassId());
    }

    public int skillCount() {
        return allClasses.values().stream().mapToInt(c -> c.ownSkills().size()).sum();
    }
}
