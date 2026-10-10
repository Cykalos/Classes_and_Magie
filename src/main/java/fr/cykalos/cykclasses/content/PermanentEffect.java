package fr.cykalos.cykclasses.content;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Effet de potion permanent (« speed:1 » = Vitesse I). */
public record PermanentEffect(PotionEffectType type, int amplifier) {

    public static List<PermanentEffect> parse(List<String> raw) {
        List<PermanentEffect> out = new ArrayList<>();
        for (String s : raw) {
            String[] parts = s.split(":");
            NamespacedKey key = NamespacedKey.minecraft(parts[0].toLowerCase(Locale.ROOT).trim());
            PotionEffectType type = Registry.MOB_EFFECT.get(key);
            if (type == null) {
                continue;
            }
            int level = parts.length > 1 ? Math.max(1, Integer.parseInt(parts[1].trim())) : 1;
            out.add(new PermanentEffect(type, level - 1));
        }
        return out;
    }

    public static List<String> strings(Map<?, ?> map, String key) {
        List<String> out = new ArrayList<>();
        if (map.get(key) instanceof List<?> list) {
            for (Object o : list) {
                out.add(String.valueOf(o));
            }
        }
        return out;
    }
}
