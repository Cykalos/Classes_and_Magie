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

    @Override
    public boolean isCreationStep() {
        return !plugin.profiles().get(viewer).hasClass();
    }

    public ClassSelectGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, hasClass(plugin, viewer) ? 5 : rows(plugin),
                plugin.messages().get(hasClass(plugin, viewer) ? "gui.profile-title" : "gui.class-title"));
    }

    private static boolean hasClass(CykClasses plugin, Player viewer) {
        return plugin.profiles().get(viewer).hasClass();
    }

    private static int rows(CykClasses plugin) {
        int count = plugin.classes().baseClasses().size();
        return Math.max(3, Math.min(6, (int) Math.ceil((count + 2) / 7.0) + 2));
    }

    @Override
    protected void build() {
        PlayerProfile profile = plugin.profiles().get(viewer);
        if (profile.hasClass()) {
            buildProfile(profile);
        } else {
            buildChoice(profile);
        }
        frame();
    }

    /** Pas encore de classe : choix parmi les classes de base. */
    private void buildChoice(PlayerProfile profile) {
        List<RpgClass> classes = new ArrayList<>(plugin.classes().baseClasses());
        int[] slots = centeredSlots(classes.size());
        for (int i = 0; i < classes.size() && i < slots.length; i++) {
            RpgClass rc = classes.get(i);
            ItemBuilder b = new ItemBuilder(rc.icon).name(rc.name).lore(ClassLore.describe(plugin, rc));
            b.lore(Text.item(""));
            b.lore(plugin.messages().item("gui.click-choose"));
            set(slots[i], b.build(), e -> {
                if (!plugin.profiles().get(viewer).hasClass() && plugin.classService().choose(viewer, rc)) {
                    viewer.closeInventory();
                }
            });
        }
        int last = getInventory().getSize() - 9;
        set(last + 4, new ItemBuilder(Material.BARRIER).name(plugin.messages().raw("gui.close")).build(), e -> viewer.closeInventory());
    }

    /** Classe choisie : menu du personnage, boutons rangés par thème et centrés sur chaque ligne. */
    private void buildProfile(PlayerProfile profile) {
        var mod = plugin.modules();
        set(4, profileHead(profile), null);

        // Ligne 1 : la classe
        List<Button> classRow = new ArrayList<>();
        RpgClass rc = plugin.classes().effective(profile);
        if (rc != null) {
            classRow.add(new Button(new ItemBuilder(rc.icon).name(rc.name).lore(ClassLore.describe(plugin, rc)).glow(true)
                    .lore(Text.item("")).lore(plugin.messages().item("gui.current")).build(), null));
        }
        classRow.add(new Button(new ItemBuilder(Material.ENCHANTED_BOOK).name(plugin.messages().raw("gui.skills-button")).build(),
                () -> new SkillsGui(plugin, viewer).open()));
        classRow.add(new Button(new ItemBuilder(Material.NETHER_STAR).name(plugin.messages().raw("gui.subclasses-button")).build(),
                () -> new SubclassGui(plugin, viewer).open()));

        // Ligne 2 : le personnage
        List<Button> charRow = new ArrayList<>();
        var race = plugin.content().race(profile.getRaceId());
        if (mod.races() && !plugin.content().races().isEmpty()) {
            charRow.add(new Button(new ItemBuilder(race == null ? Material.PLAYER_HEAD : race.icon())
                    .name(plugin.messages().raw("gui.race-button") + (race == null ? "" : " <white>" + Text.plain(race.name()))).build(),
                    () -> new RaceGui(plugin, viewer).open()));
        }
        if (mod.traits() && !plugin.content().traits().isEmpty()) {
            charRow.add(new Button(new ItemBuilder(Material.AMETHYST_SHARD).name(plugin.messages().raw("gui.traits-button")).build(), () -> {
                if (!plugin.profiles().get(viewer).isTraitsRolled()) {
                    plugin.character().rollTraits(viewer, plugin.profiles().get(viewer), false);
                }
                new TraitsGui(plugin, viewer).open();
            }));
        }
        if (mod.weaknesses()) {
            charRow.add(new Button(new ItemBuilder(Material.FERMENTED_SPIDER_EYE).name(plugin.messages().raw("gui.weaknesses-button")).build(),
                    () -> new WeaknessGui(plugin, viewer).open()));
        }
        var job = plugin.content().job(profile.getJobId());
        if (mod.jobs() && !plugin.content().jobs().isEmpty()) {
            charRow.add(new Button(new ItemBuilder(job == null ? Material.WOODEN_PICKAXE : job.icon())
                    .name(plugin.messages().raw("gui.job-button") + (job == null ? "" : " <white>" + Text.plain(job.name()))).build(),
                    () -> new JobGui(plugin, viewer).open()));
        }

        // Ligne 3 : la progression
        List<Button> progressRow = new ArrayList<>();
        if (mod.skillTree()) {
            progressRow.add(new Button(new ItemBuilder(Material.OAK_SAPLING).name(plugin.messages().raw("gui.tree-button"))
                    .lore(plugin.messages().raw("gui.points-line").replace("<points>", String.valueOf(plugin.stats().availableSkillPoints(profile))))
                    .glow(plugin.stats().availableSkillPoints(profile) > 0).build(), () -> new SkillTreeGui(plugin, viewer, 0).open()));
        }
        if (mod.attributes()) {
            progressRow.add(new Button(new ItemBuilder(Material.ANVIL).name(plugin.messages().raw("gui.attributes-button"))
                    .lore(plugin.messages().raw("gui.points-line").replace("<points>", String.valueOf(plugin.stats().availableAttributePoints(profile))))
                    .glow(plugin.stats().availableAttributePoints(profile) > 0).build(), () -> new AttributesGui(plugin, viewer).open()));
        }
        if (mod.titles()) {
            progressRow.add(new Button(new ItemBuilder(Material.NAME_TAG).name(plugin.messages().raw("gui.titles-button")).build(),
                    () -> new TitlesGui(plugin, viewer).open()));
        }
        if (mod.quests()) {
            progressRow.add(new Button(new ItemBuilder(Material.WRITABLE_BOOK).name(plugin.messages().raw("gui.quests-button")).build(),
                    () -> new QuestJournalGui(plugin, viewer).open()));
        }

        placeRow(1, classRow);
        placeRow(2, charRow);
        placeRow(3, progressRow);
        set(40, new ItemBuilder(Material.BARRIER).name(plugin.messages().raw("gui.close")).build(), e -> viewer.closeInventory());
    }

    private record Button(org.bukkit.inventory.ItemStack item, Runnable action) {
    }

    /** Place les boutons au centre de la ligne, espacés d'une case quand c'est possible. */
    private void placeRow(int row, List<Button> buttons) {
        int n = Math.min(7, buttons.size());
        if (n == 0) {
            return;
        }
        boolean spaced = n * 2 - 1 <= 7;
        int width = spaced ? n * 2 - 1 : n;
        int start = row * 9 + 1 + (7 - width) / 2;
        for (int i = 0; i < n; i++) {
            Button b = buttons.get(i);
            set(start + (spaced ? i * 2 : i), b.item(), b.action() == null ? null : e -> b.action().run());
        }
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
