package fr.cykalos.cykclasses.data;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.storage.JsonStorage;
import fr.cykalos.cykclasses.data.storage.SqliteStorage;
import fr.cykalos.cykclasses.data.storage.Storage;
import fr.cykalos.cykclasses.data.storage.YamlStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Cache des profils en ligne + accès au stockage sur un thread dédié.
 */
public final class ProfileManager {

    private final CykClasses plugin;
    private final Map<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();
    private final ExecutorService io = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "CykClasses-IO");
        t.setDaemon(true);
        return t;
    });
    private Storage storage;
    private BukkitTask autosave;

    public ProfileManager(CykClasses plugin) {
        this.plugin = plugin;
    }

    public void init() throws Exception {
        String type = plugin.getConfig().getString("storage.type", "YAML").toUpperCase(Locale.ROOT);
        storage = switch (type) {
            case "JSON" -> new JsonStorage(plugin.getDataFolder());
            case "SQLITE", "SQL", "DATABASE" -> new SqliteStorage(plugin.getDataFolder(),
                    plugin.getConfig().getString("storage.sqlite-file", "data.db"));
            default -> new YamlStorage(plugin.getDataFolder());
        };
        storage.init();
        plugin.getLogger().info("Stockage des joueurs : " + type);

        long interval = Math.max(30, plugin.getConfig().getLong("storage.autosave-interval", 300)) * 20L;
        autosave = Bukkit.getScheduler().runTaskTimer(plugin, this::saveDirty, interval, interval);
    }

    /** Appelé depuis l'AsyncPlayerPreLoginEvent (hors thread principal). */
    public PlayerProfile loadBlocking(UUID uuid) {
        try {
            return io.submit(() -> {
                PlayerProfile p = storage.load(uuid);
                return p == null ? new PlayerProfile(uuid) : p;
            }).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Chargement impossible du profil " + uuid, e);
            return null;
        }
    }

    public void put(PlayerProfile profile) {
        profiles.put(profile.getUuid(), profile);
    }

    public PlayerProfile get(Player player) {
        return profiles.computeIfAbsent(player.getUniqueId(), PlayerProfile::new);
    }

    public PlayerProfile getIfLoaded(UUID uuid) {
        return profiles.get(uuid);
    }

    public Collection<PlayerProfile> online() {
        return profiles.values();
    }

    /** Sauvegarde asynchrone d'un instantané pris sur le thread principal. */
    public void saveAsync(PlayerProfile profile) {
        PlayerProfile snapshot = PlayerProfile.fromMap(profile.getUuid(), profile.toMap());
        profile.clearDirty();
        io.execute(() -> {
            try {
                storage.save(snapshot);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Sauvegarde impossible du profil " + snapshot.getUuid(), e);
            }
        });
    }

    public void unload(UUID uuid) {
        PlayerProfile p = profiles.remove(uuid);
        if (p != null) {
            saveAsync(p);
        }
    }

    public void deleteAsync(UUID uuid) {
        io.execute(() -> {
            try {
                storage.delete(uuid);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Suppression impossible du profil " + uuid, e);
            }
        });
    }

    private void saveDirty() {
        for (PlayerProfile p : profiles.values()) {
            if (p.isDirty()) {
                saveAsync(p);
            }
        }
    }

    public void shutdown() {
        if (autosave != null) {
            autosave.cancel();
        }
        for (PlayerProfile p : profiles.values()) {
            saveAsync(p);
        }
        io.shutdown();
        try {
            if (!io.awaitTermination(15, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("Certaines sauvegardes n'ont pas pu se terminer à temps.");
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        if (storage != null) {
            storage.close();
        }
    }
}
