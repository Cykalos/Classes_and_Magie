package fr.cykalos.cykclasses.skill.impl;

import fr.cykalos.cykclasses.service.CombatService.DamageKind;
import fr.cykalos.cykclasses.service.CombatService.Status;
import fr.cykalos.cykclasses.skill.SkillContext;
import fr.cykalos.cykclasses.skill.SkillService;
import fr.cykalos.cykclasses.util.Fx;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Guerrier, Berserker, Paladin.
 */
public final class WarriorSkills {

    private WarriorSkills() {
    }

    public static void register(SkillService s) {
        s.register("CHARGE", WarriorSkills::charge);
        s.register("WAR_CRY", WarriorSkills::warCry);
        s.register("WHIRLWIND", WarriorSkills::whirlwind);
        s.register("BLOOD_RAGE", WarriorSkills::bloodRage);
        s.register("SHATTERING_BLOW", WarriorSkills::shatteringBlow);
        s.register("HOLY_SHIELD", WarriorSkills::holyShield);
        s.register("JUDGMENT", WarriorSkills::judgment);
    }

    /** Fonce en avant et percute les ennemis sur le chemin. */
    private static boolean charge(SkillContext c) {
        Player p = c.player();
        double power = c.d("power", 1.6);
        double damage = c.d("damage", 6);
        double radius = c.d("radius", 1.8);
        Vector dir = p.getLocation().getDirection().setY(0).normalize().multiply(power).setY(0.25);
        p.setVelocity(dir);
        c.sound("entity.ravager.roar", 0.7f, 1.4f);
        Set<UUID> hit = new HashSet<>();
        c.repeat(1, 1, 10, tick -> {
            Fx.particle(p.getLocation().add(0, 0.5, 0), Particle.CLOUD, 4, 0.3, 0.02);
            for (LivingEntity e : c.combat().enemiesAround(p, p.getLocation(), radius)) {
                if (hit.add(e.getUniqueId())) {
                    c.combat().damage(p, e, damage, DamageKind.MELEE);
                    e.setVelocity(dir.clone().multiply(0.8).setY(0.45));
                    c.sound(e.getLocation(), "entity.player.attack.knockback", 1f, 0.8f);
                }
            }
        });
        return true;
    }

    /** Cri qui renforce le guerrier et les alliés proches. */
    private static boolean warCry(SkillContext c) {
        Player p = c.player();
        double radius = c.d("radius", 8);
        int duration = (int) (c.d("duration", 10) * 20);
        int strength = c.i("strength-level", 1) - 1;
        int resistance = c.i("resistance-level", 1) - 1;
        for (Player ally : c.combat().alliesAround(p, radius)) {
            ally.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, strength, false, true, true));
            if (resistance >= 0) {
                ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, resistance, false, true, true));
            }
            Fx.particle(ally.getLocation().add(0, 1, 0), Particle.ANGRY_VILLAGER, 6, 0.5, 0);
        }
        Fx.dustCircle(p.getLocation().add(0, 0.2, 0), radius, Color.fromRGB(200, 40, 40), 48);
        c.sound("entity.evoker.prepare_attack", 1f, 0.6f);
        return true;
    }

    /** Tournoiement : frappe tous les ennemis autour. */
    private static boolean whirlwind(SkillContext c) {
        Player p = c.player();
        double radius = c.d("radius", 4);
        double damage = c.d("damage", 5);
        double knockback = c.d("knockback", 0.8);
        int hits = c.i("hits", 3);
        c.repeat(0, 6, hits, tick -> {
            Location center = p.getLocation().add(0, 1, 0);
            Fx.circle(center, radius * 0.8, Particle.SWEEP_ATTACK, 10);
            c.sound("entity.player.attack.sweep", 1f, 0.8f + tick * 0.15f);
            for (LivingEntity e : c.combat().enemiesAround(p, p.getLocation(), radius)) {
                c.combat().damage(p, e, damage, DamageKind.MELEE);
                Vector kb = e.getLocation().toVector().subtract(p.getLocation().toVector());
                if (kb.lengthSquared() > 0) {
                    e.setVelocity(kb.normalize().multiply(knockback).setY(0.3));
                }
            }
        });
        return true;
    }

    /** Rage sanguinaire : vol de vie et dégâts bonus, coûte des PV. */
    private static boolean bloodRage(SkillContext c) {
        Player p = c.player();
        Status st = c.combat().status(p);
        st.rageUntil = System.currentTimeMillis() + (long) (c.d("duration", 10) * 1000);
        st.rageLifesteal = c.d("lifesteal", 0.25);
        st.rageBonus = c.d("damage-bonus", 0.3);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (c.d("duration", 10) * 20), 0, false, false, true));
        c.repeat(0, 5, (int) (c.d("duration", 10) * 4), tick ->
                Fx.dust(p.getLocation().add(0, 1, 0), Color.fromRGB(150, 0, 0), 1.4f, 4, 0.4));
        c.sound("entity.wither.ambient", 0.6f, 1.6f);
        return true;
    }

    /** Prochaine attaque de mêlée infligeant des dégâts massifs. */
    private static boolean shatteringBlow(SkillContext c) {
        Player p = c.player();
        Status st = c.combat().status(p);
        st.empoweredUntil = System.currentTimeMillis() + (long) (c.d("window", 6) * 1000);
        st.empoweredMultiplier = c.d("multiplier", 2.5);
        Fx.particle(p.getLocation().add(0, 1.2, 0), Particle.CRIT, 25, 0.4, 0.2);
        c.sound("item.trident.thunder", 0.5f, 1.8f);
        return true;
    }

    /** Bouclier sacré : absorption pour les alliés proches. */
    private static boolean holyShield(SkillContext c) {
        Player p = c.player();
        double radius = c.d("radius", 8);
        int duration = (int) (c.d("duration", 15) * 20);
        int level = c.i("absorption-level", 2) - 1;
        for (Player ally : c.combat().alliesAround(p, radius)) {
            ally.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration, level, false, true, true));
            Fx.dust(ally.getLocation().add(0, 1, 0), Color.fromRGB(255, 220, 90), 1.5f, 20, 0.5);
        }
        c.sound("block.beacon.power_select", 1f, 1.5f);
        return true;
    }

    /** Jugement : un éclair sacré frappe la cible visée. Bonus contre les morts-vivants. */
    private static boolean judgment(SkillContext c) {
        Player p = c.player();
        LivingEntity target = c.combat().targetInSight(p, c.d("range", 20));
        if (target == null) {
            c.noTarget();
            return false;
        }
        double damage = c.d("damage", 8);
        if (UndeadCheck.isUndead(target)) {
            damage *= c.d("undead-multiplier", 1.5);
        }
        Location top = target.getLocation().add(0, 8, 0);
        Fx.dustLine(top, target.getLocation().add(0, 1, 0), Color.fromRGB(255, 240, 150), 0.25);
        Fx.particle(target.getLocation().add(0, 1, 0), Particle.END_ROD, 20, 0.5, 0.1);
        c.sound(target.getLocation(), "entity.lightning_bolt.impact", 1f, 1.4f);
        c.combat().damage(p, target, damage, DamageKind.MAGIC);
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, false, false, false));
        return true;
    }
}
