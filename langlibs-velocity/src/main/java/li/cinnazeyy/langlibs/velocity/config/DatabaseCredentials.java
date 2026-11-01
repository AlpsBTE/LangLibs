package li.cinnazeyy.langlibs.velocity.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record DatabaseCredentials(
        String url,
        String name,
        String username,
        String password
) {}