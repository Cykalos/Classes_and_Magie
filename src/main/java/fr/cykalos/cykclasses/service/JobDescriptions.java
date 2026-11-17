package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.JobRule;

import java.util.Locale;
import java.util.regex.Pattern;

/** Short, readable text of a reserved action (uses "display" if set in jobs.yml). */
public final class JobDescriptions {

    private final CykClasses plugin;

    public JobDescriptions(CykClasses plugin) {
        this.plugin = plugin;
    }

    public String describe(JobRule r) {
        String action = plugin.messages().raw("job.action-" + r.action().name().toLowerCase(Locale.ROOT));
        if (r.display() != null && !r.display().isBlank()) {
            return action + fr.cykalos.cykclasses.config.Lang.tr(" : ", ": ") + r.display();
        }
        if (r.targets().isEmpty()) {
            return action;
        }
        StringBuilder sb = new StringBuilder();
        int shown = 0;
        for (Pattern p : r.targets()) {
            String raw = p.pattern();
            if (raw.contains("(") || raw.contains("|")) {
                continue; // complex pattern: unreadable in game, use "display"
            }
            if (shown == 2) {
                sb.append("…");
                break;
            }
            if (shown > 0) {
                sb.append(", ");
            }
            sb.append(raw.replace(".*", "").replace("\\.", ".").replace('_', ' ').toLowerCase(Locale.ROOT).trim());
            shown++;
        }
        return shown == 0 ? action : action + fr.cykalos.cykclasses.config.Lang.tr(" : ", ": ") + sb;
    }
}
