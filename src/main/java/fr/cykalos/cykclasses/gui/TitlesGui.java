package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.quest.Objective;
import fr.cykalos.cykclasses.title.PlayerTitle;
import fr.cykalos.cykclasses.title.TitleService;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Collection de titres : débloqués et à débloquer, avec les conditions et les effets.
 * Clic sur un titre débloqué : l'équiper (ou le retirer s'il est déjà équipé).
 */
public final class TitlesGui extends Gui {

    private static final int[] AREA = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    /** 0 = tous, 1 = débloqués, 2 = à débloquer */
    private final int filter;
    private final int page;

    public TitlesGui(CykClasses plugin, Player viewer) {
        this(plugin, viewer, 0, 0);
    }

    private TitlesGui(CykClasses plugin, Player viewer, int filter, int page) {
        super(plugin, viewer, 6, plugin.messages().get("titles.gui.title"));
        this.filter = filter;
        this.page = Math.max(0, page);
    }

    @Override
    protected boolean moduleEnabled() {
        return plugin.modules().titles();
    }

    @Override
    protected void build() {
        TitleService ts = plugin.titles();
        PlayerProfile p = plugin.profiles().get(viewer);

        String[] keys = {"all", "unlocked", "locked"};
        Material[] icons = {Material.BOOKSHELF, Material.NAME_TAG, Material.GRAY_DYE};
        for (int i = 0; i < 3; i++) {
            final int f = i;
            set(2 + i * 2, new ItemBuilder(icons[i]).name(plugin.messages().item("titles.gui.filter-" + keys[i]))
                    .glow(i == filter).build(), e -> new TitlesGui(plugin, viewer, f, 0).open());
        }

        List<PlayerTitle> list = new ArrayList<>();
        for (PlayerTitle t : ts.all()) {
            boolean owned = p.hasTitle(t.id());
            if ((filter == 1 && !owned) || (filter == 2 && owned)) {
                continue;
            }
            list.add(t);
        }
        int pages = Math.max(1, (list.size() + AREA.length - 1) / AREA.length);
        int current = Math.min(page, pages - 1);
        for (int i = 0; i < AREA.length; i++) {
            int index = current * AREA.length + i;
            if (index >= list.size()) {
                break;
            }
            PlayerTitle t = list.get(index);
            set(AREA[i], item(ts, p, t), e -> {
                if (!p.hasTitle(t.id())) {
                    return;
                }
                ts.equip(viewer, t.id().equals(p.getEquippedTitle()) ? null : t.id(), true);
                open();
            });
        }
        if (list.isEmpty()) {
            set(22, new ItemBuilder(Material.PAPER).name(plugin.messages().item("titles.gui.empty")).build(), null);
        }
        if (current > 0) {
            set(46, new ItemBuilder(Material.ARROW).name(plugin.messages().item("quests.gui.previous")).build(),
                    e -> new TitlesGui(plugin, viewer, filter, current - 1).open());
        }
        if (current < pages - 1) {
            set(52, new ItemBuilder(Material.ARROW).name(plugin.messages().item("quests.gui.next")).build(),
                    e -> new TitlesGui(plugin, viewer, filter, current + 1).open());
        }
        PlayerTitle eq = ts.equipped(p);
        ItemBuilder current0 = new ItemBuilder(eq == null ? Material.BARRIER : Material.NAME_TAG)
                .name(plugin.messages().item("titles.gui.equipped-slot"))
                .lore(eq == null ? plugin.messages().item("titles.gui.none") : Text.item(eq.name()));
        if (eq != null) {
            current0.lore(plugin.messages().item("titles.gui.click-unequip"));
        }
        set(49, current0.build(), eq == null ? null : e -> {
            ts.equip(viewer, null, true);
            open();
        });
        set(48, new ItemBuilder(Material.KNOWLEDGE_BOOK).name(plugin.messages().item("titles.gui.summary"))
                .lore(plugin.messages().item("titles.gui.summary-lore", Text.ph("count", p.titles().size()),
                        Text.ph("total", ts.all().size()))).build(), null);
        frame();
    }

    private org.bukkit.inventory.ItemStack item(TitleService ts, PlayerProfile p, PlayerTitle t) {
        boolean owned = p.hasTitle(t.id());
        boolean equipped = t.id().equals(p.getEquippedTitle()) && owned;
        TitleService.Rarity r = ts.rarity(t);
        if (!owned && t.hidden()) {
            ItemBuilder b = new ItemBuilder(Material.GRAY_DYE).name(plugin.messages().item("titles.gui.hidden-name"))
                    .lore(Text.item(r.label()))
                    .lore(plugin.messages().item("titles.gui.hidden-lore"));
            return b.build();
        }
        ItemBuilder b = new ItemBuilder(owned ? t.icon() : Material.GRAY_DYE)
                .name(Text.item(plugin.messages().raw(equipped ? "titles.gui.name-equipped" : owned ? "titles.gui.name" : "titles.gui.name-locked"),
                        Text.ph("title", t.name())))
                .glow(equipped)
                .lore(Text.item(r.label()));
        if (!t.description().isEmpty()) {
            b.lore(Text.itemLore(t.description()));
        }
        b.lore(Component.empty());
        if (t.hasEffects()) {
            b.lore(plugin.messages().item("titles.gui.effects"));
            t.modifiers().describe().forEach(l -> b.lore(Text.item("<gray> • " + l)));
            t.effects().forEach(e -> b.lore(Text.item("<gray> • <aqua>" + e.type().getKey().getKey().replace('_', ' ') + " " + (e.amplifier() + 1))));
            for (Flaw f : t.flaws()) {
                if (f.description != null && !f.description.isBlank()) {
                    b.lore(Text.item("<gray> • " + f.description));
                }
            }
        } else {
            b.lore(plugin.messages().item("titles.gui.no-effect"));
        }
        if (t.unique()) {
            UUID owner = ts.uniqueOwner(t);
            if (owner == null) {
                b.lore(plugin.messages().item("titles.gui.unique-free"));
            } else if (!owner.equals(viewer.getUniqueId())) {
                String n = Bukkit.getOfflinePlayer(owner).getName();
                b.lore(plugin.messages().item("titles.gui.unique-owned", Text.ph("player", n == null ? "?" : n)));
            }
        }
        b.lore(Component.empty());
        if (owned) {
            b.lore(plugin.messages().item(equipped ? "titles.gui.click-unequip" : "titles.gui.click-equip"));
        } else {
            if (t.unlock().isEmpty()) {
                b.lore(plugin.messages().item("titles.gui.special"));
            }
            for (Component c : plugin.quests().missingRequirements(viewer, p, t.requirements())) {
                b.lore(Text.item(plugin.messages().raw("quests.gui.requirement"), Text.phc("requirement", c)));
            }
            for (int i = 0; i < t.unlock().size(); i++) {
                Objective o = t.unlock().get(i);
                double v = ts.value(viewer, p, t, i);
                boolean done = v >= o.amount;
                b.lore(Text.item(plugin.messages().raw(done ? "quests.gui.objective-done" : "quests.gui.objective"),
                        Text.ph("objective", plugin.quests().objectiveText(o)),
                        Text.ph("progress", plugin.quests().progressText(v, o.amount))));
            }
        }
        return b.build();
    }
}
