package nl.gjorgdy.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.database.identifiers.LongIdentifier;
public class Functions {

    public static Identifier createIdentifier(User user) {
        return new LongIdentifier(Identifier.Types.discord_user, user.getIdLong());
    }

    public static Identifier createIdentifier(Role role) {
        return new LongIdentifier(Identifier.Types.discord_role, role.getIdLong());
    }

    public static Identifier createIdentifier(Channel channel) {
        return new LongIdentifier(Identifier.Types.discord_channel, channel.getIdLong());
    }

    public static Identifier createIdentifier(Guild guild) {
        return new LongIdentifier(Identifier.Types.discord_guild, guild.getIdLong());
    }

}
