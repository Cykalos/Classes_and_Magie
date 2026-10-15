package fr.cykalos.cykclasses.service;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.content.AttributeDef;
import fr.cykalos.cykclasses.content.Job;
import fr.cykalos.cykclasses.content.Kit;
import fr.cykalos.cykclasses.content.KitItem;
import fr.cykalos.cykclasses.content.Race;
import fr.cykalos.cykclasses.content.SkillTree;
import fr.cykalos.cykclasses.content.Trait;
import fr.cykalos.cykclasses.content.TreeNode;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.gui.ClassSelectGui;
import fr.cykalos.cykclasses.gui.JobGui;
import fr.cykalos.cykclasses.gui.RaceGui;
import fr.cykalos.cykclasses.gui.TraitsGui;
import fr.cykalos.cykclasses.gui.WeaknessGui;
import fr.cykalos.cykclasses.content.Weakness;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Création du personnage (race, traits, métier), kits de départ, arbre de compétences et attributs.
 */
public final class CharacterService {

    private final CykClasses plugin;
    /** trait unique -> détenteur */
    private final Map<String, UUID> uniqueOwners = new HashMap<>();
    private File uniquesFile;

    public CharacterService(CykClasses plugin) {
        this.plugin = plugin;
    }

    public void loadUniques() {
        uniquesFile = new File(plugin.getDataFolder(), "uniques.yml");
        uniqueOwners.clear();
        YamlConfiguration y = YamlConfiguration.loadConfiguration(uniquesFile);
        for (String k : y.getKeys(false)) {
            try {
                uniqueOwners.put(k.toLowerCase(Locale.ROOT), UUID.fromString(y.getString(k, "")));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void saveUniques() {
        YamlConfiguration y = new YamlConfiguration();
        uniqueOwners.forEach((k, v) -> y.set(k, v.toString()));
        try {
            y.save(uniquesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Sauvegarde impossible de uniques.yml", e);
        }
    }

    public UUID uniqueOwner(String traitId) {
        return uniqueOwners.get(traitId.toLowerCase(Locale.ROOT));
    }

    // ------------------------------------------------------------ Étapes de création

    /** Ouvre l'étape suivante de la création du personnage, s'il en reste une. */
    public void continueCreation(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            PlayerProfile p = plugin.profiles().get(player);
            var mod = plugin.modules();
            if (!p.hasClass()) {
                new ClassSelectGui(plugin, player).open();
            } else if (mod.races() && p.getRaceId() == null && !plugin.content().races().isEmpty()) {
                new RaceGui(plugin, player).open();
            } else if (mod.traits() && !p.isTraitsConfirmed() && !plugin.content().traits().isEmpty()) {
                // Tirés une seule fois : fermer le menu ou se déconnecter ne relance pas le tirage
                if (!p.isTraitsRolled()) {
                    rollTraits(player, p, false);
                }
                new TraitsGui(plugin, player).open();
            } else if (mod.weaknesses() && !p.isWeaknessesConfirmed() && hasWeaknessStep(player, p)) {
                if (!p.isWeaknessesRolled()) {
                    rollWeaknesses(player, p, false);
                }
                new WeaknessGui(plugin, player).open();
            } else if (mod.jobs() && p.getJobId() == null && !p.isJobPromptDone() && !plugin.content().jobs().isEmpty()) {
                new JobGui(plugin, player).open();
            }
        }, 2L);
    }

    public boolean chooseRace(Player player, Race race) {
        PlayerProfile p = plugin.profiles().get(player);
        if (p.getRaceId() != null && !plugin.getConfig().getBoolean("character.race-can-change", false)) {
            plugin.messages().send(player, "race.already");
            return false;
        }
        p.setRaceId(race.id());
        plugin.stats().apply(player);
        plugin.messages().send(player, "race.chosen", Text.ph("race", race.name()));
        Race.Affinity pen = race.penaltyFor(p.getClassId(), p.getSubclassId());
        if (pen != null) {
            plugin.messages().send(player, "race.penalty", Text.ph("description", pen.description()));
        }
        player.playSound(Sound.sound(Key.key("block.enchantment_table.use"), Sound.Source.PLAYER, 1f, 1.2f));
        continueCreation(player);
        return true;
    }

    // ------------------------------------------------------------ Traits

    public int maxRerolls() {
        return plugin.content().traitsConfig().getInt("roll.rerolls", 3);
    }

    public int rerollsLeft(PlayerProfile p) {
        return Math.max(0, maxRerolls() - p.getRerollsUsed());
    }

    public void rerollTraits(Player player) {
        PlayerProfile p = plugin.profiles().get(player);
        if (rerollsLeft(p) <= 0) {
            plugin.messages().send(player, "traits.no-reroll");
            return;
        }
        p.setRerollsUsed(p.getRerollsUsed() + 1);
        rollTraits(player, p, true);
        player.playSound(Sound.sound(Key.key("block.amethyst_block.resonate"), Sound.Source.PLAYER, 1f, 1.3f));
    }

    /** Tire de nouveaux traits (libère d'abord un éventuel trait unique). */
    public void rollTraits(Player player, PlayerProfile p, boolean announce) {
        releaseUniques(p);
        YamlConfiguration cfg = plugin.content().traitsConfig();
        int count = Math.max(0, cfg.getInt("roll.count", 3));
        int maxNegative = cfg.getInt("roll.max-negative", 1);
        int minPositive = cfg.getInt("roll.min-positive", 1);
        double uniqueChance = cfg.getDouble("roll.unique-chance", 0.01);
        ThreadLocalRandom r = ThreadLocalRandom.current();

        List<Trait> pool = new ArrayList<>();
        List<Trait> uniques = new ArrayList<>();
        for (Trait t : plugin.content().traits()) {
            if (t.unique()) {
                if (uniqueOwner(t.id()) == null) {
                    uniques.add(t);
                }
            } else {
                pool.add(t);
            }
        }
        List<Trait> picked = new ArrayList<>();
        int negatives = 0;
        for (int i = 0; i < count; i++) {
            List<Trait> candidates = new ArrayList<>();
            for (Trait t : pool) {
                if (picked.contains(t) || conflicts(t, picked)) {
                    continue;
                }
                if (t.kind() == Trait.Kind.NEGATIVE && negatives >= maxNegative) {
                    continue;
                }
                int positives = (int) picked.stream().filter(x -> x.kind() == Trait.Kind.POSITIVE).count();
                int remaining = count - i;
                if (t.kind() != Trait.Kind.POSITIVE && positives + remaining <= minPositive) {
                    continue;
                }
                candidates.add(t);
            }
            Trait t = weighted(candidates, r);
            if (t == null) {
                break;
            }
            picked.add(t);
            if (t.kind() == Trait.Kind.NEGATIVE) {
                negatives++;
            }
        }
        if (!uniques.isEmpty() && r.nextDouble() < uniqueChance) {
            List<Trait> ok = uniques.stream().filter(u -> !conflicts(u, picked)).toList();
            if (!ok.isEmpty()) {
                Trait u = ok.get(r.nextInt(ok.size()));
                picked.add(u);
                uniqueOwners.put(u.id(), p.getUuid());
                saveUniques();
                Bukkit.getServer().sendMessage(plugin.messages().get("traits.unique-broadcast",
                        Text.ph("player", player.getName()), Text.ph("trait", u.name())));
            }
        }
        List<String> ids = new ArrayList<>();
        picked.forEach(t -> ids.add(t.id()));
        p.setTraits(ids);
        p.setTraitsRolled(true);
        plugin.stats().apply(player);
        if (announce) {
            plugin.messages().send(player, "traits.rerolled", Text.ph("left", rerollsLeft(p)));
        }
    }

    private static boolean conflicts(Trait t, List<Trait> picked) {
        for (Trait o : picked) {
            if (t.conflicts().contains(o.id()) || o.conflicts().contains(t.id())) {
                return true;
            }
        }
        return false;
    }

    private static Trait weighted(List<Trait> list, ThreadLocalRandom r) {
        double total = list.stream().mapToDouble(t -> Math.max(0, t.weight())).sum();
        if (total <= 0) {
            return null;
        }
        double x = r.nextDouble(total);
        for (Trait t : list) {
            x -= Math.max(0, t.weight());
            if (x < 0) {
                return t;
            }
        }
        return list.getLast();
    }

    private void releaseUniques(PlayerProfile p) {
        boolean changed = uniqueOwners.entrySet().removeIf(e -> e.getValue().equals(p.getUuid()));
        if (changed) {
            saveUniques();
        }
    }

    /** Admin : retire tous les traits (le joueur pourra en retirer de nouveaux). */
    public void resetTraits(Player player) {
        PlayerProfile p = plugin.profiles().get(player);
        releaseUniques(p);
        p.setTraits(List.of());
        p.setTraitsRolled(false);
        p.setTraitsConfirmed(false);
        p.setRerollsUsed(0);
        plugin.stats().apply(player);
    }

    // ------------------------------------------------------------ Faiblesses

    private YamlConfiguration wcfg() {
        return plugin.content().weaknessesConfig();
    }

    public boolean weaknessRollEnabled() {
        return wcfg() != null && wcfg().getBoolean("roll.enabled", true);
    }

    /** L'étape « faiblesses » de la création a-t-elle quelque chose à montrer ? */
    private boolean hasWeaknessStep(Player player, PlayerProfile p) {
        if (!wcfg().getBoolean("roll.show-step", true)) {
            // Tirage silencieux, sans menu : le joueur les verra avec /faiblesses.
            if (!p.isWeaknessesRolled()) {
                rollWeaknesses(player, p, false);
            }
            p.setWeaknessesConfirmed(true);
            return false;
        }
        return (weaknessRollEnabled() && wcfg().getInt("roll.max", 2) > 0 && !plugin.content().weaknesses().isEmpty())
                || !predefinedOf(p).isEmpty();
    }

    public List<Weakness> predefinedOf(PlayerProfile p) {
        if (!plugin.modules().weaknesses()) {
            return List.of();
        }
        String race = plugin.modules().races() ? p.getRaceId() : null;
        List<Weakness> out = new ArrayList<>();
        for (Weakness w : plugin.content().predefinedWeaknesses(race, p.getClassId(), p.getSubclassId())) {
            // exclude-races / exclude-classes s'appliquent aussi aux faiblesses imposées (évite les doublons)
            boolean excluded = (race != null && w.excludeRaces().contains(race))
                    || w.excludeClasses().contains(p.getClassId())
                    || (p.getSubclassId() != null && w.excludeClasses().contains(p.getSubclassId()));
            if (!excluded) {
                out.add(w);
            }
        }
        return out;
    }

    public List<Weakness> rolledOf(PlayerProfile p) {
        List<Weakness> out = new ArrayList<>();
        for (String id : p.getWeaknesses()) {
            Weakness w = plugin.content().weakness(id);
            if (w != null) {
                out.add(w);
            }
        }
        return out;
    }

    /** Faiblesses actives : imposées (race, classe, sous-classe) puis tirées au hasard. */
    public List<Weakness> weaknessesOf(PlayerProfile p) {
        List<Weakness> out = new ArrayList<>(predefinedOf(p));
        for (Weakness w : rolledOf(p)) {
            if (!out.contains(w)) {
                out.add(w);
            }
        }
        return out;
    }

    /**
     * Toutes les faiblesses du personnage en texte : classe, race (et malus), traits, imposées et tirées.
     */
    public List<String> allWeaknessLabels(PlayerProfile p) {
        java.util.LinkedHashSet<String> out = new java.util.LinkedHashSet<>();
        var rc = plugin.classes().effective(p);
        if (rc != null) {
            rc.flaws().forEach(f -> out.add(Text.plain(f.description)));
        }
        if (plugin.modules().races()) {
            var race = plugin.content().race(p.getRaceId());
            if (race != null) {
                race.flaws().forEach(f -> out.add(Text.plain(f.description)));
                var pen = race.penaltyFor(p.getClassId(), p.getSubclassId());
                if (pen != null) {
                    pen.flaws().forEach(f -> out.add(Text.plain(f.description)));
                }
            }
        }
        if (plugin.modules().traits()) {
            for (String id : p.getTraits()) {
                var t = plugin.content().trait(id);
                if (t != null) {
                    t.flaws().forEach(f -> out.add(Text.plain(f.description)));
                }
            }
        }
        if (plugin.modules().weaknesses()) {
            weaknessesOf(p).forEach(w -> out.add(Text.plain(w.name())));
        }
        return new ArrayList<>(out);
    }

    public int weaknessRerollsLeft(PlayerProfile p) {
        return Math.max(0, wcfg().getInt("roll.rerolls", 1) - p.getWeaknessRerollsUsed());
    }

    public void rerollWeaknesses(Player player) {
        PlayerProfile p = plugin.profiles().get(player);
        if (weaknessRerollsLeft(p) <= 0) {
            plugin.messages().send(player, "weaknesses.no-reroll");
            return;
        }
        p.setWeaknessRerollsUsed(p.getWeaknessRerollsUsed() + 1);
        rollWeaknesses(player, p, true);
        player.playSound(Sound.sound(Key.key("entity.wither.ambient"), Sound.Source.PLAYER, 0.4f, 1.6f));
    }

    /**
     * Tire les faiblesses : entre roll.min et roll.max, selon les chances de chaque faiblesse
     * pour la race et la classe du joueur. Les faiblesses imposées ne sont pas tirées en double.
     */
    public void rollWeaknesses(Player player, PlayerProfile p, boolean announce) {
        List<String> ids = new ArrayList<>();
        if (weaknessRollEnabled()) {
            int min = Math.max(0, wcfg().getInt("roll.min", 1));
            int max = Math.max(min, wcfg().getInt("roll.max", 2));
            ThreadLocalRandom r = ThreadLocalRandom.current();
            int count = min + (max > min ? r.nextInt(max - min + 1) : 0);
            String race = plugin.modules().races() ? p.getRaceId() : null;
            List<Weakness> imposed = predefinedOf(p);
            List<Weakness> picked = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                List<Weakness> candidates = new ArrayList<>();
                List<Double> weights = new ArrayList<>();
                double total = 0;
                for (Weakness w : plugin.content().weaknesses()) {
                    if (picked.contains(w) || imposed.contains(w) || weaknessConflicts(w, picked) || weaknessConflicts(w, imposed)) {
                        continue;
                    }
                    double weight = w.weightFor(race, p.getClassId(), p.getSubclassId());
                    if (weight > 0) {
                        candidates.add(w);
                        weights.add(weight);
                        total += weight;
                    }
                }
                if (candidates.isEmpty()) {
                    break;
                }
                double x = r.nextDouble(total);
                Weakness chosen = candidates.getLast();
                for (int j = 0; j < candidates.size(); j++) {
                    x -= weights.get(j);
                    if (x < 0) {
                        chosen = candidates.get(j);
                        break;
                    }
                }
                picked.add(chosen);
            }
            picked.forEach(w -> ids.add(w.id()));
        }
        p.setWeaknesses(ids);
        p.setWeaknessesRolled(true);
        if (player != null) {
            plugin.stats().apply(player);
            if (announce) {
                plugin.messages().send(player, "weaknesses.rerolled", Text.ph("left", weaknessRerollsLeft(p)));
            }
        }
    }

    private static boolean weaknessConflicts(Weakness w, List<Weakness> others) {
        for (Weakness o : others) {
            if (w.conflicts().contains(o.id()) || o.conflicts().contains(w.id())) {
                return true;
            }
        }
        return false;
    }

    /** Admin : retire les faiblesses tirées (un nouveau tirage aura lieu). */
    public void resetWeaknesses(Player player) {
        PlayerProfile p = plugin.profiles().get(player);
        p.setWeaknesses(List.of());
        p.setWeaknessesRolled(false);
        p.setWeaknessesConfirmed(false);
        p.setWeaknessRerollsUsed(0);
        plugin.stats().apply(player);
    }

    /** Admin : ajoute ou retire une faiblesse précise. */
    public boolean setWeakness(Player player, String id, boolean add) {
        PlayerProfile p = plugin.profiles().get(player);
        Weakness w = plugin.content().weakness(id);
        if (w == null) {
            return false;
        }
        List<String> ids = new ArrayList<>(p.getWeaknesses());
        if (add && !ids.contains(w.id())) {
            ids.add(w.id());
        } else if (!add) {
            ids.remove(w.id());
        }
        p.setWeaknesses(ids);
        p.setWeaknessesRolled(true);
        p.setWeaknessesConfirmed(true);
        plugin.stats().apply(player);
        return true;
    }

    // ------------------------------------------------------------ Métier

    public boolean chooseJob(Player player, Job job) {
        PlayerProfile p = plugin.profiles().get(player);
        var cfg = plugin.content().jobsConfig();
        if (p.getJobId() != null) {
            if (p.getJobId().equals(job.id())) {
                return false;
            }
            if (!cfg.getBoolean("allow-change", true)) {
                plugin.messages().send(player, "job.no-change");
                return false;
            }
            long cd = cfg.getLong("change-cooldown-hours", 24) * 3_600_000L;
            long left = p.getJobChangedAt() + cd - System.currentTimeMillis();
            if (left > 0) {
                plugin.messages().send(player, "job.cooldown", Text.ph("time", ((left + 3_599_999) / 3_600_000) + " h"));
                return false;
            }
        }
        p.setJobId(job.id());
        p.setJobPromptDone(true);
        plugin.stats().apply(player);
        plugin.messages().send(player, "job.chosen", Text.ph("job", job.name()));
        player.playSound(Sound.sound(Key.key("entity.villager.work_toolsmith"), Sound.Source.PLAYER, 1f, 1f));
        return true;
    }

    // ------------------------------------------------------------ Kits

    public void giveKit(Player player, String classId) {
        if (!plugin.modules().kits()) {
            return;
        }
        Kit kit = plugin.content().kit(classId);
        if (kit == null) {
            return;
        }
        PlayerProfile p = plugin.profiles().get(player);
        boolean once = plugin.content().kitsConfig().getBoolean("give-once", true);
        if (once && p.hasReceivedKit(classId)) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        for (KitItem ki : kit.items()) {
            ItemStack item = build(ki, player);
            if (item == null) {
                plugin.getLogger().warning("Kit " + classId + " : objet introuvable « " + ki.item() + " »");
                continue;
            }
            place(player, inv, item, ki.slot());
        }
        for (String cmd : kit.commands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()).replace("<player>", player.getName()));
        }
        p.addReceivedKit(classId);
        plugin.messages().send(player, "kit-received");
    }

    private ItemStack build(KitItem ki, Player player) {
        ItemStack item = plugin.itemFactory().create(ki.item(), ki.amount(), player);
        if (item == null) {
            return null;
        }
        boolean custom = ki.item().contains(":") && !ki.item().toLowerCase(Locale.ROOT).startsWith("minecraft:");
        if (custom && ki.name() == null && ki.lore().isEmpty() && ki.enchants().isEmpty() && !ki.unbreakable() && ki.itemModel() == null) {
            return item; // objet custom laissé tel quel
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        if (ki.name() != null) {
            meta.displayName(Text.item(ki.name()));
        }
        if (!ki.lore().isEmpty()) {
            meta.lore(Text.itemLore(ki.lore()));
        }
        ki.enchants().forEach((k, lvl) -> {
            Enchantment e = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(k));
            if (e != null) {
                meta.addEnchant(e, lvl, true);
            }
        });
        if (ki.unbreakable()) {
            meta.setUnbreakable(true);
        }
        if (ki.itemModel() != null) {
            NamespacedKey model = NamespacedKey.fromString(ki.itemModel());
            if (model != null) {
                meta.setItemModel(model);
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    private void place(Player player, PlayerInventory inv, ItemStack item, String slot) {
        if (slot != null) {
            EquipmentSlot eq = switch (slot) {
                case "HEAD", "HELMET" -> EquipmentSlot.HEAD;
                case "CHEST", "CHESTPLATE" -> EquipmentSlot.CHEST;
                case "LEGS", "LEGGINGS" -> EquipmentSlot.LEGS;
                case "FEET", "BOOTS" -> EquipmentSlot.FEET;
                case "OFFHAND", "OFF_HAND" -> EquipmentSlot.OFF_HAND;
                default -> null;
            };
            if (eq != null) {
                ItemStack current = inv.getItem(eq);
                if (current == null || current.getType().isAir()) {
                    inv.setItem(eq, item);
                    return;
                }
            } else {
                try {
                    int index = Integer.parseInt(slot);
                    if (index >= 0 && index < 36) {
                        ItemStack current = inv.getItem(index);
                        if (current == null || current.getType().isAir()) {
                            inv.setItem(index, item);
                            return;
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        inv.addItem(item).values().forEach(rest -> player.getWorld().dropItemNaturally(player.getLocation(), rest));
    }

    // ------------------------------------------------------------ Arbre de compétences

    /** Raison pour laquelle le nœud ne peut pas être amélioré, ou null si c'est possible. */
    public String cannotInvest(PlayerProfile p, SkillTree tree, TreeNode node) {
        int rank = p.getNodeRank(tree.classId() + ":" + node.id());
        if (rank >= node.maxRank()) {
            return plugin.messages().raw("tree.maxed");
        }
        if (p.getLevel() < node.requiredLevel()) {
            return plugin.messages().raw("tree.need-level").replace("<level>", String.valueOf(node.requiredLevel()));
        }
        if (node.requiredSubclass() != null && !node.requiredSubclass().equals(p.getSubclassId())) {
            var sub = plugin.classes().get(node.requiredSubclass());
            return plugin.messages().raw("tree.need-subclass").replace("<subclass>", sub == null ? node.requiredSubclass() : Text.plain(sub.name));
        }
        for (String req : node.requires()) {
            if (p.getNodeRank(tree.classId() + ":" + req) <= 0) {
                TreeNode r = tree.nodes().get(req);
                return plugin.messages().raw("tree.need-node").replace("<node>", r == null ? req : Text.plain(r.name()));
            }
        }
        if (plugin.stats().availableSkillPoints(p) < node.cost()) {
            return plugin.messages().raw("tree.no-points");
        }
        return null;
    }

    public boolean invest(Player player, SkillTree tree, TreeNode node) {
        PlayerProfile p = plugin.profiles().get(player);
        String reason = cannotInvest(p, tree, node);
        if (reason != null) {
            player.sendMessage(Text.mm(plugin.messages().raw("prefix") + "<red>" + reason));
            player.playSound(Sound.sound(Key.key("block.note_block.bass"), Sound.Source.PLAYER, 0.6f, 0.6f));
            return false;
        }
        String key = tree.classId() + ":" + node.id();
        p.setNodeRank(key, p.getNodeRank(key) + 1);
        plugin.stats().apply(player);
        player.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 0.6f, 1.8f));
        return true;
    }

    public boolean resetTree(Player player, boolean admin) {
        PlayerProfile p = plugin.profiles().get(player);
        if (!admin) {
            long cd = plugin.getConfig().getLong("progression.tree-reset-cooldown-hours", 24) * 3_600_000L;
            long left = p.getLastTreeReset() + cd - System.currentTimeMillis();
            if (left > 0) {
                plugin.messages().send(player, "reset-cooldown", Text.ph("time", ((left + 3_599_999) / 3_600_000) + " h"));
                return false;
            }
        }
        p.clearTree();
        p.setLastTreeReset(System.currentTimeMillis());
        plugin.stats().apply(player);
        plugin.messages().send(player, "tree.reset");
        return true;
    }

    // ------------------------------------------------------------ Attributs

    public boolean addAttribute(Player player, AttributeDef attr, int amount) {
        PlayerProfile p = plugin.profiles().get(player);
        int current = p.getAttribute(attr.id());
        int add = Math.min(amount, Math.min(attr.max() - current, plugin.stats().availableAttributePoints(p)));
        if (add <= 0) {
            plugin.messages().send(player, current >= attr.max() ? "attributes.maxed" : "attributes.no-points");
            player.playSound(Sound.sound(Key.key("block.note_block.bass"), Sound.Source.PLAYER, 0.6f, 0.6f));
            return false;
        }
        p.setAttribute(attr.id(), current + add);
        plugin.stats().apply(player);
        player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 0.8f, 1.2f));
        return true;
    }

    public boolean resetAttributes(Player player, boolean admin) {
        PlayerProfile p = plugin.profiles().get(player);
        if (!admin) {
            long cd = plugin.getConfig().getLong("progression.attribute-reset-cooldown-hours", 24) * 3_600_000L;
            long left = p.getLastAttributeReset() + cd - System.currentTimeMillis();
            if (left > 0) {
                plugin.messages().send(player, "reset-cooldown", Text.ph("time", ((left + 3_599_999) / 3_600_000) + " h"));
                return false;
            }
        }
        p.clearAttributes();
        p.setLastAttributeReset(System.currentTimeMillis());
        plugin.stats().apply(player);
        plugin.messages().send(player, "attributes.reset");
        return true;
    }
}
