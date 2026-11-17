package fr.cykalos.cykclasses.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

/**
 * Language of the plugin ({@code language} in config.yml): "en" (default) or "fr".
 * <p>
 * The language picks the default files written on first start (resources under
 * {@code lang/<code>/} in the jar) and the few texts that are not in messages.yml.
 * Files already on disk are never replaced: switching language on an existing server only
 * affects files that do not exist yet.
 * <p>
 * Installs made before the language setting existed have a config.yml without
 * {@code language}: they were French, so {@code language: fr} is added to keep them as they are.
 * <p>
 * The folder layout (French or English folder names) is chosen once, on the first start,
 * and never changes afterwards, so that data is never "lost" by switching language.
 */
public final class Lang {

    public static final String[] SUPPORTED = {"en", "fr"};

    private static String code = "en";
    private static boolean frenchLayout;

    private Lang() {
    }

    /** Called in onLoad, before any file is read or moved. */
    public static void init(JavaPlugin plugin) {
        File root = plugin.getDataFolder();
        File configFile = new File(root, "config.yml");
        String detected = "en";
        if (configFile.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile);
            String value = yaml.getString("language");
            if (value == null || value.isBlank()) {
                detected = "fr";
                appendLanguage(plugin, configFile);
            } else {
                detected = value.trim().toLowerCase(Locale.ROOT);
            }
        }
        Boolean layout = existingLayout(root);
        if (!configFile.exists() && Boolean.TRUE.equals(layout)) {
            detected = "fr"; // old install whose config.yml was deleted: keep it French
        }
        code = normalise(plugin, detected);
        frenchLayout = layout != null ? layout : "fr".equals(code);
    }

    /** Re-reads the language after /classe reload (the folder layout never changes). */
    public static void reload(JavaPlugin plugin, String value) {
        if (value != null && !value.isBlank()) {
            code = normalise(plugin, value.trim().toLowerCase(Locale.ROOT));
        }
    }

    private static String normalise(JavaPlugin plugin, String value) {
        for (String s : SUPPORTED) {
            if (s.equals(value)) {
                return s;
            }
        }
        plugin.getLogger().warning("Unknown language '" + value + "' in config.yml (supported: en, fr): using en.");
        return "en";
    }

    /** @return true = French folders found, false = English folders found, null = nothing yet (fresh install) */
    private static Boolean existingLayout(File root) {
        for (String fr : new String[]{"donnees", "personnage", "metiers", "objets", "affichage", "quetes", "playerdata"}) {
            if (new File(root, fr).exists()) {
                return true;
            }
        }
        for (String en : new String[]{"data", "character", "jobs", "items", "display", "quests"}) {
            if (new File(root, en).exists()) {
                return false;
            }
        }
        // Very old installs: every file at the root
        if (new File(root, "classes.yml").exists() || new File(root, "messages.yml").exists()) {
            return true;
        }
        return null;
    }

    private static void appendLanguage(JavaPlugin plugin, File configFile) {
        try {
            String text = Files.readString(configFile.toPath(), StandardCharsets.UTF_8);
            String add = (text.endsWith("\n") || text.isEmpty() ? "" : "\n")
                    + "\n# Plugin language: en or fr. Only files that do not exist yet are created in this language.\n"
                    + "language: fr\n";
            Files.writeString(configFile.toPath(), add, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            plugin.getLogger().info("Existing install detected: 'language: fr' added to config.yml (set it to en for English).");
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not add 'language: fr' to config.yml: " + ex.getMessage());
        }
    }

    /** "en" or "fr". */
    public static String code() {
        return code;
    }

    public static boolean french() {
        return "fr".equals(code);
    }

    /** True when the folders use their French names (installs made before 1.12 or in French). */
    public static boolean frenchLayout() {
        return frenchLayout;
    }

    /** Picks the text matching the language. */
    public static String tr(String fr, String en) {
        return french() ? fr : en;
    }

    /** Path of a default file in the jar, e.g. "classes.yml" → "lang/en/classes.yml". */
    public static String resource(String name) {
        return "lang/" + code + "/" + name;
    }

    public static InputStream open(JavaPlugin plugin, String name) {
        InputStream in = plugin.getResource(resource(name));
        if (in == null && !"en".equals(code)) {
            in = plugin.getResource("lang/en/" + name);
        }
        return in;
    }

    /** Writes the default version of a file to {@code target} if it does not exist. */
    public static boolean saveDefault(JavaPlugin plugin, String name, File target) {
        if (target.exists()) {
            return false;
        }
        try (InputStream in = open(plugin, name)) {
            if (in == null) {
                return false;
            }
            Files.createDirectories(target.getParentFile().toPath());
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not create " + target.getPath() + ": " + ex.getMessage());
            return false;
        }
    }
}
