package fr.cykalos.cykclasses.data.storage;

import fr.cykalos.cykclasses.data.PlayerProfile;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Un fichier YAML par joueur dans {@code playerdata/}. */
public final class YamlStorage implements Storage {

    private final File folder;

    public YamlStorage(File dataFolder) {
        this.folder = new File(dataFolder, "joueurs");
    }

    @Override
    public void init() {
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Impossible de créer " + folder);
        }
    }

    @Override
    public PlayerProfile load(UUID uuid) {
        File file = new File(folder, uuid + ".yml");
        if (!file.exists()) {
            return null;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        return PlayerProfile.fromMap(uuid, deep(yaml));
    }

    private static Map<String, Object> deep(ConfigurationSection section) {
        Map<String, Object> out = new HashMap<>();
        for (String key : section.getKeys(false)) {
            Object v = section.get(key);
            out.put(key, v instanceof ConfigurationSection cs ? deep(cs) : v);
        }
        return out;
    }

    @Override
    public void save(PlayerProfile profile) throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        profile.toMap().forEach(yaml::set);
        yaml.save(new File(folder, profile.getUuid() + ".yml"));
    }

    @Override
    public void delete(UUID uuid) {
        File file = new File(folder, uuid + ".yml");
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    @Override
    public void close() {
    }
}
