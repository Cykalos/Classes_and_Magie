package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.hook.UniItemBridge;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.logging.Level;

/**
 * Création et identification d'objets : vanilla (« minecraft:iron_sword »), MythicMobs (« mythic:Nom »)
 * ou tout plugin supporté par UniItem (« craftengine:pack:id », « executableitems:id », « itemsadder:ns:id »...).
 */
public final class ItemFactory {

    private final CykClasses plugin;
    private UniItemBridge uni;

    public ItemFactory(CykClasses plugin) {
        this.plugin = plugin;
    }

    /** À appeler une fois tous les plugins chargés. */
    public void init() {
        try {
            uni = new UniItemBridge();
            plugin.getLogger().info("Objets custom (UniItem) : " + String.join(", ", uni.types()));
        } catch (Throwable t) {
            uni = null;
            plugin.getLogger().log(Level.WARNING, "UniItem indisponible : seuls les objets vanilla et MythicMobs sont gérés.", t);
        }
    }

    public ItemStack create(String id, int amount, Player player) {
        if (id == null || id.isBlank()) {
            return null;
        }
        String lower = id.toLowerCase(Locale.ROOT);
        if (!id.contains(":") || lower.startsWith("minecraft:")) {
            Material m = Material.matchMaterial(id);
            return m == null || m.isAir() ? null : new ItemStack(m, Math.max(1, amount));
        }
        if ((lower.startsWith("mythic:") || lower.startsWith("mythicmobs:")) && plugin.mythic() != null) {
            ItemStack it = plugin.mythic().item(id.substring(id.indexOf(':') + 1), amount);
            if (it != null) {
                return it;
            }
        }
        if (uni != null) {
            ItemStack it = uni.item(id, player);
            if (it != null) {
                it.setAmount(Math.max(1, amount));
                return it;
            }
        }
        return null;
    }

    /** Identifiant custom en minuscules (« craftengine:pack:epee », « mythic:epee »...), ou null si vanilla. */
    public String customId(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        if (plugin.mythic() != null) {
            String m = plugin.mythic().itemId(item);
            if (m != null) {
                return ("mythic:" + m).toLowerCase(Locale.ROOT);
            }
        }
        if (uni != null) {
            String k = uni.key(item);
            if (k != null) {
                return k.toLowerCase(Locale.ROOT);
            }
        }
        return null;
    }
}
