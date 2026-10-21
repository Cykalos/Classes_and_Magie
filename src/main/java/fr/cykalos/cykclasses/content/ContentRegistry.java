package fr.cykalos.cykclasses.content;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.StatModifier;
import fr.cykalos.cykclasses.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Charge races.yml, traits.yml, jobs.yml, skilltree.yml, attributes.yml, kits.yml et items.yml.
 */
public final class ContentRegistry {

    private final CykClasses plugin;
    private final Map<String, Race> races = new LinkedHashMap<>();
    private final Map<String, Trait> traits = new LinkedHashMap<>();
    private final Map<String, Job> jobs = new LinkedHashMap<>();
    private final List<JobRule> jobRules = new ArrayList<>();
    private final Map<String, SkillTree> trees = new LinkedHashMap<>();
    private final Map<String, AttributeDef> attributes = new LinkedHashMap<>();
    private final Map<String, Kit> kits = new LinkedHashMap<>();
    private final List<ItemRule> itemRules = new ArrayList<>();
    private final Map<String, Weakness> weaknesses = new LinkedHashMap<>();
    /** race -> faiblesses imposées */
    private final Map<String, List<String>> raceWeaknesses = new HashMap<>();
    /** classe ou sous-classe -> faiblesses imposées */
    private final Map<String, List<String>> classWeaknesses = new HashMap<>();
    private YamlConfiguration weaknessesConfig;
    private final Set<String> warnedClassWeaknesses = new java.util.HashSet<>();
    private YamlConfiguration traitsConfig;
    private YamlConfiguration jobsConfig;
    private YamlConfiguration treeConfig;
    private YamlConfiguration attributesConfig;
    private YamlConfiguration kitsConfig;

    public ContentRegistry(CykClasses plugin) {
        this.plugin = plugin;
    }

    private YamlConfiguration yaml(String name) {
        return fr.cykalos.cykclasses.config.ConfigFiles.load(plugin, name);
    }

    public void load() {
        races.clear();
        traits.clear();
        jobs.clear();
        jobRules.clear();
        trees.clear();
        attributes.clear();
        kits.clear();
        itemRules.clear();
        weaknesses.clear();
        raceWeaknesses.clear();
        classWeaknesses.clear();
        loadRaces(yaml("races.yml"));
        traitsConfig = yaml("traits.yml");
        loadTraits(traitsConfig);
        jobsConfig = yaml("jobs.yml");
        if (!jobsConfig.contains("leveling") || !jobsConfig.contains("jobs.joaillier")) {
            // Ancien format (avant les niveaux de métier) : sauvegarde puis nouveau fichier par défaut.
            File old = fr.cykalos.cykclasses.config.ConfigFiles.file(plugin, "jobs.yml");
            File backup = new File(fr.cykalos.cykclasses.config.ConfigFiles.dataFolder(plugin), "sauvegardes/jobs-ancien-" + System.currentTimeMillis() + ".yml");
            backup.getParentFile().mkdirs();
            if (old.renameTo(backup)) {
                plugin.getLogger().warning("jobs.yml mis à jour (niveaux de métier). L'ancien fichier est conservé : " + backup.getName());
                jobsConfig = yaml("jobs.yml");
            }
        }
        loadJobs(jobsConfig);
        treeConfig = yaml("skilltree.yml");
        loadTrees(treeConfig);
        attributesConfig = yaml("attributes.yml");
        loadAttributes(attributesConfig);
        kitsConfig = yaml("kits.yml");
        loadKits(kitsConfig);
        loadItemRules(yaml("items.yml"));
        weaknessesConfig = yaml("weaknesses.yml");
        loadWeaknesses(weaknessesConfig);
        plugin.getLogger().info(races.size() + " races, " + traits.size() + " traits, " + weaknesses.size() + " faiblesses, "
                + jobs.size() + " métiers, "
                + trees.size() + " arbres, " + attributes.size() + " attributs, " + kits.size() + " kits, "
                + itemRules.size() + " restrictions d'objets.");
    }

    // ------------------------------------------------------------ Chargement

    private List<Flaw> flaws(List<Map<?, ?>> raw, String owner) {
        List<Flaw> out = new ArrayList<>();
        for (Map<?, ?> m : raw) {
            Flaw f = Flaw.load(m);
            if (f == null) {
                plugin.getLogger().warning("Défaut invalide (" + owner + ") : " + m);
            } else {
                out.add(f);
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<?, ?>> mapList(Object o) {
        List<Map<?, ?>> out = new ArrayList<>();
        if (o instanceof List<?> list) {
            for (Object e : list) {
                if (e instanceof Map<?, ?> m) {
                    out.add(m);
                }
            }
        }
        return out;
    }

    private static List<Pattern> patterns(List<String> raw) {
        List<Pattern> out = new ArrayList<>();
        for (String s : raw) {
            out.add(Pattern.compile(s.toUpperCase(Locale.ROOT).replace(".", "\\.").replace("*", ".*")));
        }
        return out;
    }

    private static List<Pattern> idPatterns(List<String> raw) {
        List<Pattern> out = new ArrayList<>();
        for (String s : raw) {
            out.add(Pattern.compile(s.toLowerCase(Locale.ROOT).replace(".", "\\.").replace("*", ".*")));
        }
        return out;
    }

    private static List<String> lower(List<String> raw) {
        List<String> out = new ArrayList<>();
        raw.forEach(s -> out.add(s.toLowerCase(Locale.ROOT)));
        return out;
    }

    private void loadRaces(YamlConfiguration y) {
        ConfigurationSection root = y.getConfigurationSection("races");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) {
                continue;
            }
            List<String> imposed = lower(s.getStringList("weaknesses"));
            if (!imposed.isEmpty()) {
                raceWeaknesses.computeIfAbsent(id.toLowerCase(Locale.ROOT), k -> new ArrayList<>()).addAll(imposed);
            }
            Map<String, Race.Affinity> penalties = new HashMap<>();
            ConfigurationSection pen = s.getConfigurationSection("class-penalties");
            if (pen != null) {
                for (String cls : pen.getKeys(false)) {
                    ConfigurationSection p = pen.getConfigurationSection(cls);
                    if (p != null) {
                        penalties.put(cls.toLowerCase(Locale.ROOT), new Race.Affinity(
                                p.getString("description", "Race inadaptée à cette classe"),
                                StatModifier.load(p.getConfigurationSection("stats")),
                                flaws(p.getMapList("flaws"), "race " + id + "/" + cls)));
                    }
                }
            }
            races.put(id.toLowerCase(Locale.ROOT), new Race(id.toLowerCase(Locale.ROOT), s.getString("name", id),
                    ItemBuilder.material(s.getString("icon"), Material.PLAYER_HEAD), s.getStringList("description"),
                    StatModifier.load(s.getConfigurationSection("stats")), flaws(s.getMapList("flaws"), "race " + id),
                    PermanentEffect.parse(s.getStringList("effects")), lower(s.getStringList("recommended-classes")),
                    penalties));
        }
    }

    private void loadTraits(YamlConfiguration y) {
        ConfigurationSection root = y.getConfigurationSection("traits");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) {
                continue;
            }
            Trait.Rarity rarity;
            Trait.Kind kind;
            try {
                rarity = Trait.Rarity.valueOf(s.getString("rarity", "COMMON").toUpperCase(Locale.ROOT));
                kind = Trait.Kind.valueOf(s.getString("kind", "POSITIVE").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Trait " + id + " : rareté ou type invalide.");
                continue;
            }
            double defWeight = y.getDouble("roll.rarity-weights." + rarity.name(), 10);
            traits.put(id.toLowerCase(Locale.ROOT), new Trait(id.toLowerCase(Locale.ROOT), s.getString("name", id),
                    s.getStringList("description"), rarity, kind, s.getDouble("weight", defWeight),
                    StatModifier.load(s.getConfigurationSection("stats")), flaws(s.getMapList("flaws"), "trait " + id),
                    PermanentEffect.parse(s.getStringList("effects")), lower(s.getStringList("conflicts"))));
        }
    }

    private void loadWeaknesses(YamlConfiguration y) {
        ConfigurationSection root = y.getConfigurationSection("weaknesses");
        if (root != null) {
            for (String id : root.getKeys(false)) {
                ConfigurationSection s = root.getConfigurationSection(id);
                if (s == null) {
                    continue;
                }
                Material icon = Material.matchMaterial(s.getString("icon", "FERMENTED_SPIDER_EYE"));
                String key = id.toLowerCase(Locale.ROOT);
                weaknesses.put(key, new Weakness(key, s.getString("name", id),
                        icon == null ? Material.FERMENTED_SPIDER_EYE : icon, s.getStringList("description"),
                        s.getString("severity", ""), s.getDouble("weight", 10),
                        lower(s.getStringList("races")), lower(s.getStringList("classes")),
                        lower(s.getStringList("exclude-races")), lower(s.getStringList("exclude-classes")),
                        weights(s.getConfigurationSection("race-weights")), weights(s.getConfigurationSection("class-weights")),
                        StatModifier.load(s.getConfigurationSection("stats")), flaws(s.getMapList("flaws"), "faiblesse " + id),
                        PermanentEffect.parse(s.getStringList("effects")), lower(s.getStringList("conflicts"))));
            }
        }
        readPredefined(y.getConfigurationSection("predefined.races"), raceWeaknesses);
        readPredefined(y.getConfigurationSection("predefined.classes"), classWeaknesses);
        // Faiblesses imposées dans races.yml (weaknesses: [...]) : on retire les identifiants inconnus.
        raceWeaknesses.forEach((race, ids) -> ids.removeIf(w -> {
            boolean unknown = !weaknesses.containsKey(w);
            if (unknown) {
                plugin.getLogger().warning("races.yml : faiblesse inconnue « " + w + " » (race " + race + ")");
            }
            return unknown;
        }));
        warnedClassWeaknesses.clear();
    }

    private void readPredefined(ConfigurationSection section, Map<String, List<String>> into) {
        if (section == null) {
            return;
        }
        for (String owner : section.getKeys(false)) {
            List<String> ids = new ArrayList<>();
            for (String w : lower(section.getStringList(owner))) {
                if (weaknesses.containsKey(w)) {
                    ids.add(w);
                } else {
                    plugin.getLogger().warning("weaknesses.yml : faiblesse imposée inconnue « " + w + " » (" + owner + ")");
                }
            }
            into.computeIfAbsent(owner.toLowerCase(Locale.ROOT), k -> new ArrayList<>()).addAll(ids);
        }
    }

    private static Map<String, Double> weights(ConfigurationSection s) {
        Map<String, Double> out = new HashMap<>();
        if (s != null) {
            for (String k : s.getKeys(false)) {
                out.put(k.toLowerCase(Locale.ROOT), s.getDouble(k, 1.0));
            }
        }
        return out;
    }

    private void loadJobs(YamlConfiguration y) {
        ConfigurationSection root = y.getConfigurationSection("jobs");
        if (root != null) {
            for (String id : root.getKeys(false)) {
                ConfigurationSection s = root.getConfigurationSection(id);
                if (s != null) {
                    List<Job.XpSource> sources = new ArrayList<>();
                    for (Map<?, ?> m : s.getMapList("xp-sources")) {
                        try {
                            JobRule.Action action = JobRule.Action.valueOf(String.valueOf(m.get("action")).toUpperCase(Locale.ROOT));
                            double xp = m.get("xp") instanceof Number n ? n.doubleValue() : 1;
                            sources.add(new Job.XpSource(action, patterns(PermanentEffect.strings(m, "targets")), xp));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Source d'XP invalide (métier " + id + ") : " + m);
                        }
                    }
                    Map<String, Job.Specialization> specs = new LinkedHashMap<>();
                    ConfigurationSection sp = s.getConfigurationSection("specializations");
                    if (sp != null) {
                        for (String sid : sp.getKeys(false)) {
                            ConfigurationSection one = sp.getConfigurationSection(sid);
                            if (one == null) {
                                continue;
                            }
                            List<Job.Bonus> bonuses = new ArrayList<>();
                            for (Map<?, ?> b : one.getMapList("bonuses")) {
                                bonuses.add(new Job.Bonus(patterns(PermanentEffect.strings(b, "targets")),
                                        b.get("rarity-levels") instanceof Number n ? n.intValue() : 0,
                                        b.get("power") instanceof Number n ? n.doubleValue() : 0,
                                        b.get("xp-multiplier") instanceof Number n ? n.doubleValue() : 1,
                                        b.get("purity") instanceof Number n ? n.doubleValue() : 0,
                                        b.get("gem-chance") instanceof Number n ? n.doubleValue() : 0));
                            }
                            specs.put(sid.toLowerCase(Locale.ROOT), new Job.Specialization(sid.toLowerCase(Locale.ROOT),
                                    one.getString("name", sid), ItemBuilder.material(one.getString("icon"), Material.BOOK),
                                    one.getStringList("description"), one.getInt("min-level", 20),
                                    StatModifier.load(one.getConfigurationSection("stats")),
                                    PermanentEffect.parse(one.getStringList("effects")), bonuses));
                        }
                    }
                    jobs.put(id.toLowerCase(Locale.ROOT), new Job(id.toLowerCase(Locale.ROOT), s.getString("name", id),
                            ItemBuilder.material(s.getString("icon"), Material.WOODEN_PICKAXE), s.getStringList("description"),
                            StatModifier.load(s.getConfigurationSection("stats")), PermanentEffect.parse(s.getStringList("effects")),
                            sources, specs));
                }
            }
        }
        for (Map<?, ?> m : y.getMapList("restrictions")) {
            try {
                JobRule.Action action = JobRule.Action.valueOf(String.valueOf(m.get("action")).toUpperCase(Locale.ROOT));
                List<String> jobsAllowed = lower(PermanentEffect.strings(m, "jobs"));
                String message = m.containsKey("message") ? String.valueOf(m.get("message")) : null;
                int minLevel = m.get("min-level") instanceof Number n ? n.intValue() : 0;
                String display = m.containsKey("display") ? String.valueOf(m.get("display")) : null;
                jobRules.add(new JobRule(action, patterns(PermanentEffect.strings(m, "targets")), jobsAllowed, message, minLevel, display));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Restriction de métier invalide : " + m);
            }
        }
    }

    private void loadTrees(YamlConfiguration y) {
        ConfigurationSection root = y.getConfigurationSection("trees");
        if (root == null) {
            return;
        }
        for (String cls : root.getKeys(false)) {
            ConfigurationSection t = root.getConfigurationSection(cls);
            if (t == null) {
                continue;
            }
            Map<String, TreeNode> nodes = new LinkedHashMap<>();
            ConfigurationSection ns = t.getConfigurationSection("nodes");
            if (ns != null) {
                for (String id : ns.getKeys(false)) {
                    ConfigurationSection n = ns.getConfigurationSection(id);
                    if (n == null) {
                        continue;
                    }
                    Map<String, SkillMod> mods = new HashMap<>();
                    ConfigurationSection sm = n.getConfigurationSection("skills");
                    if (sm != null) {
                        for (String skill : sm.getKeys(false)) {
                            ConfigurationSection one = sm.getConfigurationSection(skill);
                            if (one != null) {
                                mods.put(skill.toLowerCase(Locale.ROOT), SkillMod.load(one.getValues(false)));
                            }
                        }
                    }
                    String sub = n.getString("required-subclass");
                    nodes.put(id.toLowerCase(Locale.ROOT), new TreeNode(id.toLowerCase(Locale.ROOT), n.getString("name", id),
                            ItemBuilder.material(n.getString("icon"), Material.BOOK), n.getStringList("description"),
                            Math.max(0, Math.min(8, n.getInt("x", 0))), Math.max(0, Math.min(4, n.getInt("y", 0))),
                            Math.max(1, n.getInt("max-rank", 1)), Math.max(1, n.getInt("cost", 1)),
                            lower(n.getStringList("requires")), n.getInt("required-level", 1),
                            sub == null ? null : sub.toLowerCase(Locale.ROOT),
                            StatModifier.load(n.getConfigurationSection("stats")), mods));
                }
            }
            trees.put(cls.toLowerCase(Locale.ROOT), new SkillTree(cls.toLowerCase(Locale.ROOT), t.getString("title", cls), nodes));
        }
    }

    private void loadAttributes(YamlConfiguration y) {
        ConfigurationSection root = y.getConfigurationSection("attributes");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s != null) {
                attributes.put(id.toLowerCase(Locale.ROOT), new AttributeDef(id.toLowerCase(Locale.ROOT), s.getString("name", id),
                        ItemBuilder.material(s.getString("icon"), Material.PAPER), s.getStringList("description"),
                        s.getInt("max", 50), StatModifier.load(s.getConfigurationSection("per-point"))));
            }
        }
    }

    private void loadKits(YamlConfiguration y) {
        ConfigurationSection root = y.getConfigurationSection("kits");
        if (root == null) {
            return;
        }
        for (String cls : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(cls);
            if (s == null) {
                continue;
            }
            List<KitItem> items = new ArrayList<>();
            for (Map<?, ?> m : s.getMapList("items")) {
                Object item = m.get("item");
                if (item == null) {
                    continue;
                }
                Map<String, Integer> ench = new HashMap<>();
                if (m.get("enchants") instanceof Map<?, ?> em) {
                    em.forEach((k, v) -> {
                        if (v instanceof Number n) {
                            ench.put(String.valueOf(k).toLowerCase(Locale.ROOT), n.intValue());
                        }
                    });
                }
                items.add(new KitItem(String.valueOf(item),
                        m.get("amount") instanceof Number n ? n.intValue() : 1,
                        m.containsKey("slot") ? String.valueOf(m.get("slot")).toUpperCase(Locale.ROOT) : null,
                        m.containsKey("name") ? String.valueOf(m.get("name")) : null,
                        PermanentEffect.strings(m, "lore"), ench,
                        Boolean.TRUE.equals(m.get("unbreakable")),
                        m.containsKey("item-model") ? String.valueOf(m.get("item-model")) : null));
            }
            kits.put(cls.toLowerCase(Locale.ROOT), new Kit(cls.toLowerCase(Locale.ROOT), items, s.getStringList("commands")));
        }
    }

    private void loadItemRules(YamlConfiguration y) {
        ConfigurationSection root = y.getConfigurationSection("rules");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) {
                continue;
            }
            Map<String, Integer> attrs = new HashMap<>();
            ConfigurationSection a = s.getConfigurationSection("conditions.attributes");
            if (a != null) {
                for (String k : a.getKeys(false)) {
                    attrs.put(k.toLowerCase(Locale.ROOT), a.getInt(k));
                }
            }
            Set<ItemRule.Use> blocked = EnumSet.noneOf(ItemRule.Use.class);
            List<String> blockList = s.getStringList("block");
            if (blockList.isEmpty()) {
                blocked = EnumSet.allOf(ItemRule.Use.class);
            } else {
                for (String b : blockList) {
                    try {
                        blocked.add(ItemRule.Use.valueOf(b.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            itemRules.add(new ItemRule(id, patterns(s.getStringList("match.materials")),
                    idPatterns(s.getStringList("match.custom-ids")), s.getStringList("match.name-contains"),
                    s.getStringList("match.lore-contains"), lower(s.getStringList("conditions.classes")),
                    lower(s.getStringList("conditions.races")), lower(s.getStringList("conditions.jobs")),
                    s.getInt("conditions.min-level", 0), attrs, blocked, s.getString("message")));
        }
    }

    // ------------------------------------------------------------ Accès

    public Race race(String id) { return id == null ? null : races.get(id.toLowerCase(Locale.ROOT)); }
    public Collection<Race> races() { return races.values(); }
    public Trait trait(String id) { return id == null ? null : traits.get(id.toLowerCase(Locale.ROOT)); }
    public Collection<Trait> traits() { return traits.values(); }
    public Job job(String id) { return id == null ? null : jobs.get(id.toLowerCase(Locale.ROOT)); }
    public Collection<Job> jobs() { return jobs.values(); }
    public List<JobRule> jobRules() { return jobRules; }
    public SkillTree tree(String classId) { return classId == null ? null : trees.get(classId.toLowerCase(Locale.ROOT)); }
    public AttributeDef attribute(String id) { return id == null ? null : attributes.get(id.toLowerCase(Locale.ROOT)); }
    public Collection<AttributeDef> attributes() { return attributes.values(); }
    public Kit kit(String classId) { return classId == null ? null : kits.get(classId.toLowerCase(Locale.ROOT)); }
    public List<ItemRule> itemRules() { return itemRules; }
    public YamlConfiguration traitsConfig() { return traitsConfig; }
    public Weakness weakness(String id) { return id == null ? null : weaknesses.get(id.toLowerCase(Locale.ROOT)); }
    public Collection<Weakness> weaknesses() { return weaknesses.values(); }
    public YamlConfiguration weaknessesConfig() { return weaknessesConfig; }

    /** Faiblesses imposées par la race, la classe et la sous-classe (sans doublon). */
    public List<Weakness> predefinedWeaknesses(String raceId, String classId, String subclassId) {
        java.util.LinkedHashSet<String> ids = new java.util.LinkedHashSet<>();
        if (raceId != null) {
            ids.addAll(raceWeaknesses.getOrDefault(raceId.toLowerCase(Locale.ROOT), List.of()));
        }
        if (classId != null) {
            ids.addAll(classWeaknesses.getOrDefault(classId.toLowerCase(Locale.ROOT), List.of()));
        }
        if (subclassId != null) {
            ids.addAll(classWeaknesses.getOrDefault(subclassId.toLowerCase(Locale.ROOT), List.of()));
        }
        // Faiblesses imposées dans classes.yml (weaknesses: [...]) de la sous-classe (ou de la classe)
        var rc = plugin.classes() == null ? null
                : plugin.classes().get(subclassId != null ? subclassId : classId);
        if (rc == null && classId != null && plugin.classes() != null) {
            rc = plugin.classes().get(classId);
        }
        if (rc != null) {
            ids.addAll(rc.weaknessIds());
        }
        List<Weakness> out = new ArrayList<>();
        for (String id : ids) {
            Weakness w = weaknesses.get(id);
            if (w != null) {
                out.add(w);
            } else if (warnedClassWeaknesses.add(id)) {
                plugin.getLogger().warning("classes.yml : faiblesse inconnue « " + id + " »");
            }
        }
        return out;
    }
    public YamlConfiguration jobsConfig() { return jobsConfig; }
    public YamlConfiguration treeConfig() { return treeConfig; }
    public YamlConfiguration attributesConfig() { return attributesConfig; }
    public YamlConfiguration kitsConfig() { return kitsConfig; }
}
