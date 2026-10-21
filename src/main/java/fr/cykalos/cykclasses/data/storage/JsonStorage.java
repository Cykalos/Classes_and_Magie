package fr.cykalos.cykclasses.data.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.cykalos.cykclasses.data.PlayerProfile;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/** Un fichier JSON par joueur dans {@code playerdata/}. */
public final class JsonStorage implements Storage {

    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File folder;

    public JsonStorage(File dataFolder) {
        this.folder = new File(dataFolder, "joueurs");
    }

    @Override
    public void init() {
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Impossible de créer " + folder);
        }
    }

    @Override
    public PlayerProfile load(UUID uuid) throws Exception {
        File file = new File(folder, uuid + ".json");
        if (!file.exists()) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            PlayerProfile p = GSON.fromJson(reader, PlayerProfile.class);
            return p == null ? null : p.normalize();
        }
    }

    @Override
    public void save(PlayerProfile profile) throws Exception {
        File tmp = new File(folder, profile.getUuid() + ".json.tmp");
        try (Writer writer = Files.newBufferedWriter(tmp.toPath(), StandardCharsets.UTF_8)) {
            GSON.toJson(profile, writer);
        }
        Files.move(tmp.toPath(), new File(folder, profile.getUuid() + ".json").toPath(),
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    @Override
    public void delete(UUID uuid) throws Exception {
        Files.deleteIfExists(new File(folder, uuid + ".json").toPath());
    }

    @Override
    public void close() {
    }
}
