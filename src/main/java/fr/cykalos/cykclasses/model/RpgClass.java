package fr.cykalos.cykclasses.model;

import fr.cykalos.cykclasses.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Une classe (ou sous-classe si {@link #parent} n'est pas nul).
 */
public final class RpgClass {

    public final String id;
    public final String name;
    public final Material icon;
    public final List<String> description;
    public final RpgClass parent;
    public final ClassStats stats;
    private final List<Flaw> ownFlaws;
    private final boolean inheritFlaws;
    private final List<String> ownWeaknesses;
    private final Map<String, SkillDefinition> ownSkills;
    private final List<Requirement> requirements;
    private final Map<String, RpgClass> subclasses = new LinkedHashMap<>();

    private RpgClass(String id, String name, Material icon, List<String> description, RpgClass parent,
                     ClassStats stats, List<Flaw> ownFlaws, boolean inheritFlaws, List<String> ownWeaknesses,
                     Map<String, SkillDefinition> ownSkills, List<Requirement> requirements) {
        this.ownWeaknesses = ownWeaknesses;
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.parent = parent;
        this.stats = stats;
        this.ownFlaws = ownFlaws;
        this.inheritFlaws = inheritFlaws;
        this.ownSkills = ownSkills;
        this.requirements = requirements;
    }

    public static RpgClass load(String id, ConfigurationSection s, RpgClass parent, Logger log) {
        ClassStats stats = ClassStats.load(s.getConfigurationSection("stats"), parent == null ? null : parent.stats);

        List<Flaw> flaws = new ArrayList<>();
        for (Map<?, ?> raw : s.getMapList("flaws")) {
            Flaw f = Flaw.load(raw);
            if (f == null) {
                log.warning("Défaut invalide dans la classe " + id + " : " + raw);
            } else {
                flaws.add(f);
            }
        }

        Map<String, SkillDefinition> skills = new LinkedHashMap<>();
        ConfigurationSection skillSec = s.getConfigurationSection("skills");
        if (skillSec != null) {
            for (String key : skillSec.getKeys(false)) {
                ConfigurationSection sk = skillSec.getConfigurationSection(key);
                if (sk != null) {
                    SkillDefinition def = SkillDefinition.load(key, sk);
                    skills.put(def.id, def);
                }
            }
        }

        List<Requirement> reqs = new ArrayList<>();
        for (Map<?, ?> raw : s.getMapList("requirements")) {
            Requirement r = Requirement.load(raw);
            if (r == null) {
                log.warning("Condition invalide dans la classe " + id + " : " + raw);
            } else {
                reqs.add(r);
            }
        }

        RpgClass rc = new RpgClass(
                id.toLowerCase(Locale.ROOT),
                s.getString("name", id),
                ItemBuilder.material(s.getString("icon"), Material.BOOK),
                s.getStringList("description"),
                parent,
                stats,
                flaws,
                s.getBoolean("inherit-flaws", true),
                s.getStringList("weaknesses").stream().map(w -> w.toLowerCase(Locale.ROOT)).toList(),
                skills,
                reqs
        );

        ConfigurationSection subs = s.getConfigurationSection("subclasses");
        if (subs != null && parent == null) {
            for (String key : subs.getKeys(false)) {
                ConfigurationSection sub = subs.getConfigurationSection(key);
                if (sub != null) {
                    RpgClass child = load(key, sub, rc, log);
                    rc.subclasses.put(child.id, child);
                }
            }
        }
        return rc;
    }

    public boolean isSubclass() {
        return parent != null;
    }

    public RpgClass root() {
        return parent == null ? this : parent;
    }

    /** Compétences disponibles : celles du parent puis celles de la sous-classe. */
    public Map<String, SkillDefinition> skills() {
        if (parent == null) {
            return Collections.unmodifiableMap(ownSkills);
        }
        Map<String, SkillDefinition> all = new LinkedHashMap<>(parent.skills());
        all.putAll(ownSkills);
        return all;
    }

    public Map<String, SkillDefinition> ownSkills() {
        return Collections.unmodifiableMap(ownSkills);
    }

    public List<Flaw> flaws() {
        if (parent == null || !inheritFlaws) {
            return Collections.unmodifiableList(ownFlaws);
        }
        List<Flaw> all = new ArrayList<>(parent.flaws());
        all.addAll(ownFlaws);
        return all;
    }

    /** Faiblesses imposées par cette classe (weaknesses: [...]), héritées du parent sauf inherit-flaws: false. */
    public List<String> weaknessIds() {
        if (parent == null || !inheritFlaws) {
            return ownWeaknesses;
        }
        List<String> all = new ArrayList<>(parent.weaknessIds());
        all.addAll(ownWeaknesses);
        return all;
    }

    public List<Requirement> requirements() {
        return Collections.unmodifiableList(requirements);
    }

    public Map<String, RpgClass> subclasses() {
        return Collections.unmodifiableMap(subclasses);
    }
}
