package fr.cykalos.cykclasses.listener;

import fr.cykalos.cykclasses.gui.Gui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class GuiListener implements Listener {

    private final fr.cykalos.cykclasses.CykClasses plugin;

    public GuiListener(fr.cykalos.cykclasses.CykClasses plugin) {
        this.plugin = plugin;
    }

    /** Fermer un menu de création (Échap / E) ne permet pas de sauter l'étape : elle est rouverte. */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof Gui gui)) {
            return;
        }
        // Fermetures forcées par le jeu (téléportation, monde déchargé…) : l'étape est rouverte aussi.
        // La mort est gérée à la réapparition, la déconnexion à la reconnexion.
        switch (event.getReason()) {
            case PLAYER -> gui.onPlayerClose();
            case TELEPORT, CANT_USE, UNLOADED -> { }
            default -> {
                return;
            }
        }
        if (gui.isCreationStep() && plugin.getConfig().getBoolean("force-class-selection", true)) {
            plugin.character().continueCreation(gui.viewer());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder(false) instanceof Gui gui) {
            gui.handleClick(event);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder(false) instanceof Gui) {
            event.setCancelled(true);
        }
    }
}
