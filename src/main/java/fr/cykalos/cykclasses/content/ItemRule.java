package fr.cykalos.cykclasses.content;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Item usage restriction: which items (filter) and which conditions are needed to use them.
 */
public record ItemRule(String id, List<Pattern> materials, List<Pattern> customIds, List<String> nameContains,
                       List<String> loreContains, List<String> classes, List<String> races, List<String> jobs,
                       int minLevel, Map<String, Integer> minAttributes, Set<Use> blocked, String message) {

    public enum Use { ATTACK, SHOOT, USE, WEAR }
}
