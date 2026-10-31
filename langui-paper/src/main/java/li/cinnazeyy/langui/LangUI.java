package li.cinnazeyy.langui;

import com.alpsbte.alpslib.utils.head.HeadLookupService;
import li.cinnazeyy.langui.commands.CMD_Language;
import li.cinnazeyy.langui.util.LangUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import static net.kyori.adventure.text.Component.text;

public final class LangUI extends JavaPlugin {

    private static LangUI plugin;

    private HeadLookupService headLookupService;

    public static LangUI getPlugin() {
        return plugin;
    }
    @Override
    public void onEnable() {
        // Plugin startup login
        plugin = this;

        // Register Events
        getServer().getPluginManager().registerEvents(new MenuFunctionListener(), plugin);

        // Register Commands
        final CommandMap commandMap = Bukkit.getServer().getCommandMap();
        commandMap.register("LangUI", new CMD_Language("language"));

        // Load config
        createConfig();

        if (LangUI.getPlugin().getConfig().getBoolean("languageSelection.useHeads")) {
            headLookupService = HeadLookupService.createForPlugin(this).registerEvents(this);
        }

        // Load language files
        try {
            LangUtil.init();
            getLogger().info("Successfully loaded language files.");
        } catch (Exception ex) {
            getPlugin().getComponentLogger().error(text("Unable to load language files."), ex);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void createConfig() {
        File createConfig = new File(getDataFolder(), "config.yml");
        if (!createConfig.exists()) {
            saveResource("config.yml", false);
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(createConfig);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE,e.getMessage());
        }
    }

    public HeadLookupService getHeadLookupService() {
        return headLookupService;
    }
}
