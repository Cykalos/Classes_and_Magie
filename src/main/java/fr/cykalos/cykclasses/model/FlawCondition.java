package fr.cykalos.cykclasses.model;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * Conditions facultatives d'un défaut ou d'une faiblesse : le défaut ne s'applique que si TOUTES
 * les conditions renseignées sont vraies. Sans condition, il s'applique toujours.
 * <pre>
 * conditions:
 *   time: NIGHT                 # DAY ou NIGHT
 *   weather: [RAIN, THUNDER]    # CLEAR, RAIN, THUNDER
 *   dimensions: [NETHER]        # NORMAL, NETHER, THE_END
 *   worlds: [world_pvp]
 *   biomes: ["*DESERT*", "BADLANDS"]
 *   light-below: 8              # niveau de lumière du bloc
 *   light-above: 12
 *   under-sky: true             # à ciel ouvert
 *   in-water: true
 *   in-rain: true
 *   in-lava: true
 *   health-below: 0.3           # fraction des PV max (0.3 = 30 %)
 *   health-above: 0.9
 *   y-below: 0
 *   y-above: 120
 *   sneaking: true
 *   sprinting: true
 *   gliding: true
 *   holding: ["*_SWORD"]        # objet en main principale
 *   wearing: ["GOLDEN_*"]       # au moins une pièce d'armure
 *   permission: "monserveur.malediction"
 *   chance: 0.25                # 25 % de chances à chaque vérification
 * </pre>
 */
public final class FlawCondition {

    public static final FlawCondition ALWAYS = new FlawCondition();

    private String time;
    private List<String> weather = List.of();
    private List<String> dimensions = List.of();
    private List<String> worlds = List.of();
    private List<Pattern> biomes = List.of();
    private Integer lightBelow;
    private Integer lightAbove;
    private Boolean underSky;
    private Boolean inWater;
    private Boolean inRain;
    private Boolean inLava;
    private Double healthBelow;
    private Double healthAbove;
    private Double yBelow;
    private Double yAbove;
    private Boolean sneaking;
    private Boolean sprinting;
    private Boolean gliding;
    private List<Pattern> holding = List.of();
    private List<Pattern> wearing = List.of();
    private String permission;
    private double chance = 1.0;
    private boolean empty = true;

    private FlawCondition() {
    }

    public static FlawCondition load(Object raw) {
        if (raw instanceof org.bukkit.configuration.ConfigurationSection section) {
            raw = section.getValues(false);
        }
        if (!(raw instanceof Map<?, ?> m) || m.isEmpty()) {
            return ALWAYS;
        }
        FlawCondition c = new FlawCondition();
        c.empty = false;
        if (m.get("time") != null) {
            c.time = String.valueOf(m.get("time")).toUpperCase(Locale.ROOT);
        }
        c.weather = upper(m.get("weather"));
        c.dimensions = upper(m.get("dimensions"));
        c.worlds = strings(m.get("worlds"));
        c.biomes = patterns(m.get("biomes"));
        c.lightBelow = integer(m.get("light-below"));
        c.lightAbove = integer(m.get("light-above"));
        c.underSky = bool(m.get("under-sky"));
        c.inWater = bool(m.get("in-water"));
        c.inRain = bool(m.get("in-rain"));
        c.inLava = bool(m.get("in-lava"));
        c.healthBelow = number(m.get("health-below"));
        c.healthAbove = number(m.get("health-above"));
        c.yBelow = number(m.get("y-below"));
        c.yAbove = number(m.get("y-above"));
        c.sneaking = bool(m.get("sneaking"));
        c.sprinting = bool(m.get("sprinting"));
        c.gliding = bool(m.get("gliding"));
        c.holding = patterns(m.get("holding"));
        c.wearing = patterns(m.get("wearing"));
        c.permission = m.get("permission") == null ? null : String.valueOf(m.get("permission"));
        Double chance = number(m.get("chance"));
        c.chance = chance == null ? 1.0 : chance;
        return c;
    }

    public boolean isAlways() {
        return empty;
    }

    /** Vrai si toutes les conditions sont remplies pour ce joueur (null = hors ligne : on considère vrai). */
    public boolean test(Player p) {
        if (empty || p == null) {
            return true;
        }
        World w = p.getWorld();
        if (time != null) {
            boolean day = w.isDayTime();
            if (time.equals("DAY") != day) {
                return false;
            }
        }
        if (!weather.isEmpty()) {
            String now = w.isThundering() ? "THUNDER" : w.hasStorm() ? "RAIN" : "CLEAR";
            if (!weather.contains(now)) {
                return false;
            }
        }
        if (!dimensions.isEmpty() && !dimensions.contains(w.getEnvironment().name())) {
            return false;
        }
        if (!worlds.isEmpty() && worlds.stream().noneMatch(n -> n.equalsIgnoreCase(w.getName()))) {
            return false;
        }
        Block b = p.getLocation().getBlock();
        if (!biomes.isEmpty()) {
            String biome = biomeName(b);
            if (biomes.stream().noneMatch(pt -> pt.matcher(biome).matches())) {
                return false;
            }
        }
        if (lightBelow != null && b.getLightLevel() >= lightBelow) {
            return false;
        }
        if (lightAbove != null && b.getLightLevel() <= lightAbove) {
            return false;
        }
        if (underSky != null && (b.getLightFromSky() >= 15) != underSky) {
            return false;
        }
        if (inWater != null && p.isInWater() != inWater) {
            return false;
        }
        if (inRain != null && p.isInRain() != inRain) {
            return false;
        }
        if (inLava != null && p.isInLava() != inLava) {
            return false;
        }
        if (healthBelow != null || healthAbove != null) {
            var attr = p.getAttribute(Attribute.MAX_HEALTH);
            double ratio = attr == null ? 1 : p.getHealth() / attr.getValue();
            if (healthBelow != null && ratio >= healthBelow) {
                return false;
            }
            if (healthAbove != null && ratio <= healthAbove) {
                return false;
            }
        }
        double y = p.getLocation().getY();
        if (yBelow != null && y >= yBelow) {
            return false;
        }
        if (yAbove != null && y <= yAbove) {
            return false;
        }
        if (sneaking != null && p.isSneaking() != sneaking) {
            return false;
        }
        if (sprinting != null && p.isSprinting() != sprinting) {
            return false;
        }
        if (gliding != null && p.isGliding() != gliding) {
            return false;
        }
        if (!holding.isEmpty() && !matches(holding, p.getInventory().getItemInMainHand())) {
            return false;
        }
        if (!wearing.isEmpty()) {
            boolean any = false;
            for (ItemStack armor : p.getInventory().getArmorContents()) {
                if (matches(wearing, armor)) {
                    any = true;
                    break;
                }
            }
            if (!any) {
                return false;
            }
        }
        if (permission != null && !p.hasPermission(permission)) {
            return false;
        }
        return chance >= 1.0 || ThreadLocalRandom.current().nextDouble() < chance;
    }

    @SuppressWarnings("deprecation")
    private static String biomeName(Block b) {
        try {
            return b.getBiome().getKey().getKey().toUpperCase(Locale.ROOT);
        } catch (Throwable t) {
            return "";
        }
    }

    private static boolean matches(List<Pattern> patterns, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        String name = item.getType().name();
        for (Pattern p : patterns) {
            if (p.matcher(name).matches()) {
                return true;
            }
        }
        return false;
    }

    static List<String> strings(Object o) {
        List<String> out = new ArrayList<>();
        if (o instanceof List<?> l) {
            l.forEach(x -> out.add(String.valueOf(x)));
        } else if (o != null) {
            out.add(String.valueOf(o));
        }
        return out;
    }

    private static List<String> upper(Object o) {
        List<String> out = new ArrayList<>();
        strings(o).forEach(s -> out.add(s.toUpperCase(Locale.ROOT)));
        return out;
    }

    static List<Pattern> patterns(Object o) {
        List<Pattern> out = new ArrayList<>();
        for (String s : strings(o)) {
            out.add(Pattern.compile(s.toUpperCase(Locale.ROOT).replace(".", "\\.").replace("*", ".*")));
        }
        return out;
    }

    private static Boolean bool(Object o) {
        return o == null ? null : Boolean.valueOf(String.valueOf(o));
    }

    private static Integer integer(Object o) {
        return o instanceof Number n ? n.intValue() : null;
    }

    private static Double number(Object o) {
        return o instanceof Number n ? n.doubleValue() : null;
    }
}
