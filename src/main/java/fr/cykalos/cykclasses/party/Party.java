package fr.cykalos.cykclasses.party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Un groupe de joueurs. Les règles sont fixées à la création et ne changent plus.
 */
public final class Party {

    /** Répartition de l'XP d'une élimination. */
    public enum XpMode {
        /** L'XP est divisée entre les membres proches (avec un bonus de groupe). */
        SHARED,
        /** Chaque membre proche reçoit l'XP complète de l'élimination. */
        EQUAL
    }

    private final UUID id = UUID.randomUUID();
    private UUID leader;
    private final Set<UUID> members = new CopyOnWriteArraySet<>();
    private final XpMode xpMode;
    private final boolean friendlyFire;
    private final long createdAt = System.currentTimeMillis();

    public Party(UUID leader, XpMode xpMode, boolean friendlyFire) {
        this.leader = leader;
        this.xpMode = xpMode;
        this.friendlyFire = friendlyFire;
        members.add(leader);
    }

    public UUID id() { return id; }
    public UUID leader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public boolean isLeader(UUID uuid) { return leader.equals(uuid); }
    public XpMode xpMode() { return xpMode; }
    public boolean friendlyFire() { return friendlyFire; }
    public long createdAt() { return createdAt; }

    public Set<UUID> members() { return Collections.unmodifiableSet(members); }
    public boolean contains(UUID uuid) { return members.contains(uuid); }
    void add(UUID uuid) { members.add(uuid); }
    void remove(UUID uuid) { members.remove(uuid); }
    public int size() { return members.size(); }

    public List<Player> onlineMembers() {
        List<Player> out = new ArrayList<>();
        for (UUID uuid : members) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                out.add(p);
            }
        }
        return out;
    }
}
