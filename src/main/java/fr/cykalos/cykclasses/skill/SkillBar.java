package fr.cykalos.cykclasses.skill;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.model.SkillDefinition;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.inventory.meta.components.UseCooldownComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Mode compétences : la barre d'action (slots 1 à 8) est remplacée par les icônes des compétences,
 * l'objet tenu est déplacé dans le slot 9 et reste en main.
 *
 * <p>Pourquoi : le client n'envoie aucun paquet quand on appuie sur la touche du slot déjà sélectionné.
 * En gardant toujours la main sur le slot 9, chaque touche 1-8 provoque un changement de slot,
 * que l'on annule pour lancer la compétence.</p>
 *
 * <p>Les objets d'origine sont gardés en mémoire ET dans les données persistantes du joueur,
 * pour être restaurés même après un crash du serveur.</p>
 */
public final class SkillBar {

    public static final int SKILL_SLOTS = 8;
    public static final int HAND_SLOT = 8;

    private record Saved(ItemStack[] hotbar, int heldSlot) {
    }

    private final CykClasses plugin;
    private final NamespacedKey iconKey;
    private final NamespacedKey savedKey;
    private final NamespacedKey savedSlotKey;
    private final Map<UUID, Saved> saved = new HashMap<>();

    public SkillBar(CykClasses plugin) {
        this.plugin = plugin;
        this.iconKey = new NamespacedKey(plugin, "skill_icon");
        this.savedKey = new NamespacedKey(plugin, "saved_hotbar");
        this.savedSlotKey = new NamespacedKey(plugin, "saved_held_slot");
    }

    public boolean isActive(Player player) {
        return saved.containsKey(player.getUniqueId());
    }

    public boolean isIcon(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(iconKey, PersistentDataType.STRING);
    }

    public void toggle(Player player) {
        if (isActive(player)) {
            exit(player, true);
        } else {
            enter(player);
        }
    }

    public void enter(Player player) {
        PlayerProfile profile = plugin.profiles().get(player);
        RpgClass rc = plugin.classes().effective(profile);
        if (rc == null) {
            plugin.messages().send(player, "no-class");
            return;
        }
        if (isActive(player)) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        int held = inv.getHeldItemSlot();
        ItemStack[] hotbar = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            ItemStack it = inv.getItem(i);
            hotbar[i] = it == null ? null : it.clone();
        }
        Saved s = new Saved(hotbar, held);
        saved.put(player.getUniqueId(), s);
        persist(player, s);

        ItemStack hand = hotbar[held];
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, null);
        }
        inv.setItem(HAND_SLOT, hand);
        inv.setHeldItemSlot(HAND_SLOT);
        refresh(player);
        applyCooldownOverlays(player, profile, rc);

        profile.setCastMode(true);
        plugin.messages().send(player, "cast-mode-on");
        player.playSound(Sound.sound(Key.key("block.amethyst_block.chime"), Sound.Source.PLAYER, 1f, 1.4f));
    }

    /**
     * Restaure la barre d'origine. L'objet du slot 9 (l'arme tenue, éventuellement usée) retourne à sa place.
     */
    public void exit(Player player, boolean feedback) {
        Saved s = saved.remove(player.getUniqueId());
        PlayerProfile profile = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (profile != null) {
            profile.setCastMode(false);
        }
        if (s == null) {
            return;
        }
        restore(player, s);
        clearPersisted(player);
        if (feedback) {
            plugin.messages().actionBar(player, "cast-mode-off");
            player.playSound(Sound.sound(Key.key("block.amethyst_block.chime"), Sound.Source.PLAYER, 1f, 0.7f));
        }
    }

    private void restore(Player player, Saved s) {
        PlayerInventory inv = player.getInventory();
        ItemStack hand = inv.getItem(HAND_SLOT);
        if (isIcon(hand)) {
            hand = null;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack current = inv.getItem(i);
            if (isIcon(current)) {
                inv.setItem(i, null);
            }
        }
        // Un objet arrivé entre-temps sur un slot (ramassage) ne doit pas être écrasé.
        List<ItemStack> overflow = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (i == HAND_SLOT) {
                ItemStack occupant = inv.getItem(HAND_SLOT);
                if (occupant != null && !occupant.isEmpty() && !occupant.equals(hand)) {
                    overflow.add(occupant);
                }
                inv.setItem(HAND_SLOT, null);
                continue;
            }
            ItemStack occupant = inv.getItem(i);
            if (occupant != null && !occupant.isEmpty()) {
                overflow.add(occupant);
                inv.setItem(i, null);
            }
        }
        for (int i = 0; i < 9; i++) {
            if (i == s.heldSlot()) {
                inv.setItem(i, hand);
            } else {
                inv.setItem(i, s.hotbar()[i]);
            }
        }
        inv.setHeldItemSlot(s.heldSlot());
        for (ItemStack it : overflow) {
            inv.addItem(it).values().forEach(rest -> player.getWorld().dropItemNaturally(player.getLocation(), rest));
        }
    }

    /** À la connexion : restaure une barre restée active (crash) et supprime toute icône égarée. */
    public void recover(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        byte[] bytes = pdc.get(savedKey, PersistentDataType.BYTE_ARRAY);
        if (bytes != null) {
            try {
                ItemStack[] items = ItemStack.deserializeItemsFromBytes(bytes);
                ItemStack[] hotbar = new ItemStack[9];
                for (int i = 0; i < 9 && i < items.length; i++) {
                    hotbar[i] = items[i] == null || items[i].isEmpty() ? null : items[i];
                }
                int held = pdc.getOrDefault(savedSlotKey, PersistentDataType.INTEGER, 0);
                restore(player, new Saved(hotbar, held));
                plugin.getLogger().info("Barre d'objets de " + player.getName() + " restaurée après un arrêt imprévu.");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Restauration impossible de la barre de " + player.getName(), e);
            }
            clearPersisted(player);
        }
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (isIcon(inv.getItem(i))) {
                inv.setItem(i, null);
            }
        }
    }

    /**
     * À la mort : retire les icônes des objets lâchés et remet les objets d'origine
     * (dans l'inventaire si keepInventory, sinon dans les objets lâchés).
     */
    public void handleDeath(Player player, List<ItemStack> drops, boolean keepInventory) {
        drops.removeIf(this::isIcon);
        Saved s = saved.get(player.getUniqueId());
        if (s == null) {
            return;
        }
        if (keepInventory) {
            exit(player, false);
            return;
        }
        saved.remove(player.getUniqueId());
        PlayerProfile profile = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (profile != null) {
            profile.setCastMode(false);
        }
        for (int i = 0; i < 9; i++) {
            if (i != s.heldSlot() && s.hotbar()[i] != null && !s.hotbar()[i].isEmpty()) {
                drops.add(s.hotbar()[i]);
            }
        }
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (isIcon(inv.getItem(i))) {
                inv.setItem(i, null);
            }
        }
        clearPersisted(player);
    }

    public void exitAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (isActive(player)) {
                exit(player, false);
            }
        }
    }

    // ------------------------------------------------------------ Icônes

    /** Met à jour les icônes (recharge, verrouillage, mana). */
    public void refresh(Player player) {
        if (!isActive(player)) {
            return;
        }
        PlayerProfile profile = plugin.profiles().get(player);
        RpgClass rc = plugin.classes().effective(profile);
        if (rc == null) {
            exit(player, false);
            return;
        }
        List<SkillDefinition> skills = new ArrayList<>(rc.skills().values());
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < SKILL_SLOTS; i++) {
            ItemStack current = inv.getItem(i);
            if (current != null && !current.isEmpty() && !isIcon(current)) {
                continue; // objet ramassé : on n'écrase rien
            }
            // Slot sans compétence : case de remplissage, pour que le butin ramassé aille dans l'inventaire
            ItemStack icon = i < skills.size() ? icon(profile, skills.get(i), i) : filler();
            if (icon == null ? current == null || current.isEmpty()
                    : current != null && current.isSimilar(icon) && current.getAmount() == icon.getAmount()) {
                continue; // inchangé : évite d'envoyer des paquets inutiles
            }
            inv.setItem(i, icon);
        }
    }

    private ItemStack filler() {
        ItemStack item = new ItemStack(material("casting.empty-slot-icon", Material.GRAY_STAINED_GLASS_PANE));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(net.kyori.adventure.text.Component.text(" "));
        meta.getPersistentDataContainer().set(iconKey, PersistentDataType.STRING, "");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack icon(PlayerProfile profile, SkillDefinition s, int index) {
        long now = System.currentTimeMillis();
        boolean locked = profile.getLevel() < s.unlockLevel;
        long cd = profile.getCooldownEnd(s.id) - now;
        boolean noMana = profile.getMana() < s.manaCost;

        Material mat = locked ? material("casting.locked-icon", Material.GRAY_DYE) : s.icon;
        ItemStack item = new ItemStack(mat, cd > 0 && !locked ? (int) Math.min(64, Math.max(1, Math.ceil(cd / 1000.0))) : 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        String prefix = locked ? "<dark_gray>" : cd > 0 ? "<red>" : noMana ? "<blue>" : "";
        meta.displayName(Text.item("<white>" + (index + 1) + ". </white>" + prefix + (locked ? Text.plain(s.name) : s.name)));
        List<Component> lore = new ArrayList<>(Text.itemLore(s.description));
        lore.add(Component.empty());
        if (s.manaCost > 0) {
            lore.add(plugin.messages().item(noMana ? "skillbar.mana-missing" : "gui.skill-mana", Text.ph("mana", Text.format(s.manaCost))));
        }
        if (s.healthCost > 0) {
            lore.add(plugin.messages().item("gui.skill-health", Text.ph("health", Text.format(s.healthCost / 2) + "❤")));
        }
        if (locked) {
            lore.add(plugin.messages().item("gui.skill-locked", Text.ph("level", s.unlockLevel)));
        } else if (cd > 0) {
            lore.add(plugin.messages().item("skillbar.cooldown", Text.ph("time", Text.format(Math.ceil(cd / 100.0) / 10.0))));
        } else {
            lore.add(plugin.messages().item("gui.skill-cooldown", Text.ph("cooldown", Text.format(s.cooldown))));
        }
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.values());
        if (!locked && s.iconModel != null && !s.iconModel.isBlank()) {
            NamespacedKey model = NamespacedKey.fromString(s.iconModel);
            if (model != null) {
                meta.setItemModel(model);
            }
        }
        if (!locked && s.customModelData != null) {
            CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
            cmd.setFloats(List.of(s.customModelData));
            meta.setCustomModelDataComponent(cmd);
        }
        // Groupe de recharge propre à chaque compétence : permet l'animation de recharge du client.
        UseCooldownComponent useCd = meta.getUseCooldown();
        useCd.setCooldownSeconds((float) Math.max(0.05, s.cooldown));
        useCd.setCooldownGroup(cooldownGroup(s));
        meta.setUseCooldown(useCd);
        meta.setMaxStackSize(64);
        meta.getPersistentDataContainer().set(iconKey, PersistentDataType.STRING, s.id);
        item.setItemMeta(meta);
        return item;
    }

    private NamespacedKey cooldownGroup(SkillDefinition s) {
        return new NamespacedKey(plugin, "skill/" + s.id.replaceAll("[^a-z0-9_./-]", "_"));
    }

    /** Affiche l'animation de recharge sur l'icône de la compétence. */
    public void showCooldown(Player player, SkillDefinition s) {
        if (!isActive(player) || s.cooldown <= 0) {
            return;
        }
        PlayerProfile profile = plugin.profiles().get(player);
        RpgClass rc = plugin.classes().effective(profile);
        if (rc == null) {
            return;
        }
        List<SkillDefinition> skills = new ArrayList<>(rc.skills().values());
        int index = skills.indexOf(s);
        if (index < 0 || index >= SKILL_SLOTS) {
            return;
        }
        ItemStack icon = player.getInventory().getItem(index);
        if (isIcon(icon)) {
            double mult = plugin.stats().skillMod(profile, s.id).cooldownMultiplier();
            player.setCooldown(icon, (int) Math.round(s.cooldown * mult * 20));
        }
    }

    private void applyCooldownOverlays(Player player, PlayerProfile profile, RpgClass rc) {
        long now = System.currentTimeMillis();
        List<SkillDefinition> skills = new ArrayList<>(rc.skills().values());
        for (int i = 0; i < Math.min(SKILL_SLOTS, skills.size()); i++) {
            long cd = profile.getCooldownEnd(skills.get(i).id) - now;
            ItemStack icon = player.getInventory().getItem(i);
            if (cd > 0 && isIcon(icon)) {
                player.setCooldown(icon, (int) Math.ceil(cd / 50.0));
            }
        }
    }

    private Material material(String path, Material def) {
        Material m = Material.matchMaterial(plugin.getConfig().getString(path, def.name()));
        return m == null || m.isAir() ? def : m;
    }

    // ------------------------------------------------------------ Persistance

    private void persist(Player player, Saved s) {
        try {
            List<ItemStack> list = new ArrayList<>();
            for (ItemStack it : s.hotbar()) {
                list.add(it == null ? ItemStack.empty() : it);
            }
            byte[] bytes = ItemStack.serializeItemsAsBytes(list);
            player.getPersistentDataContainer().set(savedKey, PersistentDataType.BYTE_ARRAY, bytes);
            player.getPersistentDataContainer().set(savedSlotKey, PersistentDataType.INTEGER, s.heldSlot());
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Sauvegarde de secours de la barre impossible pour " + player.getName(), e);
        }
    }

    private void clearPersisted(Player player) {
        player.getPersistentDataContainer().remove(savedKey);
        player.getPersistentDataContainer().remove(savedSlotKey);
    }
}
