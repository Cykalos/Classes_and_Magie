package fr.cykalos.cykclasses.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Emplacement de chaque fichier dans le dossier du plugin, rangé par thème :
 * <pre>
 * config.yml
 * classes/      classes.yml, skilltree.yml, attributes.yml
 * personnage/   races.yml, traits.yml, weaknesses.yml, titles.yml
 * metiers/      jobs.yml, forge.yml, gems.yml
 * objets/       kits.yml, items.yml
 * affichage/    hud.yml, messages.yml
 * quetes/       un ou plusieurs fichiers de quêtes (tous les .yml du dossier sont lus)
 * donnees/      données du serveur et des joueurs (ne pas modifier à la main)
 * </pre>
 * Les fichiers des anciennes versions (tous à la racine) sont déplacés automatiquement au démarrage.
 */
public final class ConfigFiles {

    public static final String DATA = "donnees";
    public static final String QUESTS = "quetes";

    private static final Map<String, String> PATHS = new LinkedHashMap<>();

    static {
        put("classes", "classes.yml", "skilltree.yml", "attributes.yml");
        put("personnage", "races.yml", "traits.yml", "weaknesses.yml", "titles.yml");
        put("metiers", "jobs.yml", "forge.yml", "gems.yml");
        put("objets", "kits.yml", "items.yml");
        put("affichage", "hud.yml", "messages.yml");
        put(DATA, "uniques.yml", "forge-uniques.yml", "titres-uniques.yml");
    }

    private ConfigFiles() {
    }

    private static void put(String folder, String... names) {
        for (String n : names) {
            PATHS.put(n, folder + "/" + n);
        }
    }

    /** Chemin relatif d'un fichier (ex. « classes.yml » → « classes/classes.yml »). */
    public static String path(String name) {
        return PATHS.getOrDefault(name, name);
    }

    public static File file(JavaPlugin plugin, String name) {
        return new File(plugin.getDataFolder(), path(name));
    }

    public static File dataFolder(JavaPlugin plugin) {
        return new File(plugin.getDataFolder(), DATA);
    }

    /** Charge un fichier de configuration, en créant la version par défaut s'il n'existe pas. */
    public static YamlConfiguration load(JavaPlugin plugin, String name) {
        File file = file(plugin, name);
        if (!file.exists() && plugin.getResource(path(name)) != null) {
            plugin.saveResource(path(name), false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /** Déplace les fichiers des anciennes versions (à la racine) vers leur dossier. */
    public static void migrate(JavaPlugin plugin) {
        File root = plugin.getDataFolder();
        if (!root.isDirectory()) {
            return;
        }
        int moved = 0;
        for (Map.Entry<String, String> e : PATHS.entrySet()) {
            moved += move(plugin, new File(root, e.getKey()), new File(root, e.getValue()));
        }
        // Données des joueurs et sauvegardes
        moved += move(plugin, new File(root, "playerdata"), new File(dataFolder(plugin), "joueurs"));
        String sqlite = plugin.getConfig().getString("storage.sqlite-file", "data.db");
        if (!sqlite.contains("/")) {
            moved += move(plugin, new File(root, sqlite), new File(dataFolder(plugin), sqlite));
        }
        File[] backups = root.listFiles((d, n) -> n.startsWith("jobs-ancien-") && n.endsWith(".yml"));
        if (backups != null) {
            for (File b : backups) {
                moved += move(plugin, b, new File(dataFolder(plugin), "sauvegardes/" + b.getName()));
            }
        }
        if (moved > 0) {
            plugin.getLogger().info(moved + " fichier(s) rangé(s) dans les nouveaux dossiers (classes/, personnage/, metiers/, objets/, affichage/, donnees/).");
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
            plugin.getLogger().warning("Impossible de déplacer " + from.getName() + " vers " + to.getPath() + " : " + ex.getMessage());
            return 0;
        }
    }
}
