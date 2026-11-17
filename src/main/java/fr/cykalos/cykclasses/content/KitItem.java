package fr.cykalos.cykclasses.content;

import java.util.List;
import java.util.Map;

/**
 * Kit item. {@code item}: "minecraft:iron_sword", or a custom item through UniItem
 * ("craftengine:pack:sword", "executableitems:sword", "mythicmobs:Sword", "itemsadder:..."...).
 */
public record KitItem(String item, int amount, String slot, String name, List<String> lore,
                      Map<String, Integer> enchants, boolean unbreakable, String itemModel) {
}
