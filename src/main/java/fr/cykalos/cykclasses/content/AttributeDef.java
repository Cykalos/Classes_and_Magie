package fr.cykalos.cykclasses.content;

import fr.cykalos.cykclasses.model.StatModifier;
import org.bukkit.Material;

import java.util.List;

/** Attribute improved with points (Strength, Intelligence...). Effects are given per point. */
public record AttributeDef(String id, String name, Material icon, List<String> description, int max,
                           StatModifier perPoint) {
}
