package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.AttributeDef;
import fr.cykalos.cykclasses.content.ItemRule;
import fr.cykalos.cykclasses.content.Job;
import fr.cykalos.cykclasses.content.Race;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Restrictions d'objets (items.yml) : classe, race, métier, niveau et attributs requis
 * pour attaquer, tirer, utiliser ou porter un objet.
 */
public final class ItemRestrictionService implements Listener {

    private static final EquipmentSlot[] WORN = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFF_HAND};

    private final CykClasses plugin;
    private final Map<UUID, Long> lastMessage = new HashMap<>();

    public ItemRestrictionService(CykClasses plugin) {
        this.plugin = plugin;
    }

    /** Règle non respectée par le joueur pour cet usage, ou null si l'utilisation est permise. */
    public ItemRule failing(Player player, ItemStack item, ItemRule.Use use) {
        if (item == null || item.getType().isAir() || plugin.content().itemRules().isEmpty()) {
            return null;
        }
        if (player.hasPermission("cykclasses.bypass.items")) {
            return null;
        }
        PlayerProfile profile = plugin.profiles().get(player);
        String customId = null;
        boolean customResolved = false;
        for (ItemRule rule : plugin.content().itemRules()) {
            if (!rule.blocked().contains(use)) {
                continue;
            }
            if (!rule.customIds().isEmpty() && !customResolved) {
                customId = plugin.itemFactory().customId(item);
                customResolved = true;
            }
            if (matches(rule, item, customId) && !allowed(rule, profile)) {
                return rule;
            }
        }
        return null;
    }

    private boolean matches(ItemRule rule, ItemStack item, String customId) {
        boolean any = false;
        if (!rule.materials().isEmpty()) {
            any = true;
            if (anyMatch(rule.materials(), item.getType().name())) {
                return true;
            }
        }
        if (!rule.customIds().isEmpty()) {
            any = true;
            if (customId != null && anyMatch(rule.customIds(), customId)) {
                return true;
            }
        }
        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;
        if (!rule.nameContains().isEmpty()) {
            any = true;
            if (meta != null && meta.hasDisplayName()) {
                String name = PlainTextComponentSerializer.plainText().serialize(meta.displayName()).toLowerCase(Locale.ROOT);
                for (String n : rule.nameContains()) {
                    if (name.contains(n.toLowerCase(Locale.ROOT))) {
                        return true;
                    }
                }
            }
        }
        if (!rule.loreContains().isEmpty()) {
            any = true;
            if (meta != null && meta.hasLore() && meta.lore() != null) {
                for (var line : meta.lore()) {
                    String plain = PlainTextComponentSerializer.plainText().serialize(line).toLowerCase(Locale.ROOT);
                    for (String n : rule.loreContains()) {
                        if (plain.contains(n.toLowerCase(Locale.ROOT))) {
                            return true;
                        }
                    }
                }
            }
        }
        return !any;
    }

    private static boolean anyMatch(List<Pattern> patterns, String value) {
        for (Pattern p : patterns) {
            if (p.matcher(value).matches()) {
                return true;
            }
        }
        return false;
    }

    private boolean allowed(ItemRule rule, PlayerProfile p) {
        if (!rule.classes().isEmpty()) {
            boolean ok = (p.getClassId() != null && rule.classes().contains(p.getClassId()))
                    || (p.getSubclassId() != null && rule.classes().contains(p.getSubclassId()));
            if (!ok) {
                return false;
            }
        }
        if (!rule.races().isEmpty() && (p.getRaceId() == null || !rule.races().contains(p.getRaceId()))) {
            return false;
        }
        if (!rule.jobs().isEmpty() && (p.getJobId() == null || !rule.jobs().contains(p.getJobId()))) {
            return false;
        }
        if (p.getLevel() < rule.minLevel()) {
            return false;
        }
        for (Map.Entry<String, Integer> e : rule.minAttributes().entrySet()) {
            if (p.getAttribute(e.getKey()) < e.getValue()) {
                return false;
            }
        }
        return true;
    }

    /** Texte des conditions, ex. « Guerrier, Paladin · niv. 10 · Force 15 ». */
    public String describe(ItemRule rule) {
        List<String> parts = new ArrayList<>();
        if (!rule.classes().isEmpty()) {
            List<String> names = new ArrayList<>();
            for (String c : rule.classes()) {
                RpgClass rc = plugin.classes().get(c);
                names.add(rc == null ? c : Text.plain(rc.name));
            }
            parts.add(String.join(", ", names));
        }
        if (!rule.races().isEmpty()) {
            List<String> names = new ArrayList<>();
            for (String r : rule.races()) {
                Race race = plugin.content().race(r);
                names.add(race == null ? r : Text.plain(race.name()));
            }
            parts.add("race " + String.join("/", names));
        }
        if (!rule.jobs().isEmpty()) {
            List<String> names = new ArrayList<>();
            for (String j : rule.jobs()) {
                Job job = plugin.content().job(j);
                names.add(job == null ? j : Text.plain(job.name()));
            }
            parts.add("métier " + String.join("/", names));
        }
        if (rule.minLevel() > 0) {
            parts.add("niv. " + rule.minLevel());
        }
        rule.minAttributes().forEach((k, v) -> {
            AttributeDef a = plugin.content().attribute(k);
            parts.add((a == null ? k : Text.plain(a.name())) + " " + v);
        });
        return String.join(" · ", parts);
    }

    private void deny(Player player, ItemRule rule) {
        long now = System.currentTimeMillis();
        Long last = lastMessage.get(player.getUniqueId());
        if (last != null && now - last < 2000) {
            return;
        }
        lastMessage.put(player.getUniqueId(), now);
        if (rule.message() != null && !rule.message().isBlank()) {
            plugin.messages().send(player, "item-restricted-custom", Text.ph("message", rule.message()));
        } else {
            plugin.messages().send(player, "item-restricted", Text.ph("requirements", describe(rule)));
        }
    }

    // ------------------------------------------------------------ Événements

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (plugin.combat().isDealingSkillDamage(player.getUniqueId())) {
            return;
        }
        ItemRule rule = failing(player, player.getInventory().getItemInMainHand(), ItemRule.Use.ATTACK);
        if (rule != null) {
            event.setCancelled(true);
            deny(player, rule);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemRule rule = failing(player, event.getBow(), ItemRule.Use.SHOOT);
            if (rule != null) {
                event.setCancelled(true);
                deny(player, rule);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || plugin.skillBar().isIcon(item)) {
            return;
        }
        ItemRule rule = failing(event.getPlayer(), item, ItemRule.Use.USE);
        if (rule != null) {
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
            deny(event.getPlayer(), rule);
        }
    }

    /** Retire les objets portés qui ne respectent pas leurs conditions (appelé périodiquement). */
    public void enforceArmor(Player player, PlayerProfile profile) {
        if (plugin.content().itemRules().isEmpty()) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        for (EquipmentSlot slot : WORN) {
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType().isAir()) {
                continue;
            }
            ItemRule rule = failing(player, item, ItemRule.Use.WEAR);
            if (rule == null) {
                continue;
            }
            inv.setItem(slot, null);
            inv.addItem(item).values().forEach(rest -> player.getWorld().dropItemNaturally(player.getLocation(), rest));
            deny(player, rule);
        }
    }
}
