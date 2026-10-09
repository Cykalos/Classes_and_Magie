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

import java.util.List;
import java.util.Set;

/**
 * Clerc, Prêtre, Druide.
 */
public final class ClericSkills {

    private static final Set<PotionEffectType> NEGATIVE = Set.of(
            PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS,
            PotionEffectType.BLINDNESS, PotionEffectType.NAUSEA, PotionEffectType.HUNGER, PotionEffectType.MINING_FATIGUE,
            PotionEffectType.DARKNESS, PotionEffectType.LEVITATION, PotionEffectType.BAD_OMEN, PotionEffectType.UNLUCK);

    private ClericSkills() {
    }

    public static void register(SkillService s) {
        s.register("HEAL", ClericSkills::heal);
        s.register("BLESSING", ClericSkills::blessing);
        s.register("PURIFY", ClericSkills::purify);
        s.register("DIVINE_GRACE", ClericSkills::divineGrace);
        s.register("HOLY_LIGHT", ClericSkills::holyLight);
        s.register("ENTANGLING_ROOTS", ClericSkills::entanglingRoots);
        s.register("WILD_SPIRIT", ClericSkills::wildSpirit);
    }

    /** Soigne l'allié visé, sinon soi-même. */
    private static boolean heal(SkillContext c) {
        Player p = c.player();
        Player target = c.combat().allyInSight(p, c.d("range", 15));
        if (target == null) {
            target = p;
        }
        c.combat().heal(p, target, c.d("amount", 8));
        Fx.particle(target.getLocation().add(0, 1, 0), Particle.HEART, 6, 0.4, 0);
        Fx.dust(target.getLocation().add(0, 1, 0), Color.fromRGB(255, 255, 180), 1.2f, 20, 0.5);
        if (!target.equals(p)) {
            Fx.dustLine(p.getEyeLocation(), target.getEyeLocation(), Color.fromRGB(255, 240, 160), 0.5);
        }
        c.sound(target.getLocation(), "block.amethyst_block.resonate", 1f, 1.6f);
        return true;
    }

    /** Bénédiction : régénération pour soi et les alliés proches. */
    private static boolean blessing(SkillContext c) {
        Player p = c.player();
        int duration = (int) (c.d("duration", 10) * 20);
        int level = c.i("regeneration-level", 2) - 1;
        for (Player ally : c.combat().alliesAround(p, c.d("radius", 8))) {
            ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, level, false, true, true));
            Fx.particle(ally.getLocation().add(0, 2, 0), Particle.HAPPY_VILLAGER, 10, 0.4, 0);
        }
        Fx.dustCircle(p.getLocation().add(0, 0.2, 0), c.d("radius", 8), Color.fromRGB(120, 255, 120), 50);
        c.sound("block.beacon.power_select", 1f, 1.8f);
        return true;
    }

    /** Purification : retire les effets négatifs des alliés proches. */
    private static boolean purify(SkillContext c) {
        Player p = c.player();
        for (Player ally : c.combat().alliesAround(p, c.d("radius", 8))) {
            for (PotionEffect effect : List.copyOf(ally.getActivePotionEffects())) {
                if (NEGATIVE.contains(effect.getType())) {
                    ally.removePotionEffect(effect.getType());
                }
            }
            ally.setFireTicks(0);
            ally.setFreezeTicks(0);
            Fx.particle(ally.getLocation().add(0, 1, 0), Particle.END_ROD, 12, 0.4, 0.05);
        }
        c.sound("block.respawn_anchor.charge", 1f, 1.6f);
        return true;
    }

    /** Grâce divine : gros soin de zone. */
    private static boolean divineGrace(SkillContext c) {
        Player p = c.player();
        double amount = c.d("amount", 12);
        for (Player ally : c.combat().alliesAround(p, c.d("radius", 10))) {
            c.combat().heal(p, ally, amount);
            ally.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, (int) (c.d("absorption-seconds", 10) * 20), 0, false, true, true));
            Fx.particle(ally.getLocation().add(0, 1, 0), Particle.TOTEM_OF_UNDYING, 30, 0.5, 0.3);
        }
        c.sound("item.totem.use", 0.6f, 1.4f);
        return true;
    }

    /** Lumière sacrée : explosion de lumière, surtout efficace contre les morts-vivants. */
    private static boolean holyLight(SkillContext c) {
        Player p = c.player();
        double radius = c.d("radius", 6);
        double damage = c.d("damage", 5);
        double undeadMult = c.d("undead-multiplier", 2.5);
        Location center = p.getLocation().add(0, 1, 0);
        for (double r = 1; r <= radius; r += 1) {
            Fx.circle(center, r, Particle.END_ROD, (int) (r * 8));
        }
        for (LivingEntity e : c.combat().enemiesAround(p, p.getLocation(), radius)) {
            boolean undead = UndeadCheck.isUndead(e);
            c.combat().damage(p, e, undead ? damage * undeadMult : damage, DamageKind.MAGIC);
            if (undead) {
                e.setFireTicks(Math.max(e.getFireTicks(), 80));
            }
        }
        c.sound("block.beacon.activate", 1f, 2f);
        return true;
    }

    /** Racines : immobilise et blesse les ennemis autour. */
    private static boolean entanglingRoots(SkillContext c) {
        Player p = c.player();
        double radius = c.d("radius", 6);
        int duration = (int) (c.d("duration", 4) * 20);
        double dps = c.d("damage-per-second", 1.5);
        List<LivingEntity> targets = c.combat().enemiesAround(p, p.getLocation(), radius);
        if (targets.isEmpty()) {
            c.noTarget();
            return false;
        }
        for (LivingEntity e : targets) {
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 9, false, true, true));
            e.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration, 128, false, false, false));
        }
        c.sound("block.grass.break", 1f, 0.5f);
        c.repeat(0, 10, duration / 10, t -> {
            for (LivingEntity e : targets) {
                if (e.isValid()) {
                    Fx.dust(e.getLocation().add(0, 0.4, 0), Color.fromRGB(70, 120, 30), 1.4f, 10, 0.4);
                    if (t % 2 == 0) {
                        c.combat().damage(p, e, dps, DamageKind.MAGIC);
                    }
                }
            }
        });
        return true;
    }

    /** Esprit sauvage : PV bonus, force et résistance temporaires. */
    private static boolean wildSpirit(SkillContext c) {
        Player p = c.player();
        double seconds = c.d("duration", 15);
        int duration = (int) (seconds * 20);
        Status st = c.combat().status(p);
        st.wildSpiritUntil = System.currentTimeMillis() + (long) (seconds * 1000);
        c.plugin().stats().setHealthBuff(p, c.d("bonus-health", 8));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, c.i("strength-level", 1) - 1, false, true, true));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, c.i("resistance-level", 1) - 1, false, true, true));
        c.later(duration, () -> {
            if (p.isOnline() && st.wildSpiritUntil <= System.currentTimeMillis() + 50) {
                c.plugin().stats().setHealthBuff(p, 0);
            }
        });
        Fx.particle(p.getLocation().add(0, 1, 0), Particle.COMPOSTER, 40, 0.5, 0.1);
        c.sound("entity.polar_bear.warning", 1f, 0.8f);
        return true;
    }
}
