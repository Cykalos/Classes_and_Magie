package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu principal : choix d'une classe de base.
 */
public final class ClassSelectGui extends Gui {

    public ClassSelectGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, rows(plugin), plugin.messages().get("gui.class-title"));
    }

    private static int rows(CykClasses plugin) {
        int count = plugin.classes().baseClasses().size();
        return Math.max(3, Math.min(6, (int) Math.ceil((count + 2) / 7.0) + 2));
    }

    @Override
    protected void build() {
        PlayerProfile profile = plugin.profiles().get(viewer);
        List<RpgClass> classes = new ArrayList<>(plugin.classes().baseClasses());
        int[] slots = centeredSlots(classes.size());
        for (int i = 0; i < classes.size() && i < slots.length; i++) {
            RpgClass rc = classes.get(i);
            boolean current = rc.id.equals(profile.getClassId());
            ItemBuilder b = new ItemBuilder(rc.icon).name(rc.name).lore(ClassLore.describe(plugin, rc)).glow(current);
            b.lore(Text.item(""));
            if (current) {
                b.lore(plugin.messages().item("gui.current"));
            } else if (!profile.hasClass()) {
                b.lore(plugin.messages().item("gui.click-choose"));
            }
            set(slots[i], b.build(), e -> {
                if (!plugin.profiles().get(viewer).hasClass()) {
                    if (plugin.classService().choose(viewer, rc)) {
                        viewer.closeInventory();
                    }
                }
            });
        }
        int last = getInventory().getSize() - 9;
        if (profile.hasClass()) {
            set(last + 3, new ItemBuilder(Material.NETHER_STAR).name(plugin.messages().raw("gui.subclasses-button")).build(),
                    e -> new SubclassGui(plugin, viewer).open());
            set(last + 5, new ItemBuilder(Material.ENCHANTED_BOOK).name(plugin.messages().raw("gui.skills-button")).build(),
                    e -> new SkillsGui(plugin, viewer).open());
        }
        set(last + 8, new ItemBuilder(Material.BARRIER).name(plugin.messages().raw("gui.close")).build(), e -> viewer.closeInventory());
        fill();
    }

    /** Répartit les icônes au centre des lignes intérieures. */
    private int[] centeredSlots(int count) {
        int rows = getInventory().getSize() / 9;
        List<Integer> out = new ArrayList<>();
        int remaining = count;
        for (int row = 1; row < rows - 1 && remaining > 0; row++) {
            int inRow = Math.min(7, remaining);
            int start = row * 9 + (9 - (inRow * 2 - 1 > 9 ? inRow : inRow * 2 - 1)) / 2;
            boolean spaced = inRow * 2 - 1 <= 9;
            for (int j = 0; j < inRow; j++) {
                out.add(start + (spaced ? j * 2 : j));
            }
            remaining -= inRow;
        }
        return out.stream().mapToInt(Integer::intValue).toArray();
    }
}
