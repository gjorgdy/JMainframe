package nl.gjorgdy.objects.connections;

import net.dv8tion.jda.api.entities.Role;

public class DiscordRoleConnection implements Connection {

    private final Role discordRole;

    public DiscordRoleConnection(Role discordRole) {
        this.discordRole = discordRole;
    }

    @Override
    public Type getType() {
        return Type.DISCORD_ROLE;
    }

    public Role getDiscordRole() {
        return discordRole;
    }
}
