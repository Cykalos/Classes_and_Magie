package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.Job;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** Choix de la spécialisation (sous-métier) du métier actuel. */
public final class SpecGui extends Gui {

    public SpecGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, 4, plugin.messages().get("spec.gui-title"));
    }

    @Override
    protected void build() {
        PlayerProfile p = plugin.profiles().get(viewer);
        Job job = plugin.content().job(p.getJobId());
        if (job == null) {
            viewer.closeInventory();
            return;
        }
        Job.Specialization current = plugin.jobs().spec(p);
        int level = plugin.jobs().level(p);
        List<Job.Specialization> specs = new ArrayList<>(job.specializations().values());
        int[] slots = switch (specs.size()) {
            case 1 -> new int[]{13};
            case 2 -> new int[]{11, 15};
            case 3 -> new int[]{11, 13, 15};
            default -> new int[]{10, 12, 14, 16, 19, 21, 23, 25};
        };
        for (int i = 0; i < specs.size() && i < slots.length; i++) {
            Job.Specialization sp = specs.get(i);
            boolean chosen = current != null && current.id().equals(sp.id());
            boolean unlocked = level >= sp.minLevel();
            ItemBuilder b = new ItemBuilder(unlocked || chosen ? sp.icon() : Material.GRAY_DYE)
                    .name(sp.name()).lore(Text.itemLore(sp.description())).glow(chosen);
            b.lore("").lore(plugin.messages().raw("spec.bonuses"));
            sp.modifiers().describe().forEach(l -> b.lore("<gray>• " + l));
            sp.effects().forEach(ef -> b.lore("<gray>• <aqua>" + ef.type().getKey().getKey().replace('_', ' ') + " " + (ef.amplifier() + 1)));
            for (Job.Bonus bo : sp.bonuses()) {
                String on = bo.targets().isEmpty() ? plugin.messages().raw("spec.on-all") : bo.targets().stream().limit(3)
                        .map(pt -> pt.pattern().replace(".*", "*").replace("\\.", ".").toLowerCase(java.util.Locale.ROOT))
                        .reduce((a, c) -> a + ", " + c).orElse("");
                if (bo.rarityLevels() != 0) b.lore("<gray>• <green>+" + bo.rarityLevels() + " " + plugin.messages().raw("spec.rarity") + " <dark_gray>(" + on + ")");
                if (bo.power() != 0) b.lore("<gray>• <green>+" + Math.round(bo.power() * 100) + "% " + plugin.messages().raw("spec.power") + " <dark_gray>(" + on + ")");
                if (bo.purity() != 0) b.lore("<gray>• <green>+" + Math.round(bo.purity()) + " " + plugin.messages().raw("spec.purity"));
                if (bo.gemChance() != 0) b.lore("<gray>• <green>+" + Text.format(bo.gemChance() * 100) + "% " + plugin.messages().raw("spec.gem-chance"));
                if (bo.xpMultiplier() != 1) b.lore("<gray>• <green>x" + Text.format(bo.xpMultiplier()) + " " + plugin.messages().raw("spec.xp") + " <dark_gray>(" + on + ")");
            }
            b.lore("");
            if (chosen) {
                b.lore(plugin.messages().raw("spec.current"));
            } else if (current != null) {
                b.lore(plugin.messages().raw("spec.locked-choice"));
            } else if (unlocked) {
                b.lore(plugin.messages().raw("gui.click-choose"));
            } else {
                b.lore(plugin.messages().raw("spec.need-level").replace("<level>", String.valueOf(sp.minLevel())));
            }
            set(slots[i], b.build(), e -> {
                if (plugin.jobs().chooseSpec(viewer, sp)) {
                    open();
                }
            });
        }
        set(27, new ItemBuilder(Material.ARROW).name(plugin.messages().raw("gui.back")).build(), e -> new JobGui(plugin, viewer).open());
        set(35, new ItemBuilder(Material.BARRIER).name(plugin.messages().raw("gui.close")).build(), e -> viewer.closeInventory());
        frame();
    }
}
