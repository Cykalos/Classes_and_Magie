package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.Race;
import fr.cykalos.cykclasses.content.Trait;
import fr.cykalos.cykclasses.content.Weakness;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Toutes les faiblesses du personnage au même endroit, regroupées par origine :
 * classe, race, traits, faiblesses imposées et faiblesses tirées au hasard.
 */
public final class WeaknessGui extends Gui {

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    public WeaknessGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, 5, plugin.messages().get("weaknesses.gui-title"));
    }

    @Override
    protected void build() {
        PlayerProfile p = plugin.profiles().get(viewer);
        List<ItemStack> items = new ArrayList<>();

        // Classe
        RpgClass rc = plugin.classes().effective(p);
        if (rc != null && !rc.flaws().isEmpty()) {
            ItemBuilder b = new ItemBuilder(rc.icon).name(rc.name);
            flaws(b, rc.flaws());
            items.add(b.lore("").lore(plugin.messages().raw("weaknesses.from-class")).build());
        }
        // Race (+ malus si la race ne convient pas à la classe)
        Race race = plugin.modules().races() ? plugin.content().race(p.getRaceId()) : null;
        if (race != null) {
            List<Flaw> raceFlaws = new ArrayList<>(race.flaws());
            Race.Affinity pen = race.penaltyFor(p.getClassId(), p.getSubclassId());
            if (pen != null) {
                raceFlaws.addAll(pen.flaws());
            }
            if (!raceFlaws.isEmpty()) {
                ItemBuilder b = new ItemBuilder(race.icon()).name(race.name());
                flaws(b, raceFlaws);
                items.add(b.lore("").lore(plugin.messages().raw("weaknesses.from-race")).build());
            }
        }
        // Traits qui comportent des faiblesses
        if (plugin.modules().traits()) {
            for (String id : p.getTraits()) {
                Trait t = plugin.content().trait(id);
                if (t != null && !t.flaws().isEmpty()) {
                    ItemBuilder b = new ItemBuilder(Material.RED_DYE).name(t.name());
                    flaws(b, t.flaws());
                    items.add(b.lore("").lore(plugin.messages().raw("weaknesses.from-trait")).build());
                }
            }
        }
        // Faiblesses imposées puis tirées
        List<Weakness> imposed = plugin.character().predefinedOf(p);
        List<Weakness> rolled = new ArrayList<>(plugin.character().rolledOf(p));
        rolled.removeAll(imposed);
        imposed.forEach(w -> items.add(icon(w, plugin.messages().raw("weaknesses.imposed"))));
        rolled.forEach(w -> items.add(icon(w, plugin.messages().raw("weaknesses.rolled"))));

        for (int i = 0; i < items.size() && i < SLOTS.length; i++) {
            set(SLOTS[i], items.get(i), null);
        }
        if (items.isEmpty()) {
            set(22, new ItemBuilder(Material.TOTEM_OF_UNDYING).name(plugin.messages().raw("weaknesses.none")).build(), null);
        }

        if (plugin.character().weaknessRollEnabled()) {
            int left = plugin.character().weaknessRerollsLeft(p);
            set(39, new ItemBuilder(left > 0 ? Material.ENDER_EYE : Material.ENDER_PEARL)
                    .name(plugin.messages().raw("weaknesses.reroll").replace("<left>", String.valueOf(left)))
                    .lore(plugin.messages().raw("weaknesses.reroll-desc"))
                    .build(), e -> {
                plugin.character().rerollWeaknesses(viewer);
                open();
            });
        }
        set(41, new ItemBuilder(Material.LIME_CONCRETE).name(plugin.messages().raw("weaknesses.keep")).build(), e -> {
            viewer.closeInventory();
            plugin.character().continueCreation(viewer);
        });
        frame();
    }

    private static void flaws(ItemBuilder b, List<Flaw> flaws) {
        for (Flaw f : flaws) {
            b.lore("<red>• </red><gray>" + f.description);
        }
    }

    private ItemStack icon(Weakness w, String origin) {
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
            flaws(b, w.flaws());
        }
        return b.lore("").lore(origin).build();
    }
}
