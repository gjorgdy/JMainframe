package nl.gjorgdy.discord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import nl.gjorgdy.Mainframe;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.handlers.ServerHandler;
import nl.gjorgdy.database.handlers.UserHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.database.identifiers.Types;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Syncing values between Discord and Mainframe
 * Read : Discord -> Mainframe
 * Write : Mainframe -> Discord
 *
 */
public class Sync {

    public static void member(Member member, boolean syncDisplayName) {
        // Sync Display Name
        if (syncDisplayName) {
            try {
                Sync.writeDisplayName(member);
            } catch (NotRegisteredException e) {
                Discord.logger.error("Failed to sync roles of member '" + member.getEffectiveName() + "' \n" + e);
            }
        }
        // Sync Roles - additive
        try {
            Sync.readRolesAdditive(member);
            Sync.writeRolesAdditive(member);
        } catch (NotRegisteredException e) {
            Discord.logger.error("Failed to sync roles of member '" + member.getEffectiveName() + "' \n" + e);
        }
    }

    public static void readDisplayName(Member member) {
        ServerHandler sh = Mainframe.SERVERS;
        Identifier guildIdentifier = Functions.createIdentifier(member.getGuild());
        UserHandler uh = Mainframe.USERS;
        Identifier userIdentifier = Functions.createIdentifier(member);
        Bson userFilter = uh.getFilter(userIdentifier);
        try {
            String[] affix = sh.getAffix(sh.getFilter(guildIdentifier));
            String memberDisplayName = member.getEffectiveName();
            uh.setDisplayName(userFilter, memberDisplayName, affix);
        } catch (InvalidDisplayNameException | NotRegisteredException e) {
            try {
                Discord.logger.error("Failed to sync display name of '" + member.getEffectiveName() + "' \n" + e);
                writeDisplayName(member, userFilter);
            } catch (NotRegisteredException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static void writeDisplayName(Member member) throws InsufficientPermissionException, NotRegisteredException {
        UserHandler uh = Mainframe.USERS;
        Identifier userIdentifier = Functions.createIdentifier(member);
        writeDisplayName(member, uh.getFilter(userIdentifier));
    }

    public static void writeDisplayName(Member member, Bson userFilter) throws NotRegisteredException {
        String userDisplayName = Mainframe.USERS.getDisplayName(userFilter);
        writeDisplayName(member, userDisplayName);
    }

    public static void writeDisplayName(Member member, String userDisplayName) {
        try {
            ServerHandler sh = Mainframe.SERVERS;
            Identifier guildIdentifier = Functions.createIdentifier(member.getGuild());
            String[] affix = sh.getAffix(sh.getFilter(guildIdentifier));
            userDisplayName = affix[0] + " " + userDisplayName + " " + affix[1];
            member.modifyNickname(userDisplayName).queue();
        } catch (HierarchyException | InsufficientPermissionException | NotRegisteredException e) {
            Discord.logger.error("Failed to sync display name of member '" + member.getEffectiveName() + "' in guild '" + member.getGuild().getName() + "' \n  " + e);
        }
    }

    public static void readRoles(Member member) {
        readRoles(member, Functions.getGuildRoleIdentifiers(member.getGuild()));
    }

    /**
     * Discord -> Mainframe
     *
     * @param member member to read
     */
    public static void readRoles(Member member, List<Identifier> guildRoleIdentifiers) {
        // Construct lists
        List<Identifier> blacklistRoleIdentifiers = new ArrayList<>(List.copyOf(guildRoleIdentifiers));
        List<Identifier> whitelistRoleIdentifiers = new ArrayList<>();
        for (Role role : member.getRoles()) {
            Identifier roleIdentifier = Functions.createIdentifier(role);
            whitelistRoleIdentifiers.add(roleIdentifier);
            blacklistRoleIdentifiers.remove(roleIdentifier);
        }
        // Make changes
        UserHandler uh = Mainframe.USERS;
        Bson userFilter = uh.getFilter(Functions.createIdentifier(member));
        uh.syncRoles(userFilter, blacklistRoleIdentifiers, whitelistRoleIdentifiers);
    }

    public static void readRolesAdditive(Member member) {
        // Construct lists
        List<Identifier> whitelistRoleIdentifiers = member.getRoles().parallelStream().map(Functions::createIdentifier).toList();
        // Make changes
        UserHandler uh = Mainframe.USERS;
        Bson userFilter = uh.getFilter(Functions.createIdentifier(member));
        uh.addRoles(userFilter, whitelistRoleIdentifiers);
    }

    /**
     * Mainframe -> Discord
     *
     */
    public static void writeRoles(Member member, List<Identifier> userRoleIdentifiers) {
        List<Role> memberRoles = member.getRoles();
        List<Role> updatedMemberRoles = new ArrayList<>(List.copyOf(memberRoles));
        // Remove roles
        for (Role memberRole : memberRoles) {
            Identifier memberRoleIdentifier = Functions.createIdentifier(memberRole);
            // Skip if not linked
            if (!Functions.isLinked(memberRoleIdentifier))
                continue;
            // Remove if not in userRoleIdentifiers
            if (!userRoleIdentifiers.contains(memberRoleIdentifier))
                updatedMemberRoles.remove(memberRole);
        }
        // Add roles
        for (Identifier userRoleIdentifier : userRoleIdentifiers) {
            // Skip if not a discord role identifier
            if (userRoleIdentifier.type() != Types.discord_role)
                continue;
            // Skip if role not linked
            Role userRole = member.getGuild().getRoleById((long) userRoleIdentifier.id());
            if (userRole == null)
                continue;
            // Add if not in updatedRoles
            if (!updatedMemberRoles.contains(userRole)){
                updatedMemberRoles.add(userRole);
            }
        }
        // Update member
        member.getGuild().modifyMemberRoles(member, updatedMemberRoles).reason("sync").queue();
    }

    public static void writeRolesAdditive(Member member) throws NotRegisteredException {
        Bson userFilter = Mainframe.USERS.getFilter(Functions.createIdentifier(member));
        writeRolesAdditive(member, Mainframe.USERS.getRolesIdentifiers(userFilter));
    }

    public static void writeRolesAdditive(Member member, List<Identifier> userRoleIdentifiers) {
        List<Role> memberRoles = member.getRoles();
        List<Role> updatedMemberRoles = new ArrayList<>(List.copyOf(memberRoles));
        // Add roles
        for (Identifier userRoleIdentifier : userRoleIdentifiers) {
            // Skip if not a discord role identifier
            if (userRoleIdentifier.type() != Types.discord_role)
                continue;
            // Skip if role not linked
            Role userRole = member.getGuild().getRoleById((long) userRoleIdentifier.id());
            if (userRole == null)
                continue;
            // Add if not in updatedRoles
            if (!updatedMemberRoles.contains(userRole)){
                updatedMemberRoles.add(userRole);
            }
        }
        // Update member
        member.getGuild().modifyMemberRoles(member, updatedMemberRoles).reason("sync").queue();
    }

}
