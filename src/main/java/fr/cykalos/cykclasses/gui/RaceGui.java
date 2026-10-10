package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.Race;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** Choix de la race (après la classe). Affiche bonus, défauts et l'adéquation avec la classe choisie. */
public final class RaceGui extends Gui {

    public RaceGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, rows(plugin), plugin.messages().get("race.gui-title"));
    }

    private static int rows(CykClasses plugin) {
        return Math.max(3, Math.min(6, (int) Math.ceil(plugin.content().races().size() / 7.0) + 2));
    }

    @Override
    protected void build() {
        PlayerProfile p = plugin.profiles().get(viewer);
        RpgClass rc = plugin.classes().effective(p);
        List<Race> races = new ArrayList<>(plugin.content().races());
        int slot = 10;
        for (Race race : races) {
            if (slot % 9 == 8) {
                slot += 2;
            }
            if (slot >= getInventory().getSize() - 9) {
                break;
            }
            boolean current = race.id().equals(p.getRaceId());
            ItemBuilder b = new ItemBuilder(race.icon()).name(race.name()).lore(Text.itemLore(race.description())).glow(current);
            List<String> bonus = race.modifiers().describe();
            if (!bonus.isEmpty()) {
                b.lore("").lore(plugin.messages().raw("gui.stats-header"));
                bonus.forEach(l -> b.lore("<gray>• " + l));
            }
            if (!race.effects().isEmpty()) {
                race.effects().forEach(e -> b.lore("<gray>• <aqua>" + e.type().getKey().getKey().replace('_', ' ') + " " + (e.amplifier() + 1)));
            }
            if (!race.flaws().isEmpty()) {
                b.lore("").lore(plugin.messages().raw("gui.flaws-header"));
                for (Flaw f : race.flaws()) {
                    b.lore("<red>• </red><gray>" + f.description);
                }
            }
            b.lore("");
            Race.Affinity pen = race.penaltyFor(p.getClassId(), p.getSubclassId());
            if (rc != null && pen != null) {
                b.lore(plugin.messages().raw("race.unsuited").replace("<class>", Text.plain(rc.name)));
                b.lore("<red>  " + pen.description());
                pen.modifiers().describe().forEach(l -> b.lore("<gray>  • " + l));
                pen.flaws().forEach(f -> b.lore("<red>  • </red><gray>" + f.description));
            } else if (rc != null && race.recommended().contains(rc.root().id)) {
                b.lore(plugin.messages().raw("race.recommended").replace("<class>", Text.plain(rc.root().name)));
            }
            b.lore("");
            b.lore(plugin.messages().raw(current ? "gui.current" : "gui.click-choose"));
            set(slot++, b.build(), e -> {
                if (plugin.character().chooseRace(viewer, race)) {
                    viewer.closeInventory();
                }
            });
        }
        set(getInventory().getSize() - 1, new ItemBuilder(Material.BARRIER).name(plugin.messages().raw("gui.close")).build(),
                e -> viewer.closeInventory());
        fill();
    }
}
