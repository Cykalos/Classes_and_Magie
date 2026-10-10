package fr.cykalos.cykclasses.command;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.gui.ClassSelectGui;
import fr.cykalos.cykclasses.gui.SkillsGui;
import fr.cykalos.cykclasses.gui.SubclassGui;
import fr.cykalos.cykclasses.model.ClassStats;
import fr.cykalos.cykclasses.model.RpgClass;
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
import java.util.stream.Collectors;

public final class ClasseCommand implements TabExecutor {

    private final CykClasses plugin;

    public ClasseCommand(CykClasses plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                plugin.messages().sendList(sender, "help");
                return true;
            }
            new ClassSelectGui(plugin, player).open();
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help", "aide" -> {
                plugin.messages().sendList(sender, "help");
                if (sender.hasPermission("cykclasses.admin")) {
                    plugin.messages().sendList(sender, "admin-help");
                }
            }
            case "info" -> info(sender, args);
            case "choisir", "choose" -> {
                Player player = requirePlayer(sender);
                if (player == null) return true;
                if (args.length < 2) {
                    new ClassSelectGui(plugin, player).open();
                    return true;
                }
                RpgClass rc = plugin.classes().get(args[1]);
                if (rc == null || rc.isSubclass()) {
                    plugin.messages().send(sender, "unknown-class", Text.ph("class", args[1]));
                    return true;
                }
                plugin.classService().choose(player, rc);
            }
            case "sous-classe", "subclass", "evolution" -> {
                Player player = requirePlayer(sender);
                if (player == null) return true;
                if (!plugin.profiles().get(player).hasClass()) {
                    plugin.messages().send(player, "no-class");
                    return true;
                }
                if (args.length >= 2) {
                    RpgClass rc = plugin.classes().get(args[1]);
                    if (rc == null || !rc.isSubclass()) {
                        plugin.messages().send(sender, "unknown-class", Text.ph("class", args[1]));
                        return true;
                    }
                    plugin.classService().chooseSubclass(player, rc);
                    return true;
                }
                new SubclassGui(plugin, player).open();
            }
            case "competences", "skills" -> {
                Player player = requirePlayer(sender);
                if (player == null) return true;
                if (!plugin.profiles().get(player).hasClass()) {
                    plugin.messages().send(player, "no-class");
                    return true;
                }
                new SkillsGui(plugin, player).open();
            }
            case "reset" -> {
                Player player = requirePlayer(sender);
                if (player == null) return true;
                if (!player.hasPermission("cykclasses.reset")) {
                    plugin.messages().send(player, "no-permission");
                    return true;
                }
                plugin.classService().reset(player, false);
            }
            case "admin" -> admin(sender, args);
            default -> plugin.messages().sendList(sender, "help");
        }
        return true;
    }

    private Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player p) {
            return p;
        }
        plugin.messages().send(sender, "player-only");
        return null;
    }

    private void info(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                plugin.messages().send(sender, "player-not-found", Text.ph("player", args[1]));
                return;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            plugin.messages().send(sender, "player-only");
            return;
        }
        PlayerProfile profile = plugin.profiles().get(target);
        RpgClass base = plugin.classes().get(profile.getClassId());
        RpgClass sub = plugin.classes().get(profile.getSubclassId());
        ClassStats s = plugin.stats().stats(profile);
        sender.sendMessage(plugin.messages().get("info-header", Text.ph("player", target.getName())));
        sender.sendMessage(plugin.messages().get("info-class", Text.ph("class", base == null ? "<gray>Aucune" : base.name)));
        sender.sendMessage(plugin.messages().get("info-subclass", Text.ph("subclass", sub == null ? "<gray>Aucune" : sub.name)));
        sender.sendMessage(plugin.messages().get("info-level",
                Text.ph("level", profile.getLevel()),
                Text.ph("xp", Text.format(Math.floor(profile.getXp()))),
                Text.ph("xp_required", Text.format(plugin.levels().xpRequired(profile.getLevel())))));
        sender.sendMessage(plugin.messages().get("info-mana",
                Text.ph("mana", (int) profile.getMana()),
                Text.ph("max_mana", (int) plugin.stats().maxMana(profile)),
                Text.ph("regen", Text.format(s.regen(profile.getLevel())))));
        sender.sendMessage(plugin.messages().get("info-stats",
                Text.ph("melee", Text.percent(s.meleeDamage)),
                Text.ph("ranged", Text.percent(s.rangedDamage)),
                Text.ph("magic", Text.percent(s.magicDamage)),
                Text.ph("taken", Text.percent(s.damageTaken))));
    }

    private void admin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cykclasses.admin")) {
            plugin.messages().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            plugin.messages().sendList(sender, "admin-help");
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        if (action.equals("reload")) {
            plugin.reload();
            plugin.messages().send(sender, "reloaded",
                    Text.ph("classes", plugin.classes().all().size()),
                    Text.ph("skills", plugin.classes().skillCount()));
            return;
        }
        if (args.length < 3) {
            plugin.messages().sendList(sender, "admin-help");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            plugin.messages().send(sender, "player-not-found", Text.ph("player", args[2]));
            return;
        }
        PlayerProfile profile = plugin.profiles().get(target);
        switch (action) {
            case "setlevel" -> {
                int level = parseInt(args, 3, 1);
                plugin.levels().setLevel(target, level);
                plugin.messages().send(sender, "admin-set-level", Text.ph("player", target.getName()), Text.ph("level", profile.getLevel()));
            }
            case "addxp" -> {
                double xp = parseInt(args, 3, 0);
                plugin.levels().addXp(target, xp);
                plugin.messages().send(sender, "admin-add-xp", Text.ph("player", target.getName()), Text.ph("xp", Text.format(xp)));
            }
            case "setclass" -> {
                if (args.length < 4) {
                    plugin.messages().sendList(sender, "admin-help");
                    return;
                }
                RpgClass rc = plugin.classes().get(args[3]);
                if (rc == null) {
                    plugin.messages().send(sender, "unknown-class", Text.ph("class", args[3]));
                    return;
                }
                plugin.classService().apply(target, profile, rc, true);
                plugin.messages().send(sender, "admin-set-class", Text.ph("player", target.getName()), Text.ph("class", rc.name));
            }
            case "reset" -> {
                plugin.classService().reset(target, true);
                profile.setLastReset(0);
                plugin.messages().send(sender, "admin-reset", Text.ph("player", target.getName()));
            }
            case "mana" -> {
                profile.setMana(plugin.stats().maxMana(profile));
                profile.clearCooldowns();
                plugin.messages().send(sender, "admin-mana", Text.ph("player", target.getName()));
            }
            default -> plugin.messages().sendList(sender, "admin-help");
        }
    }

    private int parseInt(String[] args, int index, int def) {
        if (args.length <= index) {
            return def;
        }
        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(List.of("aide", "info", "choisir", "sous-classe", "competences", "reset"));
            if (sender.hasPermission("cykclasses.admin")) {
                options.add("admin");
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "choisir", "choose" -> plugin.classes().baseClasses().forEach(c -> options.add(c.id));
                case "sous-classe", "subclass", "evolution" -> {
                    if (sender instanceof Player p) {
                        RpgClass base = plugin.classes().get(plugin.profiles().get(p).getClassId());
                        if (base != null) {
                            options.addAll(base.subclasses().keySet());
                        }
                    }
                }
                case "info" -> Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
                case "admin" -> {
                    if (sender.hasPermission("cykclasses.admin")) {
                        options.addAll(List.of("setlevel", "addxp", "setclass", "reset", "mana", "reload"));
                    }
                }
                default -> {
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("admin")) {
            Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("setclass")) {
            plugin.classes().all().forEach(c -> options.add(c.id));
        }
        String last = args[args.length - 1].toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(last)).collect(Collectors.toList());
    }
}
