package fr.cykalos.cykclasses.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Language and location of each file in the plugin folder, organised by theme.
 * <p>
 * The language ({@code language} in config.yml: {@code en} or {@code fr}) decides the default files created
 * by the plugin (texts, classes, quests…) and the default value of every missing key. New installations
 * start in English; a server created before the language option keeps French.
 * <pre>
 *              English          French
 * config.yml
 * classes/     classes/         classes/       classes.yml, skilltree.yml, attributes.yml
 * character    character/       personnage/    races.yml, traits.yml, weaknesses.yml, titles.yml
 * jobs         jobs/            metiers/       jobs.yml, forge.yml, gems.yml
 * items        items/           objets/        kits.yml, items.yml
 * display      display/         affichage/     hud.yml, messages.yml
 * quests       quests/          quetes/        one or more quest files (every .yml file in the folder is read)
 * data         data/            donnees/       server and player data (do not edit by hand)
 * </pre>
 * A folder that already exists keeps its name whatever the language, so switching language never moves
 * or loses files. Files from older versions (all at the root) are moved automatically on startup.
 * <p>
 * Default files are stored in the jar under {@code lang/<language>/<file name>}.
 */
public final class ConfigFiles {

    public static final List<String> LANGUAGES = List.of("en", "fr");

    /** Logical folders: key → {English name, French name}. */
    private static final Map<String, String[]> FOLDERS = new LinkedHashMap<>();
    /** File name → logical folder. */
    private static final Map<String, String> FILES = new LinkedHashMap<>();

    static {
        folder("classes", "classes", "classes", "classes.yml", "skilltree.yml", "attributes.yml");
        folder("character", "character", "personnage", "races.yml", "traits.yml", "weaknesses.yml", "titles.yml");
        folder("jobs", "jobs", "metiers", "jobs.yml", "forge.yml", "gems.yml");
        folder("items", "items", "objets", "kits.yml", "items.yml");
        folder("display", "display", "affichage", "hud.yml", "messages.yml");
        folder("quests", "quests", "quetes");
        folder("data", "data", "donnees", "uniques.yml", "forge-uniques.yml", "titres-uniques.yml");
    }

    private static String language = "en";
    private static File root;
    private static final Map<String, String> resolved = new LinkedHashMap<>();

    private ConfigFiles() {
    }

    private static void folder(String key, String en, String fr, String... names) {
        FOLDERS.put(key, new String[]{en, fr});
        for (String n : names) {
            FILES.put(n, key);
        }
    }

    // ------------------------------------------------------------ Language

    /**
     * Reads the language from config.yml and resolves the folder names. A config.yml without a
     * {@code language} key comes from a version before the option: {@code language: fr} is added to it.
     * Called before anything else is loaded (onLoad and reload).
     */
    public static void init(JavaPlugin plugin) {
        root = plugin.getDataFolder();
        File config = new File(root, "config.yml");
        String lang = "en";
        if (config.exists()) {
            YamlConfiguration y = YamlConfiguration.loadConfiguration(config);
            if (y.contains("language")) {
                lang = y.getString("language", "en");
            } else {
                lang = "fr";
                addLanguageKey(plugin, config, "fr");
            }
        }
        language = LANGUAGES.contains(lang.toLowerCase(Locale.ROOT)) ? lang.toLowerCase(Locale.ROOT) : "en";
        resolved.clear();
        for (Map.Entry<String, String[]> e : FOLDERS.entrySet()) {
            String[] names = e.getValue();
            String name;
            if (new File(plugin.getDataFolder(), names[1]).isDirectory()) {
                name = names[1];
            } else if (new File(plugin.getDataFolder(), names[0]).isDirectory()) {
                name = names[0];
            } else {
                name = language.equals("fr") ? names[1] : names[0];
            }
            resolved.put(e.getKey(), name);
        }
    }

    /** Inserts "language: fr" at the top of an existing config.yml without rewriting the rest of the file. */
    private static void addLanguageKey(JavaPlugin plugin, File config, String lang) {
        try {
            String text = Files.readString(config.toPath(), StandardCharsets.UTF_8);
            String header = "# Langue des fichiers créés par le plugin et des textes par défaut : en (English) ou fr (Français)\n"
                    + "# Language of the files created by the plugin and of the default texts: en (English) or fr (French)\n"
                    + "language: " + lang + "\n\n";
            Files.writeString(config.toPath(), header + text, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not add the language option to config.yml: " + ex.getMessage());
        }
    }

    /** "en" or "fr". */
    public static String language() {
        return language;
    }

    public static boolean french() {
        return "fr".equals(language);
    }

    /** Path of a default file in the jar for the current language (e.g. "lang/en/messages.yml"). */
    public static String resource(String name) {
        return "lang/" + language + "/" + name;
    }

    // ------------------------------------------------------------ Paths

    /** Name of a logical folder on disk ("character" → "character" or "personnage"). */
    public static String folderName(String key) {
        String name = resolved.get(key);
        if (name != null) {
            return name;
        }
        String[] names = FOLDERS.get(key);
        return names == null ? key : names[french() ? 1 : 0];
    }

    /** Relative path of a file (e.g. "races.yml" → "character/races.yml"). */
    public static String path(String name) {
        String key = FILES.get(name);
        if (key == null) {
            return name;
        }
        if (name.equals("titres-uniques.yml")) {
            return folderName(key) + "/" + localized(root == null ? null : new File(root, folderName(key)),
                    "title-uniques.yml", "titres-uniques.yml");
        }
        return folderName(key) + "/" + name;
    }

    public static File file(JavaPlugin plugin, String name) {
        return new File(plugin.getDataFolder(), path(name));
    }

    public static File dataFolder(JavaPlugin plugin) {
        return new File(plugin.getDataFolder(), folderName("data"));
    }

    public static File questsFolder(JavaPlugin plugin) {
        return new File(plugin.getDataFolder(), folderName("quests"));
    }

    /** Player data folder: "players" (English) or "joueurs" (French); an existing folder keeps its name. */
    public static File playersFolder(File dataFolder) {
        return new File(dataFolder, localized(dataFolder, "players", "joueurs"));
    }

    /** Backups folder: "backups" (English) or "sauvegardes" (French); an existing folder keeps its name. */
    public static File backupsFolder(JavaPlugin plugin) {
        File data = dataFolder(plugin);
        return new File(data, localized(data, "backups", "sauvegardes"));
    }

    /** Name that already exists in {@code parent}, otherwise the name of the current language. */
    private static String localized(File parent, String en, String fr) {
        if (parent != null && new File(parent, fr).exists()) {
            return fr;
        }
        if (parent != null && new File(parent, en).exists()) {
            return en;
        }
        return french() ? fr : en;
    }

    // ------------------------------------------------------------ Loading

    /** Copies a default file of the current language from the jar, if the file does not exist yet. */
    public static boolean saveDefault(JavaPlugin plugin, String resource, File target) {
        if (target.exists()) {
            return false;
        }
        try (InputStream in = plugin.getResource(resource)) {
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

    /** Loads a configuration file, creating the default version (current language) if it does not exist. */
    public static YamlConfiguration load(JavaPlugin plugin, String name) {
        File file = file(plugin, name);
        saveDefault(plugin, resource(name), file);
        return YamlConfiguration.loadConfiguration(file);
    }

    /** Default quest files of the current language. */
    public static List<String> defaultQuestFiles() {
        return french()
                ? List.of("tutoriel.yml", "principale.yml", "de-classe.yml", "annexes.yml", "secretes.yml")
                : List.of("tutorial.yml", "main.yml", "class.yml", "side.yml", "secret.yml");
    }

    /** Moves files from older versions (at the root) to their folder. */
    public static void migrate(JavaPlugin plugin) {
        File root = plugin.getDataFolder();
        if (!root.isDirectory()) {
            return;
        }
        int moved = 0;
        for (String name : new ArrayList<>(FILES.keySet())) {
            moved += move(plugin, new File(root, name), file(plugin, name));
        }
        // Player data and backups
        File data = dataFolder(plugin);
        moved += move(plugin, new File(root, "playerdata"), playersFolder(data));
        String sqlite = YamlConfiguration.loadConfiguration(new File(root, "config.yml")).getString("storage.sqlite-file", "data.db");
        if (!sqlite.contains("/")) {
            moved += move(plugin, new File(root, sqlite), new File(data, sqlite));
        }
        File[] backups = root.listFiles((d, n) -> n.startsWith("jobs-ancien-") && n.endsWith(".yml"));
        if (backups != null) {
            for (File b : backups) {
                moved += move(plugin, b, new File(backupsFolder(plugin), b.getName()));
            }
        }
        if (moved > 0) {
            plugin.getLogger().info(moved + " file(s) moved to the theme folders.");
        }
    }

    private static int move(JavaPlugin plugin, File from, File to) {
        if (!from.exists() || to.exists()) {
            return 0;
        }
        try {
            Files.createDirectories(to.getParentFile().toPath());
            Files.move(from.toPath(), to.toPath());
            return 1;
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not move " + from.getName() + " to " + to.getPath() + ": " + ex.getMessage());
            return 0;
        }
    }
}
