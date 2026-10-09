package fr.cykalos.cykclasses.command;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.gui.SkillsGui;
import fr.cykalos.cykclasses.model.SkillDefinition;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * /competence <id|numéro> — utile pour les macros et les liaisons de touches.
 */
public final class CompetenceCommand implements TabExecutor {

    private final CykClasses plugin;

    public CompetenceCommand(CykClasses plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "player-only");
            return true;
        }
        if (!plugin.profiles().get(player).hasClass()) {
            plugin.messages().send(player, "no-class");
            return true;
        }
        if (args.length == 0) {
            new SkillsGui(plugin, player).open();
            return true;
        }
        try {
            int slot = Integer.parseInt(args[0]);
            plugin.skills().castSlot(player, slot - 1);
        } catch (NumberFormatException e) {
            plugin.skills().cast(player, args[0]);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (args.length != 1 || !(sender instanceof Player player)) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        for (SkillDefinition s : plugin.skills().skillsOf(plugin.profiles().get(player))) {
            ids.add(s.id);
        }
        String last = args[0].toLowerCase(Locale.ROOT);
        return ids.stream().filter(i -> i.startsWith(last)).collect(Collectors.toList());
    }
}
