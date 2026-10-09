package fr.cykalos.cykclasses;

import fr.cykalos.cykclasses.command.ClasseCommand;
import fr.cykalos.cykclasses.command.CompetenceCommand;
import fr.cykalos.cykclasses.config.ClassRegistry;
import fr.cykalos.cykclasses.config.Messages;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.data.ProfileManager;
import fr.cykalos.cykclasses.hook.PlaceholderHook;
import fr.cykalos.cykclasses.listener.CastingListener;
import fr.cykalos.cykclasses.listener.CombatListener;
import fr.cykalos.cykclasses.listener.ConnectionListener;
import fr.cykalos.cykclasses.listener.GuiListener;
import fr.cykalos.cykclasses.listener.ProgressListener;
import fr.cykalos.cykclasses.service.ClassService;
import fr.cykalos.cykclasses.service.CombatService;
import fr.cykalos.cykclasses.service.FlawService;
import fr.cykalos.cykclasses.service.LevelService;
import fr.cykalos.cykclasses.service.ManaService;
import fr.cykalos.cykclasses.service.StatsService;
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

    public static CykClasses instance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        messages = new Messages(this);
        messages.load();
        skills = new SkillService(this);
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

        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new ConnectionListener(this), this);
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(new ProgressListener(this), this);
        pm.registerEvents(new CastingListener(this), this);
        pm.registerEvents(new GuiListener(), this);

        registerCommand("classe", new ClasseCommand(this));
        registerCommand("competence", new CompetenceCommand(this));

        mana.start();
        flaws.start();

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
        messages.load();
        classes.load();
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
}
