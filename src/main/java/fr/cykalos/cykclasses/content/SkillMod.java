package fr.cykalos.cykclasses.content;

import java.util.Map;

/**
 * Bonus appliqué à une compétence : damage +0.1 = +10 % de dégâts/soins, cooldown -0.1 = -10 % de recharge,
 * mana -0.1 = -10 % de coût.
 */
public final class SkillMod {

    public double damage;
    public double cooldown;
    public double mana;

    public static SkillMod load(Map<?, ?> map) {
        SkillMod m = new SkillMod();
        if (map.get("damage") instanceof Number n) m.damage = n.doubleValue();
        if (map.get("cooldown") instanceof Number n) m.cooldown = n.doubleValue();
        if (map.get("mana") instanceof Number n) m.mana = n.doubleValue();
        return m;
    }

    public SkillMod add(SkillMod o, int times) {
        damage += o.damage * times;
        cooldown += o.cooldown * times;
        mana += o.mana * times;
        return this;
    }

    public double damageMultiplier() {
        return Math.max(0, 1 + damage);
    }

    public double cooldownMultiplier() {
        return Math.max(0.1, 1 + cooldown);
    }

    public double manaMultiplier() {
        return Math.max(0, 1 + mana);
    }
}
