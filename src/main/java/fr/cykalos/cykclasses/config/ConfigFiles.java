package fr.cykalos.cykclasses.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Location of each file in the plugin folder, organised by theme. Folder names depend on the
 * layout chosen on the first start (see {@link Lang#frenchLayout()}):
 * <pre>
 * English layout            French layout (installs before 1.12, or language: fr)
 * config.yml                config.yml
 * classes/                  classes/       classes.yml, skilltree.yml, attributes.yml
 * character/                personnage/    races.yml, traits.yml, weaknesses.yml, titles.yml
 * jobs/                     metiers/       jobs.yml, forge.yml, gems.yml
 * items/                    objets/        kits.yml, items.yml
 * display/                  affichage/     hud.yml, messages.yml
 * quests/                   quetes/        quest files (every .yml file in the folder is read)
 * data/                     donnees/       server and player data (do not edit by hand)
 * </pre>
 * Files from older versions (all at the root) are moved automatically on startup.
 */
public final class ConfigFiles {

    /** Data folder (French layout name kept for compatibility; use {@link #data()}). */
    public static final String DATA = "donnees";
    /** Quest folder (French layout name kept for compatibility; use {@link #quests()}). */
    public static final String QUESTS = "quetes";

    private static final Map<String, String> PATHS = new LinkedHashMap<>();
    private static boolean french = true;

    private ConfigFiles() {
    }

    /** Builds the file map for the layout chosen by {@link Lang#init}. */
    public static void init() {
        french = Lang.frenchLayout();
        PATHS.clear();
        put(french ? "classes" : "classes", "classes.yml", "skilltree.yml", "attributes.yml");
        put(french ? "personnage" : "character", "races.yml", "traits.yml", "weaknesses.yml", "titles.yml");
        put(french ? "metiers" : "jobs", "jobs.yml", "forge.yml", "gems.yml");
        put(french ? "objets" : "items", "kits.yml", "items.yml");
        put(french ? "affichage" : "display", "hud.yml", "messages.yml");
        put(data(), "uniques.yml", "forge-uniques.yml");
        PATHS.put("titres-uniques.yml", data() + "/" + (french ? "titres-uniques.yml" : "unique-titles.yml"));
    }

    /** Name of the data folder ("donnees" or "data"). */
    public static String data() {
        return french ? DATA : "data";
    }

    /** Name of the quest folder ("quetes" or "quests"). */
    public static String quests() {
        return french ? QUESTS : "quests";
    }

    /** Sub-folder of the data folder holding one file per player. */
    public static String players() {
        return french ? "joueurs" : "players";
    }

    /** Sub-folder of the data folder holding automatic backups. */
    public static String backups() {
        return french ? "sauvegardes" : "backups";
    }

    private static void put(String folder, String... names) {
        for (String n : names) {
            PATHS.put(n, folder + "/" + n);
        }
    }

    /** Relative path of a file (e.g. "races.yml" → "character/races.yml"). */
    public static String path(String name) {
        return PATHS.getOrDefault(name, name);
    }

    public static File file(JavaPlugin plugin, String name) {
        return new File(plugin.getDataFolder(), path(name));
    }

    public static File dataFolder(JavaPlugin plugin) {
        return new File(plugin.getDataFolder(), data());
    }

    /** Loads a configuration file, creating the default version (in the plugin language) if it does not exist. */
    public static YamlConfiguration load(JavaPlugin plugin, String name) {
        File file = file(plugin, name);
        Lang.saveDefault(plugin, name, file);
        return YamlConfiguration.loadConfiguration(file);
    }

    /** Moves files from older versions (at the root) to their folder. */
    public static void migrate(JavaPlugin plugin) {
        File root = plugin.getDataFolder();
        if (!root.isDirectory()) {
            return;
        }
        int moved = 0;
        for (Map.Entry<String, String> e : PATHS.entrySet()) {
            moved += move(plugin, new File(root, e.getKey()), new File(root, e.getValue()));
        }
        // Player data and backups
        moved += move(plugin, new File(root, "playerdata"), new File(dataFolder(plugin), players()));
        String sqlite = plugin.getConfig().getString("storage.sqlite-file", "data.db");
        if (!sqlite.contains("/")) {
            moved += move(plugin, new File(root, sqlite), new File(dataFolder(plugin), sqlite));
        }
        File[] backups = root.listFiles((d, n) -> n.startsWith("jobs-ancien-") && n.endsWith(".yml"));
        if (backups != null) {
            for (File b : backups) {
                moved += move(plugin, b, new File(dataFolder(plugin), backups() + "/" + b.getName()));
            }
        }
        if (moved > 0) {
            plugin.getLogger().info(moved + " file(s) moved to the new folders (classes/, " + path("races.yml").split("/")[0] + "/, …).");
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
