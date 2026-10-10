package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.party.Party;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Choix des règles du groupe avant sa création (elles ne pourront plus être changées).
 */
public final class PartyCreateGui extends Gui {

    private Party.XpMode mode;
    private boolean friendlyFire;

    public PartyCreateGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, 3, plugin.messages().get("party.gui-title"));
        this.mode = "EQUAL".equalsIgnoreCase(plugin.getConfig().getString("party.default-xp-mode", "SHARED"))
                ? Party.XpMode.EQUAL : Party.XpMode.SHARED;
        this.friendlyFire = plugin.getConfig().getBoolean("party.default-friendly-fire", true);
    }

    @Override
    protected void build() {
        double bonus = plugin.getConfig().getDouble("party.shared-bonus-per-member", 0.1);
        boolean shared = mode == Party.XpMode.SHARED;
        set(11, new ItemBuilder(shared ? Material.EXPERIENCE_BOTTLE : Material.ENCHANTED_BOOK)
                .name(plugin.messages().raw("party.gui-xp-name") + " <white>" + plugin.parties().modeName(mode))
                .lore(plugin.messages().raw(shared ? "party.gui-xp-shared-desc" : "party.gui-xp-equal-desc")
                        .replace("<bonus>", String.valueOf(Math.round(bonus * 100))))
                .lore("")
                .lore(plugin.messages().raw("party.gui-click-toggle"))
                .glow(true)
                .build(), e -> {
            mode = shared ? Party.XpMode.EQUAL : Party.XpMode.SHARED;
            open();
        });
        set(13, new ItemBuilder(friendlyFire ? Material.IRON_SWORD : Material.SHIELD)
                .name(plugin.messages().raw("party.gui-ff-name") + " <white>" + plugin.parties().ffName(friendlyFire))
                .lore(plugin.messages().raw(friendlyFire ? "party.gui-ff-on-desc" : "party.gui-ff-off-desc"))
                .lore(plugin.messages().raw("party.gui-support-desc"))
                .lore("")
                .lore(plugin.messages().raw("party.gui-click-toggle"))
                .build(), e -> {
            friendlyFire = !friendlyFire;
            open();
        });
        set(15, new ItemBuilder(Material.LIME_CONCRETE)
                .name(plugin.messages().raw("party.gui-create"))
                .lore(plugin.messages().raw("party.gui-create-desc"))
                .lore(Text.item("<gray>Max. <white>" + plugin.parties().maxSize() + "</white> joueurs"))
                .build(), e -> {
            viewer.closeInventory();
            plugin.parties().create(viewer, mode, friendlyFire);
        });
        set(26, new ItemBuilder(Material.BARRIER).name(plugin.messages().raw("gui.close")).build(), e -> viewer.closeInventory());
        frame();
    }
}
