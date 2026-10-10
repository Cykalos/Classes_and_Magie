package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.SkillMod;
import fr.cykalos.cykclasses.content.SkillTree;
import fr.cykalos.cykclasses.content.TreeNode;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.model.SkillDefinition;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/** Arbre de compétences : les nœuds sont placés selon leurs coordonnées x (0-8) / y (0-4). */
public final class SkillTreeGui extends Gui {

    private final int treeIndex;

    public SkillTreeGui(CykClasses plugin, Player viewer, int treeIndex) {
        super(plugin, viewer, 6, title(plugin, viewer, treeIndex));
        this.treeIndex = treeIndex;
    }

    private static net.kyori.adventure.text.Component title(CykClasses plugin, Player viewer, int index) {
        List<SkillTree> trees = plugin.stats().treesOf(plugin.profiles().get(viewer));
        String t = trees.isEmpty() ? "?" : trees.get(Math.min(index, trees.size() - 1)).title();
        return Text.mm("<dark_gray>" + Text.plain(t));
    }

    @Override
    protected void build() {
        PlayerProfile p = plugin.profiles().get(viewer);
        List<SkillTree> trees = plugin.stats().treesOf(p);
        if (trees.isEmpty()) {
            plugin.messages().send(viewer, "tree.none");
            viewer.closeInventory();
            return;
        }
        SkillTree tree = trees.get(Math.min(treeIndex, trees.size() - 1));
        RpgClass rc = plugin.classes().effective(p);
        for (TreeNode node : tree.nodes().values()) {
            int rank = p.getNodeRank(tree.classId() + ":" + node.id());
            String reason = plugin.character().cannotInvest(p, tree, node);
            boolean maxed = rank >= node.maxRank();
            boolean available = reason == null;
            Material mat = rank > 0 || available ? node.icon() : Material.GRAY_STAINED_GLASS_PANE;
            ItemBuilder b = new ItemBuilder(mat)
                    .name((maxed ? "<gold>" : rank > 0 ? "<green>" : available ? "<yellow>" : "<dark_gray>") + Text.plain(node.name())
                            + " <gray>[" + rank + "/" + node.maxRank() + "]")
                    .glow(rank > 0)
                    .lore(Text.itemLore(node.description()));
            List<String> mods = node.modifiers().describe();
            if (!mods.isEmpty() || !node.skillMods().isEmpty()) {
                b.lore("").lore(plugin.messages().raw("tree.per-rank"));
                mods.forEach(l -> b.lore("<gray>• " + l));
                for (Map.Entry<String, SkillMod> e : node.skillMods().entrySet()) {
                    SkillDefinition sd = rc == null ? null : rc.skills().get(e.getKey());
                    String name = sd == null ? e.getKey() : Text.plain(sd.name);
                    SkillMod m = e.getValue();
                    if (m.damage != 0) b.lore("<gray>• <light_purple>" + name + "</light_purple> : " + pct(m.damage, false) + " puissance");
                    if (m.cooldown != 0) b.lore("<gray>• <light_purple>" + name + "</light_purple> : " + pct(m.cooldown, true) + " recharge");
                    if (m.mana != 0) b.lore("<gray>• <light_purple>" + name + "</light_purple> : " + pct(m.mana, true) + " mana");
                }
            }
            b.lore("").lore("<gray>Coût : <white>" + node.cost() + "</white> point" + (node.cost() > 1 ? "s" : "") + " par rang");
            if (maxed) {
                b.lore(plugin.messages().raw("tree.maxed-line"));
            } else if (available) {
                b.lore(plugin.messages().raw("tree.click"));
            } else {
                b.lore("<red>✘ " + reason);
            }
            int slot = node.y() * 9 + node.x();
            set(slot, b.build(), e -> {
                plugin.character().invest(viewer, tree, node);
                open();
            });
        }
        int pts = plugin.stats().availableSkillPoints(p);
        set(49, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name(plugin.messages().raw("tree.points").replace("<points>", String.valueOf(pts)))
                .lore(plugin.messages().raw("tree.points-desc")
                        .replace("<per_level>", String.valueOf(plugin.getConfig().getInt("progression.skill-points-per-level", 1))))
                .build(), null);
        if (trees.size() > 1) {
            int next = (treeIndex + 1) % trees.size();
            set(53, new ItemBuilder(Material.SPECTRAL_ARROW).name(plugin.messages().raw("tree.switch")
                    .replace("<tree>", Text.plain(trees.get(next).title()))).build(), e -> new SkillTreeGui(plugin, viewer, next).open());
        }
        set(45, new ItemBuilder(Material.ARROW).name(plugin.messages().raw("gui.back")).build(), e -> new ClassSelectGui(plugin, viewer).open());
        set(47, new ItemBuilder(Material.TNT).name(plugin.messages().raw("tree.reset-button"))
                .lore(plugin.messages().raw("tree.reset-desc")
                        .replace("<hours>", String.valueOf(plugin.getConfig().getLong("progression.tree-reset-cooldown-hours", 24))))
                .build(), e -> {
            if (e.isShiftClick()) {
                plugin.character().resetTree(viewer, false);
                open();
            }
        });
        fill();
    }

    private static String pct(double v, boolean lowerIsBetter) {
        boolean good = lowerIsBetter ? v < 0 : v > 0;
        long p = Math.round(v * 100);
        return (good ? "<green>" : "<red>") + (p > 0 ? "+" : "") + p + "%<gray>";
    }
}
