package fr.cykalos.cykclasses.skill.impl;

import fr.cykalos.cykclasses.skill.SkillContext;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Particle-based simulated magic projectile: moves step by step and hits
 * the first valid entity or the first solid block. Breaks no block.
 */
final class SpellProjectile {

    private SpellProjectile() {
    }

    /**
     * @param speed   blocks per tick
     * @param range   maximum range
     * @param trail   drawn at each step
     * @param onHit   called once on impact (hit entity or null, position)
     */
    static void launch(SkillContext c, Location start, Vector direction, double speed, double range,
                       Consumer<Location> trail, BiConsumer<LivingEntity, Location> onHit) {
        Vector dir = direction.clone().normalize();
        Location pos = start.clone();
        double[] travelled = {0};
        boolean[] done = {false};
        int maxTicks = (int) Math.ceil(range / speed) + 1;
        c.repeat(0, 1, maxTicks, tick -> {
            if (done[0]) {
                return;
            }
            int steps = Math.max(1, (int) Math.ceil(speed / 0.4));
            double stepLen = speed / steps;
            for (int s = 0; s < steps; s++) {
                pos.add(dir.clone().multiply(stepLen));
                travelled[0] += stepLen;
                trail.accept(pos);
                if (!pos.getBlock().isPassable()) {
                    done[0] = true;
                    onHit.accept(null, pos.clone().subtract(dir.clone().multiply(stepLen)));
                    return;
                }
                for (LivingEntity e : pos.getNearbyLivingEntities(0.8)) {
                    if (c.combat().isValidTarget(c.player(), e)) {
                        done[0] = true;
                        onHit.accept(e, pos.clone());
                        return;
                    }
                }
                if (travelled[0] >= range) {
                    done[0] = true;
                    onHit.accept(null, pos.clone());
                    return;
                }
            }
        });
    }
}
