package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.quest.Objective;
import fr.cykalos.cykclasses.quest.Quest;
import fr.cykalos.cykclasses.quest.QuestService;
import fr.cykalos.cykclasses.quest.QuestService.Status;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Journal des quêtes : quêtes en cours, disponibles, verrouillées et terminées, par catégorie.
 * Clic : commencer ou suivre dans le panneau latéral · Maj + clic droit : abandonner.
 */
public final class QuestJournalGui extends Gui {

    /** Onglets : null = quêtes en cours (toutes catégories). */
    private static final Quest.Category[] TABS = {null, Quest.Category.TUTORIAL, Quest.Category.MAIN,
            Quest.Category.CLASS, Quest.Category.SIDE, Quest.Category.SECRET};
    private static final Material[] TAB_ICONS = {Material.WRITABLE_BOOK, Material.BOOK, Material.NETHER_STAR,
            Material.IRON_SWORD, Material.MAP, Material.ENDER_EYE};
    private static final int[] AREA = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    private final int tab;
    private final int page;

    public QuestJournalGui(CykClasses plugin, Player viewer) {
        this(plugin, viewer, -1, 0);
    }

    private QuestJournalGui(CykClasses plugin, Player viewer, int tab, int page) {
        super(plugin, viewer, 6, plugin.messages().get("quests.gui.title"));
        this.tab = tab < 0 ? defaultTab(plugin, viewer) : tab;
        this.page = Math.max(0, page);
    }

    private static int defaultTab(CykClasses plugin, Player viewer) {
        PlayerProfile p = plugin.profiles().get(viewer);
        if (!p.questStages().isEmpty()) {
            return 0;
        }
        Quest tuto = plugin.quests().tutorial();
        return tuto != null && !p.hasCompletedQuest(tuto.id) ? 1 : 2;
    }

    @Override
    protected boolean moduleEnabled() {
        return plugin.modules().quests();
    }

    @Override
    protected void build() {
        QuestService qs = plugin.quests();
        PlayerProfile p = plugin.profiles().get(viewer);

        // Onglets
        for (int i = 0; i < TABS.length; i++) {
            final int t = i;
            String key = TABS[i] == null ? "active" : TABS[i].name().toLowerCase(Locale.ROOT);
            int[] counts = counts(qs, p, TABS[i]);
            ItemBuilder b = new ItemBuilder(TAB_ICONS[i]).name(plugin.messages().item("quests.gui.tab." + key))
                    .glow(i == tab);
            if (TABS[i] == null) {
                b.lore(plugin.messages().item("quests.gui.tab-active-lore", Text.ph("count", p.questStages().size())));
            } else {
                b.lore(plugin.messages().item("quests.gui.tab-lore", Text.ph("done", counts[0]), Text.ph("total", counts[1])));
                if (TABS[i] == Quest.Category.SECRET && counts[2] > 0) {
                    b.lore(plugin.messages().item("quests.gui.secrets-left", Text.ph("count", counts[2])));
                }
            }
            set(1 + i, b.build(), e -> new QuestJournalGui(plugin, viewer, t, 0).open());
        }

        // Quêtes de l'onglet
        List<Quest> list = new ArrayList<>();
        for (Quest q : qs.all()) {
            if (TABS[tab] == null ? p.isQuestActive(q.id) : q.category == TABS[tab] && qs.visible(viewer, p, q)) {
                list.add(q);
            }
        }
        list.sort(Comparator.comparingInt((Quest q) -> rank(qs.status(viewer, p, q))).thenComparingInt(q -> q.order));
        int pages = Math.max(1, (list.size() + AREA.length - 1) / AREA.length);
        int current = Math.min(page, pages - 1);
        if (list.isEmpty()) {
            set(22, new ItemBuilder(Material.PAPER).name(plugin.messages().item("quests.gui.empty")).build(), null);
        }
        for (int i = 0; i < AREA.length; i++) {
            int index = current * AREA.length + i;
            if (index >= list.size()) {
                break;
            }
            Quest q = list.get(index);
            set(AREA[i], questItem(qs, p, q), e -> click(qs, q, e.getClick()));
        }

        // Barre du bas
        if (current > 0) {
            set(46, new ItemBuilder(Material.ARROW).name(plugin.messages().item("quests.gui.previous")).build(),
                    e -> new QuestJournalGui(plugin, viewer, tab, current - 1).open());
        }
        if (current < pages - 1) {
            set(52, new ItemBuilder(Material.ARROW).name(plugin.messages().item("quests.gui.next")).build(),
                    e -> new QuestJournalGui(plugin, viewer, tab, current + 1).open());
        }
        boolean hidden = p.isQuestTrackerHidden();
        set(48, new ItemBuilder(hidden ? Material.GRAY_DYE : Material.OAK_SIGN)
                .name(plugin.messages().item(hidden ? "quests.gui.tracker-off" : "quests.gui.tracker-on"))
                .lore(plugin.messages().item("quests.gui.tracker-lore")).build(), e -> {
            p.setQuestTrackerHidden(!p.isQuestTrackerHidden());
            open();
        });
        int done = (int) qs.all().stream().filter(q -> p.hasCompletedQuest(q.id)).count();
        set(49, new ItemBuilder(Material.KNOWLEDGE_BOOK).name(plugin.messages().item("quests.gui.summary"))
                .lore(plugin.messages().item("quests.gui.summary-lore", Text.ph("done", done),
                        Text.ph("total", qs.all().size()), Text.ph("active", p.questStages().size()))).build(), null);
        Quest tuto = qs.tutorial();
        if (tuto != null && !p.isQuestActive(tuto.id) && !p.hasCompletedQuest(tuto.id)) {
            set(50, new ItemBuilder(Material.BOOK).name(plugin.messages().item("quests.gui.tutorial"))
                    .lore(plugin.messages().item("quests.gui.tutorial-lore")).build(), e -> {
                if (qs.startQuest(viewer, tuto, false)) {
                    open();
                }
            });
        }
        frame();
    }

    private void click(QuestService qs, Quest q, ClickType click) {
        PlayerProfile p = plugin.profiles().get(viewer);
        Status st = qs.status(viewer, p, q);
        if (st == Status.ACTIVE) {
            if (click == ClickType.SHIFT_RIGHT) {
                qs.abandon(viewer, q);
            } else {
                qs.toggleTrack(viewer, q);
            }
            open();
        } else if (st == Status.AVAILABLE) {
            if (qs.startQuest(viewer, q, false)) {
                open();
            }
        }
    }

    private static int rank(Status s) {
        return switch (s) {
            case ACTIVE -> 0;
            case AVAILABLE -> 1;
            case COOLDOWN -> 2;
            case LOCKED -> 3;
            case COMPLETED -> 4;
            case HIDDEN -> 5;
        };
    }

    /** [terminées, total visible, secrètes restantes à découvrir] */
    private int[] counts(QuestService qs, PlayerProfile p, Quest.Category cat) {
        int done = 0, total = 0, secrets = 0;
        if (cat == null) {
            return new int[]{0, 0, 0};
        }
        for (Quest q : qs.all()) {
            if (q.category != cat) {
                continue;
            }
            if (qs.visible(viewer, p, q)) {
                total++;
                if (p.hasCompletedQuest(q.id)) {
                    done++;
                }
            } else if (q.hidden && !p.hasCompletedQuest(q.id) && !p.isQuestActive(q.id)) {
                secrets++;
            }
        }
        return new int[]{done, total, secrets};
    }

    private org.bukkit.inventory.ItemStack questItem(QuestService qs, PlayerProfile p, Quest q) {
        Status st = qs.status(viewer, p, q);
        Material icon = st == Status.LOCKED ? Material.GRAY_DYE : q.icon;
        String nameKey = switch (st) {
            case COMPLETED -> "quests.gui.name-completed";
            case ACTIVE -> "quests.gui.name-active";
            case LOCKED -> "quests.gui.name-locked";
            default -> "quests.gui.name";
        };
        ItemBuilder b = new ItemBuilder(icon).name(Text.item(plugin.messages().raw(nameKey), Text.ph("name", q.name)))
                .glow(st == Status.ACTIVE);
        b.lore(plugin.messages().item("quests.gui.info", Text.ph("level", q.level),
                Text.ph("category", plugin.messages().raw("quests.category." + q.category.name().toLowerCase(Locale.ROOT)))));
        if (!q.description.isEmpty()) {
            b.lore(Component.empty());
            b.lore(Text.itemLore(q.description));
        }
        b.lore(Component.empty());
        b.lore(plugin.messages().item("quests.status." + st.name().toLowerCase(Locale.ROOT)));
        switch (st) {
            case ACTIVE -> {
                int stage = p.getQuestStage(q.id);
                if (q.stages.size() > 1) {
                    b.lore(plugin.messages().item("quests.gui.stage", Text.ph("stage", stage + 1), Text.ph("stages", q.stages.size())));
                }
                List<Objective> objs = q.stages.get(stage).objectives();
                for (int i = 0; i < objs.size(); i++) {
                    double v = qs.value(viewer, p, q, stage, i);
                    boolean done = v >= objs.get(i).amount;
                    b.lore(Text.item(plugin.messages().raw(done ? "quests.gui.objective-done" : "quests.gui.objective"),
                            Text.ph("objective", qs.objectiveText(objs.get(i))),
                            Text.ph("progress", qs.progressText(v, objs.get(i).amount))));
                }
            }
            case LOCKED -> {
                for (Component c : qs.missingRequirements(viewer, p, q)) {
                    b.lore(Text.item(plugin.messages().raw("quests.gui.requirement"), Text.phc("requirement", c)));
                }
            }
            case COMPLETED, COOLDOWN -> {
                if (q.repeatable) {
                    b.lore(plugin.messages().item("quests.gui.times", Text.ph("count", p.getQuestCount(q.id))));
                }
            }
            default -> {
            }
        }
        List<Component> rewards = qs.rewardLines(q.rewards);
        if (!rewards.isEmpty()) {
            b.lore(Component.empty());
            b.lore(plugin.messages().item("quests.gui.rewards"));
            for (Component r : rewards) {
                b.lore(Text.item(plugin.messages().raw("quests.gui.reward"), Text.phc("reward", r)));
            }
        }
        b.lore(Component.empty());
        switch (st) {
            case AVAILABLE -> b.lore(plugin.messages().item("quests.gui.click-start"));
            case ACTIVE -> {
                b.lore(plugin.messages().item(p.questsTracked().contains(q.id) ? "quests.gui.click-untrack" : "quests.gui.click-track"));
                if (q.abandonable) {
                    b.lore(plugin.messages().item("quests.gui.click-abandon"));
                }
            }
            default -> {
            }
        }
        return b.build();
    }
}
