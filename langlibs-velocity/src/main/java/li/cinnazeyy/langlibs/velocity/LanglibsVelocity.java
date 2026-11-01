package li.cinnazeyy.langlibs.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import li.cinnazeyy.langlibs.velocity.config.ConfigLoader;
import li.cinnazeyy.langlibs.velocity.config.MainConfig;
import li.cinnazeyy.langlibs.velocity.database.DatabaseConnection;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

@Plugin(
        id = "langlibs-velocity",
        name = "langlibs-velocity",
        version = "1.6.0",
        description = "Proxy side language preference store for LangLibs",
        authors = {"Cinnazeyy"}
)
public class LanglibsVelocity {

    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("langlibs:main");

    private static final String RESULT_TRUE = "true";
    private static final String RESULT_FALSE = "false";

    @Inject
    private Logger logger;
    @Inject
    private ProxyServer proxyServer;
    @Inject
    @DataDirectory
    private Path dataDirectory;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        proxyServer.getChannelRegistrar().register(IDENTIFIER);

        try {
            MainConfig config = new ConfigLoader(dataDirectory).load();
            if (config == null || config.getCredentials() == null || config.getCredentials().url() == null || config.getCredentials().url().isBlank()) {
                logger.error("No database credentials configured in config.yml!");
                return;
            }
            DatabaseConnection.initialize(config.getCredentials());
            logger.info("Successfully initialized database connection.");
        } catch (Exception e) {
            logger.error("Could not initialize LangLibs-Velocity!", e);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        DatabaseConnection.close();
    }

    @Subscribe
    public void onPluginMessageFromBackend(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(IDENTIFIER)) return;
        event.setResult(PluginMessageEvent.ForwardResult.handled());
        if (!(event.getSource() instanceof ServerConnection backend)) return;

        String message = new String(event.getData(), StandardCharsets.UTF_8);
        String[] args = message.split(";");
        if (args.length < 2) {
            logger.warn("Received malformed plugin message: {}", message);
            return;
        }

        switch (args[0]) {
            case "getPlayerLang" -> handleGetPlayerLang(backend, args);
            case "setPlayerLang" -> handleSetPlayerLang(backend, args);
            default -> logger.error("Invalid method '{}' received from backend!", args[0]);
        }
    }

    private void handleGetPlayerLang(ServerConnection backend, String[] args) {
        try {
            UUID uuid = UUID.fromString(args[1]);
            String lang = DatabaseConnection.getPlayerLang(uuid);
            if (lang == null) return;

            String reply = "playerLang;" + uuid + ";" + lang;
            backend.sendPluginMessage(IDENTIFIER, reply.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException ex) {
            logger.warn("getPlayerLang received invalid uuid: {}", args[1]);
        }
    }

    private void handleSetPlayerLang(ServerConnection backend, String[] args) {
        if (args.length < 3) {
            logger.warn("setPlayerLang received with missing arguments: {}", String.join(";", args));
            sendSetResult(backend, null, false);
            return;
        }
        try {
            UUID uuid = UUID.fromString(args[1]);
            String lang = args[2];
            boolean success = DatabaseConnection.setPlayerLang(uuid, lang);
            sendSetResult(backend, uuid, success);
        } catch (IllegalArgumentException ex) {
            logger.warn("setPlayerLang received invalid uuid: {}", args[1]);
            sendSetResult(backend, null, false);
        }
    }

    private void sendSetResult(ServerConnection backend, UUID uuid, boolean success) {
        String reply = "setPlayerLangResult;" + (uuid == null ? "" : uuid) + ";" + (success ? RESULT_TRUE : RESULT_FALSE);
        backend.sendPluginMessage(IDENTIFIER, reply.getBytes(StandardCharsets.UTF_8));
    }
}