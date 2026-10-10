package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.AttributeDef;
import fr.cykalos.cykclasses.content.Job;
import fr.cykalos.cykclasses.content.PermanentEffect;
import fr.cykalos.cykclasses.content.Race;
import fr.cykalos.cykclasses.content.SkillMod;
import fr.cykalos.cykclasses.content.SkillTree;
import fr.cykalos.cykclasses.content.Trait;
import fr.cykalos.cykclasses.content.TreeNode;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.ClassStats;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.model.StatModifier;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Statistiques effectives d'un joueur = classe + race (+ pénalité de classe) + traits + métier
 * + points d'attributs + arbre de compétences. Appliquées en modificateurs d'attributs transitoires.
 */
public final class StatsService {

    /** Arbre commun à toutes les classes. */
    public static final String COMMON_TREE = "commun";

    private record Computed(ClassStats stats, List<Flaw> flaws, List<PermanentEffect> effects, Map<String, SkillMod> skillMods) {
    }

    private final CykClasses plugin;
    private final NamespacedKey healthKey;
    private final NamespacedKey speedKey;
    private final NamespacedKey armorKey;
    private final NamespacedKey attackSpeedKey;
    private final NamespacedKey buffKey;
    private final Map<UUID, Computed> cache = new HashMap<>();

    public StatsService(CykClasses plugin) {
        this.plugin = plugin;
        this.healthKey = new NamespacedKey(plugin, "class_health");
        this.speedKey = new NamespacedKey(plugin, "class_speed");
        this.armorKey = new NamespacedKey(plugin, "class_armor");
        this.attackSpeedKey = new NamespacedKey(plugin, "class_attack_speed");
        this.buffKey = new NamespacedKey(plugin, "skill_health_buff");
    }

    // ------------------------------------------------------------ Calcul

    private Computed compute(PlayerProfile profile) {
        RpgClass rc = plugin.classes().effective(profile);
        if (rc == null) {
            return new Computed(ClassStats.DEFAULT, List.of(), List.of(), Map.of());
        }
        StatModifier mod = new StatModifier();
        List<Flaw> flaws = new ArrayList<>(rc.flaws());
        List<PermanentEffect> effects = new ArrayList<>();
        Map<String, SkillMod> skillMods = new HashMap<>();

        Race race = plugin.content().race(profile.getRaceId());
        if (race != null) {
            mod.add(race.modifiers());
            flaws.addAll(race.flaws());
            effects.addAll(race.effects());
            Race.Affinity penalty = race.penaltyFor(profile.getClassId(), profile.getSubclassId());
            if (penalty != null) {
                mod.add(penalty.modifiers());
                flaws.addAll(penalty.flaws());
            }
        }
        for (String id : profile.getTraits()) {
            Trait t = plugin.content().trait(id);
            if (t != null) {
                mod.add(t.modifiers());
                flaws.addAll(t.flaws());
                effects.addAll(t.effects());
            }
        }
        Job job = plugin.content().job(profile.getJobId());
        if (job != null) {
            mod.add(job.modifiers());
            effects.addAll(job.effects());
            String specId = profile.getJobSpec(job.id());
            Job.Specialization spec = specId == null ? null : job.specializations().get(specId);
            if (spec != null) {
                mod.add(spec.modifiers());
                effects.addAll(spec.effects());
            }
        }
        for (AttributeDef a : plugin.content().attributes()) {
            mod.add(a.perPoint(), profile.getAttribute(a.id()));
        }
        Player online = org.bukkit.Bukkit.getPlayer(profile.getUuid());
        if (online != null && plugin.gear() != null) {
            mod.add(plugin.gear().equipped(online));
        }
        for (SkillTree tree : treesOf(profile)) {
            for (TreeNode node : tree.nodes().values()) {
                int rank = profile.getNodeRank(tree.classId() + ":" + node.id());
                if (rank > 0) {
                    mod.add(node.modifiers(), rank);
                    node.skillMods().forEach((skill, sm) -> skillMods.computeIfAbsent(skill, k -> new SkillMod()).add(sm, rank));
                }
            }
        }
        return new Computed(rc.stats.apply(mod), flaws, effects, skillMods);
    }

    private Computed computed(PlayerProfile profile) {
        if (profile == null) {
            return new Computed(ClassStats.DEFAULT, List.of(), List.of(), Map.of());
        }
        return cache.computeIfAbsent(profile.getUuid(), k -> compute(profile));
    }

    /** Arbres disponibles : celui de la classe de base puis l'arbre commun. */
    public List<SkillTree> treesOf(PlayerProfile profile) {
        List<SkillTree> out = new ArrayList<>();
        SkillTree own = plugin.content().tree(profile.getClassId());
        if (own != null) {
            out.add(own);
        }
        SkillTree common = plugin.content().tree(COMMON_TREE);
        if (common != null) {
            out.add(common);
        }
        return out;
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    public void invalidateAll() {
        cache.clear();
    }

    public ClassStats stats(PlayerProfile profile) {
        return computed(profile).stats();
    }

    public List<Flaw> flaws(PlayerProfile profile) {
        return computed(profile).flaws();
    }

    public List<PermanentEffect> effects(PlayerProfile profile) {
        return computed(profile).effects();
    }

    /** Bonus cumulés de l'arbre pour une compétence (jamais nul). */
    public SkillMod skillMod(PlayerProfile profile, String skillId) {
        SkillMod m = computed(profile).skillMods().get(skillId.toLowerCase(java.util.Locale.ROOT));
        return m == null ? new SkillMod() : m;
    }

    public double maxMana(PlayerProfile profile) {
        if (!profile.hasClass()) {
            return 0;
        }
        return stats(profile).maxMana(profile.getLevel());
    }

    // ------------------------------------------------------------ Points

    public int totalSkillPoints(PlayerProfile p) {
        var c = plugin.getConfig();
        return c.getInt("progression.starting-skill-points", 1)
                + c.getInt("progression.skill-points-per-level", 1) * (p.getLevel() - 1) + p.getBonusSkillPoints();
    }

    public int spentSkillPoints(PlayerProfile p) {
        int spent = 0;
        for (SkillTree tree : treesOf(p)) {
            for (TreeNode node : tree.nodes().values()) {
                spent += p.getNodeRank(tree.classId() + ":" + node.id()) * node.cost();
            }
        }
        return spent;
    }

    public int availableSkillPoints(PlayerProfile p) {
        return Math.max(0, totalSkillPoints(p) - spentSkillPoints(p));
    }

    public int totalAttributePoints(PlayerProfile p) {
        var c = plugin.getConfig();
        return c.getInt("progression.starting-attribute-points", 3)
                + c.getInt("progression.attribute-points-per-level", 2) * (p.getLevel() - 1) + p.getBonusAttributePoints();
    }

    public int availableAttributePoints(PlayerProfile p) {
        return Math.max(0, totalAttributePoints(p) - p.spentAttributePoints());
    }

    // ------------------------------------------------------------ Application

    public void apply(Player player) {
        PlayerProfile profile = plugin.profiles().get(player);
        invalidate(player.getUniqueId());
        RpgClass rc = plugin.classes().effective(profile);
        if (rc == null) {
            clear(player);
            return;
        }
        ClassStats s = stats(profile);
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
        invalidate(player.getUniqueId());
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
