package fr.cykalos.cykclasses.quest;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Conditions pour qu'une quête soit disponible :
 * <pre>
 * requirements:
 *   level: 10                 # niveau de classe minimum
 *   max-level: 40             # niveau maximum (facultatif)
 *   classes: [guerrier]       # classe ou sous-classe (vide = toutes)
 *   races: [nain]
 *   jobs: [mineur]
 *   job-level: 15             # niveau du métier actuel
 *   quests: [le_reveil]       # quêtes à avoir terminées
 *   not-quests: [autre_voie]  # quêtes à NE PAS avoir terminées (choix de camp…)
 *   permission: "monserveur.chapitre2"
 *   titles: [tueur_de_dragons]  # titres à avoir débloqués
 *   checks:                   # objectifs « état » à remplir (statistiques, progrès, totaux…)
 *     - {type: STATISTIC, targets: [DEATHS], amount: 10}
 * </pre>
 */
public record QuestRequirements(int level, int maxLevel, List<String> classes, List<String> races, List<String> jobs,
                                int jobLevel, List<String> quests, List<String> notQuests, String permission,
                                List<String> titles, List<Objective> checks) {

    public static final QuestRequirements NONE = new QuestRequirements(0, 0, List.of(), List.of(), List.of(), 0,
            List.of(), List.of(), null, List.of(), List.of());

    public static QuestRequirements load(ConfigurationSection s) {
        if (s == null) {
            return NONE;
        }
        return new QuestRequirements(s.getInt("level"), s.getInt("max-level"), lower(s.getStringList("classes")),
                lower(s.getStringList("races")), lower(s.getStringList("jobs")), s.getInt("job-level"),
                lower(s.getStringList("quests")), lower(s.getStringList("not-quests")), s.getString("permission"),
                lower(s.getStringList("titles")), checks(s));
    }

    private static List<Objective> checks(ConfigurationSection s) {
        List<Objective> out = new ArrayList<>();
        for (java.util.Map<?, ?> m : s.getMapList("checks")) {
            Objective o = Objective.load(m, "requirements.checks");
            if (o.type.isState()) {
                out.add(o);
            }
        }
        return out;
    }

    private static List<String> lower(List<String> in) {
        List<String> out = new ArrayList<>();
        in.forEach(s -> out.add(s.toLowerCase(Locale.ROOT)));
        return out;
    }
}
