package fr.cykalos.cykclasses.command;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.gui.TitlesGui;
import fr.cykalos.cykclasses.title.PlayerTitle;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /classe titres (ou /titres)        collection de titres
 * /classe titres equiper <id> · /classe titres retirer
 * /classe admin titre <joueur> <give|take|reset> [id] · /classe admin titre liberer <id>
 */
public final class TitresCommand implements TabExecutor {

    private final CykClasses plugin;

    public TitresCommand(CykClasses plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "player-only");
            return true;
        }
        if (args.length == 0) {
            new TitlesGui(plugin, player).open();
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "equiper", "equip" -> {
                PlayerTitle t = args.length > 1 ? plugin.titles().get(args[1]) : null;
                if (t == null || !plugin.profiles().get(player).hasTitle(t.id())) {
                    plugin.messages().send(player, "titles.not-owned");
                } else {
                    plugin.titles().equip(player, t.id(), true);
                }
            }
            case "retirer", "unequip" -> plugin.titles().equip(player, null, true);
            default -> new TitlesGui(plugin, player).open();
        }
        return true;
    }

    /** Administration : args = [titre, joueur|liberer, action|id, id] (depuis /classe admin titre …). */
    public boolean admin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cykclasses.admin")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }
        if (args.length >= 3 && args[1].equalsIgnoreCase("liberer")) {
            boolean ok = plugin.titles().release(args[2]);
            sender.sendMessage(Text.mm(ok ? "<green>Titre unique libéré : " + args[2] : "<red>Titre unique introuvable : " + args[2]));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Text.mm("<gray>/classe admin titre <joueur> <give|take|reset> [id] · /classe admin titre liberer <id>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.messages().send(sender, "player-not-found", Text.ph("player", args[1]));
            return true;
        }
        PlayerProfile p = plugin.profiles().get(target);
        String action = args[2].toLowerCase(Locale.ROOT);
        if (action.equals("reset")) {
            for (String id : new ArrayList<>(p.titles().keySet())) {
                plugin.titles().revoke(target, id);
            }
            sender.sendMessage(Text.mm("<green>Titres de " + target.getName() + " remis à zéro."));
            return true;
        }
        PlayerTitle t = args.length > 3 ? plugin.titles().get(args[3]) : null;
        if (t == null) {
            sender.sendMessage(Text.mm("<red>Titre introuvable."));
            return true;
        }
        switch (action) {
            case "give" -> {
                if (!plugin.titles().unlock(target, t.id(), true)) {
                    sender.sendMessage(Text.mm("<red>Impossible : titre déjà possédé, ou titre unique appartenant à un autre joueur."));
                    return true;
                }
            }
            case "take" -> plugin.titles().revoke(target, t.id());
            default -> {
                sender.sendMessage(Text.mm("<red>Action inconnue : give, take, reset"));
                return true;
            }
        }
        sender.sendMessage(Text.mm("<green>OK : " + action + " " + t.id() + " pour " + target.getName()));
        return true;
    }

    /** Complétion de /classe admin titre … (args = [titre, joueur|liberer, action|id, id]). */
    public List<String> adminTab(String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 2) {
            out.add("liberer");
            Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
        } else if (args.length == 3 && args[1].equalsIgnoreCase("liberer")) {
            plugin.titles().all().stream().filter(PlayerTitle::unique).forEach(t -> out.add(t.id()));
        } else if (args.length == 3) {
            out.addAll(List.of("give", "take", "reset"));
        } else if (args.length == 4) {
            plugin.titles().all().forEach(t -> out.add(t.id()));
        }
        String last = args[args.length - 1].toLowerCase(Locale.ROOT);
        out.removeIf(x -> !x.toLowerCase(Locale.ROOT).startsWith(last));
        return out;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.addAll(List.of("equiper", "retirer"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("equiper") && sender instanceof Player player) {
            out.addAll(plugin.profiles().get(player).titles().keySet());
        }
        String last = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        out.removeIf(x -> !x.toLowerCase(Locale.ROOT).startsWith(last));
        return out;
    }
}
