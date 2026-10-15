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
        // Le libellé prend la couleur de sa valeur (vert = bonus, rouge = malus, blanc = neutre)
        lore.add(Text.item("<gray>• " + color("Mêlée", s.meleeDamage) + " <dark_gray>·</dark_gray> " + color("Distance", s.rangedDamage)
                + " <dark_gray>·</dark_gray> " + color("Magie", s.magicDamage)));
        lore.add(Text.item("<gray>• " + colorInverse("Dégâts subis", s.damageTaken)
                + (s.speed != 0 ? " <dark_gray>·</dark_gray> " + color("Vitesse", 1 + s.speed) : "")
                + (s.critChance > 0 ? " <dark_gray>·</dark_gray> <yellow>Critique " + Math.round(s.critChance * 100) + "%</yellow>" : "")));
        // Faiblesses : défauts de la classe + faiblesses imposées, sous un seul titre
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
                lore.add(Text.item("<light_purple>• </light_purple>" + sk.name + " <dark_gray>(niv. " + sk.unlockLevel + ")"));
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
