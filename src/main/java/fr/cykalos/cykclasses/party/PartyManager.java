package fr.cykalos.cykclasses.party;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gestion des groupes et des invitations (en mémoire : les groupes ne survivent pas à un redémarrage).
 */
public final class PartyManager {

    private record Invite(Party party, UUID inviter, long expires) {
    }

    private final CykClasses plugin;
    private final Map<UUID, Party> byPlayer = new java.util.concurrent.ConcurrentHashMap<>();
    /** invité -> (groupe -> invitation) */
    private final Map<UUID, Map<UUID, Invite>> invites = new HashMap<>();

    public PartyManager(CykClasses plugin) {
        this.plugin = plugin;
    }

    public Party get(Player player) {
        return byPlayer.get(player.getUniqueId());
    }

    public Party get(UUID uuid) {
        return byPlayer.get(uuid);
    }

    public boolean sameParty(Player a, Player b) {
        Party p = byPlayer.get(a.getUniqueId());
        return p != null && p.contains(b.getUniqueId());
    }

    /** Vrai si {@code a} ne doit pas pouvoir blesser {@code b} (même groupe sans dégâts alliés). */
    public boolean isProtectedAlly(Player a, Player b) {
        if (a.equals(b)) {
            return false;
        }
        Party p = byPlayer.get(a.getUniqueId());
        return p != null && p.contains(b.getUniqueId()) && !p.friendlyFire();
    }

    public int maxSize() {
        return Math.max(2, plugin.getConfig().getInt("party.max-size", 5));
    }

    // ------------------------------------------------------------ Actions

    public Party create(Player leader, Party.XpMode mode, boolean friendlyFire) {
        if (byPlayer.containsKey(leader.getUniqueId())) {
            plugin.messages().send(leader, "party.already-in");
            return null;
        }
        Party party = new Party(leader.getUniqueId(), mode, friendlyFire);
        byPlayer.put(leader.getUniqueId(), party);
        plugin.messages().send(leader, "party.created",
                Text.ph("xp_mode", modeName(mode)), Text.ph("friendly_fire", ffName(friendlyFire)));
        plugin.hud().refreshPartyBoard(party);
        return party;
    }

    public void invite(Player inviter, Player target) {
        Party party = get(inviter);
        if (party == null) {
            plugin.messages().send(inviter, "party.not-in");
            return;
        }
        if (!party.isLeader(inviter.getUniqueId()) && !plugin.getConfig().getBoolean("party.members-can-invite", false)) {
            plugin.messages().send(inviter, "party.not-leader");
            return;
        }
        if (target.equals(inviter)) {
            return;
        }
        if (byPlayer.containsKey(target.getUniqueId())) {
            plugin.messages().send(inviter, "party.target-in-party", Text.ph("player", target.getName()));
            return;
        }
        if (party.size() >= maxSize()) {
            plugin.messages().send(inviter, "party.full", Text.ph("max", maxSize()));
            return;
        }
        long timeout = plugin.getConfig().getLong("party.invite-timeout-seconds", 60) * 1000L;
        invites.computeIfAbsent(target.getUniqueId(), k -> new LinkedHashMap<>())
                .put(party.id(), new Invite(party, inviter.getUniqueId(), System.currentTimeMillis() + timeout));
        plugin.messages().send(inviter, "party.invite-sent", Text.ph("player", target.getName()));
        plugin.messages().send(target, "party.invite-received",
                Text.ph("player", inviter.getName()),
                Text.ph("xp_mode", modeName(party.xpMode())),
                Text.ph("friendly_fire", ffName(party.friendlyFire())));
        Component accept = plugin.messages().get("party.button-accept")
                .clickEvent(ClickEvent.runCommand("/groupe accepter " + inviter.getName()))
                .hoverEvent(HoverEvent.showText(Text.mm("<green>/groupe accepter " + inviter.getName())));
        Component deny = plugin.messages().get("party.button-deny")
                .clickEvent(ClickEvent.runCommand("/groupe refuser " + inviter.getName()))
                .hoverEvent(HoverEvent.showText(Text.mm("<red>/groupe refuser " + inviter.getName())));
        target.sendMessage(Component.text("   ").append(accept).append(Component.text("  ")).append(deny));
    }

    /** @param inviterName nom de l'inviteur, ou null pour la dernière invitation reçue. */
    public void accept(Player player, String inviterName) {
        Invite invite = findInvite(player, inviterName, true);
        if (invite == null) {
            plugin.messages().send(player, "party.no-invite");
            return;
        }
        if (byPlayer.containsKey(player.getUniqueId())) {
            plugin.messages().send(player, "party.already-in");
            return;
        }
        Party party = invite.party();
        if (!isAlive(party)) {
            plugin.messages().send(player, "party.no-invite");
            return;
        }
        if (party.size() >= maxSize()) {
            plugin.messages().send(player, "party.full", Text.ph("max", maxSize()));
            return;
        }
        invites.remove(player.getUniqueId());
        party.add(player.getUniqueId());
        byPlayer.put(player.getUniqueId(), party);
        broadcast(party, "party.joined", Text.ph("player", player.getName()));
        plugin.hud().refreshPartyBoard(party);
    }

    public void deny(Player player, String inviterName) {
        Invite invite = findInvite(player, inviterName, true);
        if (invite == null) {
            plugin.messages().send(player, "party.no-invite");
            return;
        }
        Player inviter = Bukkit.getPlayer(invite.inviter());
        if (inviter != null) {
            plugin.messages().send(inviter, "party.invite-denied", Text.ph("player", player.getName()));
        }
        plugin.messages().send(player, "party.invite-denied-self");
    }

    private Invite findInvite(Player player, String inviterName, boolean remove) {
        Map<UUID, Invite> map = invites.get(player.getUniqueId());
        if (map == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        map.values().removeIf(i -> i.expires() < now || !isAlive(i.party()));
        Invite found = null;
        UUID foundKey = null;
        for (Map.Entry<UUID, Invite> e : map.entrySet()) {
            Invite i = e.getValue();
            if (inviterName == null) {
                found = i;
                foundKey = e.getKey();
            } else {
                Player inviter = Bukkit.getPlayer(i.inviter());
                String name = inviter != null ? inviter.getName() : Bukkit.getOfflinePlayer(i.inviter()).getName();
                if (name != null && name.equalsIgnoreCase(inviterName)) {
                    found = i;
                    foundKey = e.getKey();
                }
            }
        }
        if (found != null && remove) {
            map.remove(foundKey);
        }
        return found;
    }

    private boolean isAlive(Party party) {
        return byPlayer.containsValue(party);
    }

    public void leave(Player player) {
        Party party = get(player);
        if (party == null) {
            plugin.messages().send(player, "party.not-in");
            return;
        }
        removeMember(party, player.getUniqueId(), "party.left", player.getName());
        plugin.messages().send(player, "party.left-self");
    }

    public void kick(Player leader, String targetName) {
        Party party = get(leader);
        if (party == null) {
            plugin.messages().send(leader, "party.not-in");
            return;
        }
        if (!party.isLeader(leader.getUniqueId())) {
            plugin.messages().send(leader, "party.not-leader");
            return;
        }
        UUID target = memberByName(party, targetName);
        if (target == null || target.equals(leader.getUniqueId())) {
            plugin.messages().send(leader, "party.not-member", Text.ph("player", targetName));
            return;
        }
        Player online = Bukkit.getPlayer(target);
        removeMember(party, target, "party.kicked", targetName);
        if (online != null) {
            plugin.messages().send(online, "party.kicked-self");
        }
    }

    public void promote(Player leader, String targetName) {
        Party party = get(leader);
        if (party == null) {
            plugin.messages().send(leader, "party.not-in");
            return;
        }
        if (!party.isLeader(leader.getUniqueId())) {
            plugin.messages().send(leader, "party.not-leader");
            return;
        }
        UUID target = memberByName(party, targetName);
        if (target == null) {
            plugin.messages().send(leader, "party.not-member", Text.ph("player", targetName));
            return;
        }
        party.setLeader(target);
        broadcast(party, "party.new-leader", Text.ph("player", targetName));
        plugin.hud().refreshPartyBoard(party);
    }

    public void disband(Player leader) {
        Party party = get(leader);
        if (party == null) {
            plugin.messages().send(leader, "party.not-in");
            return;
        }
        if (!party.isLeader(leader.getUniqueId())) {
            plugin.messages().send(leader, "party.not-leader");
            return;
        }
        disband(party);
    }

    public void disband(Party party) {
        broadcast(party, "party.disbanded");
        for (UUID uuid : new ArrayList<>(party.members())) {
            byPlayer.remove(uuid);
            var prof = plugin.profiles().getIfLoaded(uuid);
            if (prof != null) {
                prof.setPartyChat(false);
            }
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                plugin.hud().removePartyBoard(p);
            }
        }
    }

    public void chat(Player sender, String message) {
        Party party = get(sender);
        if (party == null) {
            plugin.messages().send(sender, "party.not-in");
            return;
        }
        plugin.chat().sendParty(sender, party, message);
    }

    /** /gc sans argument : bascule le chat du joueur sur le groupe. */
    public void togglePartyChat(Player player) {
        Party party = get(player);
        var profile = plugin.profiles().get(player);
        if (party == null) {
            profile.setPartyChat(false);
            plugin.messages().send(player, "party.not-in");
            return;
        }
        profile.setPartyChat(!profile.isPartyChat());
        plugin.messages().send(player, profile.isPartyChat() ? "party.chat-on" : "party.chat-off");
    }

    public void onQuit(Player player) {
        invites.remove(player.getUniqueId());
        Party party = get(player);
        if (party != null) {
            removeMember(party, player.getUniqueId(), "party.left", player.getName());
        }
    }

    public void disbandAll() {
        for (Party party : new ArrayList<>(new java.util.HashSet<>(byPlayer.values()))) {
            for (Player p : party.onlineMembers()) {
                plugin.hud().removePartyBoard(p);
            }
        }
        byPlayer.clear();
        invites.clear();
    }

    private void removeMember(Party party, UUID uuid, String messageKey, String name) {
        party.remove(uuid);
        byPlayer.remove(uuid);
        var prof = plugin.profiles().getIfLoaded(uuid);
        if (prof != null) {
            prof.setPartyChat(false);
        }
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            plugin.hud().removePartyBoard(p);
        }
        if (party.size() <= 1 && plugin.getConfig().getBoolean("party.disband-when-alone", true)) {
            disband(party);
            return;
        }
        if (party.size() == 0) {
            return;
        }
        broadcast(party, messageKey, Text.ph("player", name));
        if (party.isLeader(uuid)) {
            UUID next = party.members().iterator().next();
            party.setLeader(next);
            Player np = Bukkit.getPlayer(next);
            broadcast(party, "party.new-leader", Text.ph("player", np != null ? np.getName() : "?"));
        }
        plugin.hud().refreshPartyBoard(party);
    }

    private UUID memberByName(Party party, String name) {
        for (UUID uuid : party.members()) {
            Player p = Bukkit.getPlayer(uuid);
            String n = p != null ? p.getName() : Bukkit.getOfflinePlayer(uuid).getName();
            if (n != null && n.equalsIgnoreCase(name)) {
                return uuid;
            }
        }
        return null;
    }

    public void broadcast(Party party, String key, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... resolvers) {
        for (Player p : party.onlineMembers()) {
            plugin.messages().send(p, key, resolvers);
        }
    }

    // ------------------------------------------------------------ XP

    /**
     * Joueurs qui reçoivent l'XP d'une élimination et la part de chacun.
     * Sans groupe : le tueur seul reçoit 100 %.
     */
    public Map<Player, Double> xpShares(Player killer, Location where, double xp) {
        Map<Player, Double> out = new LinkedHashMap<>();
        Party party = get(killer);
        if (party == null) {
            out.put(killer, xp);
            return out;
        }
        double range = plugin.getConfig().getDouble("party.xp-share-range", 40);
        List<Player> eligible = new ArrayList<>();
        for (Player p : party.onlineMembers()) {
            if (p.equals(killer) || (p.getWorld().equals(where.getWorld()) && p.getLocation().distanceSquared(where) <= range * range
                    && !p.isDead() && p.getGameMode() != GameMode.SPECTATOR)) {
                if (plugin.profiles().get(p).hasClass()) {
                    eligible.add(p);
                }
            }
        }
        if (eligible.isEmpty()) {
            out.put(killer, xp);
            return out;
        }
        if (party.xpMode() == Party.XpMode.EQUAL) {
            for (Player p : eligible) {
                out.put(p, xp);
            }
        } else {
            double bonus = plugin.getConfig().getDouble("party.shared-bonus-per-member", 0.1);
            double total = xp * (1 + bonus * (eligible.size() - 1));
            double each = total / eligible.size();
            for (Player p : eligible) {
                out.put(p, each);
            }
        }
        return out;
    }

    /** Membres en ligne d'un groupe dans un rayon autour d'un joueur (lui compris). */
    public List<Player> membersNear(Player player, double radius) {
        List<Player> out = new ArrayList<>();
        out.add(player);
        Party party = get(player);
        if (party == null) {
            return out;
        }
        for (Player p : party.onlineMembers()) {
            if (!p.equals(player) && p.getWorld().equals(player.getWorld())
                    && p.getLocation().distanceSquared(player.getLocation()) <= radius * radius
                    && !p.isDead() && p.getGameMode() != GameMode.SPECTATOR) {
                out.add(p);
            }
        }
        return out;
    }

    public void tickInvites() {
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<UUID, Map<UUID, Invite>>> it = invites.entrySet().iterator(); it.hasNext(); ) {
            Map<UUID, Invite> map = it.next().getValue();
            map.values().removeIf(i -> i.expires() < now);
            if (map.isEmpty()) {
                it.remove();
            }
        }
    }

    public String modeName(Party.XpMode mode) {
        return plugin.messages().raw(mode == Party.XpMode.EQUAL ? "party.mode-equal" : "party.mode-shared");
    }

    public String ffName(boolean ff) {
        return plugin.messages().raw(ff ? "party.ff-on" : "party.ff-off");
    }
}
