package fr.cykalos.cykclasses.config;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Fills a configuration file with the keys added in a new version of the plugin.
 * <p>
 * Bukkit ignores default values when calling {@code getString(key, default)}: keys missing from
 * the server's file are therefore copied in memory (the file on disk is not changed).
 * Sections listed in {@code userMaps} (gem lists, unique items…) belong to the
 * administrator: they are only filled when they are entirely missing, so that an entry
 * removed on purpose does not come back.
 */
public final class ConfigDefaults {

    private ConfigDefaults() {
    }

    /** @return the number of keys added */
    /** @param name logical file name (e.g. "messages.yml"); the default comes from the plugin language. */
    public static int fill(JavaPlugin plugin, YamlConfiguration target, String name, Set<String> userMaps) {
        InputStream stream = Lang.open(plugin, name);
        if (stream == null) {
            return 0;
        }
        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        target.setDefaults(defaults);
        return fill(target, defaults, userMaps);
    }

    public static int fill(Configuration target, Configuration defaults, Set<String> userMaps) {
        int added = 0;
        for (String key : defaults.getKeys(true)) {
            if (defaults.isConfigurationSection(key) || target.contains(key, true)) {
                continue;
            }
            String owner = owner(key, userMaps);
            if (owner != null && target.contains(owner, true)) {
                continue;
            }
            target.set(key, defaults.get(key));
            added++;
        }
        return added;
    }

    private static String owner(String key, Set<String> userMaps) {
        String best = null;
        for (String map : userMaps) {
            if (key.startsWith(map + ".") && (best == null || map.length() > best.length())) {
                best = map;
            }
        }
        return best;
    }
}
