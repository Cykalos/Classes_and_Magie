package fr.cykalos.cykclasses.hook;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.data.PlayerProfile;
import fr.cykalos.cykclasses.model.RpgClass;
import fr.cykalos.cykclasses.util.Text;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Placeholders : %cykclasses_class%, _subclass, _class_id, _level, _xp, _xp_required,
 * _xp_percent, _mana, _max_mana.
 */
public final class PlaceholderHook extends PlaceholderExpansion {

    private final CykClasses plugin;

    public PlaceholderHook(CykClasses plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cykclasses";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Cykalos";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        PlayerProfile profile = plugin.profiles().getIfLoaded(player.getUniqueId());
        if (profile == null) {
            return "";
        }
        RpgClass base = plugin.classes().get(profile.getClassId());
        RpgClass effective = plugin.classes().effective(profile);
        return switch (params.toLowerCase()) {
            case "class" -> base == null ? "Aucune" : Text.plain(base.name);
            case "class_id" -> base == null ? "" : base.id;
            case "subclass" -> {
                RpgClass sub = plugin.classes().get(profile.getSubclassId());
                yield sub == null ? "" : Text.plain(sub.name);
            }
            case "display" -> effective == null ? "Aucune" : Text.plain(effective.name);
            case "level" -> String.valueOf(profile.getLevel());
            case "xp" -> String.valueOf((long) profile.getXp());
            case "xp_required" -> String.valueOf((long) plugin.levels().xpRequired(profile.getLevel()));
            case "xp_percent" -> String.valueOf((int) (profile.getXp() / plugin.levels().xpRequired(profile.getLevel()) * 100));
            case "mana" -> String.valueOf((int) profile.getMana());
            case "max_mana" -> String.valueOf((int) plugin.stats().maxMana(profile));
            case "race" -> {
                var race = plugin.content().race(profile.getRaceId());
                yield race == null ? "" : Text.plain(race.name());
            }
            case "job" -> {
                var job = plugin.content().job(profile.getJobId());
                yield job == null ? "" : Text.plain(job.name());
            }
            case "traits" -> {
                java.util.List<String> names = new java.util.ArrayList<>();
                for (String id : profile.getTraits()) {
                    var t = plugin.content().trait(id);
                    if (t != null) {
                        names.add(Text.plain(t.name()));
                    }
                }
                yield String.join(", ", names);
            }
            case "weaknesses" -> String.join(", ", plugin.character().allWeaknessLabels(profile));
            case "weaknesses_count" -> String.valueOf(plugin.character().allWeaknessLabels(profile).size());
            case "skill_points" -> String.valueOf(plugin.stats().availableSkillPoints(profile));
            case "attribute_points" -> String.valueOf(plugin.stats().availableAttributePoints(profile));
            case "party_chat" -> profile.isPartyChat() ? "groupe" : "global";
            default -> null;
        };
    }
}
