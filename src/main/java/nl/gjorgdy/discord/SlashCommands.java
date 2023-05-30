package nl.gjorgdy.discord;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import nl.gjorgdy.Main;

import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyConnectedException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.RoleHandler;
import nl.gjorgdy.database.handlers.ServerHandler;
import nl.gjorgdy.database.identifiers.Identifier;

import java.util.function.Consumer;

public class SlashCommands extends ListenerAdapter {

    public static void create(Guild guild) {
        // Settings
        SlashCommandData settingsCommand = Commands.slash("settings", "Enable a certain option for this guild.")
                .addOption(OptionType.STRING, "setting", "The setting to change", true)
                .addOption(OptionType.BOOLEAN, "value", "The value to set the setting to", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        guild.upsertCommand(settingsCommand).queue();
        // Reload
        SlashCommandData reloadCommand = Commands.slash("reload", "Reload all object from Discord")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        guild.upsertCommand(reloadCommand).queue();
        // Connect Role
        SlashCommandData linkRoleCommand = Commands.slash("link_role", "Link a role from Discord with the Mainframe")
                .addOption(OptionType.ROLE, "role", "Id of role to link", true)
                .addOption(OptionType.STRING, "name", "Name of role in the Mainframe", true)
                .addOption(OptionType.BOOLEAN, "exists", "If this role already exists in the Mainframe", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        guild.upsertCommand(linkRoleCommand).queue();
        // Connect Channel
        SlashCommandData linkChannelCommand = Commands.slash("link_channel", "Link a channel from Discord with the Mainframe")
                .addOption(OptionType.CHANNEL, "channel", "Id of channel to link", true)
                .addOption(OptionType.STRING, "name", "Name of channel in the Mainframe", true)
                .addOption(OptionType.BOOLEAN, "exists", "If this channel already exists in the Mainframe", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        guild.upsertCommand(linkChannelCommand).queue();
        // Parent Role
        SlashCommandData parentRoleCommand = Commands.slash("parent_role", "Link a channel from Discord with the Mainframe")
                .addOption(OptionType.BOOLEAN, "add", "To add or to remove", true)
                .addOption(OptionType.ROLE, "role", "Role", true)
                .addOption(OptionType.ROLE, "parent_role", "Parent role", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        guild.upsertCommand(parentRoleCommand).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "settings" -> settingsCommand(event);
            case "reload" -> {
                event.reply("Reloading Mainframe Discord module").setEphemeral(true).complete();
                if (event.getGuild() == null) return;
                new Loader(event.getJDA()).start();
            }
            case "link_role" -> linkRole(event);
            case "parent_role" -> parentRole(event);
        }
    }

    private void parentRole(SlashCommandInteractionEvent event) {
        // Get command args
        boolean add;
        Role role;
        Role parentRole;
        try {
            add = event.getOption("add").getAsBoolean();
            role = event.getOption("role").getAsRole();
            parentRole = event.getOption("parent_role").getAsRole();
        } catch (NullPointerException e) { return; }
        // Get Identifiers
        Identifier roleIdentifier = Functions.createIdentifier(role);
        Identifier parentRoleIdentifier = Functions.createIdentifier(parentRole);
        // Get handler
        RoleHandler rh = Main.MONGODB.roleHandler;
        // If add is true, try adding
        if (add) {
            try {
                rh.addParentRole(rh.getFilter(roleIdentifier), parentRoleIdentifier);
                event.reply("Successfully added parent role").setEphemeral(true).queue();
            } catch (NotRegisteredException e) {
                event.reply("One of these roles is not linked").setEphemeral(true).queue();
            }
        // Otherwise try removing
        } else {
            try {
                rh.removeParentRole(rh.getFilter(roleIdentifier), parentRoleIdentifier);
                event.reply("Successfully removed parent role").setEphemeral(true).queue();
            } catch (NotRegisteredException e) {
                event.reply("One of these roles is not linked").setEphemeral(true).queue();
            }
        }
    }

    private void linkRole(SlashCommandInteractionEvent event) {
        // Get command args
        Role role;
        String name;
        boolean exists;
        try {
            role = event.getOption("role").getAsRole();
            name = event.getOption("name").getAsString();
            exists = event.getOption("exists").getAsBoolean();
        } catch (NullPointerException e) { return; }
        // Get identifier
        Identifier roleIdentifier = Functions.createIdentifier(role);
        // Get handler
        RoleHandler rh = Main.MONGODB.roleHandler;
        // Try connecting to an existing role
        if (exists) {
            try {
                rh.addConnection(rh.getFilter(name), roleIdentifier);
                event.reply("Successfully linked the role to the Mainframe").setEphemeral(true).complete();
            } catch (NotRegisteredException e) {
                event.reply("This role does not exist in the Mainframe").setEphemeral(true).complete();
            } catch (RecordAlreadyConnectedException | RecordAlreadyRegisteredException e) {
                event.reply("This role is already linked").setEphemeral(true).complete();
            }
        } else {
            // Register a new role
            try {
                rh.register(roleIdentifier, name, role.getColorRaw());
                event.reply("Successfully registered the role in the Mainframe").setEphemeral(true).complete();
            } catch (InvalidDisplayNameException e) {
                event.reply("The supplied name is invalid or in use").setEphemeral(true).complete();
            } catch (RecordAlreadyRegisteredException e) {
                event.reply("This role is already linked").setEphemeral(true).complete();
            }
        }
    }

    private void settingsCommand(SlashCommandInteractionEvent event) {
        // Get command args
        String setting;
        boolean value;
        try {
            setting = event.getOption("setting").getAsString();
            value = event.getOption("value").getAsBoolean();
        } catch (NullPointerException e) { return; }
        // Get guild identifier
        Identifier guildIdentifier = Functions.createIdentifier(event.getGuild());
        // Get server handler
        ServerHandler sh = Main.MONGODB.serverHandler;
        switch (setting) {
            case "display_names" -> {
                sh.setSyncDisplayNames(sh.getFilter(guildIdentifier), value);
                event.reply("Set setting \"display_names\" to " + value).setEphemeral(true).complete();
            }
            case "roles" -> {
                sh.setSyncRoles(sh.getFilter(guildIdentifier), value);
                event.reply("Set setting \"roles\" to " + value).setEphemeral(true).complete();
            }
            default -> event.reply("\"" + setting + "\" is an invalid setting").setEphemeral(true).complete();
        }
    }

}
