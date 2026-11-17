package fr.cykalos.cykclasses.hook;

import io.github.projectunified.uniitem.all.AllItemProvider;
import io.github.projectunified.uniitem.api.ItemKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Bridge to the UniItem library (CraftEngine, ExecutableItems, ItemsAdder, Oraxen, Nexo, MMOItems, MythicMobs...).
 * Only loaded when the library is available.
 */
public final class UniItemBridge {

    private final AllItemProvider provider = new AllItemProvider();

    public List<String> types() {
        return provider.availableTypes();
    }

    public ItemStack item(String key, Player player) {
        try {
            return provider.tryItem(ItemKey.fromString(key), player);
        } catch (Throwable t) {
            return null;
        }
    }

    /** "type:id" identifier of a custom item, or null for a vanilla item. */
    public String key(ItemStack item) {
        try {
            ItemKey k = provider.key(item);
            return k == null ? null : k.toString();
        } catch (Throwable t) {
            return null;
        }
    }
}
