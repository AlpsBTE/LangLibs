package li.cinnazeyy.langlibs.core.config;

import com.alpsbte.alpslib.io.database.DatabaseSection;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@SuppressWarnings("unused")
@ConfigSerializable
public class MainConfig {
    public MainConfig() {}

    @Comment("Where player language preferences are stored / fetched from.\n" +
            "Options: mysql | velocity")
    @Setting("data-source")
    private String dataSource = "mysql";

    @Setting("database")
    private DatabaseSection credentials;

    @Comment("NOTE: Do not change!")
    @Setting("version")
    private int version;

    public String getDataSource() {
        return dataSource == null ? "mysql" : dataSource.toLowerCase();
    }

    public DatabaseSection getCredentials() {
        return credentials;
    }

    public int getVersion() {
        return version;
    }
}
