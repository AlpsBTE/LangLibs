package li.cinnazeyy.langlibs.core;

import li.cinnazeyy.langlibs.LangLibs;
import li.cinnazeyy.langlibs.core.config.ConfigDataProvider;
import li.cinnazeyy.langlibs.core.config.LanguageConfig;
import li.cinnazeyy.langlibs.core.config.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.util.NamingSchemes;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

public class YamlConfigDataProvider implements ConfigDataProvider {
    private MainConfig mainConfig = null;
    private LanguageConfig languageConfig = null;

    private CommentedConfigurationNode configRoot = null;
    private CommentedConfigurationNode languageRoot = null;

    private YamlConfigurationLoader configLoader = null;
    private YamlConfigurationLoader languageLoader = null;


    public YamlConfigDataProvider() throws ConfigurateException {
        final ObjectMapper.Factory customFactory = ObjectMapper.factoryBuilder()
                .defaultNamingScheme(NamingSchemes.PASSTHROUGH)
                .build();

        configLoader = YamlConfigurationLoader.builder()
                .path(LangLibs.getPlugin().getDataPath().resolve("config.yml"))
                .defaultOptions(opts -> opts.serializers(build -> build.registerAnnotatedObjects(customFactory)))
                .build();
        languageLoader = YamlConfigurationLoader.builder()
                .path(LangLibs.getPlugin().getDataPath().resolve("languages.yml"))
                .defaultOptions(opts -> opts.serializers(build -> build.registerAnnotatedObjects(customFactory)))
                .build();

        loadConfigFiles();
    }

    @Override
    public MainConfig getMainConfig() {
        return mainConfig;
    }

    @Override
    public LanguageConfig getLanguageConfig() {
        return languageConfig;
    }

    @Override
    public void reloadAllConfigs() {
        try {
            configLoader.save(configRoot);
            languageLoader.save(languageRoot);

            loadConfigFiles();
        } catch (ConfigurateException e) {
            LangLibs.getPlugin().getComponentLogger().warn(text("Could not load configuration files!"));
            Bukkit.getConsoleSender().sendMessage(text("The config files must be configured!", YELLOW));
            LangLibs.getPlugin().getServer().getPluginManager().disablePlugin(LangLibs.getPlugin());
        }
    }

    private void loadConfigFiles() throws ConfigurateException {
        configRoot = configLoader.load();
        mainConfig = configRoot.get(MainConfig.class);

        languageRoot = languageLoader.load();
        languageConfig = languageRoot.get(LanguageConfig.class);
    }
}
