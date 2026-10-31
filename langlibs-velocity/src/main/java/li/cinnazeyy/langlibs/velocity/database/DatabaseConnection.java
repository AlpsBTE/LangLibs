package li.cinnazeyy.langlibs.velocity.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import li.cinnazeyy.langlibs.velocity.config.DatabaseCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    private static final String SELECT_LANG = "SELECT lang FROM langUsers WHERE uuid = ?";
    private static final String UPSERT_LANG = "INSERT INTO langUsers (uuid, lang) VALUES (?, ?) ON DUPLICATE KEY UPDATE lang = ?";

    private static HikariDataSource dataSource;

    public static void initialize(DatabaseCredentials credentials) throws ClassNotFoundException {
        Class.forName("org.mariadb.jdbc.Driver");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(credentials.url() + credentials.name());
        config.setUsername(credentials.username());
        config.setPassword(credentials.password());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(3);

        dataSource = new HikariDataSource(config);

        createTables();
    }

    public static void close() {
        if (dataSource == null || dataSource.isClosed()) return;
        dataSource.close();
    }

    public static String getPlayerLang(UUID playerUUID) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_LANG)) {
            ps.setString(1, playerUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) {
            logger.error("A SQL error occurred while loading player language!", e);
        }
        return null;
    }

    public static boolean setPlayerLang(UUID playerUUID, String lang) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(UPSERT_LANG)) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, lang);
            ps.setString(3, lang);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("A SQL error occurred while saving player language!", e);
            return false;
        }
    }

    private static void createTables() {
        final String table = "CREATE TABLE IF NOT EXISTS `langUsers` (" +
                "`uuid` varchar(36) PRIMARY KEY," +
                "`lang` varchar(10)" +
                ");";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(table)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.error("An error occurred while creating database table!", ex);
        }
    }
}