package nl.gjorgdy.discord.commands.implementations;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import nl.gjorgdy.Mainframe;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.ServerHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.discord.Functions;
import nl.gjorgdy.discord.Loader;
import nl.gjorgdy.discord.commands.MyCommand;
import org.bson.conversions.Bson;

public class GuildCommand extends ListenerAdapter implements MyCommand {

    public SlashCommandData getData() {
        return Commands.slash("guild", "Manage how a guild should be managed by Mainframe").addSubcommands(
                new SubcommandData("register", "Register or update this Discord guild within mainframe")
                        .addOption(OptionType.BOOLEAN, "display_names", "If this guild should sync member display names", false)
                        .addOption(OptionType.BOOLEAN, "roles", "If this guild should sync role colors", false)
                        .addOption(OptionType.STRING, "prefix", "Prefix in front of the name of members", false)
                        .addOption(OptionType.STRING, "suffix", "Suffix behind the name of members", false),
                new SubcommandData("unregister", "Unregister this Discord guild from mainframe")
            ).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("guild") || event.getGuild() == null) return;
        // Register
        if (event.getSubcommandName().equals("register")) {
        // Get values
            // Display Name
            OptionMapping displayNamesOM = event.getOption("display_names");
            boolean displayNames = displayNamesOM != null && displayNamesOM.getAsBoolean();
            // Roles
            OptionMapping rolesOM = event.getOption("roles");
            boolean roles = rolesOM != null && rolesOM.getAsBoolean();
            // Prefix
            OptionMapping prefixOM = event.getOption("prefix");
            String prefix = prefixOM != null ? prefixOM.getAsString() : "";
            // Suffix
            OptionMapping suffixOM = event.getOption("suffix");
            String suffix = suffixOM != null ? suffixOM.getAsString() : "";
        // Check if guild is registered
            ServerHandler sh = Mainframe.SERVERS;
            Identifier guildIdentifier = Functions.createIdentifier(event.getGuild());
            Bson guildFilter = sh.getFilter(guildIdentifier);
            // If guild is already registered
            if (sh.exists(guildFilter)) {
                // Update settings
                sh.setSyncDisplayNames(guildFilter, displayNames);
                sh.setSyncRoles(guildFilter, roles);
                sh.setPrefix(guildFilter, prefix);
                sh.setSuffix(guildFilter, suffix);
                event.reply("Successfully updated settings of this guild")
                    .setEphemeral(true)
                    .queue();
            } else {
                // Try registering this guild
                try {
                    sh.register(guildIdentifier, event.getGuild().getName(), displayNames, roles, prefix, suffix);
                    new Loader(event.getJDA()).start();
                    event.reply("Successfully registered this guild and reloaded the Discord module")
                        .setEphemeral(true)
                        .queue();
                // Failsafe
                } catch (InvalidDisplayNameException | RecordAlreadyRegisteredException e) {
                    event.reply("Encountered an exception registering this guild : \n" + e)
                        .setEphemeral(true)
                        .queue();
                }
            }

        } else if (event.getSubcommandName().equals("unregister")) {
            try {
                Mainframe.SERVERS.unregister(Functions.createIdentifier(event.getGuild()));
                event.reply("Successfully unregistered this guild")
                        .setEphemeral(true)
                        .queue();
            } catch (NotRegisteredException e) {
                event.reply("Encountered an exception unregistering this guild : \n" + e)
                    .setEphemeral(true)
                    .queue();
            }
        }

    }

}