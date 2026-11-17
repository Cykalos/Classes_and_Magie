package fr.cykalos.cykclasses.content;

import java.util.List;

/** Kit given when choosing a class or a subclass. */
public record Kit(String classId, List<KitItem> items, List<String> commands) {
}
