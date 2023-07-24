package nl.gjorgdy.discord.commands.implementations;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import nl.gjorgdy.Mainframe;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.handlers.RoleHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.discord.Functions;
import nl.gjorgdy.discord.commands.MyCommand;

public class ParentRoleCommand extends ListenerAdapter implements MyCommand {

    public SlashCommandData getData() {
        return Commands.slash("parent_role", "Link a channel from Discord with the Mainframe")
            .addOption(OptionType.BOOLEAN, "add", "To add or to remove", true)
            .addOption(OptionType.ROLE, "role", "Role", true)
            .addOption(OptionType.ROLE, "parent_role", "Parent role", true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("parent_role")) return;
        // values
        boolean add;
        Role role;
        Role parentRole;
        try {
            add = event.getOption("add").getAsBoolean();
            role = event.getOption("role").getAsRole();
            parentRole = event.getOption("parent_role").getAsRole();
        } catch (NullPointerException e) {
            event.reply("Received an error trying to run this command : \n" + e).setEphemeral(true).complete();
            return;
        }
        // Get Identifiers
        Identifier roleIdentifier = Functions.createIdentifier(role);
        Identifier parentRoleIdentifier = Functions.createIdentifier(parentRole);
        // Get handler
        RoleHandler rh = Mainframe.ROLES;
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

}
