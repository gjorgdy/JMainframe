package nl.gjorgdy.discord.listeners;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.discord.Functions;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MemberListener extends ListenerAdapter {

    Event lastEvent = null;

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        // Skip event if executed by this bot
        if (isEventByBot(event)) return;
        // Create user identifier
        Identifier userIdentifier = Functions.createIdentifier(event.getUser());
        // Go through all roles
        event.getRoles().forEach(role -> {
            Identifier roleIdentifier = Functions.createIdentifier(role);
            try {
                Main.MONGODB.userHandler.addRole(userIdentifier, roleIdentifier);
            } catch (NotRegisteredException e) {
                System.err.println("Removed non-synced role : " + role.getName());
            }
        });
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        // Skip event if executed by this bot
        if (isEventByBot(event)) return;
        // Create user identifier
        Identifier userIdentifier = Functions.createIdentifier(event.getUser());
        // Go through all roles
        event.getRoles().forEach(role -> {
            Identifier roleIdentifier = Functions.createIdentifier(role);
            try {
                Main.MONGODB.userHandler.removeRole(userIdentifier, roleIdentifier);
            } catch (NotRegisteredException e) {
                System.err.println("Removed non-synced role : " + role.getName());
            }
        });
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        // Skip event if executed by this bot
        if (isEventByBot(event)) return;
        // Create user identifier
        Identifier userIdentifier = Functions.createIdentifier(event.getUser());
        // Try updating nickname
        try {
            Main.MONGODB.userHandler.setDisplayName(userIdentifier, event.getNewNickname());
        } catch (NotRegisteredException e) {
            return;
        } catch (InvalidDisplayNameException e) {
            event.getMember().modifyNickname(event.getOldNickname()).complete();
        }
    }

    public static boolean isEventByBot(GenericGuildEvent event) {
        Optional<AuditLogEntry> optAuditLogEntry = event.getGuild().retrieveAuditLogs().stream().filter(_auditLogEntry -> _auditLogEntry.getType() == ActionType.MEMBER_UPDATE).findFirst();
        return optAuditLogEntry.isPresent() && optAuditLogEntry.get().getUser() == event.getJDA().getSelfUser();
    }

}
