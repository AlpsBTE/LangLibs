package li.cinnazeyy.langlibs.core.data;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

@SuppressWarnings("unused")
public interface DataProvider {
    void init(Plugin plugin);
    void close();

    String getPlayerLang(UUID playerUUID);
    boolean setPlayerLang(Player player, String lang);
    boolean loadPlayerLang(Player player);
    void removePlayerLang(UUID playerUUID);
}