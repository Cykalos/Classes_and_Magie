package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.Job;
import fr.cykalos.cykclasses.content.JobRule;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/** Choix du métier (un seul). */
public final class JobGui extends Gui {

    public JobGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, Math.max(3, Math.min(6, (int) Math.ceil(plugin.content().jobs().size() / 7.0) + 2)),
                plugin.messages().get("job.gui-title"));
    }

    @Override
    protected void build() {
        PlayerProfile p = plugin.profiles().get(viewer);
        int slot = 10;
        for (Job job : plugin.content().jobs()) {
            if (slot % 9 == 8) {
                slot += 2;
            }
            if (slot >= getInventory().getSize() - 9) {
                break;
            }
            boolean current = job.id().equals(p.getJobId());
            ItemBuilder b = new ItemBuilder(job.icon()).name(job.name()).lore(Text.itemLore(job.description())).glow(current);
            List<String> mods = job.modifiers().describe();
            if (!mods.isEmpty() || !job.effects().isEmpty()) {
                b.lore("").lore(plugin.messages().raw("job.perks"));
                mods.forEach(l -> b.lore("<gray>• " + l));
                job.effects().forEach(e -> b.lore("<gray>• <aqua>" + e.type().getKey().getKey().replace('_', ' ') + " " + (e.amplifier() + 1)));
            }
            long exclusive = plugin.content().jobRules().stream().filter(r -> r.jobs().contains(job.id())).count();
            if (exclusive > 0) {
                b.lore("").lore(plugin.messages().raw("job.exclusive").replace("<count>", String.valueOf(exclusive)));
                plugin.content().jobRules().stream().filter(r -> r.jobs().contains(job.id())).limit(6)
                        .forEach(r -> b.lore("<gray>• " + actionName(r)));
            }
            b.lore("").lore(plugin.messages().raw(current ? "gui.current" : "gui.click-choose"));
            set(slot++, b.build(), e -> {
                if (plugin.character().chooseJob(viewer, job)) {
                    viewer.closeInventory();
                }
            });
        }
        int last = getInventory().getSize() - 9;
        if (p.getJobId() == null) {
            set(last + 4, new ItemBuilder(Material.CLOCK).name(plugin.messages().raw("job.later")).build(), e -> {
                p.setJobPromptDone(true);
                viewer.closeInventory();
                plugin.messages().send(viewer, "job.later-info");
            });
        }
        set(last + 8, new ItemBuilder(Material.BARRIER).name(plugin.messages().raw("gui.close")).build(), e -> viewer.closeInventory());
        fill();
    }

    private String actionName(JobRule r) {
        String key = "job.action-" + r.action().name().toLowerCase(java.util.Locale.ROOT);
        String targets = r.targets().isEmpty() ? "" : " <dark_gray>(" + r.targets().stream().limit(3)
                .map(pt -> pt.pattern().replace(".*", "*").replace("\\.", ".").toLowerCase(java.util.Locale.ROOT))
                .reduce((a, c) -> a + ", " + c).orElse("") + (r.targets().size() > 3 ? "…" : "") + ")";
        return plugin.messages().raw(key) + targets;
    }
}
