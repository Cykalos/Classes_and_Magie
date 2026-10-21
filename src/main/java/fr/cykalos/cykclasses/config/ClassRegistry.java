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
 * Charge les classes et sous-classes depuis classes.yml.
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
            plugin.getLogger().severe("classes.yml ne contient aucune section 'classes' !");
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
                    plugin.getLogger().warning("Identifiant de sous-classe en double : " + sub.id);
                }
                allClasses.put(sub.id, sub);
            }
        }
        // Vérifie que chaque type de compétence existe.
        for (RpgClass rc : allClasses.values()) {
            for (SkillDefinition def : rc.ownSkills().values()) {
                if (!plugin.skills().hasType(def.type)) {
                    plugin.getLogger().warning("Compétence '" + def.id + "' (" + rc.id + ") : type inconnu " + def.type);
                }
            }
        }
    }

    public RpgClass get(String id) {
        return id == null ? null : allClasses.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<RpgClass> baseClasses() {
        return baseClasses.values();
    }

    public Collection<RpgClass> all() {
        return allClasses.values();
    }

    /** La classe effective du joueur : sa sous-classe si elle existe, sinon sa classe de base. */
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
