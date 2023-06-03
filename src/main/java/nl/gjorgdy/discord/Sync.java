package nl.gjorgdy.discord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.handlers.UserHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.database.identifiers.Types;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class Sync {

    public static void readDisplayName(Member member) {
        UserHandler uh = Main.MONGODB.userHandler;
        Identifier userIdentifier = Functions.createIdentifier(member);
        Bson userFilter = uh.getFilter(userIdentifier);
        try {
            uh.setDisplayName(userFilter, member.getEffectiveName());
        } catch (InvalidDisplayNameException e) {
            writeDisplayName(member, userFilter);
        }
    }

    public static void writeDisplayName(Member member) throws InsufficientPermissionException {
        UserHandler uh = Main.MONGODB.userHandler;
        Identifier userIdentifier = Functions.createIdentifier(member);
        writeDisplayName(member, uh.getFilter(userIdentifier));
    }

    public static void writeDisplayName(Member member, Bson userFilter) {
        String userDisplayName = Main.MONGODB.userHandler.getDisplayName(userFilter);
        writeDisplayName(member, userDisplayName);
    }

    public static void writeDisplayName(Member member, String userDisplayName) {
        try {
            member.modifyNickname(userDisplayName).queue();
        } catch (HierarchyException | InsufficientPermissionException ignored) {}
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
        UserHandler uh = Main.MONGODB.userHandler;
        Bson userFilter = uh.getFilter(Functions.createIdentifier(member));
        uh.syncRoles(userFilter, blacklistRoleIdentifiers, whitelistRoleIdentifiers);
        System.out.println("Read roles... from " + member.getEffectiveName() + " in " + member.getGuild().getName());
    }

    public static void readRolesAdditive(Member member) {
        // Construct lists
        List<Identifier> whitelistRoleIdentifiers = member.getRoles().parallelStream().map(Functions::createIdentifier).toList();
        // Make changes
        UserHandler uh = Main.MONGODB.userHandler;
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
        System.out.println("Writing roles... to " + member.getEffectiveName() + " in " + member.getGuild().getName());
    }

    public static void writeRolesAdditive(Member member) throws NotRegisteredException {
        UserHandler uh = Main.MONGODB.userHandler;
        Bson userFilter = uh.getFilter(Functions.createIdentifier(member));
        writeRolesAdditive(member, uh.getDocumentsRoleIdentifiers(userFilter));
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
