package fr.cykalos.cykclasses.skill.impl;

import fr.cykalos.cykclasses.service.CombatService.DamageKind;
import fr.cykalos.cykclasses.skill.SkillContext;
import fr.cykalos.cykclasses.skill.SkillService;
import fr.cykalos.cykclasses.util.Fx;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Archer, Chasseur, Rôdeur.
 */
public final class ArcherSkills {

    /** Marqueur PDC des flèches explosives (valeur = puissance). */
    public static final String EXPLOSIVE_KEY = "explosive_arrow";

    private ArcherSkills() {
    }

    public static void register(SkillService s) {
        s.register("PIERCING_ARROW", ArcherSkills::piercingArrow);
        s.register("VOLLEY", ArcherSkills::volley);
        s.register("DISENGAGE", ArcherSkills::disengage);
        s.register("HUNTERS_MARK", ArcherSkills::huntersMark);
        s.register("BEAR_TRAP", ArcherSkills::bearTrap);
        s.register("ARROW_RAIN", ArcherSkills::arrowRain);
        s.register("EXPLOSIVE_ARROW", ArcherSkills::explosiveArrow);
    }

    /** Marque des flèches tirées par une compétence (dégâts garantis même si la cible vient d'être touchée). */
    public static final org.bukkit.NamespacedKey SKILL_ARROW_KEY =
            new org.bukkit.NamespacedKey("classes_and_magie", "skill_arrow");

    private static Arrow shoot(Player p, Vector velocity, double damage) {
        Arrow arrow = p.launchProjectile(Arrow.class, velocity);
        // Les dégâts vanilla d'une flèche = vitesse x dégâts de base : on normalise.
        arrow.setDamage(damage / Math.max(0.1, velocity.length()));
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        arrow.setCritical(false);
        arrow.getPersistentDataContainer().set(SKILL_ARROW_KEY, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
        return arrow;
    }

    private static boolean piercingArrow(SkillContext c) {
        Player p = c.player();
        Arrow arrow = shoot(p, p.getEyeLocation().getDirection().multiply(c.d("speed", 3.5)), c.d("damage", 9));
        arrow.setPierceLevel(Math.min(127, c.i("pierce", 4)));
        arrow.setGravity(false);
        c.later(60, arrow::remove);
        c.repeat(0, 1, 40, t -> {
            if (arrow.isValid() && !arrow.isInBlock()) {
                Fx.particle(arrow.getLocation(), Particle.CRIT, 2, 0.02, 0);
            }
        });
        c.sound("item.crossbow.shoot", 1f, 0.8f);
        return true;
    }

    private static boolean volley(SkillContext c) {
        Player p = c.player();
        int count = c.i("arrows", 7);
        double spread = c.d("spread", 30);
        double damage = c.d("damage", 4);
        double speed = c.d("speed", 2.5);
        Vector base = p.getEyeLocation().getDirection();
        for (int i = 0; i < count; i++) {
            double angle = Math.toRadians(-spread / 2 + spread * i / Math.max(1, count - 1));
            Vector dir = base.clone().rotateAroundY(angle).multiply(speed);
            Arrow arrow = shoot(p, dir, damage);
            c.later(100, arrow::remove);
        }
        c.sound("entity.arrow.shoot", 1f, 0.7f);
        c.sound("item.crossbow.shoot", 1f, 1.2f);
        return true;
    }

    private static boolean disengage(SkillContext c) {
        Player p = c.player();
        Vector back = p.getLocation().getDirection().setY(0).normalize().multiply(-c.d("power", 1.4)).setY(c.d("height", 0.6));
        p.setVelocity(back);
        p.setFallDistance(0);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, false, false, false));
        Fx.particle(p.getLocation(), Particle.CLOUD, 15, 0.3, 0.05);
        c.sound("entity.phantom.flap", 1f, 1.4f);
        return true;
    }

    private static boolean huntersMark(SkillContext c) {
        Player p = c.player();
        LivingEntity target = c.combat().targetInSight(p, c.d("range", 30));
        if (target == null) {
            c.noTarget();
            return false;
        }
        double duration = c.d("duration", 12);
        c.combat().mark(target, p, (long) (duration * 1000), c.d("damage-bonus", 0.25));
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int) (duration * 20), 0, false, false, false));
        Fx.dustLine(p.getEyeLocation(), target.getEyeLocation(), Color.fromRGB(220, 30, 30), 0.5);
        c.sound("entity.experience_orb.pickup", 1f, 0.5f);
        return true;
    }

    private static boolean bearTrap(SkillContext c) {
        Player p = c.player();
        Location trap = p.getLocation().getBlock().getLocation().add(0.5, 0.1, 0.5);
        double lifetime = c.d("lifetime", 30);
        double radius = c.d("radius", 1.5);
        int root = (int) (c.d("root-seconds", 3) * 20);
        double damage = c.d("damage", 6);
        boolean[] triggered = {false};
        c.sound("block.chain.place", 1f, 0.8f);
        c.repeat(10, 5, (int) (lifetime * 4), t -> {
            if (triggered[0]) {
                return;
            }
            Fx.dustCircle(trap, radius * 0.6, Color.fromRGB(90, 90, 90), 10);
            for (LivingEntity e : c.combat().enemiesAround(p, trap, radius)) {
                triggered[0] = true;
                c.combat().damage(p, e, damage, DamageKind.RANGED);
                e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, root, 9, false, true, true));
                e.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, root, 128, false, false, false));
                Fx.particle(e.getLocation(), Particle.CRIT, 20, 0.4, 0.1);
                c.sound(trap, "block.anvil.land", 0.8f, 1.6f);
                break;
            }
        });
        return true;
    }

    private static boolean arrowRain(SkillContext c) {
        Player p = c.player();
        Location center = c.combat().targetLocation(p, c.d("range", 30));
        double area = c.d("area", 4);
        int waves = c.i("waves", 5);
        int perWave = c.i("arrows-per-wave", 6);
        double damage = c.d("damage", 3);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        Fx.dustCircle(center.clone().add(0, 0.2, 0), area, Color.fromRGB(200, 200, 200), 40);
        c.repeat(5, 8, waves, wave -> {
            for (int i = 0; i < perWave; i++) {
                Location from = center.clone().add(r.nextDouble(-area, area), 12, r.nextDouble(-area, area));
                Arrow arrow = center.getWorld().spawnArrow(from, new Vector(0, -1, 0), 2.5f, 0f);
                arrow.setShooter(p);
                arrow.setDamage(damage / 2.5);
                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                c.later(60, arrow::remove);
            }
            c.sound(center, "entity.arrow.shoot", 1f, 0.6f);
        });
        return true;
    }

    private static boolean explosiveArrow(SkillContext c) {
        Player p = c.player();
        Arrow arrow = shoot(p, p.getEyeLocation().getDirection().multiply(c.d("speed", 3)), c.d("damage", 6));
        arrow.getPersistentDataContainer().set(new NamespacedKey(c.plugin(), EXPLOSIVE_KEY), PersistentDataType.DOUBLE, c.d("power", 2.5));
        c.repeat(0, 1, 60, t -> {
            if (arrow.isValid() && !arrow.isInBlock()) {
                Fx.particle(arrow.getLocation(), Particle.SMOKE, 2, 0.02, 0);
                Fx.particle(arrow.getLocation(), Particle.FLAME, 1, 0.02, 0);
            }
        });
        c.later(200, arrow::remove);
        c.sound("entity.tnt.primed", 0.7f, 1.6f);
        return true;
    }

}
