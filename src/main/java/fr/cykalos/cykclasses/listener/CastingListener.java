package fr.cykalos.cykclasses.listener;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * F (échange de main) active le mode compétences ; les touches 1-9 lancent alors les compétences
 * sans changer l'objet tenu.
 */
public final class CastingListener implements Listener {

    private final CykClasses plugin;

    public CastingListener(CykClasses plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (!plugin.getConfig().getBoolean("casting.swap-hands-toggle", true)) {
            return;
        }
        Player player = event.getPlayer();
        PlayerProfile profile = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (profile == null || !profile.hasClass()) {
            return;
        }
        if (!profile.isCastMode() && plugin.getConfig().getBoolean("casting.require-sneak", false) && !player.isSneaking()) {
            return;
        }
        event.setCancelled(true);
        plugin.skills().toggleCastMode(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHotbar(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (profile == null || !profile.isCastMode()) {
            return;
        }
        event.setCancelled(true);
        profile.touchCastActivity();
        plugin.skills().castSlot(player, event.getNewSlot());
    }
}
