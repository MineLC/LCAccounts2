package lc.lcaccounts.configuration;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;

public class LCConfig {

    private final String name;
    private final Plugin plugin;
    Configuration configurationConfig;

    public LCConfig(String name, Plugin plugin) {
        this.name = name;
        this.plugin = plugin;
        registerConfig();
    }

    public String getName() {
        return name;
    }

    public Configuration getConfig() {
        if (this.configurationConfig == null)
            reloadConfig();
        return this.configurationConfig;
    }

    public void registerConfig() {
        File file = new File(plugin.getDataFolder(), getName()+".yml");
        if (!file.exists())
            try (InputStream in = plugin.getResourceAsStream(getName()+".yml")) {
                Files.copy(in, file.toPath(), new CopyOption[0]);
                this.configurationConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), getName()+".yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void reloadConfig() {
        registerConfig();
        try {
            this.configurationConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), getName()+".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(this.configurationConfig, new File(plugin.getDataFolder(), getName()+".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
