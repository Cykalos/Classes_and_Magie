package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.model.StatModifier;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Forge (objets de qualité avec rareté et stats aléatoires, objets uniques) et gemmes (forge.yml, gems.yml).
 * Les données sont stockées dans l'objet (PersistentDataContainer) et la description est régénérée.
 */
public final class GearService {

    public enum Category { WEAPON, RANGED, ARMOR }

    public record Rarity(String id, String label, String color, double weight, double weightPerLevel, double minWeight,
                         int minJobLevel, int minStats, int maxStats, int sockets, double power, double negativeChance) {
    }

    public record UniqueTemplate(String id, String name, List<Pattern> materials, List<String> lore, int sockets,
                                 StatModifier stats, Map<String, Object> rawStats) {
    }

    public record Gem(String id, String name, Material material, String itemModel, List<String> description,
                      double weight, Set<Category> appliesTo, StatModifier stats, Map<String, Object> rawStats) {
    }

    private static final Set<String> MULTIPLICATIVE = Set.of("melee-damage", "ranged-damage", "magic-damage", "damage-taken");

    private final CykClasses plugin;
    private final NamespacedKey kRarity, kStats, kSockets, kGems, kUnique, kUid, kCrafter, kGem;
    private YamlConfiguration forge;
    private YamlConfiguration gems;
    private final Map<String, List<Pattern>> crafters = new HashMap<>();
    private final List<Rarity> rarities = new ArrayList<>();
    private final Map<String, UniqueTemplate> uniques = new LinkedHashMap<>();
    private final Map<String, Gem> gemDefs = new LinkedHashMap<>();
    /** template unique -> uid de l'objet créé */
    private final Map<String, String> uniqueTaken = new HashMap<>();
    private File uniquesFile;

    public GearService(CykClasses plugin) {
        this.plugin = plugin;
        kRarity = new NamespacedKey(plugin, "gear_rarity");
        kStats = new NamespacedKey(plugin, "gear_stats");
        kSockets = new NamespacedKey(plugin, "gear_sockets");
        kGems = new NamespacedKey(plugin, "gear_gems");
        kUnique = new NamespacedKey(plugin, "gear_unique");
        kUid = new NamespacedKey(plugin, "gear_uid");
        kCrafter = new NamespacedKey(plugin, "gear_crafter");
        kGem = new NamespacedKey(plugin, "gem_id");
    }

    // ------------------------------------------------------------ Chargement

    private YamlConfiguration yaml(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private static List<Pattern> patterns(List<String> raw) {
        List<Pattern> out = new ArrayList<>();
        for (String s : raw) {
            out.add(Pattern.compile(s.toUpperCase(Locale.ROOT).replace(".", "\\.").replace("*", ".*")));
        }
        return out;
    }

    public void load() {
        forge = yaml("forge.yml");
        gems = yaml("gems.yml");
        crafters.clear();
        rarities.clear();
        uniques.clear();
        gemDefs.clear();
        ConfigurationSection cs = forge.getConfigurationSection("crafters");
        if (cs != null) {
            for (String job : cs.getKeys(false)) {
                crafters.put(job.toLowerCase(Locale.ROOT), patterns(cs.getStringList(job + ".targets")));
            }
        }
        ConfigurationSection rs = forge.getConfigurationSection("rarities");
        if (rs != null) {
            for (String id : rs.getKeys(false)) {
                ConfigurationSection r = rs.getConfigurationSection(id);
                if (r == null) {
                    continue;
                }
                List<Integer> range = r.getIntegerList("stats");
                rarities.add(new Rarity(id.toUpperCase(Locale.ROOT), r.getString("name", id), r.getString("color", "<white>"),
                        r.getDouble("weight", 1), r.getDouble("weight-per-level", 0), r.getDouble("min-weight", 0),
                        r.getInt("min-job-level", 0), range.isEmpty() ? 0 : range.get(0), range.size() < 2 ? (range.isEmpty() ? 0 : range.get(0)) : range.get(1),
                        r.getInt("sockets", 0), r.getDouble("power", 1), r.getDouble("negative-chance", 0.2)));
            }
        }
        ConfigurationSection us = forge.getConfigurationSection("uniques");
        if (us != null) {
            for (String id : us.getKeys(false)) {
                ConfigurationSection u = us.getConfigurationSection(id);
                if (u == null) {
                    continue;
                }
                ConfigurationSection st = u.getConfigurationSection("stats");
                uniques.put(id.toLowerCase(Locale.ROOT), new UniqueTemplate(id.toLowerCase(Locale.ROOT), u.getString("name", id),
                        patterns(u.getStringList("materials")), u.getStringList("lore"), u.getInt("sockets", 3),
                        StatModifier.load(st), st == null ? Map.of() : st.getValues(false)));
            }
        }
        ConfigurationSection gs = gems.getConfigurationSection("gems");
        if (gs != null) {
            for (String id : gs.getKeys(false)) {
                ConfigurationSection g = gs.getConfigurationSection(id);
                if (g == null) {
                    continue;
                }
                java.util.EnumSet<Category> applies = java.util.EnumSet.noneOf(Category.class);
                for (String a : g.getStringList("applies-to")) {
                    try {
                        applies.add(Category.valueOf(a.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                if (applies.isEmpty()) {
                    applies = java.util.EnumSet.allOf(Category.class);
                }
                ConfigurationSection st = g.getConfigurationSection("stats");
                Material m = Material.matchMaterial(g.getString("material", "AMETHYST_SHARD"));
                gemDefs.put(id.toLowerCase(Locale.ROOT), new Gem(id.toLowerCase(Locale.ROOT), g.getString("name", id),
                        m == null ? Material.AMETHYST_SHARD : m, g.getString("item-model"), g.getStringList("description"),
                        g.getDouble("weight", 10), applies, StatModifier.load(st), st == null ? Map.of() : st.getValues(false)));
            }
        }
        loadUniques();
    }

    private void loadUniques() {
        uniquesFile = new File(plugin.getDataFolder(), "forge-uniques.yml");
        uniqueTaken.clear();
        YamlConfiguration y = YamlConfiguration.loadConfiguration(uniquesFile);
        for (String k : y.getKeys(false)) {
            uniqueTaken.put(k.toLowerCase(Locale.ROOT), y.getString(k + ".uid", ""));
        }
    }

    private void saveUnique(String templateId, String uid, Player crafter) {
        YamlConfiguration y = YamlConfiguration.loadConfiguration(uniquesFile);
        y.set(templateId + ".uid", uid);
        y.set(templateId + ".crafter", crafter.getName());
        y.set(templateId + ".crafter-uuid", crafter.getUniqueId().toString());
        y.set(templateId + ".date", java.time.LocalDate.now().toString());
        try {
            y.save(uniquesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Sauvegarde impossible de forge-uniques.yml", e);
        }
        uniqueTaken.put(templateId, uid);
    }

    /** Admin : rend un objet unique de nouveau disponible à la forge. */
    public boolean releaseUnique(String templateId) {
        String id = templateId.toLowerCase(Locale.ROOT);
        if (uniqueTaken.remove(id) == null) {
            return false;
        }
        YamlConfiguration y = YamlConfiguration.loadConfiguration(uniquesFile);
        y.set(id, null);
        try {
            y.save(uniquesFile);
        } catch (IOException ignored) {
        }
        return true;
    }

    public Map<String, UniqueTemplate> uniques() { return uniques; }
    public Map<String, String> uniqueTaken() { return uniqueTaken; }
    public Map<String, Gem> gems() { return gemDefs; }
    public List<Rarity> rarities() { return rarities; }

    // ------------------------------------------------------------ Catégories

    public static Category category(Material m) {
        String n = m.name();
        if (n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE") || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS")
                || m == Material.SHIELD || m == Material.TURTLE_HELMET || m == Material.ELYTRA) {
            return Category.ARMOR;
        }
        if (m == Material.BOW || m == Material.CROSSBOW || m == Material.TRIDENT) {
            return Category.RANGED;
        }
        return Category.WEAPON;
    }

    /** Le métier du joueur fabrique-t-il cet objet avec une qualité ? */
    public boolean isQualityCraft(String jobId, Material result) {
        if (jobId == null) {
            return false;
        }
        List<Pattern> targets = crafters.get(jobId);
        if (targets == null) {
            return false;
        }
        for (Pattern p : targets) {
            if (p.matcher(result.name()).matches()) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------ Forge

    /** Tire la rareté selon le niveau de métier. */
    public Rarity rollRarity(int jobLevel) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double total = 0;
        double[] weights = new double[rarities.size()];
        for (int i = 0; i < rarities.size(); i++) {
            Rarity ra = rarities.get(i);
            double w = jobLevel < ra.minJobLevel() ? 0 : Math.max(ra.minWeight(), ra.weight() + ra.weightPerLevel() * jobLevel);
            weights[i] = Math.max(0, w);
            total += weights[i];
        }
        if (total <= 0) {
            return rarities.isEmpty() ? null : rarities.getFirst();
        }
        double x = r.nextDouble(total);
        for (int i = 0; i < weights.length; i++) {
            x -= weights[i];
            if (x < 0) {
                return rarities.get(i);
            }
        }
        return rarities.getLast();
    }

    /**
     * Applique une qualité à un objet fabriqué. Retourne l'objet modifié.
     */
    public ItemStack forge(ItemStack item, Player crafter, int jobLevel, String forcedRarity) {
        Rarity rarity = forcedRarity == null ? rollRarity(jobLevel) : rarityById(forcedRarity);
        if (rarity == null) {
            return item;
        }
        Category cat = category(item.getType());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (rarity.id().equals("UNIQUE")) {
            UniqueTemplate tpl = freeUnique(item.getType());
            if (tpl == null) {
                // Plus d'unique disponible pour cet objet : la meilleure rareté suivante.
                rarity = bestNonUnique();
                if (rarity == null) {
                    return item;
                }
            } else {
                String uid = UUID.randomUUID().toString();
                pdc.set(kRarity, PersistentDataType.STRING, "UNIQUE");
                pdc.set(kUnique, PersistentDataType.STRING, tpl.id());
                pdc.set(kUid, PersistentDataType.STRING, uid);
                pdc.set(kStats, PersistentDataType.STRING, encode(tpl.rawStats()));
                pdc.set(kSockets, PersistentDataType.INTEGER, tpl.sockets());
                pdc.set(kCrafter, PersistentDataType.STRING, crafter.getName());
                item.setItemMeta(meta);
                saveUnique(tpl.id(), uid, crafter);
                refreshLore(item);
                Bukkit.getServer().sendMessage(plugin.messages().get("forge.unique-broadcast",
                        Text.ph("player", crafter.getName()), Text.ph("item", tpl.name())));
                return item;
            }
        }
        Map<String, Object> stats = rollStats(cat, rarity);
        pdc.set(kRarity, PersistentDataType.STRING, rarity.id());
        pdc.set(kStats, PersistentDataType.STRING, encode(stats));
        pdc.set(kSockets, PersistentDataType.INTEGER, rarity.sockets());
        pdc.set(kCrafter, PersistentDataType.STRING, crafter.getName());
        item.setItemMeta(meta);
        refreshLore(item);
        return item;
    }

    private Rarity rarityById(String id) {
        for (Rarity r : rarities) {
            if (r.id().equalsIgnoreCase(id)) {
                return r;
            }
        }
        return null;
    }

    private Rarity bestNonUnique() {
        Rarity best = null;
        for (Rarity r : rarities) {
            if (!r.id().equals("UNIQUE")) {
                best = r;
            }
        }
        return best;
    }

    private UniqueTemplate freeUnique(Material m) {
        List<UniqueTemplate> free = new ArrayList<>();
        for (UniqueTemplate t : uniques.values()) {
            if (uniqueTaken.containsKey(t.id())) {
                continue;
            }
            for (Pattern p : t.materials()) {
                if (p.matcher(m.name()).matches()) {
                    free.add(t);
                    break;
                }
            }
        }
        return free.isEmpty() ? null : free.get(ThreadLocalRandom.current().nextInt(free.size()));
    }

    private Map<String, Object> rollStats(Category cat, Rarity rarity) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        ConfigurationSection pool = forge.getConfigurationSection("stat-pool." + cat.name().toLowerCase(Locale.ROOT));
        Map<String, Object> out = new LinkedHashMap<>();
        if (pool == null) {
            return out;
        }
        List<String> keys = new ArrayList<>(pool.getKeys(false));
        java.util.Collections.shuffle(keys);
        int count = rarity.maxStats() <= rarity.minStats() ? rarity.minStats() : r.nextInt(rarity.minStats(), rarity.maxStats() + 1);
        double negScale = forge.getDouble("negative-scale", 0.6);
        for (int i = 0; i < Math.min(count, keys.size()); i++) {
            String key = keys.get(i);
            List<Double> range = pool.getDoubleList(key);
            if (range.size() < 2) {
                continue;
            }
            double v = r.nextDouble(range.get(0), range.get(1) + 1e-9) * rarity.power();
            if (r.nextDouble() < rarity.negativeChance()) {
                v = -v * negScale;
            }
            double stored;
            if (key.equals("damage-taken")) {
                stored = 1 - v;
            } else if (MULTIPLICATIVE.contains(key)) {
                stored = 1 + v;
            } else {
                stored = v;
            }
            out.put(key, Math.round(stored * 1000) / 1000.0);
        }
        return out;
    }

    // ------------------------------------------------------------ Lecture

    public boolean isGear(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(kRarity, PersistentDataType.STRING);
    }

    public String gemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(kGem, PersistentDataType.STRING);
    }

    private static String encode(Map<String, Object> stats) {
        StringBuilder sb = new StringBuilder();
        stats.forEach((k, v) -> {
            if (v instanceof Number n) {
                if (!sb.isEmpty()) {
                    sb.append(';');
                }
                sb.append(k).append('=').append(n.doubleValue());
            }
        });
        return sb.toString();
    }

    private static Map<String, Object> decode(String s) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (s == null || s.isBlank()) {
            return out;
        }
        for (String part : s.split(";")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                try {
                    out.put(kv[0], Double.parseDouble(kv[1]));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return out;
    }

    private List<String> gemsOf(PersistentDataContainer pdc) {
        String raw = pdc.get(kGems, PersistentDataType.STRING);
        return raw == null || raw.isBlank() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    /** Bonus total d'un objet (stats + gemmes), ou null si ce n'est pas un objet forgé. */
    public StatModifier modifiers(ItemStack item) {
        if (!isGear(item)) {
            return null;
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        StatModifier m = StatModifier.load(decode(pdc.get(kStats, PersistentDataType.STRING)));
        for (String g : gemsOf(pdc)) {
            Gem gem = gemDefs.get(g);
            if (gem != null) {
                m.add(gem.stats());
            }
        }
        return m;
    }

    /** Bonus de l'équipement porté : arme en main (si arme), armures, main secondaire (bouclier). */
    public StatModifier equipped(Player player) {
        StatModifier total = new StatModifier();
        PlayerInventory inv = player.getInventory();
        ItemStack hand = inv.getItemInMainHand();
        if (isGear(hand) && category(hand.getType()) != Category.ARMOR) {
            total.add(modifiers(hand));
        }
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack it = inv.getItem(slot);
            if (isGear(it)) {
                total.add(modifiers(it));
            }
        }
        ItemStack off = inv.getItemInOffHand();
        if (isGear(off) && off.getType() == Material.SHIELD) {
            total.add(modifiers(off));
        }
        return total;
    }

    /** Empreinte de l'équipement (pour savoir quand recalculer les stats). */
    public int signature(Player player) {
        PlayerInventory inv = player.getInventory();
        int h = 1;
        for (ItemStack it : new ItemStack[]{inv.getItemInMainHand(), inv.getItemInOffHand(), inv.getHelmet(),
                inv.getChestplate(), inv.getLeggings(), inv.getBoots()}) {
            h = 31 * h + (isGear(it) ? it.getItemMeta().getPersistentDataContainer().getOrDefault(kStats, PersistentDataType.STRING, "").hashCode()
                    + gemsOf(it.getItemMeta().getPersistentDataContainer()).hashCode() + it.getType().ordinal() : 0);
        }
        return h;
    }

    // ------------------------------------------------------------ Description

    public void refreshLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String rarityId = pdc.get(kRarity, PersistentDataType.STRING);
        if (rarityId == null) {
            return;
        }
        Rarity rarity = rarityById(rarityId);
        String uniqueId = pdc.get(kUnique, PersistentDataType.STRING);
        UniqueTemplate tpl = uniqueId == null ? null : uniques.get(uniqueId);
        String color = rarity == null ? "<white>" : rarity.color();
        if (tpl != null) {
            meta.displayName(Text.item(tpl.name()));
        } else {
            meta.displayName(Text.mm(color).append(Component.translatable(item.getType().translationKey()))
                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }
        List<Component> lore = new ArrayList<>();
        lore.add(Text.item(rarity == null ? rarityId : rarity.label()));
        if (tpl != null) {
            lore.addAll(Text.itemLore(tpl.lore()));
        }
        String crafter = pdc.get(kCrafter, PersistentDataType.STRING);
        if (crafter != null) {
            lore.add(plugin.messages().item("forge.crafted-by", Text.ph("player", crafter)));
        }
        List<String> statLines = StatModifier.load(decode(pdc.get(kStats, PersistentDataType.STRING))).describe();
        if (!statLines.isEmpty()) {
            lore.add(Component.empty());
            statLines.forEach(l -> lore.add(Text.item("<gray>• " + l)));
        }
        int sockets = pdc.getOrDefault(kSockets, PersistentDataType.INTEGER, 0);
        List<String> gemList = gemsOf(pdc);
        if (sockets > 0) {
            lore.add(Component.empty());
            for (int i = 0; i < sockets; i++) {
                if (i < gemList.size()) {
                    Gem g = gemDefs.get(gemList.get(i));
                    String desc = g == null ? gemList.get(i) : g.name() + " <dark_gray>(" + String.join(", ", stripColors(g.stats().describe())) + ")";
                    lore.add(Text.item(gems.getString("socket-filled", "<white>◆ ") + desc));
                } else {
                    lore.add(Text.item(gems.getString("socket-empty", "<dark_gray>◇ Emplacement de gemme libre")));
                }
            }
        }
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    private static List<String> stripColors(List<String> lines) {
        List<String> out = new ArrayList<>();
        for (String l : lines) {
            out.add(Text.plain(l));
        }
        return out;
    }

    // ------------------------------------------------------------ Gemmes

    public ItemStack createGem(Gem gem, int amount) {
        ItemStack it = new ItemStack(gem.material(), Math.max(1, amount));
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Text.item(gem.name()));
        List<Component> lore = new ArrayList<>(Text.itemLore(gem.description()));
        lore.add(Component.empty());
        gem.stats().describe().forEach(l -> lore.add(Text.item("<gray>• " + l)));
        lore.add(Component.empty());
        List<String> cats = new ArrayList<>();
        gem.appliesTo().forEach(c -> cats.add(plugin.messages().raw("gems.category-" + c.name().toLowerCase(Locale.ROOT))));
        lore.add(plugin.messages().item("gems.applies", Text.ph("types", String.join(", ", cats))));
        lore.add(plugin.messages().item("gems.how"));
        meta.lore(lore);
        if (gem.itemModel() != null) {
            NamespacedKey model = NamespacedKey.fromString(gem.itemModel());
            if (model != null) {
                meta.setItemModel(model);
            }
        }
        meta.setEnchantmentGlintOverride(true);
        meta.getPersistentDataContainer().set(kGem, PersistentDataType.STRING, gem.id());
        it.setItemMeta(meta);
        return it;
    }

    /** Résultat d'une tentative d'incrustation. */
    public enum SocketResult { OK, NOT_GEAR, NO_SOCKET, WRONG_TYPE, UNKNOWN_GEM }

    public SocketResult socket(ItemStack target, String gemId) {
        Gem gem = gemDefs.get(gemId);
        if (gem == null) {
            return SocketResult.UNKNOWN_GEM;
        }
        if (!isGear(target)) {
            return SocketResult.NOT_GEAR;
        }
        if (!gem.appliesTo().contains(category(target.getType()))) {
            return SocketResult.WRONG_TYPE;
        }
        ItemMeta meta = target.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int sockets = pdc.getOrDefault(kSockets, PersistentDataType.INTEGER, 0);
        List<String> list = gemsOf(pdc);
        if (list.size() >= sockets) {
            return SocketResult.NO_SOCKET;
        }
        list.add(gem.id());
        pdc.set(kGems, PersistentDataType.STRING, String.join(",", list));
        target.setItemMeta(meta);
        refreshLore(target);
        return SocketResult.OK;
    }

    /** Gemme tirée au hasard lors du minage, ou null. */
    public Gem rollGemDrop(String jobId, int jobLevel, Material block) {
        ConfigurationSection d = gems.getConfigurationSection("drops");
        if (d == null || gemDefs.isEmpty()) {
            return null;
        }
        List<String> jobs = d.getStringList("jobs");
        if (!jobs.isEmpty() && (jobId == null || !jobs.contains(jobId))) {
            return null;
        }
        boolean match = false;
        for (Pattern p : patterns(d.getStringList("targets"))) {
            if (p.matcher(block.name()).matches()) {
                match = true;
                break;
            }
        }
        if (!match) {
            return null;
        }
        double chance = d.getDouble("base-chance", 0.01) + d.getDouble("chance-per-job-level", 0.001) * jobLevel;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        if (r.nextDouble() >= chance) {
            return null;
        }
        double total = gemDefs.values().stream().mapToDouble(g -> Math.max(0, g.weight())).sum();
        double x = r.nextDouble(Math.max(total, 1e-9));
        for (Gem g : gemDefs.values()) {
            x -= Math.max(0, g.weight());
            if (x < 0) {
                return g;
            }
        }
        return null;
    }
}
