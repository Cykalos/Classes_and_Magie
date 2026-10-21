package fr.cykalos.cykclasses.quest;

import fr.cykalos.cykclasses.model.FlawCondition;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Une quête : informations, conditions, étapes et récompenses. */
public final class Quest {

    public enum Category { TUTORIAL, MAIN, CLASS, SIDE, SECRET }

    /** MANUAL : depuis le journal · AUTO : dès qu'elle est disponible · OFFER : proposée dans le chat. */
    public enum Start { MANUAL, AUTO, OFFER }

    public record Stage(List<String> text, List<Objective> objectives) {
    }

    public final String id;
    public final String name;
    public final Category category;
    public final Material icon;
    public final int level;
    public final List<String> description;
    public final Start start;
    public final boolean hidden;
    public final boolean repeatable;
    public final double cooldownHours;
    public final boolean abandonable;
    public final int order;
    public final QuestRequirements requirements;
    public final FlawCondition conditions;
    public final Objective trigger;
    public final List<Stage> stages;
    public final QuestReward rewards;
    public final List<String> completeText;
    public final String file;

    private Quest(String id, String name, Category category, Material icon, int level, List<String> description,
                  Start start, boolean hidden, boolean repeatable, double cooldownHours, boolean abandonable, int order,
                  QuestRequirements requirements, FlawCondition conditions, Objective trigger, List<Stage> stages,
                  QuestReward rewards, List<String> completeText, String file) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.icon = icon;
        this.level = level;
        this.description = description;
        this.start = start;
        this.hidden = hidden;
        this.repeatable = repeatable;
        this.cooldownHours = cooldownHours;
        this.abandonable = abandonable;
        this.order = order;
        this.requirements = requirements;
        this.conditions = conditions;
        this.trigger = trigger;
        this.stages = stages;
        this.rewards = rewards;
        this.completeText = completeText;
        this.file = file;
    }

    public static Quest load(String id, ConfigurationSection s, String file, int order) {
        String where = file + " → " + id;
        Category category = parse(Category.class, s.getString("category"), Category.SIDE);
        boolean hidden = s.getBoolean("hidden", category == Category.SECRET);
        Start start = parse(Start.class, s.getString("start"), hidden ? Start.AUTO : Start.MANUAL);
        Material icon = Material.matchMaterial(s.getString("icon", "WRITABLE_BOOK"));
        List<Stage> stages = new ArrayList<>();
        for (Map<?, ?> st : s.getMapList("stages")) {
            List<String> text = new ArrayList<>();
            if (st.get("text") instanceof List<?> l) {
                l.forEach(o -> text.add(String.valueOf(o)));
            } else if (st.get("text") != null) {
                text.add(String.valueOf(st.get("text")));
            }
            List<Objective> objectives = new ArrayList<>();
            if (st.get("objectives") instanceof List<?> ol) {
                for (Object o : ol) {
                    if (o instanceof Map<?, ?> om) {
                        objectives.add(Objective.load(om, where));
                    }
                }
            }
            stages.add(new Stage(text, objectives));
        }
        if (stages.isEmpty()) {
            throw new IllegalArgumentException(where + " : aucune étape (stages)");
        }
        return new Quest(id.toLowerCase(Locale.ROOT), s.getString("name", id), category,
                icon == null ? Material.WRITABLE_BOOK : icon, s.getInt("level", 1), s.getStringList("description"),
                start, hidden, s.getBoolean("repeatable", false), s.getDouble("cooldown-hours", 0),
                s.getBoolean("abandonable", true), order,
                QuestRequirements.load(s.getConfigurationSection("requirements")),
                FlawCondition.load(s.get("conditions")),
                Objective.load(s.getConfigurationSection("trigger"), where),
                stages, QuestReward.load(s.getConfigurationSection("rewards")),
                s.getStringList("complete-text"), file);
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String raw, E def) {
        if (raw == null) {
            return def;
        }
        try {
            return Enum.valueOf(type, raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
}
