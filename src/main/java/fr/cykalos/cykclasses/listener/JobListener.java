package fr.cykalos.cykclasses.listener;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.Job;
import fr.cykalos.cykclasses.content.JobRule;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Actions réservées à certains métiers (jobs.yml → restrictions).
 */
public final class JobListener implements Listener {

    private final CykClasses plugin;
    private final Map<UUID, Long> lastMessage = new HashMap<>();

    public JobListener(CykClasses plugin) {
        this.plugin = plugin;
    }

    /** @return true si l'action est bloquée (le message est envoyé). */
    private boolean blocked(Player player, JobRule.Action action, String target, Cancellable event) {
        if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("cykclasses.bypass.jobs")) {
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
            if (p.getJobId() != null && rule.jobs().contains(p.getJobId())) {
                return false;
            }
            event.setCancelled(true);
            long now = System.currentTimeMillis();
            Long last = lastMessage.get(player.getUniqueId());
            if (last == null || now - last > 1500) {
                lastMessage.put(player.getUniqueId(), now);
                List<String> names = new ArrayList<>();
                for (String id : rule.jobs()) {
                    Job j = plugin.content().job(id);
                    names.add(j == null ? id : Text.plain(j.name()));
                }
                if (rule.message() != null) {
                    plugin.messages().send(player, "item-restricted-custom", Text.ph("message", rule.message()));
                } else {
                    plugin.messages().send(player, "job.restricted", Text.ph("jobs", String.join(", ", names)));
                }
            }
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        blocked(e.getPlayer(), JobRule.Action.BREAK, e.getBlock().getType().name(), e);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        blocked(e.getPlayer(), JobRule.Action.PLACE, e.getBlockPlaced().getType().name(), e);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCraft(CraftItemEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            blocked(p, JobRule.Action.CRAFT, e.getRecipe().getResult().getType().name(), e);
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
        blocked(e.getPlayer(), JobRule.Action.INTERACT_BLOCK, type.name(), e);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        if (e.getState() == PlayerFishEvent.State.FISHING) {
            blocked(e.getPlayer(), JobRule.Action.FISH, "ANY", e);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreed(EntityBreedEvent e) {
        if (e.getBreeder() instanceof Player p) {
            blocked(p, JobRule.Action.BREED, e.getEntityType().name(), e);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTame(EntityTameEvent e) {
        if (e.getOwner() instanceof Player p) {
            blocked(p, JobRule.Action.TAME, e.getEntityType().name(), e);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSmelt(FurnaceExtractEvent e) {
        // L'extraction ne peut pas être annulée : on retire seulement l'XP donnée.
        PlayerProfile p = plugin.profiles().getIfLoaded(e.getPlayer().getUniqueId());
        if (p == null) {
            return;
        }
        for (JobRule rule : plugin.content().jobRules()) {
            if (rule.action() == JobRule.Action.SMELT && rule.matches(e.getItemType().name())
                    && (p.getJobId() == null || !rule.jobs().contains(p.getJobId()))) {
                e.setExpToDrop(0);
                return;
            }
        }
    }
}
