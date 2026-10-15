package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.Job;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/** Choix du métier (un seul). */
public final class JobGui extends Gui {

    @Override
    protected boolean moduleEnabled() {
        return plugin.modules().jobs();
    }

    /** Fermer le menu pendant la création = « choisir plus tard ». */
    @Override
    public void onPlayerClose() {
        var p = plugin.profiles().get(viewer);
        if (p.getJobId() == null && !p.isJobPromptDone()) {
            p.setJobPromptDone(true);
            plugin.messages().send(viewer, "job.later-info");
        }
    }

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
            int level = p.getJobLevel(job.id());
            b.lore("").lore(plugin.messages().raw("job.level-line").replace("<level>", String.valueOf(level))
                    .replace("<max>", String.valueOf(plugin.jobs().maxLevel())));
            var rules = plugin.content().jobRules().stream().filter(r -> r.jobs().contains(job.id())).toList();
            if (!rules.isEmpty()) {
                b.lore("").lore(plugin.messages().raw("job.exclusive").replace("<count>", String.valueOf(rules.size())));
                rules.stream().limit(8).forEach(r -> {
                    String lvl = r.minLevel() > 1 ? (level >= r.minLevel() ? " <green>(niv. " : " <red>(niv. ") + r.minLevel() + ")" : "";
                    b.lore("<gray>• " + plugin.jobDescriptions().describe(r) + lvl);
                });
            }
            if (plugin.gear().isQualityCraft(job.id(), Material.IRON_SWORD) || plugin.gear().isQualityCraft(job.id(), Material.IRON_CHESTPLATE)) {
                b.lore(plugin.messages().raw("job.quality-line"));
            }
            b.lore("").lore(plugin.messages().raw(current ? "job.current" : "gui.click-choose"));
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
        var currentJob = plugin.content().job(p.getJobId());
        if (currentJob != null && !currentJob.specializations().isEmpty()) {
            var spec = plugin.jobs().spec(p);
            int minLvl = currentJob.specializations().values().stream().mapToInt(sp -> sp.minLevel()).min().orElse(20);
            boolean ready = plugin.jobs().level(p) >= minLvl;
            set(last + 6, new ItemBuilder(spec != null ? spec.icon() : Material.NETHER_STAR)
                    .name(plugin.messages().raw("spec.button") + (spec == null ? "" : " <white>" + Text.plain(spec.name())))
                    .lore(spec != null ? plugin.messages().raw("spec.button-chosen")
                            : ready ? plugin.messages().raw("spec.button-ready")
                            : plugin.messages().raw("spec.button-locked").replace("<level>", String.valueOf(minLvl)))
                    .glow(spec == null && ready).build(), e -> new SpecGui(plugin, viewer).open());
        }
        set(last + 8, new ItemBuilder(Material.BARRIER).name(plugin.messages().raw("gui.close")).build(), e -> viewer.closeInventory());
        frame();
    }
}
