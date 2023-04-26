package nl.gjorgdy.objects.connections;

import net.dv8tion.jda.api.entities.User;

public class DiscordConnection implements Connection {

    private final User discordUser;

    public DiscordConnection(User discordUser) {
        this.discordUser = discordUser;
    }

    @Override
    public String getType() {
        return "discord";
    }

    public User getDiscordUser() {
        return discordUser;
    }
}
