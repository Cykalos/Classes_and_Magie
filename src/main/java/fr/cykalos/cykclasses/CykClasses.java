package fr.cykalos.cykclasses;

import fr.cykalos.cykclasses.command.CharacterCommand;
import fr.cykalos.cykclasses.command.ClasseCommand;
import fr.cykalos.cykclasses.content.ContentRegistry;
import fr.cykalos.cykclasses.listener.ChatListener;
import fr.cykalos.cykclasses.listener.GearListener;
import fr.cykalos.cykclasses.service.GearService;
import fr.cykalos.cykclasses.service.JobDescriptions;
import fr.cykalos.cykclasses.service.JobService;
import fr.cykalos.cykclasses.listener.JobListener;
import fr.cykalos.cykclasses.service.CharacterService;
import fr.cykalos.cykclasses.service.ItemFactory;
import fr.cykalos.cykclasses.service.ItemRestrictionService;
import fr.cykalos.cykclasses.command.CompetenceCommand;
import fr.cykalos.cykclasses.command.GroupeCommand;
import fr.cykalos.cykclasses.config.ClassRegistry;
import fr.cykalos.cykclasses.config.Messages;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.data.ProfileManager;
import fr.cykalos.cykclasses.hook.MythicHook;
import fr.cykalos.cykclasses.hook.PlaceholderHook;
import fr.cykalos.cykclasses.listener.CastingListener;
import fr.cykalos.cykclasses.listener.CombatListener;
import fr.cykalos.cykclasses.listener.ConnectionListener;
import fr.cykalos.cykclasses.listener.GuiListener;
import fr.cykalos.cykclasses.listener.HudListener;
import fr.cykalos.cykclasses.listener.ProgressListener;
import fr.cykalos.cykclasses.service.ClassService;
import fr.cykalos.cykclasses.service.CombatService;
import fr.cykalos.cykclasses.party.PartyManager;
import fr.cykalos.cykclasses.service.FlawService;
import fr.cykalos.cykclasses.service.HudService;
import fr.cykalos.cykclasses.service.LevelService;
import fr.cykalos.cykclasses.service.ManaService;
import fr.cykalos.cykclasses.service.StatsService;
import fr.cykalos.cykclasses.skill.SkillBar;
import fr.cykalos.cykclasses.skill.SkillService;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * CykClasses — classes, sous-classes, compétences, statistiques, défauts et mana.
 */
public final class CykClasses extends JavaPlugin {

    private static CykClasses instance;

    private Messages messages;
    private SkillService skills;
    private ClassRegistry classes;
    private ProfileManager profiles;
    private StatsService stats;
    private LevelService levels;
    private ManaService mana;
    private CombatService combat;
    private FlawService flaws;
    private ClassService classService;
    private SkillBar skillBar;
    private PartyManager parties;
    private HudService hud;
    private MythicHook mythic;
    private ContentRegistry content;
    private CharacterService character;
    private ItemFactory itemFactory;
    private ItemRestrictionService items;
    private ChatListener chat;
    private GearService gear;
    private GearListener gearListener;
    private JobService jobs;
    private JobDescriptions jobDescriptions;

    public static CykClasses instance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        migrateConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        messages = new Messages(this);
        messages.load();
        skills = new SkillService(this);
        content = new ContentRegistry(this);
        content.load();
        classes = new ClassRegistry(this);
        classes.load();
        profiles = new ProfileManager(this);
        try {
            profiles.init();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Impossible d'initialiser le stockage, désactivation du plugin.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        stats = new StatsService(this);
        levels = new LevelService(this);
        mana = new ManaService(this);
        combat = new CombatService(this);
        flaws = new FlawService(this);
        classService = new ClassService(this);
        skillBar = new SkillBar(this);
        parties = new PartyManager(this);
        hud = new HudService(this);
        hud.load();
        character = new CharacterService(this);
        character.loadUniques();
        itemFactory = new ItemFactory(this);
        items = new ItemRestrictionService(this);
        chat = new ChatListener(this);
        gear = new GearService(this);
        gear.load();
        gearListener = new GearListener(this);
        jobs = new JobService(this);
        jobDescriptions = new JobDescriptions(this);

        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new ConnectionListener(this), this);
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(new ProgressListener(this), this);
        pm.registerEvents(new CastingListener(this), this);
        pm.registerEvents(new GuiListener(), this);
        pm.registerEvents(new HudListener(this), this);
        pm.registerEvents(new JobListener(this), this);
        pm.registerEvents(items, this);
        pm.registerEvents(chat, this);
        pm.registerEvents(gearListener, this);

        registerCommand("classe", new ClasseCommand(this));
        registerCommand("competence", new CompetenceCommand(this));
        GroupeCommand groupe = new GroupeCommand(this);
        registerCommand("groupe", groupe);
        registerCommand("gc", groupe);
        registerCommand("global", groupe);
        CharacterCommand charCmd = new CharacterCommand(this);
        for (String c : new String[]{"race", "traits", "metier", "arbre", "stats"}) {
            registerCommand(c, charCmd);
        }

        mana.start();
        flaws.start();
        hud.start();

        if (pm.isPluginEnabled("MythicMobs")) {
            try {
                mythic = new MythicHook();
                getLogger().info("MythicMobs détecté : mobs custom, XP par mob et compétences de type MYTHIC activés.");
            } catch (Throwable t) {
                mythic = null;
                getLogger().log(Level.WARNING, "Intégration MythicMobs impossible (version incompatible ?)", t);
            }
        }

        // Les plugins d'objets (CraftEngine, ExecutableItems...) peuvent se charger après nous.
        Bukkit.getScheduler().runTask(this, () -> itemFactory.init());

        if (pm.isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
            getLogger().info("PlaceholderAPI détecté : placeholders %cykclasses_*% activés.");
        }

        // Rechargement à chaud : charge les joueurs déjà connectés.
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile p = profiles.loadBlocking(player.getUniqueId());
            if (p != null) {
                p.setName(player.getName());
                profiles.put(p);
                skillBar.recover(player);
                stats.apply(player);
            }
        }

        getLogger().info(classes.baseClasses().size() + " classes, " + (classes.all().size() - classes.baseClasses().size())
                + " sous-classes et " + classes.skillCount() + " compétences chargées.");
    }

    private void registerCommand(String name, TabExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }
    }

    @Override
    public void onDisable() {
        if (skillBar != null) {
            skillBar.exitAll();
        }
        if (hud != null) {
            hud.shutdown();
        }
        if (parties != null) {
            parties.disbandAll();
        }
        if (mana != null) {
            mana.stop();
        }
        if (flaws != null) {
            flaws.stop();
        }
        if (stats != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                stats.clear(player);
            }
        }
        if (profiles != null) {
            profiles.shutdown();
        }
        instance = null;
    }

    public void reload() {
        reloadConfig();
        skillBar.exitAll();
        messages.load();
        hud.load();
        content.load();
        gear.load();
        classes.load();
        stats.invalidateAll();
        hud.start();
        mana.start();
        flaws.start();
        for (Player player : Bukkit.getOnlinePlayers()) {
            stats.apply(player);
        }
    }

    public Messages messages() { return messages; }
    public SkillService skills() { return skills; }
    public ClassRegistry classes() { return classes; }
    public ProfileManager profiles() { return profiles; }
    public StatsService stats() { return stats; }
    public LevelService levels() { return levels; }
    public ManaService mana() { return mana; }
    public CombatService combat() { return combat; }
    public FlawService flaws() { return flaws; }
    public ClassService classService() { return classService; }
    public SkillBar skillBar() { return skillBar; }
    public PartyManager parties() { return parties; }
    public HudService hud() { return hud; }
    /** @return le pont MythicMobs, ou null si MythicMobs n'est pas installé. */
    public MythicHook mythic() { return mythic; }
    public ContentRegistry content() { return content; }
    public CharacterService character() { return character; }
    public ItemFactory itemFactory() { return itemFactory; }
    public ItemRestrictionService items() { return items; }
    public ChatListener chat() { return chat; }
    public GearService gear() { return gear; }
    public GearListener gearListener() { return gearListener; }
    public JobService jobs() { return jobs; }
    public JobDescriptions jobDescriptions() { return jobDescriptions; }

    /** Met à jour les anciennes valeurs par défaut copiées dans config.yml par les versions précédentes. */
    private void migrateConfig() {
        var c = getConfig();
        String oldTag = "<dark_gray>[</dark_gray><class> <gray><level></gray><dark_gray>]</dark_gray>";
        if (oldTag.equals(c.getString("chat.class-tag"))) {
            c.set("chat.class-tag", "<dark_gray>[</dark_gray><class> <gray>Niv. <level></gray><dark_gray>]</dark_gray>");
            saveConfig();
        }
    }
}
