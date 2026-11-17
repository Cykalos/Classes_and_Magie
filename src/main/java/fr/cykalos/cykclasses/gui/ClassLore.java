package fr.cykalos.cykclasses.gui;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.model.ClassStats;
import fr.cykalos.cykclasses.model.Flaw;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.model.SkillDefinition;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the description (lore) of a class for menus.
 */
final class ClassLore {

    private ClassLore() {
    }

    static List<Component> describe(CykClasses plugin, RpgClass rc) {
        List<Component> lore = new ArrayList<>(Text.itemLore(rc.description));
        lore.add(Component.empty());
        ClassStats s = rc.stats;
        lore.add(plugin.messages().item("gui.stats-header"));
        lore.add(Text.item("<gray>• " + fr.cykalos.cykclasses.config.Lang.tr("PV", "Health") + ": <white>" + (s.health >= 0 ? "+" : "") + Text.format(s.health)
                + "</white> <dark_gray>(+" + Text.format(s.healthPerLevel) + "/" + fr.cykalos.cykclasses.config.Lang.tr("niv.", "lv.") + ")"));
        lore.add(Text.item("<gray>• Mana: <aqua>" + Text.format(s.mana) + "</aqua> <dark_gray>(+" + Text.format(s.manaPerLevel)
                + "/" + fr.cykalos.cykclasses.config.Lang.tr("niv.", "lv.") + ", " + Text.format(s.manaRegen) + "/s)"));
        // The label takes the colour of its value (green = bonus, red = penalty, white = neutral)
        lore.add(Text.item("<gray>• " + color(fr.cykalos.cykclasses.config.Lang.tr("Mêlée", "Melee"), s.meleeDamage) + " <dark_gray>·</dark_gray> " + color(fr.cykalos.cykclasses.config.Lang.tr("Distance", "Ranged"), s.rangedDamage)
                + " <dark_gray>·</dark_gray> " + color(fr.cykalos.cykclasses.config.Lang.tr("Magie", "Magic"), s.magicDamage)));
        lore.add(Text.item("<gray>• " + colorInverse(fr.cykalos.cykclasses.config.Lang.tr("Dégâts subis", "Damage taken"), s.damageTaken)
                + (s.speed != 0 ? " <dark_gray>·</dark_gray> " + color(fr.cykalos.cykclasses.config.Lang.tr("Vitesse", "Speed"), 1 + s.speed) : "")
                + (s.critChance > 0 ? " <dark_gray>·</dark_gray> <yellow>" + fr.cykalos.cykclasses.config.Lang.tr("Critique", "Critical") + " " + Math.round(s.critChance * 100) + "%</yellow>" : "")));
        // Weaknesses: class flaws + imposed weaknesses, under a single heading
        List<Flaw> flaws = rc.flaws();
        var imposed = plugin.modules().weaknesses()
                ? plugin.content().predefinedWeaknesses(null, rc.root().id, rc.isSubclass() ? rc.id : null)
                : List.<fr.cykalos.cykclasses.content.Weakness>of();
        if (!flaws.isEmpty() || !imposed.isEmpty()) {
            lore.add(Component.empty());
            lore.add(plugin.messages().item("gui.flaws-header"));
            for (Flaw f : flaws) {
                lore.add(Text.item("<red>• </red><gray>" + f.description));
            }
            imposed.forEach(w -> lore.add(Text.item("<red>• </red>" + w.name())));
        }
        if (!rc.skills().isEmpty()) {
            lore.add(Component.empty());
            lore.add(plugin.messages().item("gui.skills-header"));
            for (SkillDefinition sk : rc.skills().values()) {
                lore.add(Text.item("<light_purple>• </light_purple>" + sk.name + " <dark_gray>(" + fr.cykalos.cykclasses.config.Lang.tr("niv.", "lv.") + " " + sk.unlockLevel + ")"));
            }
        }
        return lore;
    }

    private static String color(String label, double mult) {
        String c = mult > 1.001 ? "green" : mult < 0.999 ? "red" : "white";
        return "<" + c + ">" + label + " " + Text.percent(mult) + "</" + c + ">";
    }

    private static String colorInverse(String label, double mult) {
        String c = mult < 0.999 ? "green" : mult > 1.001 ? "red" : "white";
        return "<" + c + ">" + label + " " + Text.percent(mult) + "</" + c + ">";
    }
}
