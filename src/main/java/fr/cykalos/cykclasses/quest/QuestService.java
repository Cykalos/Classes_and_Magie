package fr.cykalos.cykclasses.quest;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.config.ConfigFiles;
import fr.cykalos.cykclasses.content.Job;
import fr.cykalos.cykclasses.content.KitItem;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Système de quêtes : chargement du dossier quetes/, progression des joueurs, récompenses.
 * Les quêtes sont toutes facultatives.
 */
public final class QuestService {

    public enum Status { COMPLETED, ACTIVE, AVAILABLE, COOLDOWN, LOCKED, HIDDEN }

    /** Fichiers fournis par défaut, créés si le dossier quetes/ n'existe pas encore. */
    private static final String[] DEFAULT_FILES = {
            "quetes/tutoriel.yml", "quetes/principale.yml", "quetes/de-classe.yml",
            "quetes/annexes.yml", "quetes/secretes.yml"};

    private final CykClasses plugin;
    private final Map<String, Quest> quests = new LinkedHashMap<>();
    private BukkitTask task;
    private int tickCount;

    public QuestService(CykClasses plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------ Chargement

    public void load() {
        quests.clear();
        File folder = new File(plugin.getDataFolder(), ConfigFiles.QUESTS);
        if (!folder.exists()) {
            for (String res : DEFAULT_FILES) {
                if (plugin.getResource(res) != null) {
                    plugin.saveResource(res, false);
                }
            }
        }
        List<Quest> loaded = new ArrayList<>();
        int[] order = {0};
        loadFolder(folder, folder, loaded, order);
        loaded.sort(Comparator.comparing((Quest q) -> q.category).thenComparingInt(q -> q.order));
        for (Quest q : loaded) {
            if (quests.containsKey(q.id)) {
                plugin.getLogger().warning("Quête « " + q.id + " » définie deux fois (" + q.file + ") : la seconde est ignorée.");
                continue;
            }
            quests.put(q.id, q);
        }
        plugin.getLogger().info(quests.size() + " quêtes chargées.");
    }

    private void loadFolder(File root, File dir, List<Quest> out, int[] order) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        java.util.Arrays.sort(files);
        for (File f : files) {
            if (f.isDirectory()) {
                loadFolder(root, f, out, order);
                continue;
            }
            if (!f.getName().endsWith(".yml")) {
                continue;
            }
            String rel = root.toPath().relativize(f.toPath()).toString();
            YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
            ConfigurationSection section = y.getConfigurationSection("quests");
            if (section == null) {
                continue;
            }
            for (String id : section.getKeys(false)) {
                ConfigurationSection qs = section.getConfigurationSection(id);
                if (qs == null) {
                    continue;
                }
                try {
                    out.add(Quest.load(id, qs, rel, order[0]++));
                } catch (Exception e) {
                    plugin.getLogger().warning("Quête ignorée : " + e.getMessage());
                }
            }
        }
    }

    public void start() {
        stop();
        int interval = Math.max(5, plugin.getConfig().getInt("quests.check-interval-ticks", 20));
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public Collection<Quest> all() {
        return quests.values();
    }

    public Quest get(String id) {
        return id == null ? null : quests.get(id.toLowerCase(Locale.ROOT));
    }

    private boolean enabled() {
        return plugin.modules().quests();
    }

    private PlayerProfile profile(Player player) {
        return plugin.profiles().getIfLoaded(player.getUniqueId());
    }

    // ------------------------------------------------------------ État d'une quête

    public Status status(Player player, PlayerProfile p, Quest q) {
        if (p.isQuestActive(q.id)) {
            return Status.ACTIVE;
        }
        boolean done = p.hasCompletedQuest(q.id);
        if (done && !q.repeatable) {
            return Status.COMPLETED;
        }
        if (done && q.cooldownHours > 0
                && System.currentTimeMillis() < p.getQuestCompletedAt(q.id) + (long) (q.cooldownHours * 3_600_000)) {
            return Status.COOLDOWN;
        }
        if (!requirementsMet(player, p, q)) {
            return q.hidden && !done ? Status.HIDDEN : Status.LOCKED;
        }
        return Status.AVAILABLE;
    }

    public boolean requirementsMet(Player player, PlayerProfile p, Quest q) {
        return missingRequirements(player, p, q).isEmpty();
    }

    public boolean requirementsMet(Player player, PlayerProfile p, QuestRequirements r) {
        return missingRequirements(player, p, r).isEmpty();
    }

    /** Conditions non remplies, sous forme de lignes lisibles (vide = disponible). */
    public List<Component> missingRequirements(Player player, PlayerProfile p, Quest q) {
        return missingRequirements(player, p, q.requirements);
    }

    /** Conditions non remplies (quêtes et titres). */
    public List<Component> missingRequirements(Player player, PlayerProfile p, QuestRequirements r) {
        List<Component> out = new ArrayList<>();
        if (r.level() > 0 && p.getLevel() < r.level()) {
            out.add(plugin.messages().get("quests.req.level", Text.ph("level", r.level())));
        }
        if (r.maxLevel() > 0 && p.getLevel() > r.maxLevel()) {
            out.add(plugin.messages().get("quests.req.max-level", Text.ph("level", r.maxLevel())));
        }
        if (!r.classes().isEmpty() && !classMatches(p, r.classes())) {
            out.add(plugin.messages().get("quests.req.class", Text.ph("value", names(r.classes(), true))));
        }
        if (!r.races().isEmpty() && (p.getRaceId() == null || !r.races().contains(p.getRaceId().toLowerCase(Locale.ROOT)))) {
            out.add(plugin.messages().get("quests.req.race", Text.ph("value", names(r.races(), false))));
        }
        if (!r.jobs().isEmpty() && (p.getJobId() == null || !r.jobs().contains(p.getJobId().toLowerCase(Locale.ROOT)))) {
            out.add(plugin.messages().get("quests.req.job", Text.ph("value", String.join(", ", r.jobs()))));
        }
        if (r.jobLevel() > 0 && plugin.jobs().level(p) < r.jobLevel()) {
            out.add(plugin.messages().get("quests.req.job-level", Text.ph("level", r.jobLevel())));
        }
        for (String id : r.quests()) {
            if (!p.hasCompletedQuest(id)) {
                Quest other = get(id);
                out.add(plugin.messages().get("quests.req.quest", Text.ph("quest", other == null ? id : Text.plain(other.name))));
            }
        }
        for (String id : r.notQuests()) {
            if (p.hasCompletedQuest(id) || p.isQuestActive(id)) {
                Quest other = get(id);
                out.add(plugin.messages().get("quests.req.not-quest", Text.ph("quest", other == null ? id : Text.plain(other.name))));
            }
        }
        if (r.permission() != null && player != null && !player.hasPermission(r.permission())) {
            out.add(plugin.messages().get("quests.req.permission"));
        }
        for (String id : r.titles()) {
            if (!p.hasTitle(id)) {
                var t = plugin.titles() == null ? null : plugin.titles().get(id);
                out.add(plugin.messages().get("quests.req.title", Text.ph("title", t == null ? id : Text.plain(t.name()))));
            }
        }
        for (Objective o : r.checks()) {
            double v = stateValue(player, p, o);
            if (v < o.amount) {
                out.add(Text.mm(plugin.messages().raw("quests.req.check"), Text.ph("objective", objectiveText(o)),
                        Text.ph("progress", progressText(v, o.amount))));
            }
        }
        return out;
    }

    private boolean classMatches(PlayerProfile p, List<String> ids) {
        return (p.getClassId() != null && ids.contains(p.getClassId().toLowerCase(Locale.ROOT)))
                || (p.getSubclassId() != null && ids.contains(p.getSubclassId().toLowerCase(Locale.ROOT)));
    }

    private String names(List<String> ids, boolean classes) {
        List<String> out = new ArrayList<>();
        for (String id : ids) {
            if (classes) {
                RpgClass rc = plugin.classes().get(id);
                out.add(rc == null ? id : Text.plain(rc.name));
            } else {
                var race = plugin.content().race(id);
                out.add(race == null ? id : Text.plain(race.name()));
            }
        }
        return String.join(", ", out);
    }

    /** Une quête apparaît-elle dans le journal de ce joueur ? */
    public boolean visible(Player player, PlayerProfile p, Quest q) {
        Status st = status(player, p, q);
        if (st == Status.HIDDEN) {
            return false;
        }
        if (q.hidden && st != Status.ACTIVE && !p.hasCompletedQuest(q.id)) {
            return false;
        }
        if (plugin.getConfig().getBoolean("quests.hide-other-classes", true) && st != Status.ACTIVE && !p.hasCompletedQuest(q.id)) {
            if (!q.requirements.classes().isEmpty() && p.hasClass() && !classMatches(p, q.requirements.classes())
                    && !parentMatches(p, q.requirements.classes())) {
                return false;
            }
            if (!q.requirements.races().isEmpty() && p.getRaceId() != null
                    && !q.requirements.races().contains(p.getRaceId().toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        return true;
    }

    /** Quête réservée à une sous-classe de la classe du joueur : visible (il peut encore la choisir). */
    private boolean parentMatches(PlayerProfile p, List<String> ids) {
        if (p.getClassId() == null || p.getSubclassId() != null) {
            return false;
        }
        for (String id : ids) {
            RpgClass rc = plugin.classes().get(id);
            if (rc != null && rc.parent != null && rc.parent.id.equalsIgnoreCase(p.getClassId())) {
                return true;
            }
        }
        return false;
    }

    public int activeCount(PlayerProfile p) {
        return p.questStages().size();
    }

    // ------------------------------------------------------------ Démarrer, abandonner

    public boolean startQuest(Player player, Quest q, boolean force) {
        PlayerProfile p = profile(player);
        if (p == null || !enabled()) {
            return false;
        }
        if (p.isQuestActive(q.id)) {
            plugin.messages().send(player, "quests.already-active");
            return false;
        }
        if (!force) {
            Status st = status(player, p, q);
            if (st == Status.COMPLETED) {
                plugin.messages().send(player, "quests.already-done");
                return false;
            }
            if (st == Status.COOLDOWN) {
                plugin.messages().send(player, "quests.cooldown");
                return false;
            }
            if (st != Status.AVAILABLE) {
                plugin.messages().send(player, "quests.locked");
                return false;
            }
            int max = plugin.getConfig().getInt("quests.max-active", 0);
            if (max > 0 && activeCount(p) >= max) {
                plugin.messages().send(player, "quests.too-many", Text.ph("max", max));
                return false;
            }
        }
        p.setQuestStage(q.id, 0);
        p.setQuestAbandoned(q.id, false);
        int maxTracked = plugin.getConfig().getInt("quests.max-tracked", 3);
        if (plugin.getConfig().getBoolean("quests.auto-track", true) && p.questsTracked().size() < maxTracked
                && !p.questsTracked().contains(q.id)) {
            p.questsTracked().add(q.id);
        }
        String key = q.hidden ? "quests.secret-discovered" : "quests.started";
        plugin.messages().send(player, key, Text.ph("quest", q.name));
        if (q.hidden) {
            player.showTitle(Title.title(plugin.messages().get("quests.secret-title"), Text.mm(q.name),
                    Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(2500), Duration.ofMillis(500))));
            player.playSound(Sound.sound(Key.key("ui.toast.challenge_complete"), Sound.Source.PLAYER, 0.7f, 0.7f));
        } else {
            player.playSound(Sound.sound(Key.key("item.book.page_turn"), Sound.Source.PLAYER, 1f, 1f));
        }
        sendStageText(player, q, 0);
        checkStage(player, p, q);
        return true;
    }

    public boolean abandon(Player player, Quest q) {
        PlayerProfile p = profile(player);
        if (p == null || !p.isQuestActive(q.id)) {
            return false;
        }
        if (!q.abandonable) {
            plugin.messages().send(player, "quests.cannot-abandon");
            return false;
        }
        boolean wasTracked = p.questsTracked().contains(q.id);
        p.removeActiveQuest(q.id);
        // Une quête abandonnée ne redémarre plus d'elle-même (elle reste disponible dans le journal)
        p.setQuestAbandoned(q.id, true);
        if (wasTracked) {
            trackNext(p);
        }
        plugin.messages().send(player, "quests.abandoned", Text.ph("quest", q.name));
        return true;
    }

    /** La quête suivie vient de se terminer ou d'être abandonnée : une autre quête en cours prend sa place. */
    private void trackNext(PlayerProfile p) {
        if (!plugin.getConfig().getBoolean("quests.auto-track", true) || !p.questsTracked().isEmpty()) {
            return;
        }
        for (String id : p.questStages().keySet()) {
            if (get(id) != null) {
                p.questsTracked().add(id);
                p.markDirty();
                return;
            }
        }
    }

    public void toggleTrack(Player player, Quest q) {
        PlayerProfile p = profile(player);
        if (p == null || !p.isQuestActive(q.id)) {
            return;
        }
        if (p.questsTracked().remove(q.id)) {
            p.markDirty();
            plugin.messages().send(player, "quests.untracked", Text.ph("quest", q.name));
            return;
        }
        int max = plugin.getConfig().getInt("quests.max-tracked", 3);
        while (p.questsTracked().size() >= Math.max(1, max)) {
            p.questsTracked().remove(0);
        }
        p.questsTracked().add(q.id);
        p.markDirty();
        plugin.messages().send(player, "quests.tracked", Text.ph("quest", q.name));
    }

    private void sendStageText(Player player, Quest q, int stage) {
        if (stage < 0 || stage >= q.stages.size()) {
            return;
        }
        for (String line : q.stages.get(stage).text()) {
            player.sendMessage(Text.mm(line, Text.ph("player", player.getName())));
        }
        if (q.stages.size() > 1) {
            plugin.messages().send(player, "quests.new-stage", Text.ph("quest", q.name),
                    Text.ph("stage", stage + 1), Text.ph("stages", q.stages.size()));
        }
        for (Objective o : q.stages.get(stage).objectives()) {
            player.sendMessage(Text.mm(plugin.messages().raw("quests.objective-line"),
                    Text.ph("objective", objectiveText(o)), Text.ph("progress", "")));
        }
    }

    // ------------------------------------------------------------ Tutoriel

    /** Fin de la création du personnage : propose le tutoriel une seule fois. */
    public void onCreationFinished(Player player) {
        if (!enabled() || !plugin.getConfig().getBoolean("quests.offer-tutorial", true)) {
            return;
        }
        PlayerProfile p = profile(player);
        Quest tuto = get(plugin.getConfig().getString("quests.tutorial", "tutoriel"));
        if (p == null || tuto == null || p.isTutorialOffered() || p.isQuestActive(tuto.id) || p.hasCompletedQuest(tuto.id)) {
            return;
        }
        p.setTutorialOffered(true);
        for (String line : plugin.messages().rawList("quests.tutorial-offer")) {
            player.sendMessage(Text.mm(line, Text.ph("quest", tuto.name)));
        }
        player.playSound(Sound.sound(Key.key("block.note_block.chime"), Sound.Source.PLAYER, 1f, 1.2f));
    }

    public Quest tutorial() {
        return get(plugin.getConfig().getString("quests.tutorial", "tutoriel"));
    }

    // ------------------------------------------------------------ Progression

    /** Action d'un joueur (tuer, casser, fabriquer…) : fait avancer les objectifs correspondants. */
    public void progress(Player player, Objective.Type type, List<String> candidates, double amount, String entityName) {
        if (player == null) {
            return;
        }
        if (plugin.titles() != null) {
            plugin.titles().progress(player, type, candidates, amount, entityName);
        }
        if (!enabled()) {
            return;
        }
        PlayerProfile p = profile(player);
        if (p == null) {
            return;
        }
        if (!p.questStages().isEmpty()) {
            for (String id : new ArrayList<>(p.questStages().keySet())) {
                Quest q = get(id);
                if (q == null) {
                    continue;
                }
                int stage = p.getQuestStage(id);
                if (stage < 0 || stage >= q.stages.size()) {
                    continue;
                }
                List<Objective> objs = q.stages.get(stage).objectives();
                boolean changed = false;
                for (int i = 0; i < objs.size(); i++) {
                    Objective o = objs.get(i);
                    if (o.type != type || moduleSkipped(o) || !o.matchesAny(candidates) || !nameMatches(o, entityName)
                            || !o.conditions.test(player)) {
                        continue;
                    }
                    double current = p.getQuestProgress(id, i);
                    if (current >= o.amount) {
                        continue;
                    }
                    p.setQuestProgress(id, i, Math.min(o.amount, current + amount));
                    changed = true;
                    if (plugin.getConfig().getBoolean("quests.progress-actionbar", true)) {
                        player.sendActionBar(Text.mm(plugin.messages().raw("quests.progress-actionbar"),
                                Text.ph("quest", q.name), Text.ph("objective", objectiveText(o)),
                                Text.ph("progress", progressText(Math.min(o.amount, current + amount), o.amount))));
                    }
                }
                if (changed) {
                    checkStage(player, p, q);
                }
            }
        }
        // Déclencheurs des quêtes (secrètes en général) qui démarrent sur une action précise
        for (Quest q : quests.values()) {
            Objective t = q.trigger;
            if (t == null || t.type != type || p.isQuestActive(q.id) || p.isQuestAbandoned(q.id) || !t.matchesAny(candidates)
                    || !nameMatches(t, entityName) || !t.conditions.test(player) || !q.conditions.test(player)) {
                continue;
            }
            if (status(player, p, q) == Status.AVAILABLE) {
                startQuest(player, q, false);
            }
        }
    }

    public void progress(Player player, Objective.Type type, String target, double amount) {
        progress(player, type, target == null ? List.of() : List.of(target), amount, null);
    }

    private static boolean nameMatches(Objective o, String entityName) {
        if (o.entityName == null || o.entityName.isBlank()) {
            return true;
        }
        return entityName != null && entityName.toLowerCase(Locale.ROOT).contains(Text.plain(o.entityName).toLowerCase(Locale.ROOT));
    }

    private boolean moduleSkipped(Objective o) {
        if (o.module == null) {
            return false;
        }
        var m = plugin.modules();
        return switch (o.module.toLowerCase(Locale.ROOT)) {
            case "races" -> !m.races();
            case "traits" -> !m.traits();
            case "weaknesses" -> !m.weaknesses();
            case "jobs" -> !m.jobs();
            case "skill-tree" -> !m.skillTree();
            case "attributes" -> !m.attributes();
            case "parties" -> !m.parties();
            case "forge" -> !m.forge();
            case "gems" -> !m.gems();
            case "kits" -> !m.kits();
            default -> false;
        };
    }

    /** Valeur actuelle d'un objectif (stockée pour les actions, calculée pour les états). */
    public double value(Player player, PlayerProfile p, Quest q, int stage, int index) {
        Objective o = q.stages.get(stage).objectives().get(index);
        if (moduleSkipped(o)) {
            return o.amount;
        }
        if (!o.type.isState()) {
            return p.getQuestProgress(q.id, index);
        }
        return stateValue(player, p, o);
    }

    /** Valeur d'un objectif « état » (niveau, objets, statistiques…), partagée avec les titres. */
    public double stateValue(Player player, PlayerProfile p, Objective o) {
        if (player == null) {
            return 0;
        }
        return switch (o.type) {
            case LEVEL -> p.getLevel();
            case JOB_LEVEL -> (o.targets.isEmpty() || (p.getJobId() != null && o.matches(p.getJobId())))
                    ? plugin.jobs().level(p) : 0;
            case CLASS -> bool(p.getClassId() != null && (o.matches(p.getClassId())
                    || (p.getSubclassId() != null && o.matches(p.getSubclassId()))), o);
            case SUBCLASS -> bool(p.getSubclassId() != null && o.matches(p.getSubclassId()), o);
            case RACE -> bool(p.getRaceId() != null && o.matches(p.getRaceId()), o);
            case JOB -> bool(p.getJobId() != null && o.matches(p.getJobId()), o);
            case SPECIALIZATION -> {
                Job.Specialization spec = plugin.jobs().spec(p);
                yield bool(spec != null && o.matches(spec.id()), o);
            }
            case COLLECT -> Math.min(o.amount, countItems(player, o));
            case REACH -> bool(reached(player, o), o);
            case BIOME -> bool(o.matches(biome(player)) && o.conditions.test(player), o);
            case CONDITION -> bool(o.conditions.test(player), o);
            case TREE_POINTS -> p.treeNodes().values().stream().mapToInt(Integer::intValue).sum();
            case ATTRIBUTE_POINTS -> o.targets.isEmpty() ? p.spentAttributePoints()
                    : o.targets.stream().mapToInt(p::getAttribute).sum();
            case QUEST -> o.targets.isEmpty() ? p.questsCompleted().size()
                    : o.targets.stream().filter(t -> p.hasCompletedQuest(t.toLowerCase(Locale.ROOT))).count();
            case TITLES -> plugin.titles() == null ? 0 : plugin.titles().countUnlocked(p, o);
            case KILLS_TOTAL -> o.targets.isEmpty() ? p.totalKills()
                    : p.kills().entrySet().stream().filter(e -> o.matches(e.getKey())).mapToInt(Map.Entry::getValue).sum();
            case SKILL_USES_TOTAL -> o.targets.isEmpty() ? p.totalSkillUses()
                    : p.skillUses().entrySet().stream().filter(e -> o.matches(e.getKey())).mapToInt(Map.Entry::getValue).sum();
            case COUNTER -> o.targets.stream().mapToDouble(p::getCounter).sum();
            case STATISTIC -> o.targets.stream().mapToDouble(t -> statistic(player, t)).sum();
            case ADVANCEMENT -> o.targets.stream().filter(t -> advancementDone(player, t)).count();
            case PLAYTIME -> player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) / 72_000.0;
            default -> 0;
        };
    }

    /** Statistique Minecraft : « DEATHS », « WALK_ONE_CM », « MINE_BLOCK:DIAMOND_ORE », « KILL_ENTITY:ZOMBIE »… */
    private static double statistic(Player player, String raw) {
        try {
            String[] parts = raw.toUpperCase(Locale.ROOT).split(":", 2);
            org.bukkit.Statistic stat = org.bukkit.Statistic.valueOf(parts[0]);
            return switch (stat.getType()) {
                case UNTYPED -> player.getStatistic(stat);
                case BLOCK, ITEM -> {
                    org.bukkit.Material m = parts.length > 1 ? org.bukkit.Material.matchMaterial(parts[1]) : null;
                    yield m == null ? 0 : player.getStatistic(stat, m);
                }
                case ENTITY -> {
                    if (parts.length < 2) {
                        yield 0;
                    }
                    yield player.getStatistic(stat, org.bukkit.entity.EntityType.valueOf(parts[1]));
                }
            };
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    private static boolean advancementDone(Player player, String key) {
        org.bukkit.NamespacedKey nk = org.bukkit.NamespacedKey.fromString(key.toLowerCase(Locale.ROOT));
        org.bukkit.advancement.Advancement adv = nk == null ? null : Bukkit.getAdvancement(nk);
        return adv != null && player.getAdvancementProgress(adv).isDone();
    }

    private static double bool(boolean v, Objective o) {
        return v ? o.amount : 0;
    }

    private boolean reached(Player player, Objective o) {
        Location loc = player.getLocation();
        if (o.world != null && !o.world.equalsIgnoreCase(loc.getWorld().getName())) {
            return false;
        }
        if (o.x == null || o.z == null) {
            return o.world != null && o.conditions.test(player);
        }
        double dy = o.y == null ? 0 : loc.getY() - o.y;
        double dx = loc.getX() - o.x, dz = loc.getZ() - o.z;
        return dx * dx + dy * dy + dz * dz <= o.radius * o.radius && o.conditions.test(player);
    }

    @SuppressWarnings("deprecation")
    private static String biome(Player player) {
        try {
            return player.getLocation().getBlock().getBiome().getKey().getKey();
        } catch (Throwable t) {
            return "";
        }
    }

    private int countItems(Player player, Objective o) {
        int count = 0;
        for (ItemStack it : player.getInventory().getStorageContents()) {
            if (itemMatches(it, o)) {
                count += it.getAmount();
            }
        }
        return count;
    }

    private boolean itemMatches(ItemStack it, Objective o) {
        if (it == null || it.getType().isAir()) {
            return false;
        }
        if (o.matches(it.getType().name())) {
            return true;
        }
        String custom = plugin.itemFactory().customId(it);
        return custom != null && o.matches(custom);
    }

    private void takeItems(Player player, Objective o) {
        int left = (int) o.amount;
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length && left > 0; i++) {
            ItemStack it = contents[i];
            if (!itemMatches(it, o)) {
                continue;
            }
            int take = Math.min(left, it.getAmount());
            it.setAmount(it.getAmount() - take);
            left -= take;
            contents[i] = it.getAmount() <= 0 ? null : it;
        }
        player.getInventory().setStorageContents(contents);
    }

    public boolean objectiveDone(Player player, PlayerProfile p, Quest q, int stage, int index) {
        Objective o = q.stages.get(stage).objectives().get(index);
        return value(player, p, q, stage, index) >= o.amount;
    }

    /** Si tous les objectifs de l'étape sont remplis : étape suivante ou fin de la quête. */
    public void checkStage(Player player, PlayerProfile p, Quest q) {
        int stage = p.getQuestStage(q.id);
        if (stage < 0 || stage >= q.stages.size()) {
            return;
        }
        List<Objective> objs = q.stages.get(stage).objectives();
        for (int i = 0; i < objs.size(); i++) {
            if (!objectiveDone(player, p, q, stage, i)) {
                return;
            }
        }
        for (Objective o : objs) {
            if (o.type == Objective.Type.COLLECT && o.take && !moduleSkipped(o)) {
                takeItems(player, o);
            }
        }
        if (stage + 1 < q.stages.size()) {
            p.setQuestStage(q.id, stage + 1);
            player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 0.8f, 1.4f));
            sendStageText(player, q, stage + 1);
            checkStage(player, p, q);
        } else {
            complete(player, p, q);
        }
    }

    /** Administration : termine une quête immédiatement (récompenses comprises). */
    public void forceComplete(Player player, Quest q) {
        PlayerProfile p = profile(player);
        if (p != null) {
            complete(player, p, q);
        }
    }

    private void complete(Player player, PlayerProfile p, Quest q) {
        boolean wasTracked = p.questsTracked().contains(q.id);
        p.completeQuest(q.id);
        if (wasTracked) {
            trackNext(p);
        }
        plugin.messages().send(player, "quests.completed", Text.ph("quest", q.name));
        for (String line : q.completeText) {
            player.sendMessage(Text.mm(line, Text.ph("player", player.getName())));
        }
        player.showTitle(Title.title(plugin.messages().get("quests.completed-title"), Text.mm(q.name),
                Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(2000), Duration.ofMillis(500))));
        player.playSound(Sound.sound(Key.key("ui.toast.challenge_complete"), Sound.Source.PLAYER, 0.8f, 1.1f));
        giveRewards(player, p, q.rewards);
        // Les quêtes suivantes peuvent démarrer automatiquement
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                autoStart(player, p);
            }
        });
    }

    private void giveRewards(Player player, PlayerProfile p, QuestReward r) {
        if (r.isEmpty()) {
            return;
        }
        for (Component line : rewardLines(r)) {
            player.sendMessage(Text.mm(plugin.messages().raw("quests.reward-line"), Text.phc("reward", line)));
        }
        if (r.xp() > 0 && p.hasClass()) {
            plugin.levels().addXp(player, r.xp());
        }
        if (r.jobXp() > 0 && p.getJobId() != null) {
            Job job = plugin.content().job(p.getJobId());
            if (job != null) {
                plugin.jobs().addXp(player, p, job, r.jobXp());
            }
        }
        if (r.skillPoints() > 0) {
            p.setBonusSkillPoints(p.getBonusSkillPoints() + r.skillPoints());
        }
        if (r.attributePoints() > 0) {
            p.setBonusAttributePoints(p.getBonusAttributePoints() + r.attributePoints());
        }
        if (r.money() > 0 && !deposit(player, r.money())) {
            plugin.getLogger().warning("Récompense d'argent ignorée : Vault ou plugin d'économie absent.");
        }
        for (KitItem ki : r.items()) {
            ItemStack item = plugin.character().buildItem(ki, player);
            if (item == null) {
                plugin.getLogger().warning("Récompense de quête : objet introuvable « " + ki.item() + " »");
                continue;
            }
            plugin.character().giveItem(player, item);
        }
        if (plugin.titles() != null) {
            for (String t : r.titles()) {
                plugin.titles().unlock(player, t, true);
            }
        }
        for (String cmd : r.commands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    cmd.replace("%player%", player.getName()).replace("<player>", player.getName()));
        }
    }

    /** Argent via Vault, sans dépendance de compilation (réflexion). */
    private boolean deposit(Player player, double amount) {
        try {
            Class<?> eco = Class.forName("net.milkbowl.vault.economy.Economy");
            @SuppressWarnings({"unchecked", "rawtypes"})
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration((Class) eco);
            if (rsp == null) {
                return false;
            }
            eco.getMethod("depositPlayer", OfflinePlayer.class, double.class).invoke(rsp.getProvider(), player, amount);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public List<Component> rewardLines(QuestReward r) {
        List<Component> out = new ArrayList<>();
        if (r.xp() > 0) out.add(plugin.messages().get("quests.reward.xp", Text.ph("amount", Text.format(r.xp()))));
        if (r.jobXp() > 0) out.add(plugin.messages().get("quests.reward.job-xp", Text.ph("amount", Text.format(r.jobXp()))));
        if (r.skillPoints() > 0) out.add(plugin.messages().get("quests.reward.skill-points", Text.ph("amount", r.skillPoints())));
        if (r.attributePoints() > 0) out.add(plugin.messages().get("quests.reward.attribute-points", Text.ph("amount", r.attributePoints())));
        if (r.money() > 0) out.add(plugin.messages().get("quests.reward.money", Text.ph("amount", Text.format(r.money()))));
        for (KitItem ki : r.items()) {
            String name = ki.name() != null ? ki.name() : prettyItem(ki.item());
            out.add(plugin.messages().get("quests.reward.item", Text.ph("amount", ki.amount()), Text.ph("item", name)));
        }
        if (plugin.titles() != null) {
            for (String id : r.titles()) {
                var t = plugin.titles().get(id);
                if (t != null) {
                    out.add(plugin.messages().get("quests.reward.title", Text.ph("title", t.name())));
                }
            }
        }
        for (String t : r.text()) {
            out.add(Text.mm(t));
        }
        return out;
    }

    private static String prettyItem(String id) {
        String s = id.contains(":") ? id.substring(id.lastIndexOf(':') + 1) : id;
        s = s.replace('_', ' ').toLowerCase(Locale.ROOT);
        return s.isEmpty() ? id : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ------------------------------------------------------------ Vérification régulière

    private void tick() {
        if (!enabled()) {
            return;
        }
        tickCount++;
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile p = profile(player);
            if (p == null || !p.hasClass()) {
                continue;
            }
            for (String id : new ArrayList<>(p.questStages().keySet())) {
                Quest q = get(id);
                if (q == null) {
                    continue;
                }
                int stage = p.getQuestStage(id);
                if (stage >= 0 && stage < q.stages.size()
                        && q.stages.get(stage).objectives().stream().anyMatch(o -> o.type.isState() || moduleSkipped(o))) {
                    checkStage(player, p, q);
                }
            }
            // Démarrage automatique (toutes les 5 vérifications)
            if (tickCount % 5 == 0 && !plugin.character().creationPending(player)) {
                onCreationFinished(player);
                autoStart(player, p);
            }
        }
    }

    private void autoStart(Player player, PlayerProfile p) {
        for (Quest q : quests.values()) {
            if (q.start != Quest.Start.AUTO || q.trigger != null || p.isQuestActive(q.id) || p.isQuestAbandoned(q.id)) {
                continue;
            }
            if (status(player, p, q) == Status.AVAILABLE && q.conditions.test(player)) {
                startQuest(player, q, false);
            }
        }
    }

    // ------------------------------------------------------------ Textes

    public String objectiveText(Objective o) {
        if (o.display != null) {
            return o.display;
        }
        String target = o.targets.isEmpty() ? plugin.messages().raw("quests.any-target")
                : prettyItem(o.targets.get(0)) + (o.targets.size() > 1 ? "…" : "");
        return plugin.messages().raw("quests.objective." + o.type.name().toLowerCase(Locale.ROOT).replace('_', '-'))
                .replace("<amount>", Text.format(o.amount)).replace("<target>", target);
    }

    public String progressText(double value, double amount) {
        if (amount <= 1) {
            return "";
        }
        return Text.format(Math.min(value, amount)) + "/" + Text.format(amount);
    }

    /** Lignes du panneau latéral de suivi (vide si rien à afficher). */
    public List<Component> trackerLines(Player player, int maxQuests) {
        List<Component> out = new ArrayList<>();
        PlayerProfile p = profile(player);
        if (!enabled() || p == null || p.isQuestTrackerHidden()) {
            return out;
        }
        ConfigurationSection s = plugin.hud().config().getConfigurationSection("quest-tracker");
        if (s == null || !s.getBoolean("enabled", true)) {
            return out;
        }
        List<String> ids = new ArrayList<>(p.questsTracked());
        ids.removeIf(id -> !p.isQuestActive(id) || get(id) == null);

        int shown = 0;
        for (String id : ids) {
            if (shown >= maxQuests) {
                break;
            }
            Quest q = get(id);
            int stage = p.getQuestStage(id);
            out.add(Text.mm(s.getString("quest-line", "<yellow><name>"), Text.ph("name", q.name),
                    Text.ph("stage", stage + 1), Text.ph("stages", q.stages.size())));
            List<Objective> objs = q.stages.get(stage).objectives();
            for (int i = 0; i < objs.size(); i++) {
                double v = value(player, p, q, stage, i);
                boolean done = v >= objs.get(i).amount;
                out.add(Text.mm(s.getString("objective-line", " <status> <white><objective> <gray><progress>"),
                        TagResolver.resolver(
                                Text.ph("status", s.getString(done ? "done-symbol" : "todo-symbol", done ? "<green>✔" : "<gray>•")),
                                Text.ph("objective", objectiveText(objs.get(i))),
                                Text.ph("progress", progressText(v, objs.get(i).amount)))));
            }
            shown++;
        }
        return out;
    }
}
