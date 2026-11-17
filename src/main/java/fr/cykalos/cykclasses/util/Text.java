package fr.cykalos.cykclasses.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Small MiniMessage helpers.
 */
public final class Text {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private Text() {
    }

    public static Component mm(String input, TagResolver... resolvers) {
        if (input == null) {
            return Component.empty();
        }
        return MM.deserialize(input, resolvers);
    }

    /** Component for an item name/lore (not italic by default). */
    public static Component item(String input, TagResolver... resolvers) {
        return mm(input, resolvers).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static List<Component> itemLore(List<String> lines, TagResolver... resolvers) {
        List<Component> out = new ArrayList<>();
        for (String line : lines) {
            out.add(item(line, resolvers));
        }
        return out;
    }

    public static TagResolver ph(String key, Object value) {
        return Placeholder.parsed(key, String.valueOf(value));
    }

    public static TagResolver phc(String key, Component value) {
        return Placeholder.component(key, value);
    }

    public static String plain(String miniMessage) {
        return PlainTextComponentSerializer.plainText().serialize(mm(miniMessage));
    }

    public static String format(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    public static String percent(double multiplier) {
        long pct = Math.round((multiplier - 1.0) * 100);
        return (pct >= 0 ? "+" : "") + pct + "%";
    }
}
