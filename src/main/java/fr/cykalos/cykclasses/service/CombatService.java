package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.ClassStats;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Dégâts des compétences, ciblage, et états temporaires (rage, parade, marque...).
 */
public final class CombatService {

    public enum DamageKind { MAGIC, MELEE, RANGED, TRUE }

    /** États temporaires d'un joueur. Les horodatages sont en millisecondes. */
    public static final class Status {
        public long rageUntil;
        public double rageLifesteal;
        public double rageBonus;
        public long empoweredUntil;
        public double empoweredMultiplier;
        public long parryUntil;
        public double parryCounter;
        public int poisonCharges;
        public int poisonTicks;
        public int poisonAmplifier;
        public long manaShieldUntil;
        public double manaShieldRatio;
        public long wildSpiritUntil;
    }

    public record Mark(UUID caster, long until, double bonus) {
    }

    private final CykClasses plugin;
    private final Map<UUID, Status> statuses = new HashMap<>();
    private final Map<UUID, Mark> marks = new HashMap<>();
    /** Lanceurs en train d'infliger des dégâts de compétence (évite le double calcul). */
    private final Set<UUID> skillDamage = new HashSet<>();

    public CombatService(CykClasses plugin) {
        this.plugin = plugin;
    }

    public Status status(Player player) {
        return statuses.computeIfAbsent(player.getUniqueId(), k -> new Status());
    }

    public Status statusIfPresent(UUID uuid) {
        return statuses.get(uuid);
    }

    public void clear(UUID uuid) {
        statuses.remove(uuid);
        marks.values().removeIf(m -> m.caster().equals(uuid));
    }

    public boolean isDealingSkillDamage(UUID uuid) {
        return skillDamage.contains(uuid);
    }

    public void mark(LivingEntity target, Player caster, long durationMs, double bonus) {
        marks.put(target.getUniqueId(), new Mark(caster.getUniqueId(), System.currentTimeMillis() + durationMs, bonus));
    }

    public Mark markOf(Entity entity) {
        Mark m = marks.get(entity.getUniqueId());
        if (m != null && m.until() < System.currentTimeMillis()) {
            marks.remove(entity.getUniqueId());
            return null;
        }
        return m;
    }

    public double multiplier(Player player, DamageKind kind) {
        PlayerProfile profile = plugin.profiles().get(player);
        ClassStats s = plugin.stats().stats(profile);
        return switch (kind) {
            case MAGIC -> s.magicDamage;
            case MELEE -> s.meleeDamage;
            case RANGED -> s.rangedDamage;
            case TRUE -> 1.0;
        };
    }

    /**
     * Inflige des dégâts de compétence en appliquant le multiplicateur de la classe du lanceur.
     *
     * @return les dégâts réellement demandés, ou 0 si la cible est invalide.
     */
    public double damage(Player caster, LivingEntity target, double base, DamageKind kind) {
        if (!isValidTarget(caster, target)) {
            return 0;
        }
        double amount = base * multiplier(caster, kind);
        skillDamage.add(caster.getUniqueId());
        try {
            target.setNoDamageTicks(0);
            target.damage(amount, caster);
        } finally {
            skillDamage.remove(caster.getUniqueId());
        }
        plugin.profiles().get(caster).addCounter("damage_dealt", amount);
        plugin.profiles().get(caster).touchCombat();
        return amount;
    }

    public void heal(Player caster, LivingEntity target, double amount) {
        var max = target.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        double maxHp = max == null ? 20 : max.getValue();
        double before = target.getHealth();
        double healed = Math.min(maxHp, before + amount * multiplier(caster, DamageKind.MAGIC));
        target.setHealth(healed);
        if (healed > before) {
            plugin.profiles().get(caster).addCounter("healing_done", healed - before);
            plugin.hud().heal(target, caster, healed - before);
        }
    }

    public boolean isValidTarget(Player caster, Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity.equals(caster) || living.isDead() || entity instanceof ArmorStand) {
            return false;
        }
        if (living.isInvulnerable()) {
            return false;
        }
        if (entity instanceof Tameable tame && tame.isTamed() && caster.equals(tame.getOwner())) {
            return false;
        }
        if (entity instanceof Player p) {
            if (plugin.parties().isProtectedAlly(caster, p)) {
                return false;
            }
            if (!plugin.parties().sameParty(caster, p) && !plugin.getConfig().getBoolean("casting.pvp", true)) {
                return false;
            }
            return p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE;
        }
        return true;
    }

    public List<LivingEntity> enemiesAround(Player caster, Location center, double radius) {
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : center.getNearbyLivingEntities(radius)) {
            if (isValidTarget(caster, e)) {
                out.add(e);
            }
        }
        return out;
    }

    /**
     * Cibles des compétences de soutien : le lanceur et les membres de son groupe dans le rayon.
     * Sans groupe, seul le lanceur est concerné.
     */
    public Collection<Player> alliesAround(Player caster, double radius) {
        return plugin.parties().membersNear(caster, radius);
    }

    /** Première entité valide dans la ligne de mire (les blocs bloquent la vue). */
    public LivingEntity targetInSight(Player caster, double range) {
        Location eye = caster.getEyeLocation();
        Vector dir = eye.getDirection();
        RayTraceResult result = caster.getWorld().rayTrace(eye, dir, range, FluidCollisionMode.NEVER, true, 0.6,
                e -> isValidTarget(caster, e));
        if (result != null && result.getHitEntity() instanceof LivingEntity le) {
            return le;
        }
        return null;
    }

    /** Membre du groupe dans la ligne de mire, ou {@code null}. */
    public Player allyInSight(Player caster, double range) {
        Location eye = caster.getEyeLocation();
        RayTraceResult result = caster.getWorld().rayTrace(eye, eye.getDirection(), range, FluidCollisionMode.NEVER, true, 0.6,
                e -> e instanceof Player p && !e.equals(caster) && plugin.parties().sameParty(caster, p));
        if (result != null && result.getHitEntity() instanceof Player p) {
            return p;
        }
        return null;
    }

    /** Point visé (bloc ou fin de portée). */
    public Location targetLocation(Player caster, double range) {
        Location eye = caster.getEyeLocation();
        RayTraceResult result = caster.getWorld().rayTraceBlocks(eye, eye.getDirection(), range, FluidCollisionMode.NEVER, true);
        if (result != null) {
            return result.getHitPosition().toLocation(caster.getWorld());
        }
        return eye.clone().add(eye.getDirection().multiply(range));
    }
}
