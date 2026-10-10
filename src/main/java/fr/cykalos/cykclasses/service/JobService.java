package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.Job;
import fr.cykalos.cykclasses.content.JobRule;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.time.Duration;

/**
 * Niveaux et XP de métier. L'XP vient des actions listées dans jobs.yml → xp-sources.
 */
public final class JobService {

    private final CykClasses plugin;

    public JobService(CykClasses plugin) {
        this.plugin = plugin;
    }

    private YamlConfiguration cfg() {
        return plugin.content().jobsConfig();
    }

    public int maxLevel() {
        return cfg().getInt("leveling.max-level", 50);
    }

    public double xpRequired(int level) {
        return Math.round(cfg().getDouble("leveling.base-xp", 50) * Math.pow(level, cfg().getDouble("leveling.exponent", 1.5)));
    }

    public int level(PlayerProfile p) {
        return p.getJobId() == null ? 0 : p.getJobLevel(p.getJobId());
    }

    /** Donne l'XP correspondant à une action, si le métier du joueur la récompense. */
    public void reward(Player player, JobRule.Action action, String target, int amount) {
        PlayerProfile p = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (p == null || p.getJobId() == null) {
            return;
        }
        Job job = plugin.content().job(p.getJobId());
        if (job == null) {
            return;
        }
        double total = 0;
        for (Job.XpSource src : job.xpSources()) {
            if (src.action() == action && src.matches(target)) {
                total += src.xp() * Math.max(1, amount);
            }
        }
        if (total > 0) {
            addXp(player, p, job, total);
        }
    }

    public void addXp(Player player, PlayerProfile p, Job job, double amount) {
        int level = p.getJobLevel(job.id());
        if (level >= maxLevel()) {
            return;
        }
        double xp = p.getJobXp(job.id()) + amount;
        int newLevel = level;
        while (newLevel < maxLevel() && xp >= xpRequired(newLevel)) {
            xp -= xpRequired(newLevel);
            newLevel++;
        }
        if (newLevel >= maxLevel()) {
            xp = 0;
        }
        p.setJobXp(job.id(), xp);
        if (newLevel != level) {
            p.setJobLevel(job.id(), newLevel);
            plugin.messages().send(player, "job.level-up", Text.ph("job", job.name()), Text.ph("level", newLevel));
            player.showTitle(Title.title(plugin.messages().get("job.level-up-title", Text.ph("level", newLevel)),
                    Text.mm(job.name()), Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1500), Duration.ofMillis(400))));
            player.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 0.8f, 0.8f));
            // Recettes débloquées à ce niveau
            for (JobRule r : plugin.content().jobRules()) {
                if (r.jobs().contains(job.id()) && r.minLevel() > level && r.minLevel() <= newLevel) {
                    plugin.messages().send(player, "job.unlocked", Text.ph("what", plugin.jobDescriptions().describe(r)));
                }
            }
        }
        double req = xpRequired(newLevel);
        float progress = newLevel >= maxLevel() ? 1f : (float) Math.max(0, Math.min(1, xp / req));
        plugin.hud().showJobXp(player, amount, job, newLevel, progress);
    }
}
