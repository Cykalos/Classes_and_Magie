package fr.cykalos.cykclasses.listener;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.ClassStats;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.service.CombatService;
import fr.cykalos.cykclasses.service.CombatService.DamageKind;
import fr.cykalos.cykclasses.skill.impl.ArcherSkills;
import fr.cykalos.cykclasses.util.Fx;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Applique statistiques, défauts et états temporaires aux dégâts.
 */
public final class CombatListener implements Listener {

    private final CykClasses plugin;
    private final NamespacedKey explosiveKey;
    private final Map<UUID, Long> penaltyMessage = new HashMap<>();

    public CombatListener(CykClasses plugin) {
        this.plugin = plugin;
        this.explosiveKey = new NamespacedKey(plugin, ArcherSkills.EXPLOSIVE_KEY);
    }

    // ------------------------------------------------------------ Attaquant

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Player attacker = null;
        DamageKind kind = DamageKind.MELEE;
        if (damager instanceof Player p) {
            attacker = p;
        } else if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
            kind = DamageKind.RANGED;
        }

        // Groupe sans dégâts alliés : aucun coup entre membres.
        if (attacker != null && event.getEntity() instanceof Player victimPlayer
                && plugin.parties().isProtectedAlly(attacker, victimPlayer)) {
            event.setCancelled(true);
            return;
        }

        double damage = event.getDamage();

        // Marque du chasseur : bonus quel que soit l'attaquant.
        CombatService.Mark mark = plugin.combat().markOf(event.getEntity());
        if (mark != null) {
            damage *= 1 + mark.bonus();
        }

        if (attacker != null) {
            PlayerProfile profile = plugin.profiles().get(attacker);
            RpgClass rc = plugin.classes().effective(profile);
            profile.touchCombat();
            if (rc != null) {
                boolean skill = plugin.combat().isDealingSkillDamage(attacker.getUniqueId());
                CombatService.Status st = plugin.combat().statusIfPresent(attacker.getUniqueId());
                long now = System.currentTimeMillis();
                if (!skill) {
                    ClassStats s = plugin.stats().stats(profile);
                    damage *= kind == DamageKind.MELEE ? s.meleeDamage : s.rangedDamage;
                    damage *= weaponPenalty(attacker, rc);
                    if (kind == DamageKind.MELEE && s.critChance > 0 && ThreadLocalRandom.current().nextDouble() < s.critChance) {
                        damage *= s.critMultiplier;
                        plugin.hud().markCritical(event.getEntity());
                        Fx.particle(event.getEntity().getLocation().add(0, 1, 0), Particle.CRIT, 12, 0.3, 0.2);
                    }
                    if (st != null && kind == DamageKind.MELEE) {
                        if (st.empoweredUntil > now) {
                            damage *= st.empoweredMultiplier;
                            st.empoweredUntil = 0;
                            Fx.particle(event.getEntity().getLocation().add(0, 1, 0), Particle.EXPLOSION, 1, 0, 0);
                            attacker.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.PLAYER, 0.6f, 1.6f));
                        }
                        if (st.poisonCharges > 0 && event.getEntity() instanceof LivingEntity victim) {
                            st.poisonCharges--;
                            victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, st.poisonTicks, st.poisonAmplifier, false, true, true));
                        }
                    }
                }
                if (st != null && st.rageUntil > now) {
                    damage *= 1 + st.rageBonus;
                    double heal = damage * st.rageLifesteal;
                    var max = attacker.getAttribute(Attribute.MAX_HEALTH);
                    if (max != null) {
                        attacker.setHealth(Math.min(max.getValue(), attacker.getHealth() + heal));
                    }
                }
                if (!skill) {
                    profile.addCounter("damage_dealt", damage);
                }
            }
        }
        event.setDamage(Math.max(0, damage));
    }

    private double weaponPenalty(Player attacker, RpgClass rc) {
        ItemStack hand = attacker.getInventory().getItemInMainHand();
        double mult = 1.0;
        for (Flaw f : plugin.stats().flaws(plugin.profiles().get(attacker))) {
            if (f.type == Flaw.Type.WEAPON_PENALTY && f.matches(hand.getType())) {
                mult *= f.multiplier;
            }
        }
        if (mult < 1.0) {
            long now = System.currentTimeMillis();
            Long last = penaltyMessage.get(attacker.getUniqueId());
            if (last == null || now - last > 30_000) {
                penaltyMessage.put(attacker.getUniqueId(), now);
                plugin.messages().send(attacker, "flaw-weapon-penalty", Text.ph("penalty", Text.percent(mult)));
            }
        }
        return mult;
    }

    // ------------------------------------------------------------ Victime

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        PlayerProfile profile = plugin.profiles().getIfLoaded(victim.getUniqueId());
        if (profile == null) {
            return;
        }
        RpgClass rc = plugin.classes().effective(profile);
        if (rc == null) {
            return;
        }
        long now = System.currentTimeMillis();
        CombatService.Status st = plugin.combat().statusIfPresent(victim.getUniqueId());

        // Parade
        if (st != null && st.parryUntil > now && event instanceof EntityDamageByEntityEvent byEntity) {
            LivingEntity source = null;
            if (byEntity.getDamager() instanceof LivingEntity le) {
                source = le;
            } else if (byEntity.getDamager() instanceof Projectile proj && proj.getShooter() instanceof LivingEntity le) {
                source = le;
            }
            st.parryUntil = 0;
            event.setCancelled(true);
            victim.playSound(Sound.sound(Key.key("item.shield.block"), Sound.Source.PLAYER, 1f, 1.8f));
            Fx.particle(victim.getLocation().add(0, 1, 0), Particle.ENCHANTED_HIT, 20, 0.4, 0.2);
            if (source != null && plugin.combat().isValidTarget(victim, source)) {
                plugin.combat().damage(victim, source, st.parryCounter, DamageKind.MELEE);
            }
            return;
        }

        String cause = event.getCause().name();
        double damage = event.getDamage() * plugin.stats().stats(profile).damageTaken;
        for (Flaw f : plugin.stats().flaws(profile)) {
            if (f.type == Flaw.Type.DAMAGE_TAKEN && f.matchesCause(cause)) {
                damage *= f.multiplier;
            }
        }

        // Bouclier de mana : une partie des dégâts est payée en mana (1 dégât = 2 mana).
        if (st != null && st.manaShieldUntil > now && damage > 0) {
            double absorbed = damage * st.manaShieldRatio;
            double cost = absorbed * 2;
            if (profile.getMana() < cost) {
                absorbed = profile.getMana() / 2;
                cost = profile.getMana();
            }
            profile.setMana(profile.getMana() - cost);
            damage -= absorbed;
            if (absorbed > 0) {
                Fx.particle(victim.getLocation().add(0, 1, 0), Particle.ENCHANT, 15, 0.4, 0.5);
            }
        }

        event.setDamage(Math.max(0, damage));
        profile.addCounter("damage_taken", damage);
        if (event instanceof EntityDamageByEntityEvent) {
            profile.touchCombat();
        }
    }

    // ------------------------------------------------------------ Divers

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }
        Double power = arrow.getPersistentDataContainer().get(explosiveKey, PersistentDataType.DOUBLE);
        if (power == null) {
            return;
        }
        Entity shooter = arrow.getShooter() instanceof Entity e ? e : null;
        arrow.getWorld().createExplosion(arrow.getLocation(), power.floatValue(), false, false, shooter);
        arrow.remove();
    }

    @EventHandler(ignoreCancelled = true)
    public void onRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        var prof = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (prof == null || !prof.hasClass()) {
            return;
        }
        for (Flaw f : plugin.stats().flaws(prof)) {
            if (f.type == Flaw.Type.HEALING_REDUCTION) {
                event.setAmount(event.getAmount() * f.multiplier);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExhaustion(EntityExhaustionEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        var prof = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (prof == null || !prof.hasClass()) {
            return;
        }
        for (Flaw f : plugin.stats().flaws(prof)) {
            if (f.type == Flaw.Type.HUNGER) {
                event.setExhaustion(event.getExhaustion() * (float) f.multiplier);
            }
        }
    }
}
