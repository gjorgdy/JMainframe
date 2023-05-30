package nl.gjorgdy.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.RoleHandler;
import nl.gjorgdy.database.handlers.ServerHandler;
import nl.gjorgdy.database.handlers.UserHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Loader extends Thread {

    private final JDA bot;
    private final ExecutorService executor;

    public Loader(JDA bot) {
        this.bot = bot;
        executor = Executors.newFixedThreadPool(10);
    }

    @Override
    public void run() {
        bot.getGuilds().forEach(guild -> {
            executor.submit(() -> loadGuild(guild));
        });

        bot.getPresence().setActivity(Activity.watching(bot.getGuilds().size() + " servers"));
    }

    public void loadGuild(Guild guild) {
        StringBuilder status = new StringBuilder("\n> " + guild.getName());
        ServerHandler sh = Main.MONGODB.serverHandler;
        Identifier guildIdentifier = Functions.createIdentifier(guild);
        if (sh.exists(sh.getFilter(guildIdentifier))) {
            // Get display name from guild
            try {
                sh.setDisplayName(sh.getFilter(guildIdentifier), guild.getName());
            } catch (InvalidDisplayNameException | NotRegisteredException ignored) {}
        } else {
            // Register the guild
            try {
                sh.register(guildIdentifier, guild.getName());
            } catch (InvalidDisplayNameException | RecordAlreadyRegisteredException ignored) {}
        }
        // Load commands
        SlashCommands.create(guild);
        // Load users (All)
        boolean guildSyncsDisplayName = sh.doesSyncDisplayNames(sh.getFilter(guildIdentifier));
        guild.getMembers().forEach(member ->
            executor.submit(() -> loadMember(member, guildSyncsDisplayName))
        );
        status.append("\n| Loaded users");
        // Load roles (Linked)
        boolean guildSyncsRoles = sh.doesSyncRoles(sh.getFilter(guildIdentifier));
        if (guildSyncsRoles) {
            guild.getRoles().forEach(role ->
                executor.submit(() -> loadRole(role))
            );
            status.append("\n| Loaded roles");
        }
        // Print
        System.out.println(status.toString());
    }

    public static void loadRole(@NotNull Role role) {
        RoleHandler rh = Main.MONGODB.roleHandler;
        Identifier roleIdentifier = Functions.createIdentifier(role);
        Bson roleFilter = rh.getFilter(roleIdentifier);
        // Set color
        try {
            int color = rh.getColor(roleFilter);
            role.getManager().setColor(color).queue();
        } catch (NotRegisteredException ignored) {}
    }

    public static void loadMember(@NotNull Member member) {
        loadMember(member, false);
    }

    public static void loadMember(@NotNull Member member, boolean syncDisplayName) {
        UserHandler uh = Main.MONGODB.userHandler;
        Identifier userIdentifier = Functions.createIdentifier(member);
        // Register if it doesn't exist yet
        if (!uh.exists(uh.getFilter(userIdentifier))) {
            try {
                uh.register(userIdentifier, member.getEffectiveName());
            } catch (RecordAlreadyRegisteredException | InvalidDisplayNameException e) {
                return;
            }
        }
    // Sync Display Name
        if (syncDisplayName)
            Sync.writeDisplayName(member);
    // Sync Roles - additive
        try {
            Sync.readRolesAdditive(member);
            Sync.writeRolesAdditive(member);
        } catch (NotRegisteredException e) {
            System.err.println("Failed to sync member roles '" + member.getEffectiveName() + "' " + e);
        }
    }

}
