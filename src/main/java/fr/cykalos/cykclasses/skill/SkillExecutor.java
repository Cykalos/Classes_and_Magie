package fr.cykalos.cykclasses.skill;

/**
 * Implémentation d'un type de compétence.
 */
@FunctionalInterface
public interface SkillExecutor {

    /**
     * Lance la compétence.
     *
     * @return {@code true} si elle a été lancée (le coût et la recharge sont alors appliqués),
     * {@code false} si elle a échoué (pas de cible...).
     */
    boolean cast(SkillContext ctx);
}
