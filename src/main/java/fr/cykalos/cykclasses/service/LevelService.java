package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.api.event.PlayerClassLevelUpEvent;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class LevelService {

    private final CykClasses plugin;

    public LevelService(CykClasses plugin) {
        this.plugin = plugin;
    }

    public int maxLevel() {
        return plugin.getConfig().getInt("leveling.max-level", 100);
    }

    /** XP nécessaire pour passer du niveau {@code level} au suivant. */
    public double xpRequired(int level) {
        double base = plugin.getConfig().getDouble("leveling.base-xp", 100);
        double exp = plugin.getConfig().getDouble("leveling.exponent", 1.5);
        return Math.round(base * Math.pow(level, exp));
    }

    public double mobXp(String entityKey) {
        ConfigurationSection s = plugin.getConfig().getConfigurationSection("leveling.mob-xp");
        double def = plugin.getConfig().getDouble("leveling.default-mob-xp", 5);
        if (s == null) {
            return def;
        }
        return s.getDouble(entityKey.toLowerCase(Locale.ROOT), def);
    }

    public void addXp(Player player, double amount) {
        addXp(player, amount, null);
    }

    /**
     * @param unlockedBefore sous-classes déjà disponibles avant l'action (pour annoncer les nouvelles), ou null.
     */
    public void addXp(Player player, double amount, Set<String> unlockedBefore) {
        PlayerProfile profile = plugin.profiles().get(player);
        RpgClass rc = plugin.classes().effective(profile);
        if (rc == null || amount <= 0) {
            return;
        }
        if (profile.getLevel() >= maxLevel()) {
            return;
        }
        if (unlockedBefore == null) {
            unlockedBefore = availableSubclasses(player, profile);
        }
        int oldLevel = profile.getLevel();
        double xp = profile.getXp() + amount;
        int level = oldLevel;
        while (level < maxLevel() && xp >= xpRequired(level)) {
            xp -= xpRequired(level);
            level++;
        }
        if (level >= maxLevel()) {
            xp = 0;
        }
        profile.setXp(xp);
        player.sendActionBar(plugin.messages().get("xp-gain", Text.ph("xp", Text.format(amount)), Text.ph("class", rc.name)));
        if (level != oldLevel) {
            profile.setLevel(level);
            onLevelUp(player, profile, rc, oldLevel, level);
        }
        checkNewSubclasses(player, profile, unlockedBefore);
    }

    public void setLevel(Player player, int level) {
        PlayerProfile profile = plugin.profiles().get(player);
        int old = profile.getLevel();
        profile.setLevel(Math.min(level, maxLevel()));
        profile.setXp(0);
        plugin.stats().apply(player);
        if (profile.getLevel() > old) {
            Bukkit.getPluginManager().callEvent(new PlayerClassLevelUpEvent(player, old, profile.getLevel()));
        }
    }

    private void onLevelUp(Player player, PlayerProfile profile, RpgClass rc, int oldLevel, int newLevel) {
        plugin.stats().apply(player);
        if (plugin.getConfig().getBoolean("leveling.level-up-heal", true)) {
            AttributeInstance max = player.getAttribute(Attribute.MAX_HEALTH);
            if (max != null) {
                player.setHealth(max.getValue());
            }
            profile.setMana(plugin.stats().maxMana(profile));
        }
        plugin.messages().send(player, "level-up", Text.ph("level", newLevel));
        player.showTitle(Title.title(
                plugin.messages().get("level-up-title", Text.ph("level", newLevel)),
                plugin.messages().get("level-up-subtitle", Text.ph("class", rc.name)),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(500))));
        player.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 1f, 1.2f));
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 40, 0.4, 0.8, 0.4, 0.3);
        // Annonce des compétences débloquées
        rc.skills().values().stream()
                .filter(s -> s.unlockLevel > oldLevel && s.unlockLevel <= newLevel)
                .forEach(s -> player.sendMessage(Text.mm("<light_purple>✦ Nouvelle compétence : ").append(Text.mm(s.name))));
        Bukkit.getPluginManager().callEvent(new PlayerClassLevelUpEvent(player, oldLevel, newLevel));
    }

    public Set<String> availableSubclasses(Player player, PlayerProfile profile) {
        Set<String> out = new HashSet<>();
        if (!profile.hasClass() || profile.getSubclassId() != null) {
            return out;
        }
        RpgClass base = plugin.classes().get(profile.getClassId());
        if (base == null) {
            return out;
        }
        for (RpgClass sub : base.subclasses().values()) {
            if (sub.requirements().stream().allMatch(r -> r.isMet(profile, player))) {
                out.add(sub.id);
            }
        }
        return out;
    }

    /** Prévient le joueur si de nouvelles sous-classes viennent d'être débloquées. */
    public void checkNewSubclasses(Player player, PlayerProfile profile, Set<String> before) {
        for (String id : availableSubclasses(player, profile)) {
            if (!before.contains(id)) {
                RpgClass sub = plugin.classes().get(id);
                plugin.messages().send(player, "subclass-unlocked", Text.ph("class", sub.name));
                player.playSound(Sound.sound(Key.key("ui.toast.challenge_complete"), Sound.Source.PLAYER, 0.8f, 1f));
            }
        }
    }
}
