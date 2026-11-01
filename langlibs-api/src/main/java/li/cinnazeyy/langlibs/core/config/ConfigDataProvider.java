package li.cinnazeyy.langlibs.core.config;

public interface ConfigDataProvider {
    public MainConfig getMainConfig();
    public LanguageConfig getLanguageConfig();
    public void reloadAllConfigs();
}
