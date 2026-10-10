package fr.cykalos.cykclasses.content;

import java.util.List;

/** Kit donné au choix d'une classe ou d'une sous-classe. */
public record Kit(String classId, List<KitItem> items, List<String> commands) {
}
