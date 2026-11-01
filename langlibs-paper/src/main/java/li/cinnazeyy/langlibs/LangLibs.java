package li.cinnazeyy.langlibs;

import li.cinnazeyy.langlibs.core.EventListener;
import li.cinnazeyy.langlibs.core.YamlConfigDataProvider;
import li.cinnazeyy.langlibs.core.config.ConfigUtil;
import li.cinnazeyy.langlibs.core.data.DataProvider;
import li.cinnazeyy.langlibs.core.data.MysqlDataProvider;
import li.cinnazeyy.langlibs.core.data.VelocityDataProvider;
import li.cinnazeyy.langlibs.core.LangLibAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class LangLibs extends JavaPlugin {
    private static final String VERSION = "1.6.0";
    private static LangLibs plugin;
    private static YamlConfigDataProvider configProvider;
    private static DataProvider dataProvider;

    @Override
    public void onEnable() {
        plugin = this;

        // Initialize configs
        createConfig("config.yml");
        createConfig("languages.yml");
        try {
            updateLanguagesConfigIfNeeded();
            configProvider = new YamlConfigDataProvider();
            ConfigUtil.init(configProvider);
        } catch (IOException e) {
            this.getComponentLogger().warn(text("Could not load configuration files!"), e);
            Bukkit.getConsoleSender().sendMessage(text("The config files must be configured!", YELLOW));
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize data provider
        if (!initDataProvider()) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register Events
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        // Startup graphic
        Bukkit.getConsoleSender().sendMessage(empty());
        Bukkit.getConsoleSender().sendMessage(text(" ▄▄▄    ▄▀", AQUA));
        Bukkit.getConsoleSender().sendMessage(text("█   █  ▀█▀▀▀▀▄▄", AQUA));
        Bukkit.getConsoleSender().sendMessage(text("█▀▀▀█    ▀     ▀▀▄", AQUA));
        Bukkit.getConsoleSender().sendMessage(text("█   █  ▄▄█▀▀▀▄▄   ▀▄", AQUA));
        Bukkit.getConsoleSender().sendMessage(text("▄    ▄█▀  ▄▄▄███▄  ▀▄", AQUA));
        Bukkit.getConsoleSender().sendMessage(text("█   ▄█▀ ▄▄ ██▀▀██▄  ▀▄", AQUA));
        Bukkit.getConsoleSender().sendMessage(text("█   █▄  ▀▀█▀  █▀██   █", AQUA));
        Bukkit.getConsoleSender().sendMessage(text("▀▄  ▀█   ▄▄██▄ ▀▄▀   █", AQUA));
        Bukkit.getConsoleSender().sendMessage(text(" ▀▄  ▀█ ▄█████ ▄▀    ▀", AQUA));
        Bukkit.getConsoleSender().sendMessage(text("  ▀▄   ▀▀████▀▀  ▄▄█▄▄▄", AQUA));
        Bukkit.getConsoleSender().sendMessage(text("    ▀▄▄     ▄     ▄█▄▄▄", AQUA));
        Bukkit.getConsoleSender().sendMessage(text("       ▀▀▄▄▄▄█▄  █ █▄▀ █", AQUA));
        Bukkit.getConsoleSender().sendMessage(text("            ▄▀   ▀▀▀  ▀", AQUA));
        Bukkit.getConsoleSender().sendMessage(empty());

        Bukkit.getConsoleSender().sendMessage(text("[", DARK_GRAY)
                .append(text("LangLibs", AQUA))
                .append(text(" v" + VERSION, GOLD))
                .append(text("] Loaded successfully!")));
    }

    public void createConfig(String configFileName) {
        if (Files.exists(getDataPath().resolve(configFileName))) return;
        saveResource(configFileName, false);
    }

    /**
     * Merges newly added language entries from the jar default into an existing
     * languages.yml when its version is behind, preserving user customizations.
     */
    private void updateLanguagesConfigIfNeeded() throws IOException, ConfigurateException {
        Path languagesPath = getDataPath().resolve("languages.yml");
        if (!Files.exists(languagesPath)) return;

        YamlConfigurationLoader fileLoader = YamlConfigurationLoader.builder()
                .path(languagesPath)
                .build();
        CommentedConfigurationNode existing = fileLoader.load();
        int existingVersion = existing.node("version").getInt(0);

        String defaultYaml;
        try (InputStream in = Objects.requireNonNull(getResource("languages.yml"), "Missing languages.yml resource");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            defaultYaml = reader.lines().collect(Collectors.joining("\n"));
        }
        CommentedConfigurationNode defaults = YamlConfigurationLoader.builder()
                .source(() -> new BufferedReader(new StringReader(defaultYaml)))
                .build()
                .load();

        int defaultVersion = defaults.node("version").getInt(0);
        if (existingVersion >= defaultVersion) return;

        boolean changed = false;
        for (Map.Entry<Object, CommentedConfigurationNode> entry : defaults.childrenMap().entrySet()) {
            Object key = entry.getKey();
            if ("version".equals(String.valueOf(key))) continue;

            CommentedConfigurationNode target = existing.node(key);
            if (target.virtual() || target.empty()) {
                target.from(entry.getValue());
                changed = true;
            }
        }

        existing.node("version").set(defaultVersion);
        fileLoader.save(existing);

        if (changed) {
            Bukkit.getConsoleSender().sendMessage(text("Updated languages.yml from v"
                    + existingVersion + " to v" + defaultVersion + ".", AQUA));
        }
    }

    private boolean initDataProvider() {
        String source = ConfigUtil.getConfig().getDataSource();
        switch (source) {
            case "mysql","velocity" -> {}
            default -> {
                Bukkit.getConsoleSender().sendMessage(text("Unknown 'data-source' value: " + source + ". Falling back to mysql.", YELLOW));
                source = "mysql";
            }
        }

        // suppressed for now, since there will likely be more types in the future
        //noinspection SwitchStatementWithTooFewBranches
        dataProvider = switch (source) {
            case "velocity" -> new VelocityDataProvider();
            default -> new MysqlDataProvider(ConfigUtil.getConfig().getCredentials());
        };

        try {
            LangLibAPI.setDataProvider(dataProvider, this);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(text("Could not initialize data provider!", RED));
            LangLibs.getPlugin().getComponentLogger().error(text(e.getMessage()), e);
            return false;
        }

        Bukkit.getConsoleSender().sendMessage(text("Using '" + source + "' data provider.", AQUA));
        return true;
    }

    @Override
    public void reloadConfig() {
        configProvider.reloadAllConfigs();
    }

    @Override
    public void onDisable() {
        if (dataProvider != null) dataProvider.close();
    }

    public static LangLibs getPlugin() {return plugin;}
}
