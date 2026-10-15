package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.Trait;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** Traits tirés au hasard, avec possibilité de relancer. */
public final class TraitsGui extends Gui {

    @Override
    protected boolean moduleEnabled() {
        return plugin.modules().traits();
    }

    @Override
    public boolean isCreationStep() {
        return !plugin.profiles().get(viewer).isTraitsConfirmed();
    }

    public TraitsGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, 3, plugin.messages().get("traits.gui-title"));
    }

    @Override
    protected void build() {
        PlayerProfile p = plugin.profiles().get(viewer);
        List<Trait> traits = new ArrayList<>();
        for (String id : p.getTraits()) {
            Trait t = plugin.content().trait(id);
            if (t != null) {
                traits.add(t);
            }
        }
        int[] slots = switch (traits.size()) {
            case 0 -> new int[0];
            case 1 -> new int[]{13};
            case 2 -> new int[]{12, 14};
            case 3 -> new int[]{11, 13, 15};
            case 4 -> new int[]{10, 12, 14, 16};
            default -> new int[]{9, 10, 11, 12, 13, 14, 15, 16, 17};
        };
        if (traits.isEmpty()) {
            set(13, new ItemBuilder(Material.PAPER).name(plugin.messages().raw("traits.none")).build(), null);
        }
        for (int i = 0; i < traits.size() && i < slots.length; i++) {
            Trait t = traits.get(i);
            Material icon = switch (t.kind()) {
                case POSITIVE -> t.unique() ? Material.NETHER_STAR : Material.LIME_DYE;
                case NEGATIVE -> Material.RED_DYE;
                case MIXED -> Material.ORANGE_DYE;
            };
            ItemBuilder b = new ItemBuilder(icon).name(t.name()).glow(t.unique() || t.rarity() == Trait.Rarity.EPIC)
                    .lore(t.rarity().label).lore(Text.itemLore(t.description()));
            List<String> mods = t.modifiers().describe();
            if (!mods.isEmpty() || !t.flaws().isEmpty() || !t.effects().isEmpty()) {
                b.lore("");
                mods.forEach(l -> b.lore("<gray>• " + l));
                t.effects().forEach(e -> b.lore("<gray>• <aqua>" + e.type().getKey().getKey().replace('_', ' ') + " " + (e.amplifier() + 1)));
                for (Flaw f : t.flaws()) {
                    b.lore("<red>• </red><gray>" + f.description);
                }
            }
            set(slots[i], b.build(), null);
        }
        int left = plugin.character().rerollsLeft(p);
        set(21, new ItemBuilder(left > 0 ? Material.ENDER_EYE : Material.ENDER_PEARL)
                .name(plugin.messages().raw("traits.reroll").replace("<left>", String.valueOf(left)))
                .lore(plugin.messages().raw("traits.reroll-desc"))
                .build(), e -> {
            plugin.character().rerollTraits(viewer);
            open();
        });
        set(23, new ItemBuilder(Material.LIME_CONCRETE).name(plugin.messages().raw("traits.keep")).build(), e -> {
            p.setTraitsConfirmed(true);
            viewer.closeInventory();
            plugin.character().continueCreation(viewer);
        });
        frame();
    }
}
