package li.cinnazeyy.langlibs.core;

import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class EventListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(@NotNull PlayerJoinEvent event) {
        LangLibAPI.loadPlayerLang(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDisconnect(@NonNull PlayerQuitEvent event) {
        LangLibAPI.removePlayerLang(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerClientOptionsChange(@NotNull PlayerClientOptionsChangeEvent e) {
        LangLibAPI.removePlayerLang(e.getPlayer().getUniqueId());
    }
}
