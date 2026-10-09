package fr.cykalos.cykclasses.api.event;

import fr.cykalos.cykclasses.model.SkillDefinition;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/** Appelé avant qu'une compétence soit lancée. Le coût en mana peut être modifié. */
public final class SkillCastEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final SkillDefinition skill;
    private double manaCost;
    private boolean cancelled;

    public SkillCastEvent(@NotNull Player who, @NotNull SkillDefinition skill, double manaCost) {
        super(who);
        this.skill = skill;
        this.manaCost = manaCost;
    }

    public @NotNull SkillDefinition getSkill() { return skill; }
    public double getManaCost() { return manaCost; }
    public void setManaCost(double manaCost) { this.manaCost = Math.max(0, manaCost); }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
