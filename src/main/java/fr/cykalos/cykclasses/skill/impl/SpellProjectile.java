package fr.cykalos.cykclasses.skill.impl;

import fr.cykalos.cykclasses.skill.SkillContext;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Projectile magique simulé à base de particules : avance pas à pas et touche
 * la première entité valide ou le premier bloc solide. Ne casse aucun bloc.
 */
final class SpellProjectile {

    private SpellProjectile() {
    }

    /**
     * @param speed   blocs par tick
     * @param range   portée maximale
     * @param trail   dessin à chaque étape
     * @param onHit   appelé une fois à l'impact (entité touchée ou null, position)
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
