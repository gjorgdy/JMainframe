package nl.gjorgdy.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import nl.gjorgdy.Mainframe;
import nl.gjorgdy.database.handlers.RoleHandler;
import nl.gjorgdy.database.handlers.ServerHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.database.identifiers.Types;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Functions {

    public static List<Identifier> getGuildRoleIdentifiers(Guild guild) {
        return guild.getRoles().parallelStream().map(Functions::createIdentifier).toList();
    }

    public static List<Identifier> getMemberRoleIdentifiers(Member member) {
        return member.getRoles().parallelStream().map(Functions::createIdentifier).toList();
    }

    @Nullable
    public static Member getGuildMember(List<Identifier> userIdentifiers, Guild guild) {
        for (Identifier uid : userIdentifiers) {
            if (uid.type() == Types.discord_user) {
                Member member = guild.getMemberById((Long) uid.id());
                if (member != null) return member;
            }
        }
        return null;
    }

    public static boolean guildDoesSyncDisplayName(Guild guild) {
        ServerHandler sh = Mainframe.SERVERS;
        return sh.doesSyncDisplayNames(sh.getFilter(createIdentifier(guild)));
    }

    public static boolean isLinked(Role role) {
        RoleHandler rh = Mainframe.ROLES;
        return rh.exists(rh.getFilter(createIdentifier(role)));
    }

    public static boolean isLinked(Identifier roleIdentifier) {
        RoleHandler rh = Mainframe.ROLES;
        return rh.exists(rh.getFilter(roleIdentifier));
    }

    public static Identifier createIdentifier(Member member) {
        return Identifier.create(Types.discord_user, member.getUser().getIdLong());
    }

    public static Identifier createIdentifier(User user) {
        return Identifier.create(Types.discord_user, user.getIdLong());
    }

    public static Identifier createIdentifier(Role role) {
        return Identifier.create(Types.discord_role, role.getIdLong());
    }

    public static Identifier createIdentifier(Channel channel) {
        return Identifier.create(Types.discord_channel, channel.getIdLong());
    }

    public static Identifier createIdentifier(Guild guild) {
        return Identifier.create(Types.discord_guild, guild.getIdLong());
    }

}
