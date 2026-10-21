package fr.cykalos.cykclasses.title;

import fr.cykalos.cykclasses.content.PermanentEffect;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.StatModifier;
import fr.cykalos.cykclasses.quest.Objective;
import fr.cykalos.cykclasses.quest.QuestRequirements;
import org.bukkit.Material;

import java.util.List;

/**
 * Un titre : nom affiché, rareté, conditions de déblocage et effets quand il est équipé.
 *
 * @param unique  un seul joueur du serveur peut le posséder
 * @param hidden  affiché « ??? » tant qu'il n'est pas débloqué
 * @param unlock  objectifs à remplir (vide = titre donné uniquement par une quête ou un administrateur)
 */
public record PlayerTitle(String id, String name, String rarity, Material icon, List<String> description,
                          boolean unique, boolean hidden, QuestRequirements requirements, List<Objective> unlock,
                          StatModifier modifiers, List<PermanentEffect> effects, List<Flaw> flaws, int order) {

    public boolean hasEffects() {
        return !modifiers.isEmpty() || !effects.isEmpty() || !flaws.isEmpty();
    }
}
