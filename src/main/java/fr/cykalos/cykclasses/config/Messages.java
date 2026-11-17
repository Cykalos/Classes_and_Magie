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

    private static Messages instance;

    public Messages(CykClasses plugin) {
        this.plugin = plugin;
        instance = this;
    }

    /** Text of a key from anywhere (labels used by models and menus). */
    public static String text(String key) {
        return instance == null ? key : instance.raw(key);
    }

    public void load() {
        File file = ConfigFiles.file(plugin, "messages.yml");
        yaml = ConfigFiles.load(plugin, "messages.yml");
        // Keys added in a new version: the plugin's default text.
        int added = ConfigDefaults.fill(plugin, yaml, ConfigFiles.resource("messages.yml"), java.util.Set.of());
        if (added > 0) {
            plugin.getLogger().info(added + " message(s) missing from messages.yml: default text used.");
        }
        // Old default texts replaced in a new version (if the admin did not change them)
        if ("<red>Défauts :".equals(yaml.getString("gui.flaws-header"))) {
            yaml.set("gui.flaws-header", "<red>Faiblesses :");
        }
        prefix = yaml.getString("prefix", "");
    }

    public String raw(String key) {
        String value = yaml.getString(key);
        return value != null ? value : "<red>[missing message: " + key + "]";
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
