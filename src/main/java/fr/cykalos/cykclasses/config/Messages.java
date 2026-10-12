package fr.cykalos.cykclasses.config;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public final class Messages {

    private final CykClasses plugin;
    private YamlConfiguration yaml;
    private String prefix = "";

    public Messages(CykClasses plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        yaml = YamlConfiguration.loadConfiguration(file);
        // Clés ajoutées dans une nouvelle version : texte par défaut du plugin.
        int added = ConfigDefaults.fill(plugin, yaml, "messages.yml", java.util.Set.of());
        if (added > 0) {
            plugin.getLogger().info(added + " message(s) absent(s) de messages.yml : texte par défaut utilisé.");
        }
        // Anciens textes par défaut remplacés dans une nouvelle version (si l'admin ne les a pas modifiés)
        if ("<red>Défauts :".equals(yaml.getString("gui.flaws-header"))) {
            yaml.set("gui.flaws-header", "<red>Faiblesses :");
        }
        prefix = yaml.getString("prefix", "");
    }

    public String raw(String key) {
        String value = yaml.getString(key);
        return value != null ? value : "<red>[message manquant : " + key + "]";
    }

    public List<String> rawList(String key) {
        return yaml.getStringList(key);
    }

    public Component get(String key, TagResolver... resolvers) {
        return Text.mm(raw(key), resolvers);
    }

    public Component item(String key, TagResolver... resolvers) {
        return Text.item(raw(key), resolvers);
    }

    public void send(Audience to, String key, TagResolver... resolvers) {
        String msg = raw(key);
        if (msg.isEmpty()) {
            return;
        }
        to.sendMessage(Text.mm(prefix + msg, resolvers));
    }

    public void sendList(Audience to, String key) {
        for (String line : rawList(key)) {
            to.sendMessage(Text.mm(line));
        }
    }

    public void actionBar(Audience to, String key, TagResolver... resolvers) {
        to.sendActionBar(Text.mm(raw(key), resolvers));
    }
}
