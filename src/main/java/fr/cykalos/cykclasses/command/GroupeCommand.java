package fr.cykalos.cykclasses.command;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.gui.PartyCreateGui;
import fr.cykalos.cykclasses.party.Party;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * /groupe — création (avec règles), invitations, gestion et chat de groupe.
 * /gc <message> — raccourci du chat de groupe.
 */
public final class GroupeCommand implements TabExecutor {

    private final CykClasses plugin;

    public GroupeCommand(CykClasses plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "player-only");
            return true;
        }
        if (command.getName().equalsIgnoreCase("gc")) {
            if (args.length > 0) {
                plugin.parties().chat(player, String.join(" ", args));
            } else {
                plugin.parties().togglePartyChat(player);
            }
            return true;
        }
        if (command.getName().equalsIgnoreCase("global")) {
            if (args.length > 0) {
                plugin.chat().sendGlobalOnce(player, String.join(" ", args));
            } else {
                plugin.profiles().get(player).setPartyChat(false);
                plugin.messages().send(player, "party.chat-off");
            }
            return true;
        }
        if (args.length == 0) {
            if (plugin.parties().get(player) == null) {
                new PartyCreateGui(plugin, player).open();
            } else {
                info(player);
            }
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "creer", "create" -> {
                if (args.length == 1) {
                    new PartyCreateGui(plugin, player).open();
                    return true;
                }
                Party.XpMode mode = switch (args[1].toLowerCase(Locale.ROOT)) {
                    case "egal", "equal", "egale" -> Party.XpMode.EQUAL;
                    default -> Party.XpMode.SHARED;
                };
                boolean ff = args.length < 3 ? plugin.getConfig().getBoolean("party.default-friendly-fire", true)
                        : List.of("oui", "on", "true", "yes").contains(args[2].toLowerCase(Locale.ROOT));
                plugin.parties().create(player, mode, ff);
            }
            case "inviter", "invite" -> {
                if (args.length < 2) {
                    plugin.messages().sendList(player, "party.help");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    plugin.messages().send(player, "player-not-found", Text.ph("player", args[1]));
                    return true;
                }
                plugin.parties().invite(player, target);
            }
            case "accepter", "accept" -> plugin.parties().accept(player, args.length > 1 ? args[1] : null);
            case "refuser", "deny" -> plugin.parties().deny(player, args.length > 1 ? args[1] : null);
            case "quitter", "leave" -> plugin.parties().leave(player);
            case "expulser", "kick" -> {
                if (args.length < 2) {
                    plugin.messages().sendList(player, "party.help");
                    return true;
                }
                plugin.parties().kick(player, args[1]);
            }
            case "chef", "lead", "promote" -> {
                if (args.length < 2) {
                    plugin.messages().sendList(player, "party.help");
                    return true;
                }
                plugin.parties().promote(player, args[1]);
            }
            case "dissoudre", "disband" -> plugin.parties().disband(player);
            case "info", "liste", "list" -> info(player);
            case "chat" -> {
                if (args.length > 1) {
                    plugin.parties().chat(player, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                }
            }
            default -> plugin.messages().sendList(player, "party.help");
        }
        return true;
    }

    private void info(Player player) {
        Party party = plugin.parties().get(player);
        if (party == null) {
            plugin.messages().send(player, "party.not-in");
            return;
        }
        player.sendMessage(plugin.messages().get("party.info-header", Text.ph("size", party.size()), Text.ph("max", plugin.parties().maxSize())));
        player.sendMessage(plugin.messages().get("party.info-rules",
                Text.ph("xp_mode", plugin.parties().modeName(party.xpMode())),
                Text.ph("friendly_fire", plugin.parties().ffName(party.friendlyFire()))));
        for (UUID uuid : party.members()) {
            Player p = Bukkit.getPlayer(uuid);
            String name = p != null ? p.getName() : Bukkit.getOfflinePlayer(uuid).getName();
            String hp = "";
            if (p != null) {
                var max = p.getAttribute(Attribute.MAX_HEALTH);
                hp = " <red>" + (int) Math.ceil(p.getHealth()) + "/" + (max == null ? 20 : (int) max.getValue()) + "❤";
                var rc = plugin.classes().effective(plugin.profiles().get(p));
                if (rc != null) {
                    hp += " <gray>· </gray>" + rc.name + " <gray>niv. " + plugin.profiles().get(p).getLevel();
                }
            }
            player.sendMessage(Text.mm((party.isLeader(uuid) ? "<gold>★ " : "<gray>• ") + "<white>" + name + hp));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("gc") || command.getName().equalsIgnoreCase("global")) {
            return List.of();
        }
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(List.of("creer", "inviter", "accepter", "refuser", "quitter", "expulser", "chef", "dissoudre", "info", "chat"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "creer", "create" -> options.addAll(List.of("partage", "egal"));
                case "inviter", "invite", "accepter", "refuser", "expulser", "chef" ->
                        Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
                default -> {
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("creer")) {
            options.addAll(List.of("oui", "non"));
        }
        String last = args[args.length - 1].toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(last)).collect(Collectors.toList());
    }
}
