package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.hook.MythicHook;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.party.Party;
import fr.cykalos.cykclasses.util.Text;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Affichages : XP gagnée, dégâts flottants, PV de la cible, panneau du groupe. Réglages dans hud.yml.
 */
public final class HudService {

    private static final class XpState {
        BossBar bar;
        double combo;
        long until;
        Component actionbar;
    }

    private static final class TargetState {
        BossBar bar;
        UUID target;
        long until;
    }

    private static final class HeadState {
        TextDisplay display;
        final Set<UUID> viewers = new HashSet<>();
        long until;
    }

    private final CykClasses plugin;
    private YamlConfiguration cfg;
    private BukkitTask task;
    private BukkitTask followTask;
    private final Map<UUID, XpState> xp = new HashMap<>();
    private final Map<UUID, XpState> jobXp = new HashMap<>();
    private final Map<UUID, TargetState> targets = new HashMap<>();
    private final Map<UUID, HeadState> heads = new HashMap<>();
    private final Map<UUID, Boolean> pendingCrit = new HashMap<>();
    private final Set<TextDisplay> floating = new HashSet<>();
    private final Map<UUID, Scoreboard> previousBoards = new HashMap<>();
    private final Map<UUID, Scoreboard> partyBoards = new HashMap<>();
    private int tickCount;

    public HudService(CykClasses plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "hud.yml");
        if (!file.exists()) {
            plugin.saveResource("hud.yml", false);
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        fr.cykalos.cykclasses.config.ConfigDefaults.fill(plugin, cfg, "hud.yml", java.util.Set.of());
    }

    public YamlConfiguration config() {
        return cfg;
    }

    public void start() {
        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 5L, 5L);
        if (followTask != null) {
            followTask.cancel();
        }
        followTask = Bukkit.getScheduler().runTaskTimer(plugin, this::followHeads, 2L, 2L);
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (followTask != null) {
            followTask.cancel();
            followTask = null;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            clear(p);
            removePartyBoard(p);
        }
        for (HeadState h : heads.values()) {
            h.display.remove();
        }
        heads.clear();
        floating.forEach(Entity::remove);
        floating.clear();
    }

    /** Nettoie tout ce qui concerne un joueur (déconnexion). */
    public void clear(Player player) {
        XpState x = xp.remove(player.getUniqueId());
        if (x != null && x.bar != null) {
            player.hideBossBar(x.bar);
        }
        XpState jx = jobXp.remove(player.getUniqueId());
        if (jx != null && jx.bar != null) {
            player.hideBossBar(jx.bar);
        }
        TargetState t = targets.remove(player.getUniqueId());
        if (t != null && t.bar != null) {
            player.hideBossBar(t.bar);
        }
        HeadState own = heads.remove(player.getUniqueId());
        if (own != null) {
            own.display.remove();
        }
        partyBoards.remove(player.getUniqueId());
        previousBoards.remove(player.getUniqueId());
    }

    private void tick() {
        long now = System.currentTimeMillis();
        tickCount++;
        // XP
        for (Iterator<Map.Entry<UUID, XpState>> it = xp.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, XpState> e = it.next();
            if (e.getValue().until < now) {
                Player p = Bukkit.getPlayer(e.getKey());
                if (p != null && e.getValue().bar != null) {
                    p.hideBossBar(e.getValue().bar);
                }
                it.remove();
            }
        }
        for (Iterator<Map.Entry<UUID, XpState>> it = jobXp.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, XpState> e = it.next();
            if (e.getValue().until < now) {
                Player p = Bukkit.getPlayer(e.getKey());
                if (p != null && e.getValue().bar != null) {
                    p.hideBossBar(e.getValue().bar);
                }
                it.remove();
            }
        }
        // Cibles (bossbar)
        for (Iterator<Map.Entry<UUID, TargetState>> it = targets.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, TargetState> e = it.next();
            Player viewer = Bukkit.getPlayer(e.getKey());
            TargetState st = e.getValue();
            Entity target = Bukkit.getEntity(st.target);
            boolean gone = !(target instanceof LivingEntity le) || !le.isValid() || le.isDead();
            if (viewer == null || st.until < now || (gone && st.until - now > 1000)) {
                if (gone && viewer != null && st.until >= now) {
                    st.until = now + 1000; // laisse voir la barre vide une seconde
                    st.bar.progress(0f);
                    continue;
                }
                if (viewer != null) {
                    viewer.hideBossBar(st.bar);
                }
                it.remove();
            } else if (!gone) {
                updateTargetBar(st, (LivingEntity) target);
            }
        }
        // Au-dessus de la tête
        for (Iterator<Map.Entry<UUID, HeadState>> it = heads.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, HeadState> e = it.next();
            HeadState h = e.getValue();
            Entity target = Bukkit.getEntity(e.getKey());
            if (!(target instanceof LivingEntity le) || !le.isValid() || le.isDead() || h.until < now || !h.display.isValid()) {
                h.display.remove();
                it.remove();
            } else {
                h.display.text(headText(le));
            }
        }
        // Panneau de groupe (toutes les 10 ticks)
        if (tickCount % 2 == 0) {
            Set<Party> done = new HashSet<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                Party party = plugin.parties().get(p);
                if (party != null && done.add(party)) {
                    refreshPartyBoard(party);
                }
            }
            plugin.parties().tickInvites();
        }
    }

    // ------------------------------------------------------------ XP

    public void showXp(Player player, double amount, RpgClass rc, PlayerProfile profile) {
        String mode = cfg.getString("xp-display.mode", "BOSSBAR").toUpperCase(Locale.ROOT);
        if (mode.equals("NONE")) {
            return;
        }
        long now = System.currentTimeMillis();
        XpState st = xp.computeIfAbsent(player.getUniqueId(), k -> new XpState());
        boolean combo = cfg.getBoolean("xp-display.combo", true) && st.until >= now;
        st.combo = combo ? st.combo + amount : amount;
        st.until = now + (long) (cfg.getDouble("xp-display.duration-seconds", 5) * 1000);
        double req = plugin.levels().xpRequired(profile.getLevel());
        boolean max = profile.getLevel() >= plugin.levels().maxLevel();
        float progress = max ? 1f : (float) Math.max(0, Math.min(1, profile.getXp() / req));
        TagResolver[] r = {
                Text.ph("xp", Text.format(Math.round(st.combo * 10) / 10.0)),
                Text.ph("class", rc.name),
                Text.ph("level", profile.getLevel()),
                Text.ph("percent", (int) Math.floor(progress * 100))
        };
        if (mode.equals("BOSSBAR") || mode.equals("BOTH")) {
            Component title = Text.mm(cfg.getString("xp-display.bossbar.format", "+<xp> XP"), r);
            if (st.bar == null) {
                st.bar = BossBar.bossBar(title, progress, color(cfg.getString("xp-display.bossbar.color"), BossBar.Color.BLUE),
                        overlay(cfg.getString("xp-display.bossbar.style"), BossBar.Overlay.NOTCHED_10));
                player.showBossBar(st.bar);
            } else {
                st.bar.name(title);
                st.bar.progress(progress);
            }
        }
        if (mode.equals("ACTIONBAR") || mode.equals("BOTH")) {
            st.actionbar = Text.mm(cfg.getString("xp-display.actionbar.format", "+<xp> XP"), r);
            player.sendActionBar(st.actionbar);
        } else {
            st.actionbar = null;
        }
    }

    /** XP de métier : barre de boss séparée (hud.yml → job-xp-display). */
    public void showJobXp(Player player, double amount, fr.cykalos.cykclasses.content.Job job, int level, float progress) {
        if (!cfg.getBoolean("job-xp-display.enabled", true)) {
            return;
        }
        long now = System.currentTimeMillis();
        XpState st = jobXp.computeIfAbsent(player.getUniqueId(), k -> new XpState());
        boolean combo = st.until >= now;
        st.combo = combo ? st.combo + amount : amount;
        st.until = now + (long) (cfg.getDouble("job-xp-display.duration-seconds", 4) * 1000);
        Component title = Text.mm(cfg.getString("job-xp-display.format", "<gold>+<xp> XP <job> Niv. <level>"),
                Text.ph("xp", Text.format(Math.round(st.combo * 10) / 10.0)), Text.ph("job", job.name()),
                Text.ph("level", level), Text.ph("percent", (int) Math.floor(progress * 100)));
        if (st.bar == null) {
            st.bar = BossBar.bossBar(title, progress, color(cfg.getString("job-xp-display.color"), BossBar.Color.YELLOW),
                    overlay(cfg.getString("job-xp-display.style"), BossBar.Overlay.NOTCHED_10));
            player.showBossBar(st.bar);
        } else {
            st.bar.name(title);
            st.bar.progress(progress);
        }
    }

    /** Texte d'XP à afficher dans la barre d'action pendant sa durée de vie, sinon null. */
    public Component xpActionbar(Player player) {
        XpState st = xp.get(player.getUniqueId());
        return st == null || st.until < System.currentTimeMillis() ? null : st.actionbar;
    }

    // ------------------------------------------------------------ Dégâts

    /** Marque le prochain coup sur cette victime comme critique (critique de classe). */
    public void markCritical(Entity victim) {
        pendingCrit.put(victim.getUniqueId(), Boolean.TRUE);
    }

    public boolean consumeCritical(Entity victim) {
        return pendingCrit.remove(victim.getUniqueId()) != null;
    }

    public void damage(LivingEntity victim, Player attacker, boolean hasSource, double amount, boolean crit, boolean skill) {
        ConfigurationSection s = cfg.getConfigurationSection("damage-indicators");
        if (s == null || !s.getBoolean("enabled", true) || amount <= 0) {
            return;
        }
        boolean isPlayer = victim instanceof Player;
        if ((isPlayer && !s.getBoolean("show-on-players", true)) || (!isPlayer && !s.getBoolean("show-on-mobs", true))) {
            return;
        }
        if (!hasSource && !s.getBoolean("show-environmental", false)) {
            return;
        }
        String key = crit ? "critical" : skill ? "skill" : isPlayer ? "player-hit" : "normal";
        String text = s.getString("formats." + key, "-<damage>");
        Component comp = Text.mm(text, Text.ph("damage", number(amount, s.getInt("decimals", 1))));
        float scale = (float) (crit ? s.getDouble("critical-scale", 1.4) : s.getDouble("scale", 1.0));
        spawnFloating(victim, comp, scale, viewers(s.getString("visible-to", "INVOLVED"), attacker, victim));
    }

    public void heal(LivingEntity target, Player healer, double amount) {
        ConfigurationSection s = cfg.getConfigurationSection("damage-indicators");
        if (s == null || !s.getBoolean("enabled", true) || amount < 0.5) {
            return;
        }
        Component comp = Text.mm(s.getString("formats.heal", "+<amount>"), Text.ph("amount", number(amount, s.getInt("decimals", 1))));
        spawnFloating(target, comp, (float) s.getDouble("scale", 1.0), viewers(s.getString("visible-to", "INVOLVED"), healer, target));
    }

    /** @return null = visible par tous, sinon la liste des joueurs autorisés. */
    private List<Player> viewers(String mode, Player attacker, LivingEntity victim) {
        mode = mode.toUpperCase(Locale.ROOT);
        if (mode.equals("ALL")) {
            return null;
        }
        List<Player> out = new ArrayList<>();
        if (attacker != null) {
            // En groupe : les alliés proches voient aussi les dégâts de leurs coéquipiers
            out.addAll(cfg.getBoolean("party.share-with-party", true) ? partyNear(attacker) : List.of(attacker));
        }
        if (mode.equals("INVOLVED") && victim instanceof Player p && !out.contains(p)) {
            out.add(p);
        }
        return out;
    }

    private void spawnFloating(LivingEntity victim, Component text, float scale, List<Player> viewers) {
        if (viewers != null && viewers.isEmpty()) {
            return;
        }
        ConfigurationSection s = cfg.getConfigurationSection("damage-indicators");
        int duration = Math.max(5, s == null ? 22 : s.getInt("duration-ticks", 22));
        double rise = s == null ? 0.035 : s.getDouble("rise-per-tick", 0.035);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        Location loc = victim.getLocation().add(r.nextDouble(-0.5, 0.5), victim.getHeight() * 0.75 + r.nextDouble(0, 0.4), r.nextDouble(-0.5, 0.5));
        TextDisplay d = victim.getWorld().spawn(loc, TextDisplay.class, td -> {
            td.text(text);
            td.setPersistent(false);
            td.setBillboard(Display.Billboard.CENTER);
            td.setShadowed(true);
            td.setSeeThrough(false);
            td.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            td.setBrightness(new Display.Brightness(15, 15));
            td.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(scale), new AxisAngle4f()));
            if (viewers != null) {
                td.setVisibleByDefault(false);
            }
        });
        if (viewers != null) {
            for (Player p : viewers) {
                p.showEntity(plugin, d);
            }
        }
        floating.add(d);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (d.isValid()) {
                d.setInterpolationDelay(0);
                d.setInterpolationDuration(duration);
                d.setTransformation(new Transformation(new Vector3f(0, (float) (rise * duration), 0), new AxisAngle4f(),
                        new Vector3f(scale * 0.8f), new AxisAngle4f()));
            }
        }, 2L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            d.remove();
            floating.remove(d);
        }, duration + 2L);
    }

    private static String number(double v, int decimals) {
        if (decimals <= 0) {
            return String.valueOf(Math.round(v));
        }
        double f = Math.pow(10, decimals);
        double rounded = Math.round(v * f) / f;
        return rounded == Math.floor(rounded) ? String.valueOf((long) rounded) : String.valueOf(rounded);
    }

    // ------------------------------------------------------------ PV de la cible

    /** Appelé quand {@code attacker} touche {@code victim} (mis à jour au tick suivant, après les dégâts). */
    public void onHit(Player attacker, LivingEntity victim) {
        ConfigurationSection s = cfg.getConfigurationSection("target-health");
        if (s == null || !s.getBoolean("enabled", true) || attacker.equals(victim)) {
            return;
        }
        boolean isPlayer = victim instanceof Player;
        if ((isPlayer && !s.getBoolean("show-for-players", true)) || (!isPlayer && !s.getBoolean("show-for-mobs", true))) {
            return;
        }
        String mode = s.getString("mode", "BOTH").toUpperCase(Locale.ROOT);
        long until = System.currentTimeMillis() + (long) (s.getDouble("duration-seconds", 5) * 1000);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!attacker.isOnline()) {
                return;
            }
            boolean alive = victim.isValid() && !victim.isDead();
            List<Player> group = cfg.getBoolean("party.share-with-party", true) ? partyNear(attacker) : List.of(attacker);
            // En groupe : pas de bossbar (barre au-dessus de la tête à la place), visible par les alliés
            boolean partyHead = group.size() > 1 && cfg.getBoolean("party.no-bossbar-in-party", true);
            if (!partyHead && (mode.equals("BOSSBAR") || mode.equals("BOTH"))) {
                TargetState st = targets.get(attacker.getUniqueId());
                if (st == null) {
                    st = new TargetState();
                    st.bar = BossBar.bossBar(Component.empty(), 1f, BossBar.Color.RED,
                            overlay(s.getString("bossbar.style"), BossBar.Overlay.PROGRESS));
                    targets.put(attacker.getUniqueId(), st);
                    attacker.showBossBar(st.bar);
                }
                st.target = victim.getUniqueId();
                st.until = alive ? until : System.currentTimeMillis() + 1000;
                updateTargetBar(st, victim);
            }
            if (partyHead) {
                TargetState old = targets.remove(attacker.getUniqueId());
                if (old != null) {
                    attacker.hideBossBar(old.bar);
                }
            }
            if (alive && (partyHead || mode.equals("ABOVE_HEAD") || mode.equals("BOTH"))) {
                for (Player p : group) {
                    showHead(p, victim, until);
                }
            }
        });
    }

    /** Le joueur et les membres de son groupe proches de lui (même monde, 48 blocs). */
    private List<Player> partyNear(Player player) {
        List<Player> out = new ArrayList<>();
        out.add(player);
        var party = plugin.parties().get(player);
        if (party == null) {
            return out;
        }
        double range = cfg.getDouble("party.range", 48);
        for (UUID id : party.members()) {
            Player m = Bukkit.getPlayer(id);
            if (m != null && !m.equals(player) && m.getWorld().equals(player.getWorld())
                    && m.getLocation().distanceSquared(player.getLocation()) <= range * range) {
                out.add(m);
            }
        }
        return out;
    }

    private void updateTargetBar(TargetState st, LivingEntity target) {
        ConfigurationSection s = cfg.getConfigurationSection("target-health");
        double max = maxHealth(target);
        double hp = target.isDead() ? 0 : Math.max(0, target.getHealth());
        float pct = (float) Math.max(0, Math.min(1, hp / max));
        st.bar.name(Text.mm(s.getString("bossbar.format", "<name> <health>/<max_health>"), resolvers(target, hp, max)));
        st.bar.progress(pct);
        int p = Math.round(pct * 100);
        String colorKey = p <= s.getInt("low-threshold", 30) ? "color-low" : p <= s.getInt("medium-threshold", 60) ? "color-medium" : "color-high";
        st.bar.color(color(s.getString("bossbar." + colorKey), BossBar.Color.RED));
    }

    private void showHead(Player viewer, LivingEntity target, long until) {
        ConfigurationSection s = cfg.getConfigurationSection("target-health.above-head");
        boolean all = s != null && "ALL".equalsIgnoreCase(s.getString("visible-to", "DAMAGERS"));
        HeadState h = heads.get(target.getUniqueId());
        if (h == null || !h.display.isValid()) {
            h = new HeadState();
            float scale = s == null ? 0.9f : (float) s.getDouble("scale", 0.9);
            float offset = s == null ? 0.45f : (float) s.getDouble("height-offset", 0.45);
            int bg = parseArgb(s == null ? "40000000" : s.getString("background", "40000000"));
            TextDisplay d = target.getWorld().spawn(headLocation(target), TextDisplay.class, td -> {
                td.setPersistent(false);
                td.setBillboard(Display.Billboard.CENTER);
                td.setShadowed(true);
                td.setBackgroundColor(Color.fromARGB(bg));
                td.setAlignment(TextDisplay.TextAlignment.CENTER);
                td.setTransformation(new Transformation(new Vector3f(0, offset, 0), new AxisAngle4f(), new Vector3f(scale), new AxisAngle4f()));
                if (!all) {
                    td.setVisibleByDefault(false);
                }
            });
            d.setTeleportDuration(2);
            h.display = d;
            heads.put(target.getUniqueId(), h);
        }
        if (!all && h.viewers.add(viewer.getUniqueId())) {
            viewer.showEntity(plugin, h.display);
        }
        h.until = until;
        h.display.text(headText(target));
    }

    private static Location headLocation(LivingEntity target) {
        Location loc = target.getLocation().add(0, target.getHeight(), 0);
        loc.setYaw(0);
        loc.setPitch(0);
        return loc;
    }

    /** Fait suivre les affichages au-dessus des têtes (sans monter sur l'entité, pour ne pas bloquer ses téléportations). */
    private void followHeads() {
        for (Map.Entry<UUID, HeadState> e : heads.entrySet()) {
            Entity target = Bukkit.getEntity(e.getKey());
            TextDisplay d = e.getValue().display;
            if (target instanceof LivingEntity le && d.isValid()) {
                Location to = headLocation(le);
                if (!to.getWorld().equals(d.getWorld()) || to.distanceSquared(d.getLocation()) > 0.0004) {
                    d.teleport(to);
                }
            }
        }
    }

    private Component headText(LivingEntity target) {
        ConfigurationSection s = cfg.getConfigurationSection("target-health.above-head");
        double max = maxHealth(target);
        double hp = Math.max(0, target.getHealth());
        return Text.mm(s == null ? "<health>/<max_health>" : s.getString("format", "<health>/<max_health>"), resolvers(target, hp, max));
    }

    private TagResolver[] resolvers(LivingEntity target, double hp, double max) {
        ConfigurationSection s = cfg.getConfigurationSection("target-health");
        int pct = (int) Math.round(hp / max * 100);
        String levelTag = "";
        MythicHook mythic = plugin.mythic();
        if (mythic != null && !(target instanceof Player)) {
            var info = mythic.info(target);
            if (info.isPresent() && info.get().level() > 0) {
                levelTag = s.getString("level-format", " [<level>]").replace("<level>", Text.format(Math.floor(info.get().level())));
            }
        }
        return new TagResolver[]{
                Text.phc("name", name(target)),
                Text.ph("level_tag", levelTag),
                Text.ph("health", Text.format(Math.ceil(hp * 10) / 10.0)),
                Text.ph("max_health", Text.format(Math.round(max * 10) / 10.0)),
                Text.ph("percent", pct),
                Text.ph("bar", bar(hp / max, s.getInt("above-head.bar-length", 10), s.getString("above-head.bar-char", "▌"),
                        s.getString("above-head.bar-full", "<green>"), s.getString("above-head.bar-medium", "<yellow>"),
                        s.getString("above-head.bar-low", "<red>"), s.getString("above-head.bar-empty", "<dark_gray>"),
                        s.getInt("medium-threshold", 60), s.getInt("low-threshold", 30)))
        };
    }

    private static String bar(double ratio, int length, String ch, String full, String medium, String low, String empty,
                              int mediumThreshold, int lowThreshold) {
        ratio = Math.max(0, Math.min(1, ratio));
        int filled = (int) Math.ceil(ratio * length);
        if (ratio <= 0) {
            filled = 0;
        }
        int pct = (int) Math.round(ratio * 100);
        String color = pct <= lowThreshold ? low : pct <= mediumThreshold ? medium : full;
        return color + ch.repeat(filled) + empty + ch.repeat(Math.max(0, length - filled));
    }

    public Component name(LivingEntity e) {
        if (e instanceof Player p) {
            return p.displayName();
        }
        Component custom = e.customName();
        if (custom != null) {
            return custom;
        }
        MythicHook mythic = plugin.mythic();
        if (mythic != null) {
            Component m = mythic.displayName(e);
            if (m != null) {
                return m;
            }
        }
        return Component.translatable(e.getType().translationKey());
    }

    private static double maxHealth(LivingEntity e) {
        var attr = e.getAttribute(Attribute.MAX_HEALTH);
        return attr == null ? 20 : Math.max(1, attr.getValue());
    }

    // ------------------------------------------------------------ Panneau du groupe

    public void refreshPartyBoard(Party party) {
        ConfigurationSection s = cfg.getConfigurationSection("party-board");
        if (s == null || !s.getBoolean("enabled", true)) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        for (UUID uuid : party.members()) {
            Player p = Bukkit.getPlayer(uuid);
            String leader = party.isLeader(uuid) ? s.getString("leader-symbol", "★ ") : "";
            if (p == null) {
                String n = Bukkit.getOfflinePlayer(uuid).getName();
                lines.add(Text.mm(s.getString("offline-line", "<name>"), Text.ph("name", n == null ? "?" : n)));
                continue;
            }
            PlayerProfile prof = plugin.profiles().get(p);
            RpgClass rc = plugin.classes().effective(prof);
            double max = maxHealth(p);
            double hp = p.isDead() ? 0 : p.getHealth();
            ConfigurationSection th = cfg.getConfigurationSection("target-health");
            var race = plugin.content().race(prof.getRaceId());
            TagResolver[] r = {
                    Text.ph("leader", leader),
                    Text.ph("name", p.getName()),
                    Text.ph("health", (int) Math.ceil(hp)),
                    Text.ph("max_health", (int) Math.ceil(max - 1e-6)),
                    Text.ph("mana", (int) prof.getMana()),
                    Text.ph("max_mana", (int) plugin.stats().maxMana(prof)),
                    Text.ph("class", rc == null ? "<gray>-" : rc.name),
                    Text.ph("race", race == null ? "" : race.name()),
                    Text.ph("level", prof.getLevel()),
                    Text.ph("bar", bar(hp / max, s.getInt("bar-length", 6), th.getString("above-head.bar-char", "▌"),
                            th.getString("above-head.bar-full", "<green>"), th.getString("above-head.bar-medium", "<yellow>"),
                            th.getString("above-head.bar-low", "<red>"), th.getString("above-head.bar-empty", "<dark_gray>"),
                            th.getInt("medium-threshold", 60), th.getInt("low-threshold", 30)))
            };
            List<String> memberLines = s.getStringList("member-lines");
            if (memberLines.isEmpty()) {
                memberLines = List.of(s.getString("line", "<name> <health>"));
            }
            for (String ml : memberLines) {
                lines.add(Text.mm(ml, r));
            }
        }
        String footer = s.getString("footer", "");
        if (footer != null && !footer.isBlank()) {
            lines.add(Component.empty());
            lines.add(Text.mm(footer, Text.ph("xp_mode", plugin.parties().modeName(party.xpMode())),
                    Text.ph("friendly_fire", plugin.parties().ffName(party.friendlyFire()))));
        }
        Component title = Text.mm(s.getString("title", "Groupe"));
        for (Player p : party.onlineMembers()) {
            applyBoard(p, title, lines);
        }
    }

    private void applyBoard(Player player, Component title, List<Component> lines) {
        Scoreboard sb = partyBoards.get(player.getUniqueId());
        if (sb == null || player.getScoreboard() != sb) {
            if (sb == null) {
                previousBoards.put(player.getUniqueId(), player.getScoreboard());
            }
            sb = Bukkit.getScoreboardManager().getNewScoreboard();
            partyBoards.put(player.getUniqueId(), sb);
            player.setScoreboard(sb);
        }
        Objective obj = sb.getObjective("cykparty");
        if (obj == null) {
            obj = sb.registerNewObjective("cykparty", Criteria.DUMMY, title);
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.numberFormat(NumberFormat.blank());
        } else {
            obj.displayName(title);
        }
        for (String entry : new HashSet<>(sb.getEntries())) {
            int index = entry.startsWith("l") ? parseIndex(entry) : -1;
            if (index < 0 || index >= lines.size()) {
                sb.resetScores(entry);
            }
        }
        for (int i = 0; i < lines.size(); i++) {
            Score score = obj.getScore("l" + i);
            score.setScore(lines.size() - i);
            score.customName(lines.get(i));
        }
    }

    private static int parseIndex(String entry) {
        try {
            return Integer.parseInt(entry.substring(1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void removePartyBoard(Player player) {
        Scoreboard sb = partyBoards.remove(player.getUniqueId());
        Scoreboard previous = previousBoards.remove(player.getUniqueId());
        if (sb != null && player.getScoreboard() == sb) {
            player.setScoreboard(previous != null ? previous : Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    // ------------------------------------------------------------ Utilitaires

    private static BossBar.Color color(String name, BossBar.Color def) {
        if (name == null) {
            return def;
        }
        try {
            return BossBar.Color.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    private static BossBar.Overlay overlay(String name, BossBar.Overlay def) {
        if (name == null) {
            return def;
        }
        try {
            return BossBar.Overlay.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    private static int parseArgb(String hex) {
        try {
            return (int) Long.parseLong(hex.replace("#", ""), 16);
        } catch (NumberFormatException e) {
            return 0x40000000;
        }
    }
}
