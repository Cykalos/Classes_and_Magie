package fr.cykalos.cykclasses.command;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.gui.QuestJournalGui;
import fr.cykalos.cykclasses.quest.Quest;
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
 * /classe quetes (ou /quetes)              journal
 * /classe quetes commencer|abandonner|suivre <id>
 * /classe quetes accepter|refuser <id>     répondre à une quête proposée dans le chat
 * /classe quetes panneau                   afficher / masquer le panneau de suivi
 * /classe quetes tutoriel [oui|non]        lancer ou refuser le tutoriel
 * /classe admin quete <joueur> <start|complete|reset|resetall> [id]
 */
public final class QuetesCommand implements TabExecutor {

    private final CykClasses plugin;

    public QuetesCommand(CykClasses plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "player-only");
            return true;
        }
        if (args.length == 0) {
            new QuestJournalGui(plugin, player).open();
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "panneau", "tracker" -> {
                PlayerProfile p = plugin.profiles().get(player);
                p.setQuestTrackerHidden(!p.isQuestTrackerHidden());
                plugin.messages().send(player, p.isQuestTrackerHidden() ? "quests.tracker-hidden" : "quests.tracker-shown");
            }
            case "tutoriel", "tutorial" -> {
                Quest tuto = plugin.quests().tutorial();
                if (tuto == null) {
                    plugin.messages().send(player, "quests.unknown");
                } else if (args.length > 1 && (args[1].equalsIgnoreCase("non") || args[1].equalsIgnoreCase("no"))) {
                    plugin.messages().send(player, "quests.tutorial-declined");
                } else {
                    plugin.quests().startQuest(player, tuto, false);
                }
            }
            case "refuser", "decline" -> {
                Quest q = args.length > 1 ? plugin.quests().get(args[1]) : null;
                if (q != null) {
                    plugin.messages().send(player, "quests.offer-declined", Text.ph("quest", q.name));
                }
            }
            case "commencer", "start", "accepter", "accept", "abandonner", "abandon", "suivre", "track" -> {
                Quest q = args.length > 1 ? plugin.quests().get(args[1]) : null;
                PlayerProfile p = plugin.profiles().get(player);
                if (q == null || !plugin.quests().visible(player, p, q)) {
                    plugin.messages().send(player, "quests.unknown");
                    return true;
                }
                switch (sub) {
                    case "commencer", "start", "accepter", "accept" -> plugin.quests().startQuest(player, q, false);
                    case "abandonner", "abandon" -> plugin.quests().abandon(player, q);
                    default -> plugin.quests().toggleTrack(player, q);
                }
            }
            default -> new QuestJournalGui(plugin, player).open();
        }
        return true;
    }

    /** Administration : args = [quete, joueur, action, id] (depuis /classe admin quete …). */
    public boolean admin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cykclasses.admin")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Text.mm("<gray>/classe admin quete <joueur> <start|complete|reset|resetall> [id]"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.messages().send(sender, "player-not-found", Text.ph("player", args[1]));
            return true;
        }
        PlayerProfile p = plugin.profiles().get(target);
        String action = args[2].toLowerCase(Locale.ROOT);
        if (action.equals("resetall")) {
            for (Quest q : plugin.quests().all()) {
                p.resetQuest(q.id);
            }
            p.setTutorialOffered(false);
            sender.sendMessage(Text.mm("<green>Quêtes de " + target.getName() + " remises à zéro."));
            return true;
        }
        Quest q = args.length > 3 ? plugin.quests().get(args[3]) : null;
        if (q == null) {
            plugin.messages().send(sender, "quests.unknown");
            return true;
        }
        switch (action) {
            case "start" -> plugin.quests().startQuest(target, q, true);
            case "complete" -> plugin.quests().forceComplete(target, q);
            case "reset" -> p.resetQuest(q.id);
            default -> {
                sender.sendMessage(Text.mm("<red>Action inconnue : start, complete, reset, resetall"));
                return true;
            }
        }
        sender.sendMessage(Text.mm("<green>OK : " + action + " " + q.id + " pour " + target.getName()));
        return true;
    }

    /** Complétion de /classe admin quete … (args = [quete, joueur, action, id]). */
    public List<String> adminTab(String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 2) {
            Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
        } else if (args.length == 3) {
            out.addAll(List.of("start", "complete", "reset", "resetall"));
        } else if (args.length == 4) {
            plugin.quests().all().forEach(q -> out.add(q.id));
        }
        String last = args[args.length - 1].toLowerCase(Locale.ROOT);
        out.removeIf(x -> !x.toLowerCase(Locale.ROOT).startsWith(last));
        return out;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.addAll(List.of("commencer", "abandonner", "suivre", "panneau", "tutoriel"));
        } else if (args.length == 2 && sender instanceof Player player) {
            PlayerProfile p = plugin.profiles().get(player);
            for (Quest q : plugin.quests().all()) {
                if (plugin.quests().visible(player, p, q)) {
                    out.add(q.id);
                }
            }
        }
        String last = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        out.removeIf(x -> !x.toLowerCase(Locale.ROOT).startsWith(last));
        return out;
    }
}
