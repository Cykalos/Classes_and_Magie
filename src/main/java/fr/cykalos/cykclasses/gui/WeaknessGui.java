package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.Weakness;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** Faiblesses du personnage : imposées (race, classe) et tirées au hasard, avec relance. */
public final class WeaknessGui extends Gui {

    public WeaknessGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, 4, plugin.messages().get("weaknesses.gui-title"));
    }

    @Override
    protected void build() {
        PlayerProfile p = plugin.profiles().get(viewer);
        List<Weakness> imposed = plugin.character().predefinedOf(p);
        List<Weakness> rolled = new ArrayList<>(plugin.character().rolledOf(p));
        rolled.removeAll(imposed);

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int i = 0;
        for (Weakness w : imposed) {
            if (i < slots.length) {
                set(slots[i++], icon(w, plugin.messages().raw("weaknesses.imposed")), null);
            }
        }
        for (Weakness w : rolled) {
            if (i < slots.length) {
                set(slots[i++], icon(w, plugin.messages().raw("weaknesses.rolled")), null);
            }
        }
        if (i == 0) {
            set(13, new ItemBuilder(Material.TOTEM_OF_UNDYING).name(plugin.messages().raw("weaknesses.none")).build(), null);
        }

        if (plugin.character().weaknessRollEnabled()) {
            int left = plugin.character().weaknessRerollsLeft(p);
            set(30, new ItemBuilder(left > 0 ? Material.ENDER_EYE : Material.ENDER_PEARL)
                    .name(plugin.messages().raw("weaknesses.reroll").replace("<left>", String.valueOf(left)))
                    .lore(plugin.messages().raw("weaknesses.reroll-desc"))
                    .build(), e -> {
                plugin.character().rerollWeaknesses(viewer);
                open();
            });
        }
        set(32, new ItemBuilder(Material.LIME_CONCRETE).name(plugin.messages().raw("weaknesses.keep")).build(), e -> {
            viewer.closeInventory();
            plugin.character().continueCreation(viewer);
        });
        frame();
    }

    private org.bukkit.inventory.ItemStack icon(Weakness w, String origin) {
        ItemBuilder b = new ItemBuilder(w.icon()).name(w.name());
        if (!w.severity().isEmpty()) {
            b.lore(w.severity());
        }
        b.lore(Text.itemLore(w.description()));
        List<String> mods = w.modifiers().describe();
        if (!mods.isEmpty() || !w.flaws().isEmpty() || !w.effects().isEmpty()) {
            b.lore("");
            mods.forEach(l -> b.lore("<gray>• " + l));
            w.effects().forEach(e -> b.lore("<gray>• <aqua>" + e.type().getKey().getKey().replace('_', ' ') + " " + (e.amplifier() + 1)));
            for (Flaw f : w.flaws()) {
                b.lore("<red>• </red><gray>" + f.description);
            }
        }
        b.lore("").lore(origin);
        return b.build();
    }
}
