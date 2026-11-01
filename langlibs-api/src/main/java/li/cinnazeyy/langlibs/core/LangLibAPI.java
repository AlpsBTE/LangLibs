package li.cinnazeyy.langlibs.core;

import li.cinnazeyy.langlibs.core.data.DataProvider;
import li.cinnazeyy.langlibs.core.event.LanguageChangeEvent;
import li.cinnazeyy.langlibs.core.file.LanguageFile;
import li.cinnazeyy.langlibs.core.language.Language;
import li.cinnazeyy.langlibs.core.language.LanguageUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

@SuppressWarnings("unused")
public class LangLibAPI {

    private static final HashMap<Plugin, LanguageFile[]> pluginLangFiles = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(LangLibAPI.class);

    private static DataProvider dataProvider;
    private static Plugin plugin;

    @SuppressWarnings("unused")
    public static void register(Plugin plugin, LanguageFile[] langFiles) {
        pluginLangFiles.put(plugin,langFiles);
        Bukkit.getConsoleSender().sendMessage(text("Registered plugin " + plugin.getName() + " to the language system", NamedTextColor.GREEN));
    }

    public static void setDataProvider(DataProvider provider, Plugin owningPlugin) {
        dataProvider = provider;
        plugin = owningPlugin;
        provider.init(owningPlugin);
    }

    public static DataProvider getDataProvider() {
        return dataProvider;
    }

    public static String getPlayerLang(UUID playerUUID) {
        if (dataProvider != null) {
            String lang = dataProvider.getPlayerLang(playerUUID);
            if (lang != null) return lang;
        }
        // No persisted value (yet) -> fall back to the client's locale tag.
        Player p = Bukkit.getPlayer(playerUUID);
        if (p != null) {
            String locale = LanguageUtil.getLocaleTagByPlayer(p);
            if (locale != null) return locale;
        }
        return "en_US";
    }

    public static void setPlayerLang(Player player, String lang) {
        if (dataProvider == null || plugin == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = dataProvider.setPlayerLang(player, lang);
            if (!success) return;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                fireLanguageChangeEvent(player, lang);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            });
        });
    }

    public static void removePlayerLang(UUID playerUUID) {
        if (dataProvider != null) dataProvider.removePlayerLang(playerUUID);
    }

    public static void loadPlayerLang(Player player) {
        if (dataProvider == null || plugin == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean loaded = dataProvider.loadPlayerLang(player);
            if (!loaded) return;
            String lang = dataProvider.getPlayerLang(player.getUniqueId());
            if (lang == null) return;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                fireLanguageChangeEvent(player, lang);
            });
        });
    }

    public static LanguageFile @NonNull [] getLanguageFiles(Plugin plugin) {
        LanguageFile[] languageFiles = pluginLangFiles.get(plugin);
        if (languageFiles == null) throw new RuntimeException("LanguageAPI has not been registered yet!");
        return languageFiles;
    }

    private static void fireLanguageChangeEvent(Player player, String lang) {
        if (plugin == null) {
            logger.warn("Cannot fire LanguageChangeEvent: no plugin reference available.");
            return;
        }
        try {
            Bukkit.getPluginManager().callEvent(new LanguageChangeEvent(player, Language.valueOf(lang)));
        } catch (IllegalArgumentException ex) {
            logger.warn("Unknown language '{}' for {} - skipping LanguageChangeEvent", lang, player.getName());
        }
    }
}