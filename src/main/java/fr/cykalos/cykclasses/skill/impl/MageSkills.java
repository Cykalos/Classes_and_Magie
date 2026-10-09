package fr.cykalos.cykclasses.skill.impl;

import fr.cykalos.cykclasses.service.CombatService.DamageKind;
import fr.cykalos.cykclasses.service.CombatService.Status;
import fr.cykalos.cykclasses.skill.SkillContext;
import fr.cykalos.cykclasses.skill.SkillService;
import fr.cykalos.cykclasses.util.Fx;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Mage, Pyromancien, Cryomancien.
 */
public final class MageSkills {

    private MageSkills() {
    }

    public static void register(SkillService s) {
        s.register("ARCANE_BOLT", MageSkills::arcaneBolt);
        s.register("BLINK", MageSkills::blink);
        s.register("MANA_SHIELD", MageSkills::manaShield);
        s.register("FIREBALL", MageSkills::fireball);
        s.register("METEOR_SHOWER", MageSkills::meteorShower);
        s.register("FLAME_AURA", MageSkills::flameAura);
        s.register("FROST_NOVA", MageSkills::frostNova);
        s.register("ICE_LANCE", MageSkills::iceLance);
    }

    private static boolean arcaneBolt(SkillContext c) {
        Player p = c.player();
        double damage = c.d("damage", 6);
        c.sound("entity.illusioner.cast_spell", 1f, 1.6f);
        SpellProjectile.launch(c, p.getEyeLocation(), p.getEyeLocation().getDirection(), c.d("speed", 1.5), c.d("range", 25),
                loc -> Fx.dust(loc, Color.fromRGB(170, 80, 255), 1.1f, 2, 0.05),
                (target, loc) -> {
                    Fx.particle(loc, Particle.WITCH, 20, 0.3, 0.1);
                    if (target != null) {
                        c.combat().damage(p, target, damage, DamageKind.MAGIC);
                        c.sound(loc, "entity.evoker.cast_spell", 1f, 1.8f);
                    }
                });
        return true;
    }

    private static boolean blink(SkillContext c) {
        Player p = c.player();
        double distance = c.d("distance", 8);
        Location start = p.getLocation();
        Vector dir = start.getDirection().normalize();
        Location best = null;
        for (double d = 0.5; d <= distance; d += 0.5) {
            Location test = start.clone().add(dir.clone().multiply(d));
            Block feet = test.getBlock();
            Block head = feet.getRelative(0, 1, 0);
            if (!feet.isPassable() || !head.isPassable()) {
                break;
            }
            best = test;
        }
        if (best == null) {
            c.noTarget();
            return false;
        }
        Fx.particle(start.clone().add(0, 1, 0), Particle.PORTAL, 40, 0.4, 0.5);
        best.setYaw(start.getYaw());
        best.setPitch(start.getPitch());
        p.teleport(best);
        p.setFallDistance(0);
        Fx.particle(best.clone().add(0, 1, 0), Particle.REVERSE_PORTAL, 40, 0.4, 0.05);
        c.sound("entity.enderman.teleport", 1f, 1.4f);
        return true;
    }

    private static boolean manaShield(SkillContext c) {
        Player p = c.player();
        Status st = c.combat().status(p);
        double duration = c.d("duration", 10);
        st.manaShieldUntil = System.currentTimeMillis() + (long) (duration * 1000);
        st.manaShieldRatio = c.d("absorb-ratio", 0.5);
        c.repeat(0, 4, (int) (duration * 5), tick -> Fx.circle(p.getLocation().add(0, 1, 0), 0.8, Particle.ENCHANT, 8));
        c.sound("block.beacon.activate", 0.8f, 1.8f);
        return true;
    }

    private static boolean fireball(SkillContext c) {
        Player p = c.player();
        double damage = c.d("damage", 8);
        double radius = c.d("radius", 2.5);
        int fire = (int) (c.d("burn-seconds", 4) * 20);
        c.sound("entity.blaze.shoot", 1f, 1f);
        SpellProjectile.launch(c, p.getEyeLocation(), p.getEyeLocation().getDirection(), c.d("speed", 1.2), c.d("range", 30),
                loc -> {
                    Fx.particle(loc, Particle.FLAME, 3, 0.08, 0.01);
                    Fx.particle(loc, Particle.SMOKE, 1, 0.05, 0);
                },
                (target, loc) -> {
                    Fx.particle(loc, Particle.EXPLOSION, 2, 0.3, 0);
                    Fx.particle(loc, Particle.LAVA, 10, 0.6, 0);
                    c.sound(loc, "entity.generic.explode", 0.9f, 1.3f);
                    for (LivingEntity e : c.combat().enemiesAround(p, loc, radius)) {
                        c.combat().damage(p, e, e.equals(target) ? damage : damage * 0.6, DamageKind.MAGIC);
                        e.setFireTicks(Math.max(e.getFireTicks(), fire));
                    }
                });
        return true;
    }

    private static boolean meteorShower(SkillContext c) {
        Player p = c.player();
        Location center = c.combat().targetLocation(p, c.d("range", 30));
        int count = c.i("count", 8);
        double area = c.d("area", 5);
        double radius = c.d("radius", 2.5);
        double damage = c.d("damage", 7);
        Fx.dustCircle(center.clone().add(0, 0.2, 0), area, Color.fromRGB(255, 90, 0), 60);
        c.sound(center, "entity.wither.shoot", 1f, 0.6f);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < count; i++) {
            long delay = 10L + i * 5L;
            Location impact = center.clone().add(r.nextDouble(-area, area), 0, r.nextDouble(-area, area));
            c.later(delay, () -> {
                Location sky = impact.clone().add(2, 12, 2);
                Fx.line(sky, impact, Particle.FLAME, 0.4);
                Fx.particle(impact, Particle.EXPLOSION, 1, 0.1, 0);
                Fx.particle(impact, Particle.LAVA, 8, 0.8, 0);
                c.sound(impact, "entity.generic.explode", 0.6f, 1.2f);
                for (LivingEntity e : c.combat().enemiesAround(p, impact, radius)) {
                    c.combat().damage(p, e, damage, DamageKind.MAGIC);
                    e.setFireTicks(Math.max(e.getFireTicks(), 60));
                }
            });
        }
        return true;
    }

    private static boolean flameAura(SkillContext c) {
        Player p = c.player();
        double radius = c.d("radius", 3.5);
        double damage = c.d("damage-per-second", 2);
        int seconds = (int) c.d("duration", 8);
        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, seconds * 20 + 20, 0, false, false, true));
        c.sound("item.firecharge.use", 1f, 0.8f);
        c.repeat(0, 10, seconds * 2, tick -> {
            Location loc = p.getLocation().add(0, 0.3, 0);
            Fx.circle(loc, radius, Particle.FLAME, 24);
            Fx.circle(loc.add(0, 0.8, 0), radius * 0.6, Particle.SMALL_FLAME, 12);
            if (tick % 2 == 0) {
                for (LivingEntity e : c.combat().enemiesAround(p, p.getLocation(), radius)) {
                    c.combat().damage(p, e, damage, DamageKind.MAGIC);
                    e.setFireTicks(Math.max(e.getFireTicks(), 40));
                }
            }
        });
        return true;
    }

    private static boolean frostNova(SkillContext c) {
        Player p = c.player();
        double radius = c.d("radius", 5);
        double damage = c.d("damage", 4);
        int slow = (int) (c.d("slow-seconds", 4) * 20);
        int slowLevel = c.i("slow-level", 3) - 1;
        int freeze = (int) (c.d("freeze-seconds", 6) * 20);
        for (double r = 1; r <= radius; r += 1) {
            Fx.circle(p.getLocation().add(0, 0.3, 0), r, Particle.SNOWFLAKE, (int) (r * 10));
        }
        c.sound("block.glass.break", 1f, 0.7f);
        c.sound("entity.player.hurt_freeze", 1f, 1f);
        for (LivingEntity e : c.combat().enemiesAround(p, p.getLocation(), radius)) {
            c.combat().damage(p, e, damage, DamageKind.MAGIC);
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slow, slowLevel, false, true, true));
            e.setFreezeTicks(Math.max(e.getFreezeTicks(), e.getMaxFreezeTicks() + freeze));
            Fx.particle(e.getLocation().add(0, 1, 0), Particle.ITEM_SNOWBALL, 10, 0.3, 0.05);
        }
        return true;
    }

    private static boolean iceLance(SkillContext c) {
        Player p = c.player();
        double damage = c.d("damage", 7);
        double slowedMult = c.d("slowed-multiplier", 1.8);
        c.sound("entity.player.hurt_freeze", 1f, 1.6f);
        SpellProjectile.launch(c, p.getEyeLocation(), p.getEyeLocation().getDirection(), c.d("speed", 2.2), c.d("range", 30),
                loc -> Fx.dust(loc, Color.fromRGB(150, 220, 255), 1.0f, 2, 0.04),
                (target, loc) -> {
                    Fx.particle(loc, Particle.SNOWFLAKE, 25, 0.3, 0.05);
                    if (target != null) {
                        boolean slowed = target.hasPotionEffect(PotionEffectType.SLOWNESS) || target.getFreezeTicks() > 0;
                        c.combat().damage(p, target, slowed ? damage * slowedMult : damage, DamageKind.MAGIC);
                        c.sound(loc, "block.glass.break", 1f, 1.5f);
                    }
                });
        return true;
    }
}
