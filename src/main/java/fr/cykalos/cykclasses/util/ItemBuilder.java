package fr.cykalos.cykclasses.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class ItemBuilder {

    private final ItemStack stack;
    private Component name;
    private final List<Component> lore = new ArrayList<>();
    private boolean glow;

    public ItemBuilder(Material material) {
        this.stack = new ItemStack(material == null || material.isAir() ? Material.BARRIER : material);
    }

    public ItemBuilder name(Component name) {
        this.name = name;
        return this;
    }

    public ItemBuilder name(String miniMessage) {
        return name(Text.item(miniMessage));
    }

    public ItemBuilder lore(Component line) {
        lore.add(line);
        return this;
    }

    public ItemBuilder lore(String miniMessage) {
        return lore(Text.item(miniMessage));
    }

    public ItemBuilder lore(List<Component> lines) {
        lore.addAll(lines);
        return this;
    }

    public ItemBuilder glow(boolean glow) {
        this.glow = glow;
        return this;
    }

    public ItemStack build() {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.displayName(name);
            }
            if (!lore.isEmpty()) {
                meta.lore(lore);
            }
            meta.addItemFlags(ItemFlag.values());
            if (glow) {
                meta.setEnchantmentGlintOverride(true);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static Material material(String name, Material fallback) {
        if (name == null) {
            return fallback;
        }
        Material m = Material.matchMaterial(name);
        return m == null ? fallback : m;
    }
}
