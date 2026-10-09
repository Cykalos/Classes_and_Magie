package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.ClassStats;
import fr.cykalos.cykclasses.model.RpgClass;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

/**
 * Applique les statistiques de classe sous forme de modificateurs d'attributs transitoires
 * (ils ne sont jamais sauvegardés dans les données du joueur).
 */
public final class StatsService {

    private final CykClasses plugin;
    private final NamespacedKey healthKey;
    private final NamespacedKey speedKey;
    private final NamespacedKey armorKey;
    private final NamespacedKey attackSpeedKey;
    private final NamespacedKey buffKey;

    public StatsService(CykClasses plugin) {
        this.plugin = plugin;
        this.healthKey = new NamespacedKey(plugin, "class_health");
        this.speedKey = new NamespacedKey(plugin, "class_speed");
        this.armorKey = new NamespacedKey(plugin, "class_armor");
        this.attackSpeedKey = new NamespacedKey(plugin, "class_attack_speed");
        this.buffKey = new NamespacedKey(plugin, "skill_health_buff");
    }

    public ClassStats stats(PlayerProfile profile) {
        RpgClass rc = plugin.classes().effective(profile);
        return rc == null ? ClassStats.DEFAULT : rc.stats;
    }

    public double maxMana(PlayerProfile profile) {
        if (!profile.hasClass()) {
            return 0;
        }
        return stats(profile).maxMana(profile.getLevel());
    }

    public void apply(Player player) {
        PlayerProfile profile = plugin.profiles().get(player);
        RpgClass rc = plugin.classes().effective(profile);
        if (rc == null) {
            clear(player);
            return;
        }
        ClassStats s = rc.stats;
        int level = profile.getLevel();
        set(player, Attribute.MAX_HEALTH, healthKey, s.bonusHealth(level), AttributeModifier.Operation.ADD_NUMBER);
        set(player, Attribute.MOVEMENT_SPEED, speedKey, s.speed, AttributeModifier.Operation.ADD_SCALAR);
        set(player, Attribute.ARMOR, armorKey, s.armor, AttributeModifier.Operation.ADD_NUMBER);
        set(player, Attribute.ATTACK_SPEED, attackSpeedKey, s.attackSpeed, AttributeModifier.Operation.ADD_SCALAR);

        AttributeInstance health = player.getAttribute(Attribute.MAX_HEALTH);
        if (health != null && player.getHealth() > health.getValue()) {
            player.setHealth(health.getValue());
        }
        double max = maxMana(profile);
        if (profile.getMana() > max) {
            profile.setMana(max);
        }
    }

    public void clear(Player player) {
        remove(player, Attribute.MAX_HEALTH, healthKey);
        remove(player, Attribute.MAX_HEALTH, buffKey);
        remove(player, Attribute.MOVEMENT_SPEED, speedKey);
        remove(player, Attribute.ARMOR, armorKey);
        remove(player, Attribute.ATTACK_SPEED, attackSpeedKey);
        AttributeInstance health = player.getAttribute(Attribute.MAX_HEALTH);
        if (health != null && player.getHealth() > health.getValue()) {
            player.setHealth(health.getValue());
        }
    }

    /** Bonus de PV temporaire (compétences type Esprit sauvage). */
    public void setHealthBuff(Player player, double amount) {
        set(player, Attribute.MAX_HEALTH, buffKey, amount, AttributeModifier.Operation.ADD_NUMBER);
        AttributeInstance health = player.getAttribute(Attribute.MAX_HEALTH);
        if (health != null && player.getHealth() > health.getValue()) {
            player.setHealth(health.getValue());
        }
    }

    private void set(Player player, Attribute attribute, NamespacedKey key, double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) {
            return;
        }
        inst.removeModifier(key);
        if (amount != 0) {
            inst.addTransientModifier(new AttributeModifier(key, amount, op, EquipmentSlotGroup.ANY));
        }
    }

    private void remove(Player player, Attribute attribute, NamespacedKey key) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst != null) {
            inst.removeModifier(key);
        }
    }
}
