package fr.cykalos.cykclasses.content;

import fr.cykalos.cykclasses.model.StatModifier;
import org.bukkit.Material;

import java.util.List;

/** Attribut améliorable avec des points (Force, Intelligence...). Les effets sont donnés par point. */
public record AttributeDef(String id, String name, Material icon, List<String> description, int max,
                           StatModifier perPoint) {
}
