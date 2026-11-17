package fr.cykalos.cykclasses.listener;

import fr.cykalos.cykclasses.CykClasses;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

/**
 * Protects the player until their character creation is finished:
 * they take no damage, monsters do not target them, and they cannot attack.
 * config.yml → creation-protection (true by default).
 */
public final class CreationProtectionListener implements Listener {

    private final CykClasses plugin;

    public CreationProtectionListener(CykClasses plugin) {
        this.plugin = plugin;
    }

    private boolean isProtected(Entity entity) {
        if (!(entity instanceof Player player)
                || !plugin.getConfig().getBoolean("force-class-selection", true)
                || !plugin.getConfig().getBoolean("creation-protection", true)
                || plugin.profiles().getIfLoaded(player.getUniqueId()) == null) {
            return false;
        }
        return plugin.character().creationPending(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (isProtected(event.getEntity())) {
            event.setCancelled(true);
            event.getEntity().setFireTicks(0);
            return;
        }
        // A protected player cannot attack either (no abuse of the invulnerability)
        if (event instanceof EntityDamageByEntityEvent byEntity) {
            Entity damager = byEntity.getDamager();
            if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
                damager = shooter;
            }
            if (isProtected(damager)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (isProtected(event.getTarget())) {
            event.setCancelled(true);
        }
    }
}
