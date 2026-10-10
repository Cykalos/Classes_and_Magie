package fr.cykalos.cykclasses.content;

import org.bukkit.Material;
import fr.cykalos.cykclasses.model.StatModifier;

import java.util.List;

/** Métier : un seul par joueur. Certaines actions sont réservées à certains métiers (voir {@link JobRule}). */
public record Job(String id, String name, Material icon, List<String> description, StatModifier modifiers,
                  List<PermanentEffect> effects) {
}
