package nl.gjorgdy.objects.connections;

import net.dv8tion.jda.api.entities.User;

public class DiscordUserConnection implements Connection {

    private final User discordUser;

    public DiscordUserConnection(User discordUser) {
        this.discordUser = discordUser;
    }

    @Override
    public Type getType() {
        return Type.DISCORD_USER;
    }

    public User getDiscordUser() {
        return discordUser;
    }
}
