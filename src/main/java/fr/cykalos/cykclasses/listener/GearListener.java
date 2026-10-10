package fr.cykalos.cykclasses.listener;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.service.GearService;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Forge (qualité à la fabrication), incrustation des gemmes, gemmes trouvées en minant,
 * et recalcul des stats quand l'équipement change.
 */
public final class GearListener implements Listener {

    private final CykClasses plugin;
    private final Map<UUID, Integer> signatures = new HashMap<>();

    public GearListener(CykClasses plugin) {
        this.plugin = plugin;
    }

    /** Recalcule les stats si l'équipement porté a changé. */
    public void checkEquipment(Player player) {
        int sig = plugin.gear().signature(player);
        Integer old = signatures.put(player.getUniqueId(), sig);
        if (old == null || old != sig) {
            plugin.stats().apply(player);
        }
    }

    private void checkLater(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                checkEquipment(player);
            }
        });
    }

    public void forget(UUID uuid) {
        signatures.remove(uuid);
    }

    // ------------------------------------------------------------ Forge

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraft(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack current = e.getCurrentItem();
        if (current == null || current.getType().isAir()) {
            return;
        }
        PlayerProfile p = plugin.profiles().get(player);
        if (!plugin.gear().isQualityCraft(p.getJobId(), current.getType())) {
            return;
        }
        if (e.isShiftClick() || e.getClick() == ClickType.NUMBER_KEY || e.getClick() == ClickType.DROP
                || e.getClick() == ClickType.CONTROL_DROP) {
            e.setCancelled(true);
            plugin.messages().send(player, "forge.one-at-a-time");
            return;
        }
        ItemStack cursor = e.getView().getCursor();
        if (cursor != null && !cursor.getType().isAir()) {
            e.setCancelled(true);
            plugin.messages().send(player, "forge.empty-cursor");
            return;
        }
        ItemStack forged = plugin.gear().forge(current.clone(), player, plugin.jobs().level(p), null);
        e.setCurrentItem(forged);
        player.playSound(Sound.sound(Key.key("block.anvil.use"), Sound.Source.PLAYER, 0.6f, 1.2f));
    }

    // ------------------------------------------------------------ Gemmes

    @EventHandler(priority = EventPriority.HIGH)
    public void onSocket(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player) || e.getClick() != ClickType.LEFT && e.getClick() != ClickType.RIGHT) {
            return;
        }
        ItemStack cursor = e.getCursor();
        ItemStack target = e.getCurrentItem();
        String gemId = plugin.gear().gemId(cursor);
        if (gemId == null || target == null || target.getType().isAir() || plugin.gear().gemId(target) != null) {
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE) {
            plugin.messages().send(player, "gems.no-creative");
            return;
        }
        GearService.SocketResult result = plugin.gear().socket(target, gemId);
        switch (result) {
            case OK -> {
                e.setCancelled(true);
                e.setCurrentItem(target);
                ItemStack rest = cursor.clone();
                rest.setAmount(cursor.getAmount() - 1);
                e.getView().setCursor(rest.getAmount() <= 0 ? null : rest);
                plugin.messages().send(player, "gems.socketed");
                player.playSound(Sound.sound(Key.key("block.amethyst_block.place"), Sound.Source.PLAYER, 1f, 1.4f));
                checkLater(player);
            }
            case NO_SOCKET -> {
                e.setCancelled(true);
                plugin.messages().send(player, "gems.no-socket");
            }
            case WRONG_TYPE -> {
                e.setCancelled(true);
                plugin.messages().send(player, "gems.wrong-type");
            }
            default -> {
                // NOT_GEAR : clic normal, on laisse faire
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMine(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || e.getBlock().hasMetadata("cyk_placed")) {
            return;
        }
        PlayerProfile p = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (p == null) {
            return;
        }
        GearService.Gem gem = plugin.gear().rollGemDrop(p.getJobId(), plugin.jobs().level(p), e.getBlock().getType());
        if (gem != null) {
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation().add(0.5, 0.5, 0.5), plugin.gear().createGem(gem, 1));
            plugin.messages().send(player, "gems.found", Text.ph("gem", gem.name()));
            player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 1f, 0.6f));
        }
    }

    // ------------------------------------------------------------ Équipement

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeld(PlayerItemHeldEvent e) {
        checkLater(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent e) {
        checkLater(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player p) {
            checkLater(p);
        }
    }
}
