package fr.cykalos.cykclasses.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Données persistantes d'un joueur. Les champs {@code transient} ne sont jamais sauvegardés.
 */
public final class PlayerProfile {

    private UUID uuid;
    private String name;
    private String classId;
    private String subclassId;
    private int level = 1;
    private double xp;
    private double mana;
    private long lastReset;
    private Map<String, Integer> kills = new HashMap<>();
    private Map<String, Integer> skillUses = new HashMap<>();
    private Map<String, Double> counters = new HashMap<>();
    private String raceId;
    private List<String> traits = new ArrayList<>();
    private boolean traitsRolled;
    private int rerollsUsed;
    private List<String> weaknesses = new ArrayList<>();
    private boolean weaknessesRolled;
    private int weaknessRerollsUsed;
    private String jobId;
    private long jobChangedAt;
    private boolean jobPromptDone;
    /** Points investis par attribut. */
    private Map<String, Integer> attributes = new HashMap<>();
    /** Rangs des nœuds d'arbre, clé « arbre:nœud ». */
    private Map<String, Integer> treeNodes = new HashMap<>();
    private int bonusSkillPoints;
    private int bonusAttributePoints;
    private long lastTreeReset;
    private long lastAttributeReset;
    private List<String> kitsReceived = new ArrayList<>();
    /** Niveau et XP par métier (conservés si le joueur change de métier). */
    private Map<String, Integer> jobLevels = new HashMap<>();
    private Map<String, Double> jobXp = new HashMap<>();
    /** Spécialisation choisie par métier. */
    private Map<String, String> jobSpecs = new HashMap<>();

    private transient Map<String, Long> cooldowns = new HashMap<>();
    private transient boolean castMode;
    private transient long lastCastActivity;
    private transient long lastCombat;
    private transient boolean dirty;
    private transient boolean partyChat;

    /** Requis par Gson. */
    @SuppressWarnings("unused")
    private PlayerProfile() {
    }

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
    }

    /** Ré-initialise les champs transients après une désérialisation Gson. */
    public PlayerProfile normalize() {
        if (kills == null) kills = new HashMap<>();
        if (skillUses == null) skillUses = new HashMap<>();
        if (counters == null) counters = new HashMap<>();
        if (cooldowns == null) cooldowns = new HashMap<>();
        if (traits == null) traits = new ArrayList<>();
        if (weaknesses == null) weaknesses = new ArrayList<>();
        if (attributes == null) attributes = new HashMap<>();
        if (treeNodes == null) treeNodes = new HashMap<>();
        if (kitsReceived == null) kitsReceived = new ArrayList<>();
        if (jobLevels == null) jobLevels = new HashMap<>();
        if (jobXp == null) jobXp = new HashMap<>();
        if (jobSpecs == null) jobSpecs = new HashMap<>();
        if (level < 1) level = 1;
        return this;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; markDirty(); }
    public String getSubclassId() { return subclassId; }
    public void setSubclassId(String subclassId) { this.subclassId = subclassId; markDirty(); }
    public boolean hasClass() { return classId != null; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, level); markDirty(); }
    public double getXp() { return xp; }
    public void setXp(double xp) { this.xp = Math.max(0, xp); markDirty(); }

    public double getMana() { return mana; }
    public void setMana(double mana) { this.mana = Math.max(0, mana); }

    public long getLastReset() { return lastReset; }
    public void setLastReset(long lastReset) { this.lastReset = lastReset; markDirty(); }

    public int getKills(String type) { return kills.getOrDefault(type.toLowerCase(Locale.ROOT), 0); }
    public void addKill(String type) { kills.merge(type.toLowerCase(Locale.ROOT), 1, Integer::sum); markDirty(); }
    public int totalKills() { return kills.entrySet().stream().filter(e -> !e.getKey().equals("player") && !e.getKey().startsWith("mythic:")).mapToInt(Map.Entry::getValue).sum(); }
    public Map<String, Integer> kills() { return kills; }

    public int getSkillUses(String id) { return skillUses.getOrDefault(id.toLowerCase(Locale.ROOT), 0); }
    public void addSkillUse(String id) { skillUses.merge(id.toLowerCase(Locale.ROOT), 1, Integer::sum); markDirty(); }
    public int totalSkillUses() { return skillUses.values().stream().mapToInt(Integer::intValue).sum(); }
    public Map<String, Integer> skillUses() { return skillUses; }

    public double getCounter(String key) { return counters.getOrDefault(key.toLowerCase(Locale.ROOT), 0.0); }
    public void addCounter(String key, double amount) { counters.merge(key.toLowerCase(Locale.ROOT), amount, Double::sum); markDirty(); }
    public Map<String, Double> counters() { return counters; }

    public String getRaceId() { return raceId; }
    public void setRaceId(String raceId) { this.raceId = raceId; markDirty(); }
    public List<String> getTraits() { return traits; }
    public void setTraits(List<String> traits) { this.traits = new ArrayList<>(traits); markDirty(); }
    public boolean isTraitsRolled() { return traitsRolled; }
    public void setTraitsRolled(boolean rolled) { this.traitsRolled = rolled; markDirty(); }
    public List<String> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<String> ids) { this.weaknesses = new ArrayList<>(ids); markDirty(); }
    public boolean isWeaknessesRolled() { return weaknessesRolled; }
    public void setWeaknessesRolled(boolean rolled) { this.weaknessesRolled = rolled; markDirty(); }
    public int getWeaknessRerollsUsed() { return weaknessRerollsUsed; }
    public void setWeaknessRerollsUsed(int used) { this.weaknessRerollsUsed = Math.max(0, used); markDirty(); }
    public int getRerollsUsed() { return rerollsUsed; }
    public void setRerollsUsed(int used) { this.rerollsUsed = Math.max(0, used); markDirty(); }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; this.jobChangedAt = System.currentTimeMillis(); markDirty(); }
    public long getJobChangedAt() { return jobChangedAt; }
    public boolean isJobPromptDone() { return jobPromptDone; }
    public void setJobPromptDone(boolean done) { this.jobPromptDone = done; markDirty(); }

    public int getAttribute(String id) { return attributes.getOrDefault(id.toLowerCase(Locale.ROOT), 0); }
    public void setAttribute(String id, int value) { attributes.put(id.toLowerCase(Locale.ROOT), Math.max(0, value)); markDirty(); }
    public Map<String, Integer> attributes() { return attributes; }
    public int spentAttributePoints() { return attributes.values().stream().mapToInt(Integer::intValue).sum(); }
    public void clearAttributes() { attributes.clear(); markDirty(); }

    public int getNodeRank(String key) { return treeNodes.getOrDefault(key.toLowerCase(Locale.ROOT), 0); }
    public void setNodeRank(String key, int rank) { treeNodes.put(key.toLowerCase(Locale.ROOT), Math.max(0, rank)); markDirty(); }
    public Map<String, Integer> treeNodes() { return treeNodes; }
    public void clearTree() { treeNodes.clear(); markDirty(); }

    public int getBonusSkillPoints() { return bonusSkillPoints; }
    public void setBonusSkillPoints(int v) { this.bonusSkillPoints = v; markDirty(); }
    public int getBonusAttributePoints() { return bonusAttributePoints; }
    public void setBonusAttributePoints(int v) { this.bonusAttributePoints = v; markDirty(); }
    public long getLastTreeReset() { return lastTreeReset; }
    public void setLastTreeReset(long t) { this.lastTreeReset = t; markDirty(); }
    public long getLastAttributeReset() { return lastAttributeReset; }
    public void setLastAttributeReset(long t) { this.lastAttributeReset = t; markDirty(); }
    public boolean hasReceivedKit(String id) { return kitsReceived.contains(id.toLowerCase(Locale.ROOT)); }
    public List<String> kitsReceived() { return kitsReceived; }
    public void addReceivedKit(String id) { kitsReceived.add(id.toLowerCase(Locale.ROOT)); markDirty(); }

    public int getJobLevel(String job) { return job == null ? 0 : jobLevels.getOrDefault(job, 1); }
    public void setJobLevel(String job, int level) { jobLevels.put(job, Math.max(1, level)); markDirty(); }
    public double getJobXp(String job) { return job == null ? 0 : jobXp.getOrDefault(job, 0.0); }
    public void setJobXp(String job, double xp) { jobXp.put(job, Math.max(0, xp)); markDirty(); }

    public String getJobSpec(String job) { return job == null ? null : jobSpecs.get(job); }
    public void setJobSpec(String job, String spec) {
        if (spec == null) jobSpecs.remove(job); else jobSpecs.put(job, spec);
        markDirty();
    }

    public boolean isPartyChat() { return partyChat; }
    public void setPartyChat(boolean partyChat) { this.partyChat = partyChat; }

    public long getCooldownEnd(String skill) { return cooldowns.getOrDefault(skill, 0L); }
    public void setCooldownEnd(String skill, long end) { cooldowns.put(skill, end); }
    public void clearCooldowns() { cooldowns.clear(); }

    public boolean isCastMode() { return castMode; }
    public void setCastMode(boolean castMode) { this.castMode = castMode; this.lastCastActivity = System.currentTimeMillis(); }
    public long getLastCastActivity() { return lastCastActivity; }
    public void touchCastActivity() { this.lastCastActivity = System.currentTimeMillis(); }

    public long getLastCombat() { return lastCombat; }
    public void touchCombat() { this.lastCombat = System.currentTimeMillis(); }

    public boolean isDirty() { return dirty; }
    public void markDirty() { this.dirty = true; }
    public void clearDirty() { this.dirty = false; }

    /** Remet la progression de classe à zéro. */
    public void resetClass(boolean keepLevel) {
        classId = null;
        subclassId = null;
        if (!keepLevel) {
            level = 1;
            xp = 0;
        }
        mana = 0;
        cooldowns.clear();
        castMode = false;
        treeNodes.clear();
        markDirty();
    }

    // --- Sérialisation YAML (Map) ---

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uuid", uuid.toString());
        map.put("name", name);
        map.put("class", classId);
        map.put("subclass", subclassId);
        map.put("level", level);
        map.put("xp", xp);
        map.put("mana", mana);
        map.put("last-reset", lastReset);
        map.put("kills", new HashMap<>(kills));
        map.put("skill-uses", new HashMap<>(skillUses));
        map.put("counters", new HashMap<>(counters));
        map.put("race", raceId);
        map.put("traits", new ArrayList<>(traits));
        map.put("traits-rolled", traitsRolled);
        map.put("rerolls-used", rerollsUsed);
        map.put("weaknesses", new ArrayList<>(weaknesses));
        map.put("weaknesses-rolled", weaknessesRolled);
        map.put("weakness-rerolls-used", weaknessRerollsUsed);
        map.put("job", jobId);
        map.put("job-changed-at", jobChangedAt);
        map.put("job-prompt-done", jobPromptDone);
        map.put("attributes", new HashMap<>(attributes));
        map.put("tree", new HashMap<>(treeNodes));
        map.put("bonus-skill-points", bonusSkillPoints);
        map.put("bonus-attribute-points", bonusAttributePoints);
        map.put("last-tree-reset", lastTreeReset);
        map.put("last-attribute-reset", lastAttributeReset);
        map.put("kits-received", new ArrayList<>(kitsReceived));
        map.put("job-levels", new HashMap<>(jobLevels));
        map.put("job-xp", new HashMap<>(jobXp));
        map.put("job-specs", new HashMap<>(jobSpecs));
        return map;
    }

    public static PlayerProfile fromMap(UUID uuid, Map<String, Object> map) {
        PlayerProfile p = new PlayerProfile(uuid);
        p.name = (String) map.get("name");
        p.classId = (String) map.get("class");
        p.subclassId = (String) map.get("subclass");
        p.level = map.get("level") instanceof Number n ? n.intValue() : 1;
        p.xp = map.get("xp") instanceof Number n ? n.doubleValue() : 0;
        p.mana = map.get("mana") instanceof Number n ? n.doubleValue() : 0;
        p.lastReset = map.get("last-reset") instanceof Number n ? n.longValue() : 0;
        if (map.get("kills") instanceof Map<?, ?> m) {
            m.forEach((k, v) -> { if (v instanceof Number n) p.kills.put(String.valueOf(k), n.intValue()); });
        }
        if (map.get("skill-uses") instanceof Map<?, ?> m) {
            m.forEach((k, v) -> { if (v instanceof Number n) p.skillUses.put(String.valueOf(k), n.intValue()); });
        }
        if (map.get("counters") instanceof Map<?, ?> m) {
            m.forEach((k, v) -> { if (v instanceof Number n) p.counters.put(String.valueOf(k), n.doubleValue()); });
        }
        p.raceId = (String) map.get("race");
        if (map.get("traits") instanceof List<?> l) {
            l.forEach(o -> p.traits.add(String.valueOf(o)));
        }
        p.traitsRolled = Boolean.TRUE.equals(map.get("traits-rolled"));
        p.rerollsUsed = map.get("rerolls-used") instanceof Number n ? n.intValue() : 0;
        if (map.get("weaknesses") instanceof List<?> wl) {
            wl.forEach(o -> p.weaknesses.add(String.valueOf(o)));
        }
        p.weaknessesRolled = Boolean.TRUE.equals(map.get("weaknesses-rolled"));
        p.weaknessRerollsUsed = map.get("weakness-rerolls-used") instanceof Number n ? n.intValue() : 0;
        p.jobId = (String) map.get("job");
        p.jobChangedAt = map.get("job-changed-at") instanceof Number n ? n.longValue() : 0;
        p.jobPromptDone = Boolean.TRUE.equals(map.get("job-prompt-done"));
        if (map.get("attributes") instanceof Map<?, ?> m) {
            m.forEach((k, v) -> { if (v instanceof Number n) p.attributes.put(String.valueOf(k), n.intValue()); });
        }
        if (map.get("tree") instanceof Map<?, ?> m) {
            m.forEach((k, v) -> { if (v instanceof Number n) p.treeNodes.put(String.valueOf(k), n.intValue()); });
        }
        p.bonusSkillPoints = map.get("bonus-skill-points") instanceof Number n ? n.intValue() : 0;
        p.bonusAttributePoints = map.get("bonus-attribute-points") instanceof Number n ? n.intValue() : 0;
        p.lastTreeReset = map.get("last-tree-reset") instanceof Number n ? n.longValue() : 0;
        p.lastAttributeReset = map.get("last-attribute-reset") instanceof Number n ? n.longValue() : 0;
        if (map.get("kits-received") instanceof List<?> l) {
            l.forEach(o -> p.kitsReceived.add(String.valueOf(o)));
        }
        if (map.get("job-levels") instanceof Map<?, ?> m) {
            m.forEach((k, v) -> { if (v instanceof Number n) p.jobLevels.put(String.valueOf(k), n.intValue()); });
        }
        if (map.get("job-xp") instanceof Map<?, ?> m) {
            m.forEach((k, v) -> { if (v instanceof Number n) p.jobXp.put(String.valueOf(k), n.doubleValue()); });
        }
        if (map.get("job-specs") instanceof Map<?, ?> m) {
            m.forEach((k, v) -> { if (v != null) p.jobSpecs.put(String.valueOf(k), String.valueOf(v)); });
        }
        return p.normalize();
    }
}
