package li.cinnazeyy.langlibs.core.data;

import com.alpsbte.alpslib.io.database.DatabaseConnection;
import com.alpsbte.alpslib.io.database.DatabaseSection;
import com.alpsbte.alpslib.io.database.SqlHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

public class MysqlDataProvider implements DataProvider {
    private static final Logger logger = LoggerFactory.getLogger(MysqlDataProvider.class);

    private static final String SELECT_LANG = "SELECT uuid, lang FROM langUsers WHERE uuid = ?";
    private static final String UPSERT_LANG = "INSERT INTO langUsers (uuid, lang) VALUES (?, ?) ON DUPLICATE KEY UPDATE uuid = ?, lang = ?";

    private final Map<UUID, String> cache = new HashMap<>();
    private final DatabaseSection credentials;

    public MysqlDataProvider(DatabaseSection credentials) {
        this.credentials = credentials;
    }

    @Override
    public void init(Plugin plugin) {
        try {
            DatabaseConnection.initializeDatabase(credentials, true);
            String initScript = readResource(plugin, "DATABASE.sql");
            try (var con = DatabaseConnection.getConnection(); var s = con.createStatement()) {
                s.execute(initScript);
            }
            Bukkit.getConsoleSender().sendMessage(text("Successfully initialized database connection.", GREEN));
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize database connection", e);
        }
    }

    private static String readResource(Plugin plugin, String name) throws Exception {
        InputStream in = plugin.getResource(name);
        if (in == null) throw new IllegalStateException("Resource not found: " + name);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    @Override
    public void close() {
        cache.clear();
    }

    @Override
    public String getPlayerLang(UUID playerUUID) {
        return cache.get(playerUUID);
    }

    @Override
    public boolean setPlayerLang(Player player, String lang) {
        cache.put(player.getUniqueId(), lang);
        try {
            SqlHelper.runQuery(UPSERT_LANG, ps -> {
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, lang);
                ps.setString(3, player.getUniqueId().toString());
                ps.setString(4, lang);
                ps.executeUpdate();
            });
            return true;
        } catch (SQLException e) {
            logger.error("A SQL error occurred while saving player language!", e);
            return false;
        }
    }

    @Override
    public boolean loadPlayerLang(Player player) {
        final UUID uuid = player.getUniqueId();
        final boolean[] loaded = {false};
        try {
            SqlHelper.runQuery(SELECT_LANG, ps -> {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        cache.put(uuid, rs.getString(2));
                        loaded[0] = true;
                    }
                }
            });
        } catch (SQLException e) {
            logger.error("A SQL error occurred while loading player language!", e);
        }
        return loaded[0];
    }

    @Override
    public void removePlayerLang(UUID playerUUID) {
        cache.remove(playerUUID);
    }
}