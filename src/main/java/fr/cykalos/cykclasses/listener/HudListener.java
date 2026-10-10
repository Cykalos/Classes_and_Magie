package fr.cykalos.cykclasses.listener;

import fr.cykalos.cykclasses.CykClasses;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Dégâts flottants et PV de la cible. Écoute en MONITOR : fonctionne aussi pour les dégâts
 * infligés par d'autres plugins (MythicMobs...).
 */
public final class HudListener implements Listener {

    private final CykClasses plugin;

    public HudListener(CykClasses plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim) || victim instanceof ArmorStand) {
            return;
        }
        Player attacker = null;
        boolean vanillaCrit = false;
        boolean hasSource = false;
        if (event instanceof EntityDamageByEntityEvent byEntity) {
            hasSource = true;
            Entity damager = byEntity.getDamager();
            if (damager instanceof Player p) {
                attacker = p;
            } else if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) {
                attacker = p;
            } else if (damager instanceof Tameable pet && pet.getOwner() instanceof Player p) {
                attacker = p;
            }
            vanillaCrit = byEntity.isCritical();
        }
        boolean crit = plugin.hud().consumeCritical(victim) || vanillaCrit;
        boolean skill = attacker != null && plugin.combat().isDealingSkillDamage(attacker.getUniqueId());
        double damage = event.getFinalDamage();
        plugin.hud().damage(victim, attacker, hasSource, damage, crit, skill);
        if (attacker != null) {
            plugin.hud().onHit(attacker, victim);
        }
    }
}
