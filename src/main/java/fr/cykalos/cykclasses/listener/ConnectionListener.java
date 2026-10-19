package fr.cykalos.cykclasses.listener;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ConnectionListener implements Listener {

    private final CykClasses plugin;
    private final Map<UUID, PlayerProfile> pending = new ConcurrentHashMap<>();

    public ConnectionListener(CykClasses plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        PlayerProfile profile = plugin.profiles().loadBlocking(event.getUniqueId());
        if (profile == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    Component.text("CykClasses : impossible de charger tes données, réessaie dans un instant."));
            return;
        }
        pending.put(event.getUniqueId(), profile);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = pending.remove(player.getUniqueId());
        if (profile == null) {
            // Rechargement à chaud : le profil n'est pas passé par la pré-connexion.
            profile = plugin.profiles().getIfLoaded(player.getUniqueId());
            if (profile == null) {
                profile = new PlayerProfile(player.getUniqueId());
            }
        }
        profile.setName(player.getName());
        plugin.profiles().put(profile);
        plugin.skillBar().recover(player);
        plugin.stats().apply(player);

        if (plugin.getConfig().getBoolean("force-class-selection", true)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }
                if (!plugin.profiles().get(player).hasClass()) {
                    plugin.messages().send(player, "must-choose-class");
                }
                plugin.character().continueCreation(player);
            }, 40L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.skillBar().exit(event.getPlayer(), false);
        plugin.parties().onQuit(event.getPlayer());
        plugin.hud().clear(event.getPlayer());
        plugin.gearListener().forget(uuid);
        plugin.combat().clear(uuid);
        plugin.profiles().unload(uuid);
        pending.remove(uuid);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.stats().apply(player);
                PlayerProfile profile = plugin.profiles().get(player);
                profile.setMana(plugin.stats().maxMana(profile) * 0.5);
                profile.setCastMode(false);
                // Mort pendant la création : le menu a été fermé par la mort, on reprend l'étape
                if (plugin.getConfig().getBoolean("force-class-selection", true)) {
                    plugin.character().continueCreation(player);
                }
            }
        }, 2L);
    }
}
