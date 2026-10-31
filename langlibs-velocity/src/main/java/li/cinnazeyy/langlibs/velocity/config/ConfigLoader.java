package li.cinnazeyy.langlibs.velocity.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.util.NamingSchemes;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    private final YamlConfigurationLoader loader;

    public ConfigLoader(Path dataDirectory) {
        try {
            Files.createDirectories(dataDirectory);
        } catch (Exception e) {
            logger.warn("Could not create plugin data directory!", e);
        }

        Path configPath = dataDirectory.resolve("config.yml");
        if (!Files.exists(configPath)) {
            try (InputStream in = ConfigLoader.class.getResourceAsStream("/config.yml")) {
                if (in != null) Files.copy(in, configPath);
            } catch (Exception e) {
                logger.warn("Could not write default config.yml!", e);
            }
        }

        final ObjectMapper.Factory factory = ObjectMapper.factoryBuilder()
                .defaultNamingScheme(NamingSchemes.PASSTHROUGH)
                .build();

        this.loader = YamlConfigurationLoader.builder()
                .path(configPath)
                .defaultOptions(opts -> opts.serializers(build -> build.registerAnnotatedObjects(factory)))
                .build();
    }

    public MainConfig load() throws ConfigurateException {
        CommentedConfigurationNode root = loader.load();
        return root.get(MainConfig.class);
    }
}