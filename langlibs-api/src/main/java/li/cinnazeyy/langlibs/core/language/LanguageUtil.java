package li.cinnazeyy.langlibs.core.language;

import li.cinnazeyy.langlibs.core.LangLibAPI;
import li.cinnazeyy.langlibs.core.file.LanguageFile;
import li.cinnazeyy.langlibs.core.file.LanguageFileFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class LanguageUtil extends LanguageFileFactory {
    private static final String CONFIG_VERSION_PATH = "version";
    public LanguageFile[] languageFiles;

    @SuppressWarnings("unused")
    public LanguageUtil(Plugin plugin) {
        super(LangLibAPI.getLanguageFiles(plugin));
        LanguageFile[] langFiles = LangLibAPI.getLanguageFiles(plugin);
        languageFiles = langFiles;

        Arrays.stream(langFiles).forEach(lang -> {
            if (!lang.getFile().exists()) createFile(lang);
            else if (lang.getConfiguredVersion() != lang.getVersion()) updateFile(lang);
            lang.loadFile();
        });
    }

    @SuppressWarnings("unused")
    public String get(CommandSender sender, String key) {
        if (sender instanceof Player p) {
            var languageFile = getLanguageFileByPlayer(p.getUniqueId());
            var translation = languageFile.getTranslation(key);
            if (languageFile.lastTranslationWasNotSuccessful()) translation = languageFiles[0].getTranslation(key);
            return translation;
        }
        return languageFiles[0].getTranslation(key);
    }

    /**
     * Resolves a translation for a specific language, independent of the player's current locale.
     */
    public String get(Language language, String key) {
        return Arrays.stream(languageFiles)
                .filter(langFile -> langFile.getLanguage() == language)
                .findFirst()
                .orElse(languageFiles[0])
                .getTranslation(key);
    }

    @SuppressWarnings("unused")
    public String get(UUID playerUUID, String key) {
        return getLanguageFileByPlayer(playerUUID).getTranslation(key);
    }

    @SuppressWarnings("unused")
    public String get(CommandSender sender, String key, String... args) {
        if (sender instanceof Player p) {
            var languageFile = getLanguageFileByPlayer(p.getUniqueId());
            var translation = languageFile.getTranslation(key, args);
            if (languageFile.lastTranslationWasNotSuccessful()) translation = languageFiles[0].getTranslation(key, args);
            return translation;
        }
        return languageFiles[0].getTranslation(key, args);
    }

    @SuppressWarnings("unused")
    public String get(UUID playerUUID, String key, String... args) {
        return getLanguageFileByPlayer(playerUUID).getTranslation(key, args);
    }

    @SuppressWarnings("unused")
    public Component getComponent(UUID playerUUID, String key, TextColor baseColor, Component... args) {
        Pattern pattern = Pattern.compile("\\{[^}]+}|[^{]+");
        Matcher matcher = pattern.matcher(getLanguageFileByPlayer(playerUUID).getTranslation(key));

        ArrayList<String> result = new ArrayList<>();

        while (matcher.find()) {
            result.add(matcher.group().trim());
        }

        Component outputComponent = Component.empty();
        for (String part : result) {
            if (!part.startsWith("{")) outputComponent = outputComponent.append(Component.text(part, baseColor));
            else {
                try {
                    int index = Integer.parseInt(part.substring(1, 2));
                    outputComponent = outputComponent.append(args[index]);
                } catch (Exception e) {
                    outputComponent = outputComponent.append(Component.text(part, baseColor));
                }
            }
        }
        return outputComponent;
    }

    @SuppressWarnings("unused")
    public List<String> getList(CommandSender sender, String key) {
        if (sender instanceof Player p) {
            var languageFile = getLanguageFileByPlayer(p.getUniqueId());
            var translation = languageFile.getTranslations(key);
            if (languageFile.lastTranslationWasNotSuccessful()) translation = languageFiles[0].getTranslations(key);
            return translation;
        }
        return languageFiles[0].getTranslations(key);
    }

    @SuppressWarnings("unused")
    public List<String> getList(UUID playerUUID, String key) {
        return getLanguageFileByPlayer(playerUUID).getTranslations(key);
    }

    @SuppressWarnings("unused")
    public List<String> getList(CommandSender sender, String key, String... args) {
        if (sender instanceof Player p) {
            var languageFile = getLanguageFileByPlayer(p.getUniqueId());
            var translation = languageFile.getTranslations(key, args);
            if (languageFile.lastTranslationWasNotSuccessful()) translation = languageFiles[0].getTranslations(key, args);
            return translation;
        }
        return languageFiles[0].getTranslations(key, args);
    }

    @SuppressWarnings("unused")
    public List<String> getList(UUID playerUUID, String key, String... args) {
        return getLanguageFileByPlayer(playerUUID).getTranslations(key, args);
    }

    public LanguageFile getLanguageFileByPlayer(@NotNull UUID playerUUID) {
        String locale = LangLibAPI.getPlayerLang(playerUUID);

        return Arrays.stream(languageFiles)
                .filter(lang -> lang.getLanguage().toString().equalsIgnoreCase(locale))
                .findFirst()
                .orElseGet(() -> Arrays.stream(languageFiles)
                        .filter(lang -> lang.additionalLang != null && Arrays.stream(lang.additionalLang).anyMatch(l -> l.equalsIgnoreCase(locale)))
                        .findFirst()
                        .orElse(languageFiles[0]));
    }

    @SuppressWarnings("unused")
    public static String getLocaleTagByPlayer(Player player) {
        return player != null ? player.locale().toString() : null;
    }
}