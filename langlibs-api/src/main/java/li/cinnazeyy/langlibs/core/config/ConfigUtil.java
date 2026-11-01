package li.cinnazeyy.langlibs.core.config;

public class ConfigUtil {
    private ConfigUtil() {}

    private static ConfigDataProvider configProvider;
    @SuppressWarnings("unused")
    public static void init(ConfigDataProvider provider) {
        configProvider = provider;
    }

    @SuppressWarnings("unused")
    public static MainConfig getConfig() {
        return configProvider.getMainConfig();
    }

    public static LanguageConfig getLanguageConfig() {
        return configProvider.getLanguageConfig();
    }
}
