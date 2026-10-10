package fr.cykalos.cykclasses.model;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Statistiques d'une classe. Les sous-classes héritent des valeurs de leur parent
 * et ne redéfinissent que ce qu'elles souhaitent changer.
 */
public final class ClassStats {

    /** PV bonus (en demi-cœurs) ajoutés aux 20 PV de base. Peut être négatif. */
    public final double health;
    public final double healthPerLevel;
    public final double mana;
    public final double manaPerLevel;
    /** Mana régénéré par seconde. */
    public final double manaRegen;
    public final double manaRegenPerLevel;
    public final double meleeDamage;
    public final double rangedDamage;
    public final double magicDamage;
    /** Multiplicateur des dégâts reçus (0.8 = -20% de dégâts subis). */
    public final double damageTaken;
    /** Bonus de vitesse de déplacement (0.1 = +10%). */
    public final double speed;
    /** Points d'armure bonus. */
    public final double armor;
    /** Bonus de vitesse d'attaque (0.1 = +10%). */
    public final double attackSpeed;
    /** Chance de coup critique (0.0 - 1.0) pour les attaques de mêlée. */
    public final double critChance;
    public final double critMultiplier;

    public static final ClassStats DEFAULT = new ClassStats(0, 0, 100, 1, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1.5);

    public ClassStats(double health, double healthPerLevel, double mana, double manaPerLevel, double manaRegen,
                      double manaRegenPerLevel, double meleeDamage, double rangedDamage, double magicDamage,
                      double damageTaken, double speed, double armor, double attackSpeed,
                      double critChance, double critMultiplier) {
        this.health = health;
        this.healthPerLevel = healthPerLevel;
        this.mana = mana;
        this.manaPerLevel = manaPerLevel;
        this.manaRegen = manaRegen;
        this.manaRegenPerLevel = manaRegenPerLevel;
        this.meleeDamage = meleeDamage;
        this.rangedDamage = rangedDamage;
        this.magicDamage = magicDamage;
        this.damageTaken = damageTaken;
        this.speed = speed;
        this.armor = armor;
        this.attackSpeed = attackSpeed;
        this.critChance = critChance;
        this.critMultiplier = critMultiplier;
    }

    public static ClassStats load(ConfigurationSection s, ClassStats parent) {
        ClassStats p = parent == null ? DEFAULT : parent;
        if (s == null) {
            return p;
        }
        return new ClassStats(
                s.getDouble("health", p.health),
                s.getDouble("health-per-level", p.healthPerLevel),
                s.getDouble("mana", p.mana),
                s.getDouble("mana-per-level", p.manaPerLevel),
                s.getDouble("mana-regen", p.manaRegen),
                s.getDouble("mana-regen-per-level", p.manaRegenPerLevel),
                s.getDouble("melee-damage", p.meleeDamage),
                s.getDouble("ranged-damage", p.rangedDamage),
                s.getDouble("magic-damage", p.magicDamage),
                s.getDouble("damage-taken", p.damageTaken),
                s.getDouble("speed", p.speed),
                s.getDouble("armor", p.armor),
                s.getDouble("attack-speed", p.attackSpeed),
                s.getDouble("crit-chance", p.critChance),
                s.getDouble("crit-multiplier", p.critMultiplier)
        );
    }

    /** Statistiques résultant de l'application d'un modificateur (race, traits, points, arbre...). */
    public ClassStats apply(StatModifier m) {
        if (m == null || m.isEmpty()) {
            return this;
        }
        return new ClassStats(
                health + m.health,
                healthPerLevel + m.healthPerLevel,
                mana + m.mana,
                manaPerLevel + m.manaPerLevel,
                manaRegen + m.manaRegen,
                manaRegenPerLevel + m.manaRegenPerLevel,
                meleeDamage * m.meleeDamage,
                rangedDamage * m.rangedDamage,
                magicDamage * m.magicDamage,
                damageTaken * m.damageTaken,
                speed + m.speed,
                armor + m.armor,
                attackSpeed + m.attackSpeed,
                Math.max(0, Math.min(1, critChance + m.critChance)),
                Math.max(1, critMultiplier + m.critMultiplier));
    }

    public double maxMana(int level) {
        return Math.max(0, mana + manaPerLevel * (level - 1));
    }

    public double bonusHealth(int level) {
        return health + healthPerLevel * (level - 1);
    }

    public double regen(int level) {
        return manaRegen + manaRegenPerLevel * (level - 1);
    }
}
