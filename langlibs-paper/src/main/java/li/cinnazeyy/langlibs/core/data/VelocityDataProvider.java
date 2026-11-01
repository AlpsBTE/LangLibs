package li.cinnazeyy.langlibs.core.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class VelocityDataProvider implements DataProvider, PluginMessageListener {
    public static final String CHANNEL = "langlibs:main";
    private static final long RESPONSE_TIMEOUT_SECONDS = 2;
    private static final Logger logger = LoggerFactory.getLogger(VelocityDataProvider.class);

    private final Map<UUID, String> cache = new HashMap<>();
    private final Map<UUID, CompletableFuture<String>> pendingLoads = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Boolean>> pendingSets = new ConcurrentHashMap<>();

    private Plugin plugin;

    @Override
    public void init(Plugin plugin) {
        this.plugin = plugin;
        var messenger = plugin.getServer().getMessenger();
        messenger.registerOutgoingPluginChannel(plugin, CHANNEL);
        messenger.registerIncomingPluginChannel(plugin, CHANNEL, this);
    }

    @Override
    public void close() {
        if (plugin != null) {
            var messenger = plugin.getServer().getMessenger();
            messenger.unregisterOutgoingPluginChannel(plugin, CHANNEL);
            messenger.unregisterIncomingPluginChannel(plugin, CHANNEL, this);
        }
        cache.clear();
        pendingLoads.clear();
        pendingSets.clear();
    }

    @Override
    public String getPlayerLang(UUID playerUUID) {
        return cache.get(playerUUID);
    }

    @Override
    public boolean setPlayerLang(Player player, String lang) {
        UUID uuid = player.getUniqueId();
        cache.put(uuid, lang);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        pendingSets.put(uuid, future);
        try {
            sendToProxy(uuid, "setPlayerLang;" + uuid + ";" + lang);
            return future.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException | TimeoutException e) {
            return false;
        } finally {
            pendingSets.remove(uuid);
        }
    }

    @Override
    public boolean loadPlayerLang(Player player) {
        UUID uuid = player.getUniqueId();
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingLoads.put(uuid, future);
        try {
            sendToProxy(uuid, "getPlayerLang;" + uuid);
            String lang = future.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (lang != null) cache.put(uuid, lang);
            return lang != null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException | TimeoutException e) {
            return false;
        } finally {
            pendingLoads.remove(uuid);
        }
    }

    @Override
    public void removePlayerLang(UUID playerUUID) {
        cache.remove(playerUUID);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!CHANNEL.equalsIgnoreCase(channel)) return;

        String[] args = new String(message, StandardCharsets.UTF_8).split(";");
        if (args.length < 2) {
            logger.warn("Received malformed message on channel {}: {}", channel, new String(message));
            return;
        }

        switch (args[0]) {
            case "playerLang" -> handlePlayerLangReply(args);
            case "setPlayerLangResult" -> handleSetPlayerLangResult(args);
            default -> logger.warn("Received unknown message type '{}' on channel {}", args[0], channel);
        }
    }

    private void handlePlayerLangReply(String[] args) {
        if (args.length < 3) {
            logger.warn("Received malformed playerLang reply: {}", String.join(";", args));
            return;
        }
        try {
            UUID uuid = UUID.fromString(args[1]);
            String lang = args[2];
            CompletableFuture<String> future = pendingLoads.remove(uuid);
            if (future != null) future.complete(lang);
        } catch (IllegalArgumentException ex) {
            logger.warn("Received invalid uuid in proxy reply: {}", args[1]);
        }
    }

    private void handleSetPlayerLangResult(String[] args) {
        if (args.length < 3) {
            logger.warn("Received malformed setPlayerLangResult reply: {}", String.join(";", args));
            return;
        }
        try {
            UUID uuid = UUID.fromString(args[1]);
            boolean success = Boolean.parseBoolean(args[2]);
            CompletableFuture<Boolean> future = pendingSets.remove(uuid);
            if (future != null) future.complete(success);
        } catch (IllegalArgumentException ex) {
            logger.warn("Received invalid uuid in setPlayerLangResult reply: {}", args[1]);
        }
    }

    private void sendToProxy(UUID playerUUID, String payload) {
        if (plugin == null) return;
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) {
            logger.debug("Skipping proxy message for {}: player is offline", playerUUID);
            return;
        }
        player.sendPluginMessage(plugin, CHANNEL, payload.getBytes(StandardCharsets.UTF_8));
    }
}