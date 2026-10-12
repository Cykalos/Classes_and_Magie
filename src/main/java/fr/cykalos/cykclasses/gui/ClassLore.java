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
 * Construit la description (lore) d'une classe pour les menus.
 */
final class ClassLore {

    private ClassLore() {
    }

    static List<Component> describe(CykClasses plugin, RpgClass rc) {
        List<Component> lore = new ArrayList<>(Text.itemLore(rc.description));
        lore.add(Component.empty());
        ClassStats s = rc.stats;
        lore.add(plugin.messages().item("gui.stats-header"));
        lore.add(Text.item("<gray>• PV : <white>" + (s.health >= 0 ? "+" : "") + Text.format(s.health)
                + "</white> <dark_gray>(+" + Text.format(s.healthPerLevel) + "/niv.)"));
        lore.add(Text.item("<gray>• Mana : <aqua>" + Text.format(s.mana) + "</aqua> <dark_gray>(+" + Text.format(s.manaPerLevel)
                + "/niv., " + Text.format(s.manaRegen) + "/s)"));
        lore.add(Text.item("<gray>• Mêlée " + color(s.meleeDamage) + " · Distance " + color(s.rangedDamage)
                + " · Magie " + color(s.magicDamage)));
        lore.add(Text.item("<gray>• Dégâts subis " + colorInverse(s.damageTaken)
                + (s.speed != 0 ? " · Vitesse " + color(1 + s.speed) : "")
                + (s.critChance > 0 ? " · Critique <yellow>" + Math.round(s.critChance * 100) + "%" : "")));
        List<Flaw> flaws = rc.flaws();
        if (!flaws.isEmpty()) {
            lore.add(Component.empty());
            lore.add(plugin.messages().item("gui.flaws-header"));
            for (Flaw f : flaws) {
                lore.add(Text.item("<red>• </red><gray>" + f.description));
            }
        }
        if (plugin.modules().weaknesses()) {
            var imposed = plugin.content().predefinedWeaknesses(null, rc.root().id, rc.isSubclass() ? rc.id : null);
            if (!imposed.isEmpty()) {
                lore.add(Component.empty());
                lore.add(plugin.messages().item("weaknesses.header"));
                imposed.forEach(w -> lore.add(Text.item("<red>• </red>" + w.name())));
            }
        }
        if (!rc.skills().isEmpty()) {
            lore.add(Component.empty());
            lore.add(plugin.messages().item("gui.skills-header"));
            for (SkillDefinition sk : rc.skills().values()) {
                lore.add(Text.item("<light_purple>• </light_purple>" + sk.name + " <dark_gray>(niv. " + sk.unlockLevel + ")"));
            }
        }
        return lore;
    }

    private static String color(double mult) {
        String c = mult > 1.001 ? "<green>" : mult < 0.999 ? "<red>" : "<white>";
        return c + Text.percent(mult);
    }

    private static String colorInverse(double mult) {
        String c = mult < 0.999 ? "<green>" : mult > 1.001 ? "<red>" : "<white>";
        return c + Text.percent(mult);
    }
}
