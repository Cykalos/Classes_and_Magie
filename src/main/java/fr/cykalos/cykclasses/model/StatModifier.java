package fr.cykalos.cykclasses.model;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

/**
 * Modificateur de statistiques (race, trait, point d'attribut, nœud d'arbre...).
 *
 * <p>Valeurs <b>additives</b> : health, health-per-level, mana, mana-per-level, mana-regen,
 * mana-regen-per-level, speed, armor, attack-speed, crit-chance, crit-multiplier.<br>
 * Valeurs <b>multiplicatives</b> (1.1 = +10 %) : melee-damage, ranged-damage, magic-damage, damage-taken.</p>
 */
public final class StatModifier {

    public double health, healthPerLevel, mana, manaPerLevel, manaRegen, manaRegenPerLevel;
    public double speed, armor, attackSpeed, critChance, critMultiplier;
    public double meleeDamage = 1, rangedDamage = 1, magicDamage = 1, damageTaken = 1;

    public static StatModifier load(ConfigurationSection s) {
        StatModifier m = new StatModifier();
        if (s == null) {
            return m;
        }
        return load(s.getValues(false));
    }

    public static StatModifier load(Map<?, ?> map) {
        StatModifier m = new StatModifier();
        if (map == null) {
            return m;
        }
        m.health = num(map, "health", 0);
        m.healthPerLevel = num(map, "health-per-level", 0);
        m.mana = num(map, "mana", 0);
        m.manaPerLevel = num(map, "mana-per-level", 0);
        m.manaRegen = num(map, "mana-regen", 0);
        m.manaRegenPerLevel = num(map, "mana-regen-per-level", 0);
        m.speed = num(map, "speed", 0);
        m.armor = num(map, "armor", 0);
        m.attackSpeed = num(map, "attack-speed", 0);
        m.critChance = num(map, "crit-chance", 0);
        m.critMultiplier = num(map, "crit-multiplier", 0);
        m.meleeDamage = num(map, "melee-damage", 1);
        m.rangedDamage = num(map, "ranged-damage", 1);
        m.magicDamage = num(map, "magic-damage", 1);
        m.damageTaken = num(map, "damage-taken", 1);
        return m;
    }

    private static double num(Map<?, ?> map, String key, double def) {
        Object o = map.get(key);
        return o instanceof Number n ? n.doubleValue() : def;
    }

    /** Ajoute {@code times} fois le modificateur {@code o} à celui-ci. */
    public StatModifier add(StatModifier o, int times) {
        if (o == null || times <= 0) {
            return this;
        }
        health += o.health * times;
        healthPerLevel += o.healthPerLevel * times;
        mana += o.mana * times;
        manaPerLevel += o.manaPerLevel * times;
        manaRegen += o.manaRegen * times;
        manaRegenPerLevel += o.manaRegenPerLevel * times;
        speed += o.speed * times;
        armor += o.armor * times;
        attackSpeed += o.attackSpeed * times;
        critChance += o.critChance * times;
        critMultiplier += o.critMultiplier * times;
        meleeDamage *= Math.pow(o.meleeDamage, times);
        rangedDamage *= Math.pow(o.rangedDamage, times);
        magicDamage *= Math.pow(o.magicDamage, times);
        damageTaken *= Math.pow(o.damageTaken, times);
        return this;
    }

    public StatModifier add(StatModifier o) {
        return add(o, 1);
    }

    public boolean isEmpty() {
        return health == 0 && healthPerLevel == 0 && mana == 0 && manaPerLevel == 0 && manaRegen == 0
                && manaRegenPerLevel == 0 && speed == 0 && armor == 0 && attackSpeed == 0 && critChance == 0
                && critMultiplier == 0 && meleeDamage == 1 && rangedDamage == 1 && magicDamage == 1 && damageTaken == 1;
    }

    /** Description courte pour les menus, ex. « +4 PV, +10% Mêlée, -5% Dégâts subis ». */
    public java.util.List<String> describe() {
        java.util.List<String> out = new java.util.ArrayList<>();
        addLine(out, health, "PV", false);
        addLine(out, healthPerLevel, "PV/niv.", false);
        addLine(out, mana, "Mana", false);
        addLine(out, manaPerLevel, "Mana/niv.", false);
        addLine(out, manaRegen, "Régén. mana/s", false);
        addLine(out, armor, "Armure", false);
        addPct(out, speed, "Vitesse");
        addPct(out, attackSpeed, "Vitesse d'attaque");
        addPct(out, critChance, "Critique");
        addLine(out, critMultiplier, "Mult. critique", false);
        addMult(out, meleeDamage, "Mêlée", false);
        addMult(out, rangedDamage, "Distance", false);
        addMult(out, magicDamage, "Magie", false);
        addMult(out, damageTaken, "Dégâts subis", true);
        return out;
    }

    private static void addLine(java.util.List<String> out, double v, String label, boolean inverse) {
        if (Math.abs(v) < 1e-9) {
            return;
        }
        boolean good = inverse ? v < 0 : v > 0;
        out.add((good ? "<green>" : "<red>") + (v > 0 ? "+" : "") + fmt(v) + " " + label);
    }

    private static void addPct(java.util.List<String> out, double v, String label) {
        if (Math.abs(v) < 1e-9) {
            return;
        }
        out.add((v > 0 ? "<green>+" : "<red>") + fmt(v * 100) + "% " + label);
    }

    private static void addMult(java.util.List<String> out, double v, String label, boolean inverse) {
        if (Math.abs(v - 1) < 1e-9) {
            return;
        }
        boolean good = inverse ? v < 1 : v > 1;
        double pct = (v - 1) * 100;
        out.add((good ? "<green>" : "<red>") + (pct > 0 ? "+" : "") + fmt(pct) + "% " + label);
    }

    private static String fmt(double v) {
        double r = Math.round(v * 10) / 10.0;
        return r == Math.floor(r) ? String.valueOf((long) r) : String.valueOf(r);
    }
}
