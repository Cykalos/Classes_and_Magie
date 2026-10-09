package fr.cykalos.cykclasses.skill.impl;

import fr.cykalos.cykclasses.service.CombatService.DamageKind;
import fr.cykalos.cykclasses.service.CombatService.Status;
import fr.cykalos.cykclasses.skill.SkillContext;
import fr.cykalos.cykclasses.skill.SkillService;
import fr.cykalos.cykclasses.util.Fx;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Assassin, Ombre, Duelliste.
 */
public final class AssassinSkills {

    private AssassinSkills() {
    }

    public static void register(SkillService s) {
        s.register("SHADOW_STEP", AssassinSkills::shadowStep);
        s.register("VANISH", AssassinSkills::vanish);
        s.register("POISON_BLADE", AssassinSkills::poisonBlade);
        s.register("SHADOW_VEIL", AssassinSkills::shadowVeil);
        s.register("EXECUTION", AssassinSkills::execution);
        s.register("PARRY", AssassinSkills::parry);
        s.register("LUNGE", AssassinSkills::lunge);
    }

    /** Se téléporte dans le dos de la cible et la frappe. */
    private static boolean shadowStep(SkillContext c) {
        Player p = c.player();
        LivingEntity target = c.combat().targetInSight(p, c.d("range", 15));
        if (target == null) {
            c.noTarget();
            return false;
        }
        Vector facing = target.getLocation().getDirection().setY(0);
        if (facing.lengthSquared() == 0) {
            facing = new Vector(0, 0, 1);
        }
        Location behind = target.getLocation().subtract(facing.normalize().multiply(1.2));
        if (!behind.getBlock().isPassable() || !behind.clone().add(0, 1, 0).getBlock().isPassable()) {
            behind = target.getLocation();
        }
        Vector look = target.getLocation().toVector().subtract(behind.toVector());
        behind.setDirection(look.lengthSquared() == 0 ? facing : look);
        Fx.particle(p.getLocation().add(0, 1, 0), Particle.LARGE_SMOKE, 25, 0.3, 0.02);
        p.teleport(behind);
        Fx.particle(behind.clone().add(0, 1, 0), Particle.LARGE_SMOKE, 25, 0.3, 0.02);
        c.sound("entity.enderman.teleport", 0.8f, 0.6f);
        c.combat().damage(p, target, c.d("damage", 6), DamageKind.MELEE);
        return true;
    }

    /** Disparaît : invisibilité, vitesse, et les monstres perdent leur cible. */
    private static boolean vanish(SkillContext c) {
        Player p = c.player();
        int duration = (int) (c.d("duration", 6) * 20);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration, 0, false, false, true));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, c.i("speed-level", 2) - 1, false, false, true));
        for (LivingEntity e : p.getLocation().getNearbyLivingEntities(c.d("radius", 16))) {
            if (e instanceof Mob mob && p.equals(mob.getTarget())) {
                mob.setTarget(null);
            }
        }
        Fx.particle(p.getLocation().add(0, 1, 0), Particle.SQUID_INK, 30, 0.4, 0.05);
        c.sound("entity.illusioner.mirror_move", 1f, 1f);
        return true;
    }

    /** Les prochaines attaques empoisonnent la cible. */
    private static boolean poisonBlade(SkillContext c) {
        Player p = c.player();
        Status st = c.combat().status(p);
        st.poisonCharges = c.i("hits", 3);
        st.poisonTicks = (int) (c.d("poison-seconds", 5) * 20);
        st.poisonAmplifier = c.i("poison-level", 2) - 1;
        Fx.dust(p.getLocation().add(0, 1, 0), Color.fromRGB(60, 160, 40), 1.2f, 20, 0.4);
        c.sound("entity.spider.ambient", 1f, 1.6f);
        return true;
    }

    /** Voile d'ombre : aveugle et ralentit les ennemis autour. */
    private static boolean shadowVeil(SkillContext c) {
        Player p = c.player();
        double radius = c.d("radius", 6);
        int duration = (int) (c.d("duration", 5) * 20);
        for (LivingEntity e : c.combat().enemiesAround(p, p.getLocation(), radius)) {
            e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, 0, false, true, true));
            e.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, duration, 0, false, false, true));
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 1, false, true, true));
            if (e instanceof Mob mob && p.equals(mob.getTarget())) {
                mob.setTarget(null);
            }
        }
        for (double r = 1; r <= radius; r += 1.5) {
            Fx.circle(p.getLocation().add(0, 0.5, 0), r, Particle.LARGE_SMOKE, (int) (r * 8));
        }
        c.sound("entity.warden.sonic_charge", 0.6f, 1.6f);
        return true;
    }

    /** Exécution : dégâts croissants selon les PV manquants de la cible. */
    private static boolean execution(SkillContext c) {
        Player p = c.player();
        LivingEntity target = c.combat().targetInSight(p, c.d("range", 4));
        if (target == null) {
            c.noTarget();
            return false;
        }
        var maxAttr = target.getAttribute(Attribute.MAX_HEALTH);
        double max = maxAttr == null ? 20 : maxAttr.getValue();
        double missing = 1 - (target.getHealth() / max);
        double damage = c.d("damage", 5) * (1 + missing * c.d("missing-health-multiplier", 3));
        Fx.dust(target.getLocation().add(0, 1, 0), Color.fromRGB(80, 0, 0), 1.6f, 30, 0.4);
        Fx.particle(target.getLocation().add(0, 1, 0), Particle.SWEEP_ATTACK, 3, 0.3, 0);
        c.sound(target.getLocation(), "entity.player.attack.crit", 1f, 0.6f);
        c.combat().damage(p, target, damage, DamageKind.MELEE);
        return true;
    }

    /** Parade : annule le prochain coup reçu et riposte. */
    private static boolean parry(SkillContext c) {
        Player p = c.player();
        Status st = c.combat().status(p);
        st.parryUntil = System.currentTimeMillis() + (long) (c.d("window", 1.5) * 1000);
        st.parryCounter = c.d("counter-damage", 8);
        Fx.particle(p.getLocation().add(0, 1, 0), Particle.ENCHANTED_HIT, 15, 0.4, 0.1);
        c.sound("item.shield.block", 1f, 1.4f);
        return true;
    }

    /** Fente : bond vers l'avant qui transperce les ennemis. */
    private static boolean lunge(SkillContext c) {
        Player p = c.player();
        Vector dir = p.getLocation().getDirection().normalize().multiply(c.d("power", 1.3));
        dir.setY(Math.max(0.1, Math.min(0.4, dir.getY())));
        p.setVelocity(dir);
        double damage = c.d("damage", 6);
        Set<UUID> hit = new HashSet<>();
        c.sound("entity.player.attack.sweep", 1f, 1.6f);
        c.repeat(1, 1, 8, t -> {
            Fx.particle(p.getLocation().add(0, 1, 0), Particle.CRIT, 3, 0.2, 0);
            for (LivingEntity e : c.combat().enemiesAround(p, p.getLocation().add(0, 1, 0), 1.7)) {
                if (hit.add(e.getUniqueId())) {
                    c.combat().damage(p, e, damage, DamageKind.MELEE);
                    c.sound(e.getLocation(), "entity.player.attack.strong", 1f, 1.2f);
                }
            }
        });
        return true;
    }
}
