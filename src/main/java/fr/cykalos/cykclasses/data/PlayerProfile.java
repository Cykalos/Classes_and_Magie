package fr.cykalos.cykclasses.data;

import java.util.HashMap;
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

    private transient Map<String, Long> cooldowns = new HashMap<>();
    private transient boolean castMode;
    private transient long lastCastActivity;
    private transient long lastCombat;
    private transient boolean dirty;

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
    public int totalKills() { return kills.entrySet().stream().filter(e -> !e.getKey().equals("player")).mapToInt(Map.Entry::getValue).sum(); }
    public Map<String, Integer> kills() { return kills; }

    public int getSkillUses(String id) { return skillUses.getOrDefault(id.toLowerCase(Locale.ROOT), 0); }
    public void addSkillUse(String id) { skillUses.merge(id.toLowerCase(Locale.ROOT), 1, Integer::sum); markDirty(); }
    public int totalSkillUses() { return skillUses.values().stream().mapToInt(Integer::intValue).sum(); }
    public Map<String, Integer> skillUses() { return skillUses; }

    public double getCounter(String key) { return counters.getOrDefault(key.toLowerCase(Locale.ROOT), 0.0); }
    public void addCounter(String key, double amount) { counters.merge(key.toLowerCase(Locale.ROOT), amount, Double::sum); markDirty(); }
    public Map<String, Double> counters() { return counters; }

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
        return p.normalize();
    }
}
