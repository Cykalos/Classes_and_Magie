package fr.cykalos.cykclasses.listener;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.skill.SkillBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * F active/désactive le mode compétences ; les touches 1-8 lancent alors les compétences.
 * Protège aussi les icônes (impossible de les déplacer, jeter ou poser).
 */
public final class CastingListener implements Listener {

    private final CykClasses plugin;

    public CastingListener(CykClasses plugin) {
        this.plugin = plugin;
    }

    private SkillBar bar() {
        return plugin.skillBar();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (bar().isActive(player)) {
            event.setCancelled(true);
            bar().exit(player, true);
            return;
        }
        if (!plugin.getConfig().getBoolean("casting.swap-hands-toggle", true)) {
            return;
        }
        PlayerProfile profile = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (profile == null || !profile.hasClass()) {
            return;
        }
        if (plugin.getConfig().getBoolean("casting.require-sneak", false) && !player.isSneaking()) {
            return;
        }
        event.setCancelled(true);
        bar().enter(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHotbar(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!bar().isActive(player)) {
            return;
        }
        event.setCancelled(true);
        PlayerProfile profile = plugin.profiles().get(player);
        profile.touchCastActivity();
        int slot = event.getNewSlot();
        if (slot < SkillBar.SKILL_SLOTS) {
            plugin.skills().castSlot(player, slot);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && bar().isActive(player)) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTask(plugin, () -> bar().exit(player, true));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player && bar().isActive(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player && bar().isActive(player)) {
            bar().exit(player, false);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (bar().isActive(event.getPlayer()) || bar().isIcon(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (bar().isIcon(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent event) {
        if (bar().isIcon(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        bar().handleDeath(event.getPlayer(), event.getDrops(), event.getKeepInventory());
    }

    @EventHandler
    public void onWorld(PlayerChangedWorldEvent event) {
        if (bar().isActive(event.getPlayer())) {
            bar().exit(event.getPlayer(), false);
        }
    }

    @EventHandler
    public void onGameMode(PlayerGameModeChangeEvent event) {
        if (bar().isActive(event.getPlayer())) {
            bar().exit(event.getPlayer(), false);
        }
    }
}
