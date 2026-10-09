package fr.cykalos.cykclasses.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/** Appelé quand un joueur gagne un niveau de classe. */
public final class PlayerClassLevelUpEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final int oldLevel;
    private final int newLevel;

    public PlayerClassLevelUpEvent(@NotNull Player who, int oldLevel, int newLevel) {
        super(who);
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public int getOldLevel() { return oldLevel; }
    public int getNewLevel() { return newLevel; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
