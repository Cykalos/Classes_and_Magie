package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.model.SkillDefinition;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Liste des compétences du joueur. Cliquer en lance une.
 */
public final class SkillsGui extends Gui {

    public SkillsGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, 4, title(plugin, viewer));
    }

    private static net.kyori.adventure.text.Component title(CykClasses plugin, Player viewer) {
        RpgClass rc = plugin.classes().effective(plugin.profiles().get(viewer));
        return plugin.messages().get("gui.skills-title", Text.ph("class", rc == null ? "?" : Text.plain(rc.name)));
    }

    @Override
    protected void build() {
        PlayerProfile profile = plugin.profiles().get(viewer);
        RpgClass rc = plugin.classes().effective(profile);
        if (rc == null) {
            viewer.closeInventory();
            return;
        }
        List<SkillDefinition> skills = new ArrayList<>(rc.skills().values());
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        for (int i = 0; i < skills.size() && i < slots.length; i++) {
            SkillDefinition s = skills.get(i);
            boolean unlocked = profile.getLevel() >= s.unlockLevel;
            ItemBuilder b = new ItemBuilder(unlocked ? s.icon : Material.GRAY_DYE)
                    .name("<white>" + (i + 1) + ". </white>" + s.name)
                    .lore(Text.itemLore(s.description))
                    .lore(Text.item(""));
            if (s.manaCost > 0) {
                b.lore(plugin.messages().item("gui.skill-mana", Text.ph("mana", Text.format(s.manaCost))));
            }
            if (s.healthCost > 0) {
                b.lore(plugin.messages().item("gui.skill-health", Text.ph("health", Text.format(s.healthCost / 2) + "❤")));
            }
            b.lore(plugin.messages().item("gui.skill-cooldown", Text.ph("cooldown", Text.format(s.cooldown))));
            b.lore(plugin.messages().item(unlocked ? "gui.skill-unlock" : "gui.skill-locked", Text.ph("level", s.unlockLevel)));
            if (unlocked) {
                b.lore(plugin.messages().item("gui.skill-click"));
            }
            set(slots[i], b.build(), e -> {
                viewer.closeInventory();
                plugin.skills().cast(viewer, s);
            });
        }
        set(27, new ItemBuilder(Material.ARROW).name(plugin.messages().raw("gui.back")).build(),
                e -> new ClassSelectGui(plugin, viewer).open());
        set(35, new ItemBuilder(Material.BARRIER).name(plugin.messages().raw("gui.close")).build(), e -> viewer.closeInventory());
        frame();
    }
}
