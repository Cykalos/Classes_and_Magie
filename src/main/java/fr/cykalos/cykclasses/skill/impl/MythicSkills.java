package fr.cykalos.cykclasses.skill.impl;

import fr.cykalos.cykclasses.skill.SkillContext;
import fr.cykalos.cykclasses.skill.SkillService;
import fr.cykalos.cykclasses.util.Text;

/**
 * {@code MYTHIC} skill type: casts a MythicMobs skill from the player.
 *
 * <pre>
 * my_skill:
 *   type: MYTHIC
 *   params: {skill: "MythicSkillName", power: 1.0}
 * </pre>
 */
public final class MythicSkills {

    private MythicSkills() {
    }

    public static void register(SkillService s) {
        s.register("MYTHIC", MythicSkills::mythic);
    }

    private static boolean mythic(SkillContext c) {
        String skill = c.def().s("skill", null);
        if (skill == null || skill.isBlank()) {
            c.plugin().getLogger().warning("Skill " + c.def().id + ": missing 'skill' parameter (MYTHIC type).");
            return false;
        }
        if (c.plugin().mythic() == null) {
            c.plugin().messages().send(c.player(), "skill-needs-mythicmobs");
            return false;
        }
        float power = (float) c.d("power", 1.0);
        boolean ok = c.plugin().mythic().castSkill(c.player(), skill, power);
        if (!ok) {
            c.plugin().getLogger().warning("MythicMobs skill not found or failed: " + skill);
        }
        return ok;
    }
}
