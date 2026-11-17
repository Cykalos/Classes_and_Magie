package fr.cykalos.cykclasses.title;

import fr.cykalos.cykclasses.content.PermanentEffect;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.StatModifier;
import fr.cykalos.cykclasses.quest.Objective;
import fr.cykalos.cykclasses.quest.QuestRequirements;
import org.bukkit.Material;

import java.util.List;

/**
 * A title: display name, rarity, unlock conditions and effects while equipped.
 *
 * @param unique  only one player on the server can own it
 * @param hidden  shown as "???" until it is unlocked
 * @param unlock  objectives to meet (empty = title only given by a quest or an administrator)
 */
public record PlayerTitle(String id, String name, String rarity, Material icon, List<String> description,
                          boolean unique, boolean hidden, QuestRequirements requirements, List<Objective> unlock,
                          StatModifier modifiers, List<PermanentEffect> effects, List<Flaw> flaws, int order) {

    public boolean hasEffects() {
        return !modifiers.isEmpty() || !effects.isEmpty() || !flaws.isEmpty();
    }
}
