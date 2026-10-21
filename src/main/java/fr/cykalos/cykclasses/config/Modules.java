package fr.cykalos.cykclasses.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Modules activables dans config.yml → modules. Un module désactivé disparaît du jeu :
 * étape de création sautée, commande indisponible, aucun effet sur les stats.
 */
public final class Modules {

    private boolean races = true;
    private boolean traits = true;
    private boolean weaknesses = true;
    private boolean jobs = true;
    private boolean skillTree = true;
    private boolean attributes = true;
    private boolean parties = true;
    private boolean forge = true;
    private boolean gems = true;
    private boolean kits = true;
    private boolean itemRestrictions = true;
    private boolean quests = true;

    public void load(FileConfiguration c) {
        races = c.getBoolean("modules.races", true);
        traits = c.getBoolean("modules.traits", true);
        weaknesses = c.getBoolean("modules.weaknesses", true);
        jobs = c.getBoolean("modules.jobs", true);
        skillTree = c.getBoolean("modules.skill-tree", true);
        attributes = c.getBoolean("modules.attributes", true);
        parties = c.getBoolean("modules.parties", true);
        forge = c.getBoolean("modules.forge", true);
        gems = c.getBoolean("modules.gems", true);
        kits = c.getBoolean("modules.kits", true);
        itemRestrictions = c.getBoolean("modules.item-restrictions", true);
        quests = c.getBoolean("modules.quests", true);
    }

    public boolean races() { return races; }
    public boolean traits() { return traits; }
    public boolean weaknesses() { return weaknesses; }
    public boolean jobs() { return jobs; }
    public boolean skillTree() { return skillTree; }
    public boolean attributes() { return attributes; }
    public boolean parties() { return parties; }
    public boolean forge() { return forge; }
    public boolean gems() { return gems; }
    public boolean kits() { return kits; }
    public boolean itemRestrictions() { return itemRestrictions; }
    public boolean quests() { return quests; }

    /** Module associé à une commande (null = toujours disponible). */
    public boolean commandEnabled(String command) {
        return switch (command) {
            case "race" -> races;
            case "traits" -> traits;
            case "faiblesses" -> weaknesses;
            case "metier" -> jobs;
            case "arbre" -> skillTree;
            case "stats" -> attributes;
            case "groupe", "gc" -> parties;
            case "quetes" -> quests;
            default -> true;
        };
    }
}
