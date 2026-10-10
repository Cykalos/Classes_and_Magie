package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.ClassStats;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.model.SkillDefinition;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Régénération du mana et affichage du HUD (barre d'action).
 */
public final class ManaService {

    private final CykClasses plugin;
    private final List<BukkitTask> tasks = new ArrayList<>();

    public ManaService(CykClasses plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        long regenInterval = Math.max(1, plugin.getConfig().getLong("mana.regen-interval-ticks", 20));
        tasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> regen(regenInterval), regenInterval, regenInterval));
        if (plugin.getConfig().getBoolean("hud.enabled", true)) {
            long hud = Math.max(2, plugin.getConfig().getLong("hud.interval-ticks", 10));
            tasks.add(Bukkit.getScheduler().runTaskTimer(plugin, this::hud, hud, hud));
        }
    }

    public void stop() {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
    }

    private void regen(long intervalTicks) {
        boolean inCombatRegen = plugin.getConfig().getBoolean("mana.regen-in-combat", true);
        double combatMult = plugin.getConfig().getDouble("mana.combat-regen-multiplier", 0.5);
        long now = System.currentTimeMillis();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = plugin.profiles().getIfLoaded(player.getUniqueId());
            if (profile == null) {
                continue;
            }
            RpgClass rc = plugin.classes().effective(profile);
            if (rc == null || player.isDead()) {
                continue;
            }
            double max = plugin.stats().maxMana(profile);
            if (profile.getMana() >= max) {
                continue;
            }
            ClassStats s = rc.stats;
            double amount = s.regen(profile.getLevel()) * (intervalTicks / 20.0);
            if (now - profile.getLastCombat() < 5000) {
                if (!inCombatRegen) {
                    continue;
                }
                amount *= combatMult;
            }
            for (Flaw f : rc.flaws()) {
                if (f.type == Flaw.Type.DARKNESS_MANA_BLOCK && player.getLocation().getBlock().getLightLevel() < 8) {
                    amount = 0;
                }
            }
            profile.setMana(Math.min(max, profile.getMana() + amount));
        }
    }

    private void hud() {
        String format = plugin.getConfig().getString("hud.format", "");
        long now = System.currentTimeMillis();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = plugin.profiles().getIfLoaded(player.getUniqueId());
            if (profile == null || !profile.hasClass()) {
                continue;
            }
            RpgClass rc = plugin.classes().effective(profile);
            if (rc == null) {
                continue;
            }
            if (plugin.skillBar().isActive(player)) {
                long timeout = plugin.getConfig().getLong("casting.timeout-seconds", 15) * 1000L;
                if (timeout > 0 && now - profile.getLastCastActivity() > timeout) {
                    plugin.skillBar().exit(player, true);
                } else {
                    plugin.skillBar().refresh(player);
                    player.sendActionBar(castBar(player, profile, rc));
                    continue;
                }
            }
            Component xpBar = plugin.hud().xpActionbar(player);
            if (xpBar != null) {
                player.sendActionBar(xpBar);
                continue;
            }
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            double xpReq = plugin.levels().xpRequired(profile.getLevel());
            int pct = profile.getLevel() >= plugin.levels().maxLevel() ? 100 : (int) Math.floor(profile.getXp() / xpReq * 100);
            player.sendActionBar(Text.mm(format,
                    Text.ph("health", (int) Math.ceil(player.getHealth())),
                    Text.ph("max_health", maxHealth == null ? 20 : (int) Math.round(maxHealth.getValue())),
                    Text.ph("mana", (int) Math.floor(profile.getMana())),
                    Text.ph("max_mana", (int) Math.round(plugin.stats().maxMana(profile))),
                    Text.ph("level", profile.getLevel()),
                    Text.ph("xp_percent", pct),
                    Text.ph("class", rc.name)));
        }
    }

    public Component castBar(Player player, PlayerProfile profile, RpgClass rc) {
        List<SkillDefinition> skills = new ArrayList<>(rc.skills().values());
        Component line = Component.empty();
        long now = System.currentTimeMillis();
        for (int i = 0; i < Math.min(9, skills.size()); i++) {
            SkillDefinition s = skills.get(i);
            Component entry;
            if (profile.getLevel() < s.unlockLevel) {
                entry = plugin.messages().get("cast-mode-entry-locked", Text.ph("slot", i + 1));
            } else {
                long cd = profile.getCooldownEnd(s.id) - now;
                if (cd > 0) {
                    entry = plugin.messages().get("cast-mode-entry-cooldown", Text.ph("slot", i + 1),
                            Text.ph("name", Text.plain(s.name)), Text.ph("time", Text.format(Math.ceil(cd / 100.0) / 10.0)));
                } else {
                    entry = plugin.messages().get("cast-mode-entry", Text.ph("slot", i + 1),
                            Text.ph("name", s.name), Text.ph("mana", Text.format(s.manaCost)));
                }
            }
            line = i == 0 ? entry : line.append(Text.mm(" <dark_gray>|</dark_gray> ")).append(entry);
        }
        return plugin.messages().get("cast-mode-bar", Text.phc("skills", line));
    }
}
