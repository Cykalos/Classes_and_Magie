package fr.cykalos.cykclasses.skill.impl;

import fr.cykalos.cykclasses.skill.SkillContext;
import fr.cykalos.cykclasses.skill.SkillService;
import fr.cykalos.cykclasses.util.Text;

/**
 * Type de compétence {@code MYTHIC} : lance une compétence MythicMobs depuis le joueur.
 *
 * <pre>
 * ma_competence:
 *   type: MYTHIC
 *   params: {skill: "NomDeLaCompetenceMythic", power: 1.0}
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
            c.plugin().getLogger().warning("Compétence " + c.def().id + " : paramètre 'skill' manquant (type MYTHIC).");
            return false;
        }
        if (c.plugin().mythic() == null) {
            c.player().sendMessage(Text.mm("<red>Cette compétence nécessite MythicMobs."));
            return false;
        }
        float power = (float) c.d("power", 1.0);
        boolean ok = c.plugin().mythic().castSkill(c.player(), skill, power);
        if (!ok) {
            c.plugin().getLogger().warning("Compétence MythicMobs introuvable ou échouée : " + skill);
        }
        return ok;
    }
}
