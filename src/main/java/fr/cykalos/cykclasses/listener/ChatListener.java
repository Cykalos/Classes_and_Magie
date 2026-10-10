package fr.cykalos.cykclasses.listener;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.party.Party;
import fr.cykalos.cykclasses.util.Text;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat : mode « groupe » (/gc) ou global (/global), avec la classe et le niveau affichés.
 */
public final class ChatListener implements Listener {

    private final CykClasses plugin;
    /** Joueurs dont le prochain message doit partir dans le chat global (commande /global <message>). */
    private final Set<UUID> forceGlobal = ConcurrentHashMap.newKeySet();

    public ChatListener(CykClasses plugin) {
        this.plugin = plugin;
    }

    public void sendGlobalOnce(Player player, String message) {
        forceGlobal.add(player.getUniqueId());
        player.chat(message);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        boolean force = forceGlobal.remove(player.getUniqueId());
        PlayerProfile profile = plugin.profiles().getIfLoaded(player.getUniqueId());
        Party party = plugin.parties().get(player.getUniqueId());
        if (!force && profile != null && profile.isPartyChat()) {
            if (party == null) {
                profile.setPartyChat(false);
            } else {
                Set<UUID> members = party.members();
                event.viewers().removeIf(a -> a instanceof Player v && !members.contains(v.getUniqueId())
                        && !v.hasPermission("cykclasses.chat.spy"));
                Component prefixSpy = Text.mm(plugin.getConfig().getString("chat.spy-prefix", "<dark_gray>[Espion] "));
                event.renderer((source, displayName, message, viewer) -> {
                    Component line = format("chat.party-format", source, displayName, message);
                    if (viewer instanceof Player v && !members.contains(v.getUniqueId())) {
                        return prefixSpy.append(line);
                    }
                    return line;
                });
                return;
            }
        }
        if (plugin.getConfig().getBoolean("chat.format-enabled", true)) {
            event.renderer((source, displayName, message, viewer) -> format("chat.global-format", source, displayName, message));
        }
    }

    /** Ligne de chat formatée selon config.yml (chat.global-format / chat.party-format). */
    public Component format(String path, Player source, Component displayName, Component message) {
        String fmt = plugin.getConfig().getString(path, "<name>: <message>");
        return Text.mm(fmt, Text.phc("class_tag", classTag(source)), Text.phc("name", displayName), Text.phc("message", message));
    }

    public Component classTag(Player player) {
        PlayerProfile p = plugin.profiles().getIfLoaded(player.getUniqueId());
        RpgClass rc = p == null ? null : plugin.classes().effective(p);
        if (rc == null) {
            return Text.mm(plugin.getConfig().getString("chat.no-class-tag", ""));
        }
        var race = plugin.content().race(p.getRaceId());
        return Text.mm(plugin.getConfig().getString("chat.class-tag", "[<class> <level>]"),
                Text.ph("class", rc.name), Text.ph("level", p.getLevel()),
                Text.ph("race", race == null ? "" : race.name()));
    }

    /** Message de groupe ponctuel (/gc message). */
    public void sendParty(Player sender, Party party, String message) {
        Component line = format("chat.party-format", sender, sender.displayName(), Component.text(message));
        for (Player p : party.onlineMembers()) {
            p.sendMessage(line);
        }
        Component spy = Text.mm(plugin.getConfig().getString("chat.spy-prefix", "<dark_gray>[Espion] ")).append(line);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!party.contains(p.getUniqueId()) && p.hasPermission("cykclasses.chat.spy")) {
                p.sendMessage(spy);
            }
        }
        Audience console = Bukkit.getConsoleSender();
        console.sendMessage(line);
    }
}
