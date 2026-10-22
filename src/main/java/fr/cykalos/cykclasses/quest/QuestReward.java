package fr.cykalos.cykclasses.quest;

import fr.cykalos.cykclasses.content.KitItem;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Récompenses d'une quête :
 * <pre>
 * rewards:
 *   xp: 200                 # XP de classe
 *   job-xp: 100             # XP du métier actuel
 *   skill-points: 1         # points d'arbre
 *   attribute-points: 1     # points d'attributs
 *   money: 150              # argent (Vault et un plugin d'économie)
 *   items:                  # même format que kits.yml
 *     - {item: "minecraft:iron_ingot", amount: 8}
 *     - {item: "mythic:EpeeRunique", name: "<gold>Lame de l'aube"}
 *   titles: [legende_de_la_guilde]   # titres débloqués
 *   commands: ["lp user %player% permission set monserveur.grade"]
 *   text: ["<gray>Une récompense spéciale"]   # lignes en plus dans le journal
 * </pre>
 */
public record QuestReward(double xp, double jobXp, int skillPoints, int attributePoints, double money,
                          List<KitItem> items, List<String> titles, List<String> commands, List<String> text) {

    public static final QuestReward NONE = new QuestReward(0, 0, 0, 0, 0, List.of(), List.of(), List.of(), List.of());

    public static QuestReward load(ConfigurationSection s) {
        if (s == null) {
            return NONE;
        }
        List<KitItem> items = new ArrayList<>();
        for (Object o : s.getList("items", List.of())) {
            if (o instanceof Map<?, ?> m && m.get("item") != null) {
                items.add(kitItem(m));
            } else if (o instanceof String str) {
                items.add(new KitItem(str, 1, null, null, List.of(), Map.of(), false, null));
            }
        }
        return new QuestReward(s.getDouble("xp"), s.getDouble("job-xp"), s.getInt("skill-points"),
                s.getInt("attribute-points"), s.getDouble("money"), items, s.getStringList("titles"), s.getStringList("commands"),
                s.getStringList("text"));
    }

    static KitItem kitItem(Map<?, ?> m) {
        Map<String, Integer> ench = new HashMap<>();
        if (m.get("enchants") instanceof Map<?, ?> em) {
            em.forEach((k, v) -> {
                if (v instanceof Number n) {
                    ench.put(String.valueOf(k).toLowerCase(Locale.ROOT), n.intValue());
                }
            });
        }
        List<String> lore = new ArrayList<>();
        if (m.get("lore") instanceof List<?> l) {
            l.forEach(x -> lore.add(String.valueOf(x)));
        }
        return new KitItem(String.valueOf(m.get("item")), m.get("amount") instanceof Number n ? n.intValue() : 1,
                null, m.get("name") == null ? null : String.valueOf(m.get("name")), lore, ench,
                Boolean.TRUE.equals(m.get("unbreakable")),
                m.get("item-model") == null ? null : String.valueOf(m.get("item-model")));
    }

    public boolean isEmpty() {
        return xp <= 0 && jobXp <= 0 && skillPoints <= 0 && attributePoints <= 0 && money <= 0
                && items.isEmpty() && titles.isEmpty() && commands.isEmpty() && text.isEmpty();
    }
}
