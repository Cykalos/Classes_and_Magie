package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple inventory menu: each slot can have a click action.
 */
public abstract class Gui implements InventoryHolder {

    protected final CykClasses plugin;
    protected final Player viewer;
    private final Inventory inventory;
    private final Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();

    protected Gui(CykClasses plugin, Player viewer, int rows, Component title) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
    }

    protected abstract void build();

    /** Module this menu depends on (config.yml → modules). Disabled: the menu does not open. */
    protected boolean moduleEnabled() {
        return true;
    }

    public void open() {
        if (!moduleEnabled()) {
            plugin.messages().send(viewer, "module-disabled");
            if (viewer.getOpenInventory().getTopInventory().getHolder(false) instanceof Gui) {
                viewer.closeInventory();
            }
            return;
        }
        inventory.clear();
        actions.clear();
        build();
        viewer.openInventory(inventory);
    }

    protected void set(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        inventory.setItem(slot, item);
        if (action != null) {
            actions.put(slot, action);
        }
    }

    protected void fill() {
        ItemStack pane = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, pane);
            }
        }
    }

    /** Frame: black glass border, empty inside to make the buttons stand out. */
    protected void frame() {
        ItemStack border = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        int size = inventory.getSize();
        int rows = size / 9;
        for (int i = 0; i < size; i++) {
            int row = i / 9, col = i % 9;
            boolean edge = row == 0 || row == rows - 1 || col == 0 || col == 8;
            if (edge && inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }
    }

    /**
     * Mandatory character creation step: if the player closes this menu (Esc / E key),
     * the current step is opened again.
     */
    public boolean isCreationStep() {
        return false;
    }

    /** Called when the player closes the menu themselves. */
    public void onPlayerClose() {
    }

    public Player viewer() {
        return viewer;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getClickedInventory() != inventory) {
            return;
        }
        Consumer<InventoryClickEvent> action = actions.get(event.getRawSlot());
        if (action != null) {
            action.accept(event);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
