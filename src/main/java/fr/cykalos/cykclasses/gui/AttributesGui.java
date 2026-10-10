package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.AttributeDef;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.ClassStats;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** Répartition des points d'attributs. Clic = +1, clic avec Maj = +5. */
public final class AttributesGui extends Gui {

    public AttributesGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, 4, plugin.messages().get("attributes.gui-title"));
    }

    @Override
    protected void build() {
        PlayerProfile p = plugin.profiles().get(viewer);
        List<AttributeDef> attrs = new ArrayList<>(plugin.content().attributes());
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        for (int i = 0; i < attrs.size() && i < slots.length; i++) {
            AttributeDef a = attrs.get(i);
            int value = p.getAttribute(a.id());
            ItemBuilder b = new ItemBuilder(a.icon())
                    .name(a.name() + " <gray>[<white>" + value + "</white>/" + a.max() + "]")
                    .lore(Text.itemLore(a.description()))
                    .lore("")
                    .lore(plugin.messages().raw("attributes.per-point"));
            a.perPoint().describe().forEach(l -> b.lore("<gray>• " + l));
            b.lore("").lore(plugin.messages().raw("attributes.click"));
            b.glow(value > 0);
            set(slots[i], b.build(), e -> {
                plugin.character().addAttribute(viewer, a, e.isShiftClick() ? 5 : 1);
                open();
            });
        }
        ClassStats s = plugin.stats().stats(p);
        ItemBuilder summary = new ItemBuilder(Material.BOOK)
                .name(plugin.messages().raw("attributes.points").replace("<points>", String.valueOf(plugin.stats().availableAttributePoints(p))))
                .lore(plugin.messages().raw("attributes.points-desc")
                        .replace("<per_level>", String.valueOf(plugin.getConfig().getInt("progression.attribute-points-per-level", 2))))
                .lore("")
                .lore(plugin.messages().raw("gui.stats-header"))
                .lore("<gray>• PV max : <white>" + Text.format(Math.ceil(20 + s.bonusHealth(p.getLevel()))))
                .lore("<gray>• Mana max : <aqua>" + Text.format(Math.round(s.maxMana(p.getLevel()))) + " <gray>(+" + Text.format(s.regen(p.getLevel())) + "/s)")
                .lore("<gray>• Mêlée <white>" + Text.percent(s.meleeDamage) + "</white> · Distance <white>" + Text.percent(s.rangedDamage)
                        + "</white> · Magie <white>" + Text.percent(s.magicDamage))
                .lore("<gray>• Dégâts subis <white>" + Text.percent(s.damageTaken) + "</white> · Critique <white>"
                        + Math.round(s.critChance * 100) + "% <gray>(x" + Text.format(s.critMultiplier) + ")")
                .lore("<gray>• Armure <white>+" + Text.format(s.armor) + "</white> · Vitesse <white>" + Text.percent(1 + s.speed));
        set(31, summary.build(), null);
        set(27, new ItemBuilder(Material.ARROW).name(plugin.messages().raw("gui.back")).build(), e -> new ClassSelectGui(plugin, viewer).open());
        set(29, new ItemBuilder(Material.TNT).name(plugin.messages().raw("attributes.reset-button"))
                .lore(plugin.messages().raw("tree.reset-desc")
                        .replace("<hours>", String.valueOf(plugin.getConfig().getLong("progression.attribute-reset-cooldown-hours", 24))))
                .build(), e -> {
            if (e.isShiftClick()) {
                plugin.character().resetAttributes(viewer, false);
                open();
            }
        });
        set(35, new ItemBuilder(Material.BARRIER).name(plugin.messages().raw("gui.close")).build(), e -> viewer.closeInventory());
        frame();
    }
}
