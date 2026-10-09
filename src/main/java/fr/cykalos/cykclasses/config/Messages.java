package fr.cykalos.cykclasses.config;

import fr.cykalos.cykclasses.CykClasses;
import fr.cykalos.cykclasses.util.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        // Valeurs par défaut pour les clés absentes (mise à jour du plugin).
        var stream = plugin.getResource("messages.yml");
        if (stream != null) {
            yaml.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8)));
        }
        prefix = yaml.getString("prefix", "");
    }

    public String raw(String key) {
        return yaml.getString(key, "<red>[message manquant : " + key + "]");
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
