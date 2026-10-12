package fr.cykalos.cykclasses.listener;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.Job;
import fr.cykalos.cykclasses.content.JobRule;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Métiers : actions réservées (avec niveau requis) et gain d'XP de métier.
 */
public final class JobListener implements Listener {

    private static final String PLACED = "cyk_placed";

    private final CykClasses plugin;
    private final Map<UUID, Long> lastMessage = new HashMap<>();
    /** Dernier joueur ayant ouvert chaque alambic (pour l'XP d'alchimie). */
    private final Map<Location, UUID> brewers = new HashMap<>();

    public JobListener(CykClasses plugin) {
        this.plugin = plugin;
    }

    /** @return true si l'action est bloquée (l'événement est annulé et le message envoyé). */
    private boolean blocked(Player player, JobRule.Action action, String target, Cancellable event) {
        if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("cykclasses.bypass.jobs")
                || !plugin.modules().jobs()) {
            return false;
        }
        PlayerProfile p = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (p == null) {
            return false;
        }
        for (JobRule rule : plugin.content().jobRules()) {
            if (rule.action() != action || !rule.matches(target)) {
                continue;
            }
            boolean hasJob = p.getJobId() != null && rule.jobs().contains(p.getJobId());
            int level = plugin.jobs().level(p);
            if (hasJob && level >= rule.minLevel()) {
                continue;
            }
            event.setCancelled(true);
            if (canMessage(player)) {
                if (rule.message() != null) {
                    plugin.messages().send(player, "item-restricted-custom", Text.ph("message", rule.message()));
                } else if (hasJob) {
                    plugin.messages().send(player, "job.need-level", Text.ph("level", rule.minLevel()), Text.ph("current", level));
                } else {
                    List<String> names = new ArrayList<>();
                    for (String id : rule.jobs()) {
                        Job j = plugin.content().job(id);
                        names.add(j == null ? id : Text.plain(j.name()));
                    }
                    String lvl = rule.minLevel() > 1 ? " " + plugin.messages().raw("job.level-suffix").replace("<level>", String.valueOf(rule.minLevel())) : "";
                    plugin.messages().send(player, "job.restricted", Text.ph("jobs", String.join(", ", names) + lvl));
                }
            }
            return true;
        }
        return false;
    }

    private boolean canMessage(Player player) {
        long now = System.currentTimeMillis();
        Long last = lastMessage.get(player.getUniqueId());
        if (last != null && now - last < 1500) {
            return false;
        }
        lastMessage.put(player.getUniqueId(), now);
        return true;
    }

    // ------------------------------------------------------------ Restrictions (LOW) + XP (MONITOR)

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        blocked(e.getPlayer(), JobRule.Action.BREAK, e.getBlock().getType().name(), e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreakXp(BlockBreakEvent e) {
        // Pas d'XP pour un bloc posé par un joueur (anti-abus), ni pour une culture pas mûre.
        if (e.getBlock().hasMetadata(PLACED)) {
            return;
        }
        if (e.getBlock().getBlockData() instanceof Ageable age && age.getAge() < age.getMaximumAge()) {
            return;
        }
        plugin.jobs().reward(e.getPlayer(), JobRule.Action.BREAK, e.getBlock().getType().name(), 1);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        blocked(e.getPlayer(), JobRule.Action.PLACE, e.getBlockPlaced().getType().name(), e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlaceMark(BlockPlaceEvent e) {
        e.getBlockPlaced().setMetadata(PLACED, new FixedMetadataValue(plugin, true));
        plugin.jobs().reward(e.getPlayer(), JobRule.Action.PLACE, e.getBlockPlaced().getType().name(), 1);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCraft(CraftItemEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            blocked(p, JobRule.Action.CRAFT, e.getRecipe().getResult().getType().name(), e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftXp(CraftItemEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            ItemStack result = e.getRecipe().getResult();
            plugin.jobs().reward(p, JobRule.Action.CRAFT, result.getType().name(), 1);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) {
            return;
        }
        Material type = e.getClickedBlock().getType();
        if (e.getPlayer().isSneaking() && e.getItem() != null && e.getItem().getType().isBlock()) {
            return; // pose d'un bloc contre la station
        }
        if (!blocked(e.getPlayer(), JobRule.Action.INTERACT_BLOCK, type.name(), e) && type == Material.BREWING_STAND) {
            brewers.put(e.getClickedBlock().getLocation(), e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        if (e.getState() == PlayerFishEvent.State.FISHING) {
            blocked(e.getPlayer(), JobRule.Action.FISH, "ANY", e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFishXp(PlayerFishEvent e) {
        if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            String target = e.getCaught() instanceof org.bukkit.entity.Item item ? item.getItemStack().getType().name() : "ANY";
            plugin.jobs().reward(e.getPlayer(), JobRule.Action.FISH, target, 1);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreed(EntityBreedEvent e) {
        if (e.getBreeder() instanceof Player p && !blocked(p, JobRule.Action.BREED, e.getEntityType().name(), e)) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!e.isCancelled()) {
                    plugin.jobs().reward(p, JobRule.Action.BREED, e.getEntityType().name(), 1);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTame(EntityTameEvent e) {
        if (e.getOwner() instanceof Player p && !blocked(p, JobRule.Action.TAME, e.getEntityType().name(), e)) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!e.isCancelled()) {
                    plugin.jobs().reward(p, JobRule.Action.TAME, e.getEntityType().name(), 1);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent e) {
        if (!blocked(e.getEnchanter(), JobRule.Action.ENCHANT, e.getItem().getType().name(), e)) {
            Player p = e.getEnchanter();
            int cost = e.getExpLevelCost();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!e.isCancelled()) {
                    plugin.jobs().reward(p, JobRule.Action.ENCHANT, e.getItem().getType().name(), Math.max(1, cost));
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrew(BrewEvent e) {
        UUID uuid = brewers.get(e.getBlock().getLocation());
        Player p = uuid == null ? null : Bukkit.getPlayer(uuid);
        if (p == null) {
            return;
        }
        int potions = 0;
        for (ItemStack it : e.getResults()) {
            if (it != null && !it.getType().isAir()) {
                potions++;
            }
        }
        plugin.jobs().reward(p, JobRule.Action.BREW, "ANY", potions);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSmelt(FurnaceExtractEvent e) {
        PlayerProfile p = plugin.profiles().getIfLoaded(e.getPlayer().getUniqueId());
        if (p == null) {
            return;
        }
        for (JobRule rule : plugin.content().jobRules()) {
            if (rule.action() == JobRule.Action.SMELT && rule.matches(e.getItemType().name())
                    && (p.getJobId() == null || !rule.jobs().contains(p.getJobId()) || plugin.jobs().level(p) < rule.minLevel())) {
                e.setExpToDrop(0); // l'extraction ne peut pas être annulée : seule l'XP vanilla est retirée
                return;
            }
        }
        plugin.jobs().reward(e.getPlayer(), JobRule.Action.SMELT, e.getItemType().name(), e.getItemAmount());
    }
}
