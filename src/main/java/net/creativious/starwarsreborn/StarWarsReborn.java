package net.creativious.starwarsreborn;

import net.creativious.starwarsreborn.config.StarWarsRebornConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class StarWarsReborn extends JavaPlugin {
    public StarWarsRebornConfig config = new StarWarsRebornConfig();
    public static final String plugin_id = "starwarsreborn";

    @Override
    public void onEnable() {
        config.init(plugin_id);
        // Plugin startup logic;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
