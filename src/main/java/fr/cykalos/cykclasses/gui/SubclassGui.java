package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.Requirement;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sous-classes de la classe du joueur, avec la progression de chaque condition.
 */
public final class SubclassGui extends Gui {

    public SubclassGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, 4, plugin.messages().get("gui.subclass-title"));
    }

    @Override
    protected void build() {
        PlayerProfile profile = plugin.profiles().get(viewer);
        RpgClass base = plugin.classes().get(profile.getClassId());
        if (base == null) {
            viewer.closeInventory();
            return;
        }
        List<RpgClass> subs = new ArrayList<>(base.subclasses().values());
        int[] slots = switch (subs.size()) {
            case 0 -> new int[0];
            case 1 -> new int[]{13};
            case 2 -> new int[]{11, 15};
            case 3 -> new int[]{11, 13, 15};
            case 4 -> new int[]{10, 12, 14, 16};
            default -> new int[]{9, 11, 13, 15, 17, 19, 21, 23, 25};
        };
        if (subs.isEmpty()) {
            set(13, new ItemBuilder(Material.PAPER).name(plugin.messages().raw("no-subclasses")).build(), null);
        }
        for (int i = 0; i < subs.size() && i < slots.length; i++) {
            RpgClass sub = subs.get(i);
            boolean current = sub.id.equals(profile.getSubclassId());
            boolean met = true;
            ItemBuilder b = new ItemBuilder(sub.icon).name(sub.name).lore(ClassLore.describe(plugin, sub));
            if (!sub.requirements().isEmpty()) {
                b.lore(Text.item(""));
                b.lore(plugin.messages().item("gui.requirements-header"));
                for (Requirement r : sub.requirements()) {
                    boolean ok = r.isMet(profile, viewer);
                    met &= ok;
                    if (ok) {
                        b.lore(plugin.messages().item("gui.requirement-done", Text.ph("desc", r.description)));
                    } else {
                        b.lore(plugin.messages().item("gui.requirement-todo", Text.ph("desc", r.description),
                                Text.ph("progress", Text.format(Math.floor(r.progress(profile, viewer)))),
                                Text.ph("goal", Text.format(r.goal()))));
                    }
                }
            }
            b.lore(Text.item(""));
            if (current) {
                b.lore(plugin.messages().item("gui.current")).glow(true);
            } else if (profile.getSubclassId() == null) {
                b.lore(plugin.messages().item(met ? "gui.click-choose" : "gui.click-locked")).glow(met);
            }
            set(slots[i], b.build(), e -> {
                if (plugin.classService().chooseSubclass(viewer, sub)) {
                    viewer.closeInventory();
                }
            });
        }
        set(27, new ItemBuilder(Material.ARROW).name(plugin.messages().raw("gui.back")).build(),
                e -> new ClassSelectGui(plugin, viewer).open());
        set(35, new ItemBuilder(Material.BARRIER).name(plugin.messages().raw("gui.close")).build(), e -> viewer.closeInventory());
        fill();
    }
}
