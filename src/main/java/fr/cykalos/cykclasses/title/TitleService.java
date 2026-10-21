package fr.cykalos.cykclasses.title;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.config.ConfigDefaults;
import fr.cykalos.cykclasses.config.ConfigFiles;
import fr.cykalos.cykclasses.content.PermanentEffect;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.StatModifier;
import fr.cykalos.cykclasses.quest.Objective;
import fr.cykalos.cykclasses.quest.QuestRequirements;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Titres des joueurs (personnage/titles.yml) : déblocage selon les actions du joueur, raretés jusqu'à
 * unique et secret, effets du titre équipé, affichage au-dessus de la tête et dans le chat.
 */
public final class TitleService implements Listener {

    /** Rareté d'un titre (titles.yml → rarities). */
    public record Rarity(String id, String label, int order, boolean unique, boolean hidden, boolean broadcast) {
    }

    private static final String RELEASED_KEY = "_liberes";

    private final CykClasses plugin;
    private final Map<String, PlayerTitle> titles = new LinkedHashMap<>();
    private final Map<String, Rarity> rarities = new LinkedHashMap<>();
    private final Map<String, UUID> uniqueOwners = new HashMap<>();
    private final Set<String> released = new HashSet<>();
    private final Map<UUID, TextDisplay> displays = new HashMap<>();
    private final Map<UUID, String> displayed = new HashMap<>();
    private YamlConfiguration cfg = new YamlConfiguration();
    private File uniquesFile;
    private BukkitTask checkTask;
    private BukkitTask displayTask;

    public TitleService(CykClasses plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------ Chargement

    public void load() {
        titles.clear();
        rarities.clear();
        cfg = ConfigFiles.load(plugin, "titles.yml");
        ConfigDefaults.fill(plugin, cfg, ConfigFiles.path("titles.yml"), Set.of("titles", "rarities"));
        ConfigurationSection rs = cfg.getConfigurationSection("rarities");
        int order = 0;
        if (rs != null) {
            for (String id : rs.getKeys(false)) {
                ConfigurationSection r = rs.getConfigurationSection(id);
                if (r == null) {
                    continue;
                }
                String key = id.toUpperCase(Locale.ROOT);
                rarities.put(key, new Rarity(key, r.getString("label", id), order++, r.getBoolean("unique", false),
                        r.getBoolean("hidden", false), r.getBoolean("broadcast", false)));
            }
        }
        if (rarities.isEmpty()) {
            rarities.put("COMMON", new Rarity("COMMON", "<gray>Commun", 0, false, false, false));
        }
        List<PlayerTitle> loaded = new ArrayList<>();
        ConfigurationSection ts = cfg.getConfigurationSection("titles");
        order = 0;
        if (ts != null) {
            for (String id : ts.getKeys(false)) {
                ConfigurationSection s = ts.getConfigurationSection(id);
                if (s == null) {
                    continue;
                }
                try {
                    loaded.add(parse(id.toLowerCase(Locale.ROOT), s, order++));
                } catch (Exception e) {
                    plugin.getLogger().warning("Titre ignoré (" + id + ") : " + e.getMessage());
                }
            }
        }
        loaded.sort(Comparator.comparingInt((PlayerTitle t) -> rarity(t).order()).thenComparingInt(PlayerTitle::order));
        loaded.forEach(t -> titles.put(t.id(), t));
        loadUniques();
        plugin.getLogger().info(titles.size() + " titres chargés.");
    }

    private PlayerTitle parse(String id, ConfigurationSection s, int order) {
        String rarityId = s.getString("rarity", "COMMON").toUpperCase(Locale.ROOT);
        Rarity rarity = rarities.getOrDefault(rarityId, rarities.values().iterator().next());
        List<Objective> unlock = new ArrayList<>();
        for (Map<?, ?> m : s.getMapList("unlock")) {
            unlock.add(Objective.load(m, "titre " + id));
        }
        List<Flaw> flaws = new ArrayList<>();
        for (Map<?, ?> m : s.getMapList("flaws")) {
            Flaw f = Flaw.load(m);
            if (f != null) {
                flaws.add(f);
            }
        }
        Material icon = Material.matchMaterial(s.getString("icon", "NAME_TAG"));
        return new PlayerTitle(id, s.getString("name", id), rarity.id(), icon == null ? Material.NAME_TAG : icon,
                s.getStringList("description"), s.getBoolean("unique", rarity.unique()), s.getBoolean("hidden", rarity.hidden()),
                QuestRequirements.load(s.getConfigurationSection("requirements")), unlock,
                StatModifier.load(s.getConfigurationSection("stats")), PermanentEffect.parse(s.getStringList("effects")),
                flaws, order);
    }

    private void loadUniques() {
        uniquesFile = ConfigFiles.file(plugin, "titres-uniques.yml");
        uniqueOwners.clear();
        released.clear();
        YamlConfiguration y = YamlConfiguration.loadConfiguration(uniquesFile);
        for (String k : y.getKeys(false)) {
            if (k.equals(RELEASED_KEY)) {
                released.addAll(y.getStringList(k));
                continue;
            }
            try {
                uniqueOwners.put(k, UUID.fromString(y.getString(k, "")));
            } catch (IllegalArgumentException ignored) {
                // entrée invalide
            }
        }
    }

    private void saveUniques() {
        YamlConfiguration y = new YamlConfiguration();
        uniqueOwners.forEach((k, v) -> y.set(k, v.toString()));
        y.set(RELEASED_KEY, new ArrayList<>(released));
        try {
            y.save(uniquesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Sauvegarde impossible de titres-uniques.yml", e);
        }
    }

    public void start() {
        stop();
        int interval = Math.max(20, cfg.getInt("check-interval-ticks", 40));
        checkTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkAll, interval, interval);
        displayTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateDisplays, 1L, 1L);
    }

    public void stop() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
        if (displayTask != null) {
            displayTask.cancel();
            displayTask = null;
        }
        displays.values().forEach(TextDisplay::remove);
        displays.clear();
        displayed.clear();
    }

    // ------------------------------------------------------------ Accès

    public boolean enabled() {
        return plugin.modules().titles();
    }

    public YamlConfiguration config() {
        return cfg;
    }

    public Collection<PlayerTitle> all() {
        return titles.values();
    }

    public PlayerTitle get(String id) {
        return id == null ? null : titles.get(id.toLowerCase(Locale.ROOT));
    }

    public Rarity rarity(PlayerTitle t) {
        return rarities.getOrDefault(t.rarity(), rarities.values().iterator().next());
    }

    /** Titre équipé (null si aucun, ou si le module est désactivé). */
    public PlayerTitle equipped(PlayerProfile p) {
        if (!enabled() || p == null || p.getEquippedTitle() == null) {
            return null;
        }
        PlayerTitle t = get(p.getEquippedTitle());
        return t != null && p.hasTitle(t.id()) ? t : null;
    }

    public UUID uniqueOwner(PlayerTitle t) {
        return uniqueOwners.get(t.id());
    }

    /** Titre unique déjà possédé par un autre joueur. */
    public boolean takenByOther(PlayerTitle t, UUID uuid) {
        UUID owner = uniqueOwners.get(t.id());
        return t.unique() && owner != null && !owner.equals(uuid);
    }

    /** Nombre de titres débloqués (TITLES : ids ou « rarity:EPIC » dans targets). */
    public int countUnlocked(PlayerProfile p, Objective o) {
        int n = 0;
        for (String id : p.titles().keySet()) {
            PlayerTitle t = get(id);
            if (t != null && (o.targets.isEmpty() || o.matches(id) || o.matches("rarity:" + t.rarity()))) {
                n++;
            }
        }
        return n;
    }

    // ------------------------------------------------------------ Progression et déblocage

    /** Action d'un joueur : fait avancer les objectifs « action » des titres non débloqués. */
    public void progress(Player player, Objective.Type type, List<String> candidates, double amount, String entityName) {
        if (!enabled()) {
            return;
        }
        PlayerProfile p = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (p == null) {
            return;
        }
        for (PlayerTitle t : titles.values()) {
            if (p.hasTitle(t.id()) || t.unlock().isEmpty() || takenByOther(t, player.getUniqueId())) {
                continue;
            }
            boolean changed = false;
            for (int i = 0; i < t.unlock().size(); i++) {
                Objective o = t.unlock().get(i);
                if (o.type != type || !o.matchesAny(candidates) || !nameMatches(o, entityName) || !o.conditions.test(player)) {
                    continue;
                }
                double current = p.getTitleProgress(t.id(), i);
                if (current < o.amount) {
                    p.setTitleProgress(t.id(), i, Math.min(o.amount, current + amount));
                    changed = true;
                }
            }
            if (changed) {
                check(player, p, t);
            }
        }
    }

    private static boolean nameMatches(Objective o, String entityName) {
        if (o.entityName == null || o.entityName.isBlank()) {
            return true;
        }
        return entityName != null && entityName.toLowerCase(Locale.ROOT).contains(Text.plain(o.entityName).toLowerCase(Locale.ROOT));
    }

    public double value(Player player, PlayerProfile p, PlayerTitle t, int index) {
        Objective o = t.unlock().get(index);
        return o.type.isState() ? plugin.quests().stateValue(player, p, o) : p.getTitleProgress(t.id(), index);
    }

    private boolean canUnlock(Player player, PlayerProfile p, PlayerTitle t) {
        if (t.unlock().isEmpty() || takenByOther(t, player.getUniqueId())
                || !plugin.quests().requirementsMet(player, p, t.requirements())) {
            return false;
        }
        for (int i = 0; i < t.unlock().size(); i++) {
            if (value(player, p, t, i) < t.unlock().get(i).amount) {
                return false;
            }
        }
        return true;
    }

    private void check(Player player, PlayerProfile p, PlayerTitle t) {
        if (canUnlock(player, p, t)) {
            unlock(player, t.id(), true);
        }
    }

    private void checkAll() {
        if (!enabled()) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile p = plugin.profiles().getIfLoaded(player.getUniqueId());
            if (p == null || !p.hasClass() || plugin.character().creationPending(player)) {
                continue;
            }
            for (PlayerTitle t : titles.values()) {
                if (p.hasTitle(t.id())) {
                    checkUniqueStillOwned(player, p, t);
                } else {
                    check(player, p, t);
                }
            }
        }
    }

    /** Titre unique libéré par un administrateur : l'ancien porteur le perd. */
    private void checkUniqueStillOwned(Player player, PlayerProfile p, PlayerTitle t) {
        if (!t.unique()) {
            return;
        }
        String key = t.id() + ":" + player.getUniqueId();
        if (released.remove(key)) {
            saveUniques();
            p.removeTitle(t.id());
            refresh(player);
            plugin.messages().send(player, "titles.lost", Text.ph("title", t.name()));
            return;
        }
        UUID owner = uniqueOwners.get(t.id());
        if (owner == null) {
            uniqueOwners.put(t.id(), player.getUniqueId());
            saveUniques();
        } else if (!owner.equals(player.getUniqueId())) {
            p.removeTitle(t.id());
            refresh(player);
        }
    }

    /** Débloque un titre (quête, condition remplie, administration). */
    public boolean unlock(Player player, String id, boolean announce) {
        PlayerTitle t = get(id);
        PlayerProfile p = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (t == null || p == null || p.hasTitle(t.id())) {
            return false;
        }
        if (t.unique()) {
            if (takenByOther(t, player.getUniqueId())) {
                return false;
            }
            uniqueOwners.put(t.id(), player.getUniqueId());
            released.removeIf(k -> k.startsWith(t.id() + ":"));
            saveUniques();
        }
        p.addTitle(t.id());
        if (announce) {
            Rarity r = rarity(t);
            plugin.messages().send(player, "titles.unlocked", Text.ph("title", t.name()), Text.ph("rarity", r.label()));
            player.showTitle(Title.title(plugin.messages().get("titles.unlocked-title"), Text.mm(t.name()),
                    Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(2500), Duration.ofMillis(500))));
            player.playSound(Sound.sound(Key.key("ui.toast.challenge_complete"), Sound.Source.PLAYER, 0.8f, 1.3f));
            if (r.broadcast() || t.unique()) {
                Component msg = plugin.messages().get("titles.broadcast", Text.ph("player", player.getName()),
                        Text.ph("title", t.name()), Text.ph("rarity", r.label()));
                Bukkit.getOnlinePlayers().stream().filter(o -> !o.equals(player)).forEach(o -> o.sendMessage(msg));
            }
        }
        if (cfg.getBoolean("auto-equip-first", true) && p.getEquippedTitle() == null) {
            equip(player, t.id(), false);
        }
        return true;
    }

    /** Retire un titre à un joueur (administration). Un titre unique redevient disponible. */
    public void revoke(Player player, String id) {
        PlayerTitle t = get(id);
        PlayerProfile p = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (t == null || p == null) {
            return;
        }
        p.removeTitle(t.id());
        if (t.unique() && player.getUniqueId().equals(uniqueOwners.get(t.id()))) {
            uniqueOwners.remove(t.id());
            saveUniques();
        }
        refresh(player);
    }

    /** Libère un titre unique : n'importe qui peut de nouveau l'obtenir, l'ancien porteur le perd. */
    public boolean release(String id) {
        PlayerTitle t = get(id);
        if (t == null || !t.unique()) {
            return false;
        }
        UUID owner = uniqueOwners.remove(t.id());
        if (owner != null) {
            Player online = Bukkit.getPlayer(owner);
            if (online != null) {
                revoke(online, t.id());
                plugin.messages().send(online, "titles.lost", Text.ph("title", t.name()));
            } else {
                released.add(t.id() + ":" + owner);
            }
        }
        saveUniques();
        return true;
    }

    public void equip(Player player, String id, boolean message) {
        PlayerProfile p = plugin.profiles().get(player);
        PlayerTitle t = get(id);
        if (id != null && (t == null || !p.hasTitle(t.id()))) {
            return;
        }
        p.setEquippedTitle(t == null ? null : t.id());
        refresh(player);
        if (message) {
            if (t == null) {
                plugin.messages().send(player, "titles.unequipped");
            } else {
                plugin.messages().send(player, "titles.equipped", Text.ph("title", t.name()));
            }
        }
    }

    /** Stats et affichage à jour après un changement de titre. */
    private void refresh(Player player) {
        plugin.stats().invalidate(player.getUniqueId());
        plugin.stats().apply(player);
        removeDisplay(player.getUniqueId());
    }

    // ------------------------------------------------------------ Chat

    /** Titre pour le chat (« <title> » dans les formats de config.yml → chat). */
    public Component chatTag(Player player) {
        PlayerTitle t = equipped(plugin.profiles().getIfLoaded(player.getUniqueId()));
        if (t == null) {
            return Component.empty();
        }
        return Text.mm(cfg.getString("chat.format", "<title> "), Text.ph("title", t.name()));
    }

    // ------------------------------------------------------------ Au-dessus de la tête

    private void updateDisplays() {
        ConfigurationSection d = cfg.getConfigurationSection("display");
        boolean on = enabled() && d != null && d.getBoolean("enabled", true);
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerTitle t = on ? equipped(plugin.profiles().getIfLoaded(player.getUniqueId())) : null;
            if (t == null || !visible(player, d)) {
                removeDisplay(player.getUniqueId());
                continue;
            }
            Location loc = player.getLocation().add(0, player.getHeight() + d.getDouble("height", 0.55), 0);
            loc.setYaw(0);
            loc.setPitch(0);
            TextDisplay td = displays.get(player.getUniqueId());
            if (td == null || !td.isValid() || td.getWorld() != player.getWorld() || !t.id().equals(displayed.get(player.getUniqueId()))) {
                removeDisplay(player.getUniqueId());
                td = spawn(player, loc, t, d);
                displays.put(player.getUniqueId(), td);
                displayed.put(player.getUniqueId(), t.id());
            } else {
                td.teleport(loc);
            }
        }
    }

    private boolean visible(Player player, ConfigurationSection d) {
        if (player.isDead() || player.getGameMode() == GameMode.SPECTATOR || player.isInvisible()
                || player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            return false;
        }
        if (d.getBoolean("hide-when-sneaking", true) && player.isSneaking()) {
            return false;
        }
        for (MetadataValue v : player.getMetadata("vanished")) {
            if (v.asBoolean()) {
                return false;
            }
        }
        return true;
    }

    private TextDisplay spawn(Player owner, Location loc, PlayerTitle t, ConfigurationSection d) {
        float scale = (float) d.getDouble("scale", 0.9);
        String bg = d.getString("background", "default");
        TextDisplay td = loc.getWorld().spawn(loc, TextDisplay.class, e -> {
            e.setPersistent(false);
            e.text(Text.mm(d.getString("format", "<title>"), Text.ph("title", t.name()),
                    Text.ph("rarity", rarity(t).label())));
            e.setBillboard(Display.Billboard.CENTER);
            e.setShadowed(d.getBoolean("shadow", true));
            e.setSeeThrough(d.getBoolean("see-through", false));
            e.setTeleportDuration(2);
            e.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(),
                    new Vector3f(scale, scale, scale), new AxisAngle4f()));
            if ("none".equalsIgnoreCase(bg)) {
                e.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            } else if (bg != null && bg.startsWith("#") && bg.length() == 9) {
                try {
                    e.setBackgroundColor(Color.fromARGB((int) Long.parseLong(bg.substring(1), 16)));
                } catch (NumberFormatException ignored) {
                    // couleur par défaut
                }
            }
        });
        if (!d.getBoolean("show-own", false)) {
            owner.hideEntity(plugin, td);
        }
        return td;
    }

    private void removeDisplay(UUID uuid) {
        TextDisplay td = displays.remove(uuid);
        displayed.remove(uuid);
        if (td != null) {
            td.remove();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeDisplay(event.getPlayer().getUniqueId());
    }
}
