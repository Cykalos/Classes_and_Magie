package fr.cykalos.cykclasses.listener;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.api.event.SkillCastEvent;
import fr.cykalos.cykclasses.hook.MythicHook;
import fr.cykalos.cykclasses.quest.Objective;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Actions qui font avancer les quêtes. Casser, poser, fabriquer, cuire, pêcher, élever, apprivoiser, enchanter,
 * distiller et tailler des gemmes passent par JobService (mêmes règles anti-abus que les métiers).
 */
public final class QuestListener implements Listener {

    private final CykClasses plugin;

    public QuestListener(CykClasses plugin) {
        this.plugin = plugin;
    }

    private static String name(Entity e) {
        return e.customName() == null ? null : PlainTextComponentSerializer.plainText().serialize(e.customName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKill(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Player killer = dead.getKiller();
        if (killer == null) {
            return;
        }
        List<String> candidates = new ArrayList<>();
        candidates.add(dead.getType().name());
        MythicHook mythic = plugin.mythic();
        if (mythic != null && !(dead instanceof Player)) {
            mythic.info(dead).ifPresent(info -> candidates.add("mythic:" + info.internalName()));
        }
        plugin.quests().progress(killer, Objective.Type.KILL, candidates, 1, name(dead));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        plugin.quests().progress(event.getPlayer(), Objective.Type.CONSUME, event.getItem().getType().name(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSkill(SkillCastEvent event) {
        plugin.quests().progress(event.getPlayer(), Objective.Type.SKILL, event.getSkill().id, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Entity e = event.getRightClicked();
        plugin.quests().progress(event.getPlayer(), Objective.Type.INTERACT_ENTITY, List.of(e.getType().name()), 1, name(e));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getClickedBlock() == null || event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }
        plugin.quests().progress(event.getPlayer(), Objective.Type.INTERACT_BLOCK, event.getClickedBlock().getType().name(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage();
        if (msg.length() < 2) {
            return;
        }
        String label = msg.substring(1).split(" ")[0].toLowerCase(Locale.ROOT);
        if (label.contains(":")) {
            label = label.substring(label.indexOf(':') + 1);
        }
        List<String> candidates = new ArrayList<>();
        candidates.add(label);
        Command cmd = Bukkit.getCommandMap().getCommand(label);
        if (cmd != null) {
            candidates.add(cmd.getName());
        }
        plugin.quests().progress(event.getPlayer(), Objective.Type.COMMAND, candidates, 1, null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof Projectile pr && pr.getShooter() instanceof Entity shooter) {
            damager = shooter;
        }
        if (damager instanceof Player player && event.getEntity() instanceof LivingEntity target && !target.equals(player)) {
            plugin.quests().progress(player, Objective.Type.DAMAGE, List.of(target.getType().name()),
                    event.getFinalDamage(), name(target));
        }
    }
}
