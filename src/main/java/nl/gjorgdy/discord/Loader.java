package nl.gjorgdy.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import nl.gjorgdy.Mainframe;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.RoleHandler;
import nl.gjorgdy.database.handlers.ServerHandler;
import nl.gjorgdy.database.handlers.UserHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Loader extends Thread {

    private final JDA bot;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final List<Long> loadedMembers = new ArrayList<>();
    public Loader(JDA bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        // Sync all members
        bot.getPresence().setActivity(Activity.watching("members"));
        bot.getGuilds().forEach(guild ->
            executor.submit(() -> loadGuild(guild))
        );
        // Loaded
        bot.getPresence().setActivity(Activity.watching(bot.getGuilds().size() + " servers"));
    }

    public void loadGuild(Guild guild) {
        // Return if not registered
        ServerHandler sh = Mainframe.SERVERS;
        Identifier guildIdentifier = Functions.createIdentifier(guild);
        if (!sh.exists(sh.getFilter(guildIdentifier))) {
            Discord.logger.alert("Skipping unregistered guild '" + guild.getName() + "' ");
            return;
        }
        StringBuilder status = new StringBuilder("Loaded " + guild.getName());
        // Load users (All)
        boolean guildSyncsDisplayName = sh.doesSyncDisplayNames(sh.getFilter(guildIdentifier));
        guild.getMembers().forEach(member ->
            executor.submit(() -> loadMember(member, guildSyncsDisplayName))
        );
        status.append(" | Members");
        // Load roles (Linked)
        boolean guildSyncsRoles = sh.doesSyncRoles(sh.getFilter(guildIdentifier));
        if (guildSyncsRoles) {
            guild.getRoles().forEach(role ->
                executor.submit(() -> loadRole(role))
            );
            status.append(" | Roles");
        }
        // Print
        Discord.logger.log(status.toString());
    }

    public static void loadRole(@NotNull Role role) {
        RoleHandler rh = Mainframe.ROLES;
        Identifier roleIdentifier = Functions.createIdentifier(role);
        Bson roleFilter = rh.getFilter(roleIdentifier);
        // Set color
        try {
            if (rh.exists(roleFilter)) {
                int color = rh.getColor(roleFilter);
                role.getManager().setColor(color).queue();
            }
        } catch (NotRegisteredException e) {
            Discord.logger.error("Failed to load role \"" + role.getName() + "\" : \n" + e);
        }
    }

    public void loadMember(@NotNull Member member) {
        ServerHandler sh = Mainframe.SERVERS;
        Identifier guildIdentifier = Functions.createIdentifier(member.getGuild());
        boolean guildSyncsDisplayName = sh.doesSyncDisplayNames(sh.getFilter(guildIdentifier));
        loadMember(member, guildSyncsDisplayName);
    }

    public void loadMember(@NotNull Member member, boolean syncDisplayName) {
        UserHandler uh = Mainframe.USERS;
        Identifier userIdentifier = Functions.createIdentifier(member);
        // Register if it doesn't exist yet
        if (!uh.exists(uh.getFilter(userIdentifier)) && !loadedMembers.contains(member.getIdLong())) {
            loadedMembers.add(member.getIdLong());
            try {
                uh.register(userIdentifier, member.getEffectiveName());
            } catch (RecordAlreadyRegisteredException | InvalidDisplayNameException e) {
                Discord.logger.error("Failed to load member '" + member.getEffectiveName() + "' \n" + e);
            }
        }
        Sync.member(member, syncDisplayName);
    }

}
