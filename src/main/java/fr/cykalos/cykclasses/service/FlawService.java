package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.PermanentEffect;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Défauts « environnementaux » vérifiés périodiquement : armure interdite, eau, soleil.
 */
public final class FlawService {

    private static final EquipmentSlot[] CHECKED = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFF_HAND};

    private final CykClasses plugin;
    private BukkitTask task;
    /** Dernier déclenchement des défauts PERIODIC_DAMAGE, par joueur. */
    private final Map<UUID, Map<Flaw, Long>> periodic = new HashMap<>();

    public FlawService(CykClasses plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 40L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        periodic.keySet().removeIf(id -> Bukkit.getPlayer(id) == null);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            PlayerProfile profile = plugin.profiles().getIfLoaded(player.getUniqueId());
            RpgClass rc = plugin.classes().effective(profile);
            if (rc == null) {
                continue;
            }
            for (PermanentEffect e : plugin.stats().effects(profile)) {
                player.addPotionEffect(new PotionEffect(e.type(), 100, e.amplifier(), true, false, true));
            }
            plugin.items().enforceArmor(player, profile);
            plugin.gearListener().checkEquipment(player);
            long now = System.currentTimeMillis();
            for (Flaw f : plugin.stats().flaws(profile)) {
                switch (f.type) {
                    case FORBIDDEN_ARMOR -> {
                        if (f.active(player)) {
                            enforceArmor(player, f);
                        }
                    }
                    case EFFECT -> {
                        if (f.active(player)) {
                            for (PermanentEffect e : f.effects) {
                                player.addPotionEffect(new PotionEffect(e.type(), 60, e.amplifier(), false, false, true));
                            }
                        }
                    }
                    case PERIODIC_DAMAGE -> {
                        Map<Flaw, Long> last = periodic.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
                        long prev = last.getOrDefault(f, 0L);
                        if (now - prev >= (long) (f.interval * 1000) - 50 && f.active(player)) {
                            last.put(f, now);
                            if (f.damage > 0) {
                                player.damage(f.damage);
                            }
                            if (f.fireTicks > 0) {
                                player.setFireTicks(Math.max(player.getFireTicks(), f.fireTicks));
                            }
                        }
                    }
                    case WATER_WEAKNESS -> {
                        if (player.isInWater() && f.active(player)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0, false, false, true));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, false, false, true));
                        }
                    }
                    case SUNLIGHT_WEAKNESS -> {
                        Block b = player.getLocation().getBlock();
                        if (player.getWorld().isDayTime() && b.getLightFromSky() >= 15 && !player.getWorld().hasStorm()
                                && f.active(player)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, false, false, true));
                        }
                    }
                    default -> {
                    }
                }
            }
        }
    }

    private void enforceArmor(Player player, Flaw flaw) {
        PlayerInventory inv = player.getInventory();
        for (EquipmentSlot slot : CHECKED) {
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType().isAir() || !flaw.matches(item.getType())) {
                continue;
            }
            inv.setItem(slot, null);
            HashMap<Integer, ItemStack> left = inv.addItem(item);
            left.values().forEach(rest -> player.getWorld().dropItemNaturally(player.getLocation(), rest));
            plugin.messages().send(player, "flaw-forbidden-armor",
                    Text.ph("item", item.getType().getKey().getKey().replace('_', ' ')));
        }
    }
}
