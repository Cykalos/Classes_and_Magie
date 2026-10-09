package fr.cykalos.cykclasses.skill;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.SkillDefinition;
import fr.cykalos.cykclasses.service.CombatService;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

/**
 * Tout ce dont une compétence a besoin pour s'exécuter.
 */
public record SkillContext(CykClasses plugin, Player player, PlayerProfile profile, SkillDefinition def) {

    public CombatService combat() {
        return plugin.combat();
    }

    public double d(String key, double def) {
        return this.def.d(key, def);
    }

    public int i(String key, int def) {
        return this.def.i(key, def);
    }

    public void sound(String key, float volume, float pitch) {
        sound(player.getLocation(), key, volume, pitch);
    }

    public void sound(Location loc, String key, float volume, float pitch) {
        loc.getWorld().playSound(Sound.sound(Key.key(key), Sound.Source.PLAYER, volume, pitch), loc.getX(), loc.getY(), loc.getZ());
    }

    /** Lance une tâche répétée {@code times} fois, toutes les {@code period} ticks. */
    public BukkitTask repeat(long delay, long period, int times, Consumer<Integer> tick) {
        int[] count = {0};
        BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (count[0] >= times || !player.isOnline()) {
                holder[0].cancel();
                return;
            }
            tick.accept(count[0]++);
        }, delay, period);
        return holder[0];
    }

    public void later(long delay, Runnable task) {
        plugin.getServer().getScheduler().runTaskLater(plugin, task, delay);
    }

    public void noTarget() {
        plugin.messages().actionBar(player, "skill-no-target");
    }
}
