package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu principal : choix d'une classe de base.
 */
public final class ClassSelectGui extends Gui {

    public ClassSelectGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, rows(plugin), plugin.messages().get("gui.class-title"));
    }

    private static int rows(CykClasses plugin) {
        int count = plugin.classes().baseClasses().size();
        return Math.max(3, Math.min(6, (int) Math.ceil((count + 2) / 7.0) + 2));
    }

    @Override
    protected void build() {
        PlayerProfile profile = plugin.profiles().get(viewer);
        List<RpgClass> classes = new ArrayList<>(plugin.classes().baseClasses());
        int[] slots = centeredSlots(classes.size());
        for (int i = 0; i < classes.size() && i < slots.length; i++) {
            RpgClass rc = classes.get(i);
            boolean current = rc.id.equals(profile.getClassId());
            ItemBuilder b = new ItemBuilder(rc.icon).name(rc.name).lore(ClassLore.describe(plugin, rc)).glow(current);
            b.lore(Text.item(""));
            if (current) {
                b.lore(plugin.messages().item("gui.current"));
            } else if (!profile.hasClass()) {
                b.lore(plugin.messages().item("gui.click-choose"));
            }
            set(slots[i], b.build(), e -> {
                if (!plugin.profiles().get(viewer).hasClass()) {
                    if (plugin.classService().choose(viewer, rc)) {
                        viewer.closeInventory();
                    }
                }
            });
        }
        int last = getInventory().getSize() - 9;
        if (profile.hasClass()) {
            set(4, profileHead(profile), null);
            set(last, new ItemBuilder(Material.OAK_SAPLING).name(plugin.messages().raw("gui.tree-button"))
                    .lore(plugin.messages().raw("gui.points-line").replace("<points>", String.valueOf(plugin.stats().availableSkillPoints(profile))))
                    .glow(plugin.stats().availableSkillPoints(profile) > 0).build(),
                    e -> new SkillTreeGui(plugin, viewer, 0).open());
            set(last + 1, new ItemBuilder(Material.ANVIL).name(plugin.messages().raw("gui.attributes-button"))
                    .lore(plugin.messages().raw("gui.points-line").replace("<points>", String.valueOf(plugin.stats().availableAttributePoints(profile))))
                    .glow(plugin.stats().availableAttributePoints(profile) > 0).build(),
                    e -> new AttributesGui(plugin, viewer).open());
            set(last + 2, new ItemBuilder(Material.NETHER_STAR).name(plugin.messages().raw("gui.subclasses-button")).build(),
                    e -> new SubclassGui(plugin, viewer).open());
            set(last + 3, new ItemBuilder(Material.ENCHANTED_BOOK).name(plugin.messages().raw("gui.skills-button")).build(),
                    e -> new SkillsGui(plugin, viewer).open());
            var race = plugin.content().race(profile.getRaceId());
            if (!plugin.content().races().isEmpty()) {
                set(last + 4, new ItemBuilder(race == null ? Material.PLAYER_HEAD : race.icon())
                        .name(plugin.messages().raw("gui.race-button") + (race == null ? "" : " <white>" + Text.plain(race.name()))).build(),
                        e -> new RaceGui(plugin, viewer).open());
            }
            if (!plugin.content().traits().isEmpty()) {
                set(last + 5, new ItemBuilder(Material.AMETHYST_SHARD).name(plugin.messages().raw("gui.traits-button")).build(), e -> {
                    if (!plugin.profiles().get(viewer).isTraitsRolled()) {
                        plugin.character().rollTraits(viewer, plugin.profiles().get(viewer), false);
                    }
                    new TraitsGui(plugin, viewer).open();
                });
            }
            var job = plugin.content().job(profile.getJobId());
            if (!plugin.content().jobs().isEmpty()) {
                set(last + 6, new ItemBuilder(job == null ? Material.WOODEN_PICKAXE : job.icon())
                        .name(plugin.messages().raw("gui.job-button") + (job == null ? "" : " <white>" + Text.plain(job.name()))).build(),
                        e -> new JobGui(plugin, viewer).open());
            }
        }
        set(last + 8, new ItemBuilder(Material.BARRIER).name(plugin.messages().raw("gui.close")).build(), e -> viewer.closeInventory());
        frame();
    }

    /** Tête du joueur résumant tout son personnage. */
    private org.bukkit.inventory.ItemStack profileHead(PlayerProfile p) {
        org.bukkit.inventory.ItemStack head = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
        var meta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(viewer);
        meta.displayName(Text.item("<gold><bold>" + viewer.getName()));
        java.util.List<net.kyori.adventure.text.Component> lore = new java.util.ArrayList<>();
        RpgClass rc = plugin.classes().effective(p);
        var race = plugin.content().race(p.getRaceId());
        var job = plugin.content().job(p.getJobId());
        var spec = plugin.jobs().spec(p);
        lore.add(Text.item("<gray>Classe : " + (rc == null ? "<dark_gray>-" : rc.name) + " <gray>Niv. <white>" + p.getLevel()));
        lore.add(Text.item("<gray>Race : " + (race == null ? "<dark_gray>-" : race.name())));
        lore.add(Text.item("<gray>Métier : " + (job == null ? "<dark_gray>-" : job.name() + " <gray>Niv. <white>" + plugin.jobs().level(p))
                + (spec == null ? "" : " <dark_gray>(" + Text.plain(spec.name()) + ")")));
        java.util.List<String> traitNames = new java.util.ArrayList<>();
        for (String id : p.getTraits()) {
            var t = plugin.content().trait(id);
            if (t != null) {
                traitNames.add(t.name());
            }
        }
        if (!traitNames.isEmpty()) {
            lore.add(Text.item("<gray>Traits : " + String.join("<gray>, ", traitNames)));
        }
        lore.add(net.kyori.adventure.text.Component.empty());
        lore.add(Text.item("<gray>Mana : <aqua>" + (int) p.getMana() + "/" + (int) plugin.stats().maxMana(p)));
        lore.add(Text.item("<gray>Points d'arbre : <green>" + plugin.stats().availableSkillPoints(p)
                + " <gray>· Attributs : <yellow>" + plugin.stats().availableAttributePoints(p)));
        meta.lore(lore);
        head.setItemMeta(meta);
        return head;
    }

    /** Répartit les icônes au centre des lignes intérieures. */
    private int[] centeredSlots(int count) {
        int rows = getInventory().getSize() / 9;
        List<Integer> out = new ArrayList<>();
        int remaining = count;
        for (int row = 1; row < rows - 1 && remaining > 0; row++) {
            int inRow = Math.min(7, remaining);
            int start = row * 9 + (9 - (inRow * 2 - 1 > 9 ? inRow : inRow * 2 - 1)) / 2;
            boolean spaced = inRow * 2 - 1 <= 9;
            for (int j = 0; j < inRow; j++) {
                out.add(start + (spaced ? j * 2 : j));
            }
            remaining -= inRow;
        }
        return out.stream().mapToInt(Integer::intValue).toArray();
    }
}
