package fr.cykalos.cykclasses.listener;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.hook.MythicHook;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Suit les éliminations, les blocs minés et donne l'XP de classe.
 */
public final class ProgressListener implements Listener {

    private final CykClasses plugin;

    public ProgressListener(CykClasses plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Player killer = dead.getKiller();
        if (killer == null || killer.equals(dead)) {
            return;
        }
        PlayerProfile profile = plugin.profiles().getIfLoaded(killer.getUniqueId());
        if (profile == null || !profile.hasClass()) {
            return;
        }
        Set<String> before = plugin.levels().availableSubclasses(killer, profile);
        String key = dead instanceof Player ? "player" : dead.getType().getKey().getKey();
        profile.addKill(key);

        double xp;
        MythicHook mythic = plugin.mythic();
        Optional<MythicHook.MobInfo> mob = mythic == null || dead instanceof Player ? Optional.empty() : mythic.info(dead);
        if (mob.isPresent()) {
            MythicHook.MobInfo info = mob.get();
            profile.addKill("mythic:" + info.internalName().toLowerCase(Locale.ROOT));
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("mythicmobs.mob-xp");
            double base = section != null && section.contains(info.internalName())
                    ? section.getDouble(info.internalName())
                    : plugin.getConfig().getDouble("mythicmobs.default-xp", plugin.levels().mobXp(key));
            xp = base + Math.max(0, info.level() - 1) * plugin.getConfig().getDouble("mythicmobs.xp-per-level", 2);
        } else if (dead instanceof Player) {
            xp = plugin.getConfig().getDouble("leveling.player-kill-xp", 50);
        } else if (dead instanceof Animals && !(dead instanceof Monster)
                && plugin.getConfig().getBoolean("leveling.ignore-passive-mobs", true)) {
            xp = 0;
        } else {
            xp = plugin.levels().mobXp(key);
        }

        if (xp <= 0) {
            plugin.levels().checkNewSubclasses(killer, profile, before);
            return;
        }
        Map<Player, Double> shares = plugin.parties().xpShares(killer, dead.getLocation(), xp);
        for (Map.Entry<Player, Double> e : shares.entrySet()) {
            Player p = e.getKey();
            if (p.equals(killer)) {
                plugin.levels().addXp(p, e.getValue(), before);
            } else {
                plugin.levels().addXp(p, e.getValue());
            }
        }
        if (!shares.containsKey(killer)) {
            plugin.levels().checkNewSubclasses(killer, profile, before);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerProfile profile = plugin.profiles().getIfLoaded(event.getPlayer().getUniqueId());
        if (profile != null) {
            profile.setCastMode(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        PlayerProfile profile = plugin.profiles().getIfLoaded(event.getPlayer().getUniqueId());
        if (profile != null && profile.hasClass()) {
            profile.addCounter("blocks_mined", 1);
        }
    }
}
