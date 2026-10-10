package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.JobRule;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.service.GearService;
import fr.cykalos.cykclasses.util.ItemBuilder;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Atelier du joaillier : les gemmes brutes de l'inventaire, à tailler une par une (clic) ou toutes (Maj + clic).
 */
public final class JewelerGui extends Gui {

    private ItemStack lastResult;

    public JewelerGui(CykClasses plugin, Player viewer) {
        super(plugin, viewer, 5, plugin.messages().get("gems.gui-title"));
    }

    @Override
    protected void build() {
        PlayerProfile p = plugin.profiles().get(viewer);
        int level = plugin.jobs().level(p);
        var bonusAll = plugin.jobs().bonus(p, "*");

        // En-tête : chances de rareté et pureté au niveau actuel
        ItemBuilder info = new ItemBuilder(Material.STONECUTTER)
                .name(plugin.messages().raw("gems.gui-info").replace("<level>", String.valueOf(level)));
        plugin.gear().gemRarityChances(level + bonusAll.rarityLevels()).forEach((id, pct) -> {
            if (pct > 0.05) {
                var r = plugin.gear().gemRarityById(id);
                info.lore("<gray>• " + (r == null ? id : r.label()) + " <dark_gray>: <white>" + Text.format(Math.round(pct * 10) / 10.0) + " %");
            }
        });
        double[] range = plugin.gear().purityRange(level, bonusAll.purity());
        info.lore("").lore(plugin.messages().raw("gems.gui-purity")
                .replace("<min>", String.valueOf(Math.round(range[0]))).replace("<max>", String.valueOf(Math.round(range[1]))));
        set(4, info.build(), null);

        // Gemmes brutes de l'inventaire, regroupées par type
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ItemStack it : viewer.getInventory().getStorageContents()) {
            if (plugin.gear().isRawGem(it)) {
                counts.merge(plugin.gear().gemId(it), it.getAmount(), Integer::sum);
            }
        }
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int i = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            GearService.Gem gem = plugin.gear().gems().get(e.getKey());
            if (gem == null || i >= slots.length) {
                continue;
            }
            ItemStack icon = plugin.gear().createRawGem(gem, Math.min(64, e.getValue()));
            var meta = icon.getItemMeta();
            var lore = new java.util.ArrayList<>(meta.lore() == null ? java.util.List.<net.kyori.adventure.text.Component>of() : meta.lore());
            lore.add(net.kyori.adventure.text.Component.empty());
            lore.add(Text.item(plugin.messages().raw("gems.gui-count").replace("<count>", String.valueOf(e.getValue()))));
            lore.add(Text.item(plugin.messages().raw("gems.gui-click")));
            meta.lore(lore);
            icon.setItemMeta(meta);
            set(slots[i++], icon, ev -> {
                int times = ev.isShiftClick() ? 64 : 1;
                for (int t = 0; t < times; t++) {
                    if (!refineOne(gem)) {
                        break;
                    }
                }
                open();
            });
        }
        if (counts.isEmpty()) {
            set(22, new ItemBuilder(Material.GRAY_DYE).name(plugin.messages().raw("gems.gui-empty")).build(), null);
        }
        if (lastResult != null) {
            ItemStack shown = lastResult.clone();
            set(40, shown, null);
        }
        set(36, new ItemBuilder(Material.BARRIER).name(plugin.messages().raw("gui.close")).build(), e -> viewer.closeInventory());
        frame();
    }

    private boolean refineOne(GearService.Gem gem) {
        PlayerInventory inv = viewer.getInventory();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack it = inv.getItem(slot);
            if (plugin.gear().isRawGem(it) && gem.id().equals(plugin.gear().gemId(it))) {
                it.setAmount(it.getAmount() - 1);
                inv.setItem(slot, it.getAmount() <= 0 ? null : it);
                PlayerProfile p = plugin.profiles().get(viewer);
                var bonus = plugin.jobs().bonus(p, gem.id());
                ItemStack result = plugin.gear().refine(gem, plugin.jobs().level(p), bonus.rarityLevels(), bonus.purity());
                inv.addItem(result.clone()).values().forEach(rest -> viewer.getWorld().dropItemNaturally(viewer.getLocation(), rest));
                lastResult = result;
                plugin.jobs().reward(viewer, JobRule.Action.REFINE, gem.id(), 1);
                viewer.playSound(Sound.sound(Key.key("ui.stonecutter.take_result"), Sound.Source.PLAYER, 1f, 1.2f));
                return true;
            }
        }
        return false;
    }
}
