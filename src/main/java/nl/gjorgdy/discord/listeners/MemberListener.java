package nl.gjorgdy.discord.listeners;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.records.RoleRecord;
import nl.gjorgdy.database.records.UserRecord;
import nl.gjorgdy.discord.Functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberListener extends ListenerAdapter {

    Event lastEvent = null;

    @Override
    public void onGuildMemberUpdate(GuildMemberUpdateEvent event) {
        // Skip event if executed by this bot
        if (isEventByBot(event)) return;
        // Skip event if user is not registered
        UserRecord userRecord = Functions.getUserRecord(event.getUser());
        if (userRecord == null) {
            return;
        }
        // Sync member
        try {
            Main.MONGODB.userHandler.setDisplayName(
                    userRecord, event.getMember().getEffectiveName()
            );
        } catch (InvalidDisplayNameException e) {
            // Return old username
            event.getMember().modifyNickname(userRecord.display_name()).complete();
        }
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        // Skip event if executed by thois bot
        if (isEventByBot(event)) return;
        // Skip event if no role is registered
        List<RoleRecord> roleRecords = new ArrayList<>();
        for (Role role : event.getRoles()) {
            roleRecords.add(Functions.getRoleRecord(role));
        }
        if (roleRecords.size() == 0) {
            return;
        }
        // Skip event if user is not registered
        UserRecord userRecord = Functions.getUserRecord(event.getUser());
        if (userRecord == null) {
            return;
        }
    }

    public static boolean isEventByBot(GenericGuildEvent event) {
        Optional<AuditLogEntry> optAuditLogEntry = event.getGuild().retrieveAuditLogs().stream().filter(_auditLogEntry -> _auditLogEntry.getType() == ActionType.MEMBER_UPDATE).findFirst();
        AuditLogEntry auditLogEntry = optAuditLogEntry.get();
        return auditLogEntry.getUser() == event.getJDA().getSelfUser();
    }

}
