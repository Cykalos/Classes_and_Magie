package fr.cykalos.cykclasses.data.storage;

import fr.cykalos.cykclasses.data.PlayerProfile;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

/**
 * Base SQLite : une ligne par joueur, colonnes indexées + profil complet en JSON.
 * Le pilote est téléchargé par Paper via le champ {@code libraries} du plugin.yml.
 */
public final class SqliteStorage implements Storage {

    private final File file;
    private Connection connection;

    public SqliteStorage(File dataFolder, String fileName) {
        this.file = new File(dataFolder, fileName);
    }

    @Override
    public synchronized void init() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS cyk_players (
                        uuid TEXT PRIMARY KEY,
                        name TEXT,
                        class TEXT,
                        subclass TEXT,
                        level INTEGER NOT NULL DEFAULT 1,
                        data TEXT NOT NULL
                    )""");
        }
    }

    @Override
    public synchronized PlayerProfile load(UUID uuid) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("SELECT data FROM cyk_players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                PlayerProfile p = JsonStorage.GSON.fromJson(rs.getString(1), PlayerProfile.class);
                return p == null ? null : p.normalize();
            }
        }
    }

    @Override
    public synchronized void save(PlayerProfile profile) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO cyk_players (uuid, name, class, subclass, level, data) VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET name = excluded.name, class = excluded.class,
                subclass = excluded.subclass, level = excluded.level, data = excluded.data""")) {
            ps.setString(1, profile.getUuid().toString());
            ps.setString(2, profile.getName());
            ps.setString(3, profile.getClassId());
            ps.setString(4, profile.getSubclassId());
            ps.setInt(5, profile.getLevel());
            ps.setString(6, JsonStorage.GSON.toJson(profile));
            ps.executeUpdate();
        }
    }

    @Override
    public synchronized void delete(UUID uuid) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM cyk_players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }

    @Override
    public synchronized void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ignored) {
        }
    }
}
