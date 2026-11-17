package fr.cykalos.cykclasses.skill;

/**
 * Implementation of a skill type.
 */
@FunctionalInterface
public interface SkillExecutor {

    /**
     * Casts the skill.
     *
     * @return {@code true} if it was cast (the cost and cooldown are then applied),
     * {@code false} if it failed (no target...).
     */
    boolean cast(SkillContext ctx);
}
