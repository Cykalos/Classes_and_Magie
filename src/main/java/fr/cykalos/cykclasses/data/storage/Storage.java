package fr.cykalos.cykclasses.data.storage;

import fr.cykalos.cykclasses.data.PlayerProfile;

import java.util.UUID;

/**
 * Backend de sauvegarde. Les méthodes sont appelées hors du thread principal.
 */
public interface Storage {

    void init() throws Exception;

    /** @return le profil sauvegardé, ou {@code null} s'il n'existe pas. */
    PlayerProfile load(UUID uuid) throws Exception;

    void save(PlayerProfile profile) throws Exception;

    void delete(UUID uuid) throws Exception;

    void close();
}
