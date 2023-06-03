package nl.gjorgdy.discord.listeners;

import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.handlers.ServerHandler;
import nl.gjorgdy.discord.Functions;
import nl.gjorgdy.discord.Sync;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MemberListener extends ListenerAdapter {

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        // Skip event if executed by this bot
        if (isEventByBot(event)) return;
        // Check if any role is linked
        for (Role role : event.getRoles()) {
            if (Functions.isLinked(role)) {
                Sync.readRoles(event.getMember());
                break;
            }
        }
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        // Skip event if executed by this bot
        if (isEventByBot(event)) return;
        // Check if any role is linked
        for (Role role : event.getRoles()) {
            if (Functions.isLinked(role)) {
                Sync.readRoles(event.getMember());
                break;
            }
        }
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        // Skip event if executed by this bot
        if (isEventByBot(event)) return;
        // Skip event if sync is disabled in this guild
        ServerHandler sh = Main.MONGODB.serverHandler;
        Bson serverFilter = sh.getFilter(Functions.createIdentifier(event.getGuild()));
        if (!sh.doesSyncDisplayNames(serverFilter)) return;
        // Try updating nickname
        Sync.readDisplayName(event.getMember());
    }

    public static boolean isEventByBot(GenericGuildMemberEvent event) {
        try {
            Optional<AuditLogEntry> optAuditLogEntry = event.getGuild().retrieveAuditLogs()
                    .limit(1).complete().stream().findFirst();
            if (optAuditLogEntry.isEmpty()) return true;
            User user = optAuditLogEntry.get().getUser();
            return user != null && user.equals(event.getJDA().getSelfUser());
        } catch (InsufficientPermissionException e) {
            System.err.println("Insufficient permission : " + e.getPermission() + " in " + event.getGuild().getName());
            return true;
        }
    }

}
