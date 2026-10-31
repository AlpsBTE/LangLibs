package li.cinnazeyy.langlibs.velocity.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class MainConfig {
    @Setting("database")
    private DatabaseCredentials credentials;

    public DatabaseCredentials getCredentials() {
        return credentials;
    }
}
