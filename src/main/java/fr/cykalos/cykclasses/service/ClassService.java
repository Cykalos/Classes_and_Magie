package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.api.event.PlayerClassChangeEvent;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Choix, évolution et réinitialisation de classe.
 */
public final class ClassService {

    private final CykClasses plugin;

    public ClassService(CykClasses plugin) {
        this.plugin = plugin;
    }

    /** Choix d'une classe de base par le joueur. */
    public boolean choose(Player player, RpgClass rc) {
        PlayerProfile profile = plugin.profiles().get(player);
        if (profile.hasClass()) {
            plugin.messages().send(player, "already-has-class");
            return false;
        }
        if (rc.isSubclass()) {
            return false;
        }
        return apply(player, profile, rc, false);
    }

    /** Évolution vers une sous-classe si les conditions sont remplies. */
    public boolean chooseSubclass(Player player, RpgClass sub) {
        PlayerProfile profile = plugin.profiles().get(player);
        if (!profile.hasClass()) {
            plugin.messages().send(player, "no-class");
            return false;
        }
        if (profile.getSubclassId() != null) {
            plugin.messages().send(player, "subclass-already");
            return false;
        }
        if (sub.parent == null || !sub.parent.id.equals(profile.getClassId())) {
            return false;
        }
        boolean ok = sub.requirements().stream().allMatch(r -> r.isMet(profile, player));
        if (!ok) {
            plugin.messages().send(player, "subclass-locked", Text.ph("class", sub.name));
            return false;
        }
        return apply(player, profile, sub, false);
    }

    /**
     * Applique directement une classe ou sous-classe (sans vérifier les conditions si {@code force}).
     */
    public boolean apply(Player player, PlayerProfile profile, RpgClass rc, boolean force) {
        RpgClass old = plugin.classes().effective(profile);
        PlayerClassChangeEvent event = new PlayerClassChangeEvent(player, old, rc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() && !force) {
            return false;
        }
        if (rc.isSubclass()) {
            profile.setClassId(rc.parent.id);
            profile.setSubclassId(rc.id);
            plugin.messages().send(player, "subclass-chosen", Text.ph("class", rc.name));
            player.playSound(Sound.sound(Key.key("ui.toast.challenge_complete"), Sound.Source.PLAYER, 1f, 1f));
        } else {
            profile.setClassId(rc.id);
            profile.setSubclassId(null);
            plugin.messages().send(player, "class-chosen", Text.ph("class", rc.name));
            Bukkit.getServer().sendMessage(plugin.messages().get("class-chosen-broadcast",
                    Text.ph("player", player.getName()), Text.ph("class", rc.name)));
            player.playSound(Sound.sound(Key.key("block.enchantment_table.use"), Sound.Source.PLAYER, 1f, 1f));
        }
        profile.clearCooldowns();
        plugin.stats().apply(player);
        profile.setMana(plugin.stats().maxMana(profile));
        plugin.character().giveKit(player, rc.id);
        if (!rc.isSubclass()) {
            plugin.character().continueCreation(player);
        }
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0.3);
        return true;
    }

    public boolean reset(Player player, boolean admin) {
        PlayerProfile profile = plugin.profiles().get(player);
        if (!admin) {
            if (!plugin.getConfig().getBoolean("reset.enabled", true)) {
                plugin.messages().send(player, "reset-disabled");
                return false;
            }
            if (!profile.hasClass()) {
                plugin.messages().send(player, "no-class");
                return false;
            }
            long cd = plugin.getConfig().getLong("reset.cooldown-minutes", 60) * 60_000L;
            long left = profile.getLastReset() + cd - System.currentTimeMillis();
            if (left > 0) {
                long minutes = (left + 59_999) / 60_000;
                plugin.messages().send(player, "reset-cooldown", Text.ph("time", minutes + " min"));
                return false;
            }
        }
        RpgClass old = plugin.classes().effective(profile);
        PlayerClassChangeEvent event = new PlayerClassChangeEvent(player, old, null);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() && !admin) {
            return false;
        }
        profile.resetClass(plugin.getConfig().getBoolean("reset.keep-level", false));
        if (!plugin.getConfig().getBoolean("reset.keep-race", true)) {
            profile.setRaceId(null);
        }
        profile.setLastReset(System.currentTimeMillis());
        plugin.combat().clear(player.getUniqueId());
        plugin.stats().apply(player);
        plugin.messages().send(player, "reset-done");
        return true;
    }
}
