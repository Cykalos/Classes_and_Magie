package fr.cykalos.cykclasses.config;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Complète un fichier de configuration avec les clés ajoutées dans une nouvelle version du plugin.
 * <p>
 * Bukkit ignore les valeurs par défaut quand on appelle {@code getString(clé, défaut)} : les clés absentes
 * du fichier du serveur sont donc copiées en mémoire (le fichier sur le disque n'est pas modifié).
 * Les sections listées dans {@code userMaps} (listes de gemmes, d'objets uniques…) appartiennent à
 * l'administrateur : on ne les complète que si elles sont entièrement absentes, pour ne pas
 * faire réapparaître une entrée supprimée volontairement.
 */
public final class ConfigDefaults {

    private ConfigDefaults() {
    }

    /** @return le nombre de clés ajoutées */
    public static int fill(JavaPlugin plugin, YamlConfiguration target, String resource, Set<String> userMaps) {
        InputStream stream = plugin.getResource(resource);
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
