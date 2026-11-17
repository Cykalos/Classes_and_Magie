package fr.cykalos.cykclasses.api;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.skill.SkillExecutor;
import org.bukkit.entity.Player;

/**
 * Entry point for other plugins.
 *
 * <pre>{@code
 * RpgClass rc = CykClassesAPI.getClass(player);
 * CykClassesAPI.registerSkillType("MY_SPELL", ctx -> { ...; return true; });
 * }</pre>
 */
public final class CykClassesAPI {

    private CykClassesAPI() {
    }

    private static CykClasses plugin() {
        return CykClasses.instance();
    }

    public static PlayerProfile getProfile(Player player) {
        return plugin().profiles().get(player);
    }

    /** Effective class (subclass if one was chosen). */
    public static RpgClass getClass(Player player) {
        return plugin().classes().effective(getProfile(player));
    }

    public static int getLevel(Player player) {
        return getProfile(player).getLevel();
    }

    public static double getMana(Player player) {
        return getProfile(player).getMana();
    }

    public static void setMana(Player player, double mana) {
        PlayerProfile p = getProfile(player);
        p.setMana(Math.min(mana, plugin().stats().maxMana(p)));
    }

    public static void addXp(Player player, double xp) {
        plugin().levels().addXp(player, xp);
    }

    public static boolean castSkill(Player player, String skillId) {
        return plugin().skills().cast(player, skillId);
    }

    /** Registers a new skill type usable in classes.yml. */
    public static void registerSkillType(String type, SkillExecutor executor) {
        plugin().skills().register(type, executor);
    }
}
