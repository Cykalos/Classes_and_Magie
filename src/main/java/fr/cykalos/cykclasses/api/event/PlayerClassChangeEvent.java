package fr.cykalos.cykclasses.api.event;

import fr.cykalos.cykclasses.model.RpgClass;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Appelé quand un joueur choisit une classe ou une sous-classe (ou la perd : {@code newClass} nul). */
public final class PlayerClassChangeEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final RpgClass oldClass;
    private final RpgClass newClass;
    private boolean cancelled;

    public PlayerClassChangeEvent(@NotNull Player who, @Nullable RpgClass oldClass, @Nullable RpgClass newClass) {
        super(who);
        this.oldClass = oldClass;
        this.newClass = newClass;
    }

    public @Nullable RpgClass getOldClass() { return oldClass; }
    public @Nullable RpgClass getNewClass() { return newClass; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
