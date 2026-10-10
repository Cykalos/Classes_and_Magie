package fr.cykalos.cykclasses.command;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.Race;
import fr.cykalos.cykclasses.content.Trait;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.gui.AttributesGui;
import fr.cykalos.cykclasses.gui.JobGui;
import fr.cykalos.cykclasses.gui.RaceGui;
import fr.cykalos.cykclasses.gui.SkillTreeGui;
import fr.cykalos.cykclasses.gui.TraitsGui;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * /race, /traits, /metier, /arbre, /stats.
 */
public final class CharacterCommand implements TabExecutor {

    private final CykClasses plugin;

    public CharacterCommand(CykClasses plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "player-only");
            return true;
        }
        PlayerProfile p = plugin.profiles().get(player);
        if (!p.hasClass()) {
            plugin.messages().send(player, "no-class");
            return true;
        }
        String sub = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";
        switch (command.getName().toLowerCase(Locale.ROOT)) {
            case "race" -> {
                Race race = plugin.content().race(p.getRaceId());
                if (race == null || plugin.getConfig().getBoolean("character.race-can-change", false)) {
                    new RaceGui(plugin, player).open();
                } else {
                    plugin.messages().send(player, "race.current", Text.ph("race", race.name()));
                }
            }
            case "traits" -> {
                if (sub.equals("relancer") || sub.equals("reroll")) {
                    plugin.character().rerollTraits(player);
                    listTraits(player, p);
                    return true;
                }
                if (!p.isTraitsRolled()) {
                    plugin.character().rollTraits(player, p, false);
                }
                new TraitsGui(plugin, player).open();
            }
            case "metier" -> new JobGui(plugin, player).open();
            case "arbre" -> {
                if (sub.equals("reset")) {
                    plugin.character().resetTree(player, false);
                    return true;
                }
                new SkillTreeGui(plugin, player, 0).open();
            }
            case "stats" -> {
                if (sub.equals("reset")) {
                    plugin.character().resetAttributes(player, false);
                    return true;
                }
                new AttributesGui(plugin, player).open();
            }
            default -> {
            }
        }
        return true;
    }

    private void listTraits(Player player, PlayerProfile p) {
        for (String id : p.getTraits()) {
            Trait t = plugin.content().trait(id);
            if (t != null) {
                player.sendMessage(Text.mm("<gray>• </gray>" + t.name() + " <dark_gray>(" + Text.plain(t.rarity().label) + ")"));
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        return switch (command.getName().toLowerCase(Locale.ROOT)) {
            case "traits" -> List.of("relancer");
            case "arbre", "stats" -> List.of("reset");
            default -> List.of();
        };
    }
}
