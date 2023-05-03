package nl.gjorgdy.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.records.RoleRecord;
import nl.gjorgdy.database.records.UserRecord;
import nl.gjorgdy.database.records.identifiers.Identifier;
import nl.gjorgdy.database.records.identifiers.LongIdentifier;

public class Functions {

    public static Identifier createIdentifier(User user) {
        return new LongIdentifier(Identifier.Types.discord_user, user.getIdLong());
    }
    public static UserRecord getUserRecord(User user) {
        return Main.MONGODB.userHandler.get(createIdentifier(user));
    }

    public static Identifier createIdentifier(Role role) {
        return new LongIdentifier(Identifier.Types.discord_role, role.getIdLong());
    }
    public static RoleRecord getRoleRecord(Role role) {
        return Main.MONGODB.roleHandler.get(createIdentifier(role));
    }

    public static Identifier createIdentifier(Channel channel) {
        return new LongIdentifier(Identifier.Types.discord_channel, channel.getIdLong());
    }

    public static Identifier createIdentifier(Guild guild) {
        return new LongIdentifier(Identifier.Types.discord_guild, guild.getIdLong());
    }

}
