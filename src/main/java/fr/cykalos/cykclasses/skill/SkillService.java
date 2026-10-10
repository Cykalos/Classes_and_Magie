package fr.cykalos.cykclasses.skill;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.api.event.SkillCastEvent;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.model.SkillDefinition;
import fr.cykalos.cykclasses.skill.impl.ArcherSkills;
import fr.cykalos.cykclasses.skill.impl.AssassinSkills;
import fr.cykalos.cykclasses.skill.impl.ClericSkills;
import fr.cykalos.cykclasses.skill.impl.MageSkills;
import fr.cykalos.cykclasses.skill.impl.MythicSkills;
import fr.cykalos.cykclasses.skill.impl.WarriorSkills;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * Registre des types de compétences et logique de lancement (mana, recharge, niveau).
 */
public final class SkillService {

    private final CykClasses plugin;
    private final Map<String, SkillExecutor> executors = new HashMap<>();

    public SkillService(CykClasses plugin) {
        this.plugin = plugin;
        WarriorSkills.register(this);
        MageSkills.register(this);
        ArcherSkills.register(this);
        AssassinSkills.register(this);
        ClericSkills.register(this);
        MythicSkills.register(this);
    }

    /** Permet à d'autres plugins d'ajouter leurs propres types de compétences. */
    public void register(String type, SkillExecutor executor) {
        executors.put(type.toUpperCase(Locale.ROOT), executor);
    }

    public boolean hasType(String type) {
        return executors.containsKey(type.toUpperCase(Locale.ROOT));
    }

    public Map<String, SkillExecutor> types() {
        return Collections.unmodifiableMap(executors);
    }

    public List<SkillDefinition> skillsOf(PlayerProfile profile) {
        RpgClass rc = plugin.classes().effective(profile);
        return rc == null ? List.of() : new ArrayList<>(rc.skills().values());
    }

    public SkillDefinition find(PlayerProfile profile, String id) {
        RpgClass rc = plugin.classes().effective(profile);
        if (rc == null || id == null) {
            return null;
        }
        return rc.skills().get(id.toLowerCase(Locale.ROOT));
    }

    /** Lance la compétence numéro {@code index} (0 = première) de la classe du joueur. */
    public boolean castSlot(Player player, int index) {
        PlayerProfile profile = plugin.profiles().get(player);
        List<SkillDefinition> skills = skillsOf(profile);
        if (index < 0 || index >= skills.size()) {
            return false;
        }
        return cast(player, skills.get(index));
    }

    public boolean cast(Player player, String id) {
        PlayerProfile profile = plugin.profiles().get(player);
        if (!profile.hasClass()) {
            plugin.messages().send(player, "no-class");
            return false;
        }
        SkillDefinition def = find(profile, id);
        if (def == null) {
            plugin.messages().send(player, "skill-unknown", Text.ph("skill", id));
            return false;
        }
        return cast(player, def);
    }

    public boolean cast(Player player, SkillDefinition def) {
        PlayerProfile profile = plugin.profiles().get(player);
        if (player.isDead()) {
            return false;
        }
        if (profile.getLevel() < def.unlockLevel) {
            plugin.messages().actionBar(player, "skill-locked", Text.ph("skill", def.name), Text.ph("level", def.unlockLevel));
            return false;
        }
        long now = System.currentTimeMillis();
        long cdEnd = profile.getCooldownEnd(def.id);
        if (cdEnd > now) {
            plugin.messages().actionBar(player, "skill-cooldown", Text.ph("skill", def.name),
                    Text.ph("time", Text.format(Math.ceil((cdEnd - now) / 100.0) / 10.0)));
            fail(player);
            return false;
        }
        SkillCastEvent event = new SkillCastEvent(player, def, def.manaCost);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        double cost = event.getManaCost();
        if (profile.getMana() < cost) {
            plugin.messages().actionBar(player, "skill-no-mana", Text.ph("skill", def.name), Text.ph("cost", Text.format(cost)));
            fail(player);
            return false;
        }
        if (def.healthCost > 0 && player.getHealth() <= def.healthCost) {
            plugin.messages().actionBar(player, "skill-no-health", Text.ph("skill", def.name));
            fail(player);
            return false;
        }
        SkillExecutor executor = executors.get(def.type);
        if (executor == null) {
            plugin.messages().send(player, "skill-unknown", Text.ph("skill", def.type));
            return false;
        }
        boolean ok;
        try {
            ok = executor.cast(new SkillContext(plugin, player, profile, def));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur pendant la compétence " + def.id, e);
            return false;
        }
        if (!ok) {
            return false;
        }
        profile.setMana(profile.getMana() - cost);
        if (cost > 0) {
            profile.addCounter("mana_spent", cost);
        }
        if (def.healthCost > 0) {
            player.setHealth(Math.max(1, player.getHealth() - def.healthCost));
        }
        profile.setCooldownEnd(def.id, now + (long) (def.cooldown * 1000));
        var before = plugin.levels().availableSubclasses(player, profile);
        profile.addSkillUse(def.id);
        plugin.levels().checkNewSubclasses(player, profile, before);
        profile.touchCastActivity();
        plugin.skillBar().showCooldown(player, def);
        plugin.skillBar().refresh(player);
        return true;
    }

    private void fail(Player player) {
        player.playSound(Sound.sound(Key.key("block.note_block.bass"), Sound.Source.PLAYER, 0.6f, 0.6f));
    }

    // --- Mode compétences (F puis 1-8) : délégué à SkillBar ---

    public void toggleCastMode(Player player) {
        plugin.skillBar().toggle(player);
    }

    public void setCastMode(Player player, boolean on) {
        if (on) {
            plugin.skillBar().enter(player);
        } else {
            plugin.skillBar().exit(player, true);
        }
    }
}
