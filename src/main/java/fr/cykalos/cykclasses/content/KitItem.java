package fr.cykalos.cykclasses.content;

import java.util.List;
import java.util.Map;

/**
 * Objet de kit. {@code item} : « minecraft:iron_sword », ou un objet custom via UniItem
 * (« craftengine:pack:epee », « executableitems:epee », « mythicmobs:Epee », « itemsadder:... »...).
 */
public record KitItem(String item, int amount, String slot, String name, List<String> lore,
                      Map<String, Integer> enchants, boolean unbreakable, String itemModel) {
}
