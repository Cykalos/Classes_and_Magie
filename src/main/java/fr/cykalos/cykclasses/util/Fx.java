package fr.cykalos.cykclasses.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

/**
 * Effets de particules réutilisables.
 */
public final class Fx {

    private Fx() {
    }

    public static void dust(Location loc, Color color, float size, int count, double spread) {
        loc.getWorld().spawnParticle(Particle.DUST, loc, count, spread, spread, spread, 0, new Particle.DustOptions(color, size));
    }

    public static void particle(Location loc, Particle particle, int count, double spread, double speed) {
        loc.getWorld().spawnParticle(particle, loc, count, spread, spread, spread, speed);
    }

    public static void circle(Location center, double radius, Particle particle, int points) {
        World w = center.getWorld();
        for (int i = 0; i < points; i++) {
            double a = 2 * Math.PI * i / points;
            w.spawnParticle(particle, center.clone().add(Math.cos(a) * radius, 0, Math.sin(a) * radius), 1, 0, 0, 0, 0);
        }
    }

    public static void dustCircle(Location center, double radius, Color color, int points) {
        World w = center.getWorld();
        Particle.DustOptions opt = new Particle.DustOptions(color, 1.3f);
        for (int i = 0; i < points; i++) {
            double a = 2 * Math.PI * i / points;
            w.spawnParticle(Particle.DUST, center.clone().add(Math.cos(a) * radius, 0, Math.sin(a) * radius), 1, 0, 0, 0, 0, opt);
        }
    }

    public static void line(Location from, Location to, Particle particle, double step) {
        Vector dir = to.toVector().subtract(from.toVector());
        double length = dir.length();
        if (length == 0) {
            return;
        }
        dir.normalize().multiply(step);
        Location cur = from.clone();
        for (double d = 0; d < length; d += step) {
            from.getWorld().spawnParticle(particle, cur, 1, 0, 0, 0, 0);
            cur.add(dir);
        }
    }

    public static void dustLine(Location from, Location to, Color color, double step) {
        Vector dir = to.toVector().subtract(from.toVector());
        double length = dir.length();
        if (length == 0) {
            return;
        }
        dir.normalize().multiply(step);
        Location cur = from.clone();
        Particle.DustOptions opt = new Particle.DustOptions(color, 1.2f);
        for (double d = 0; d < length; d += step) {
            from.getWorld().spawnParticle(Particle.DUST, cur, 1, 0, 0, 0, 0, opt);
            cur.add(dir);
        }
    }
}
