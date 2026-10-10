package fr.cykalos.cykclasses.hook;

import io.github.projectunified.uniitem.all.AllItemProvider;
import io.github.projectunified.uniitem.api.ItemKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Pont vers la bibliothèque UniItem (CraftEngine, ExecutableItems, ItemsAdder, Oraxen, Nexo, MMOItems, MythicMobs...).
 * Chargée seulement si la bibliothèque est disponible.
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

    /** Identifiant « type:id » d'un objet custom, ou null pour un objet vanilla. */
    public String key(ItemStack item) {
        try {
            ItemKey k = provider.key(item);
            return k == null ? null : k.toString();
        } catch (Throwable t) {
            return null;
        }
    }
}
