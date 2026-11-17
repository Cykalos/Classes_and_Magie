package fr.cykalos.cykclasses.data.storage;

import fr.cykalos.cykclasses.data.PlayerProfile;

import java.util.UUID;

/**
 * Storage backend. Methods are called off the main thread.
 */
public interface Storage {

    void init() throws Exception;

    /** @return the saved profile, or {@code null} if it does not exist. */
    PlayerProfile load(UUID uuid) throws Exception;

    void save(PlayerProfile profile) throws Exception;

    void delete(UUID uuid) throws Exception;

    void close();
}
