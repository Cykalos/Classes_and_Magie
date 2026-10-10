package fr.cykalos.cykclasses.hook;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Pont vers MythicMobs. Cette classe n'est chargée que si MythicMobs est présent.
 */
public final class MythicHook {

    public record MobInfo(String internalName, String displayName, double level) {
    }

    public Optional<MobInfo> info(Entity entity) {
        try {
            Optional<ActiveMob> opt = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId());
            if (opt.isEmpty()) {
                return Optional.empty();
            }
            ActiveMob mob = opt.get();
            String display = mob.getDisplayName();
            return Optional.of(new MobInfo(mob.getType().getInternalName(), display, mob.getLevel()));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    public boolean isMythicMob(Entity entity) {
        return info(entity).isPresent();
    }

    /** Nom affiché d'un mob MythicMobs (codes couleur & ou § acceptés). */
    public Component displayName(Entity entity) {
        return info(entity).map(i -> {
            String raw = i.displayName() == null ? i.internalName() : i.displayName();
            return raw.indexOf('§') >= 0
                    ? LegacyComponentSerializer.legacySection().deserialize(raw)
                    : LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
        }).orElse(null);
    }

    public boolean castSkill(Player caster, String skill, float power) {
        try {
            return MythicBukkit.inst().getAPIHelper().castSkill(caster, skill, power);
        } catch (Throwable t) {
            return false;
        }
    }
}
