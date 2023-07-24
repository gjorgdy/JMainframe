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
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyConnectedException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.RoleHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.discord.Functions;
import nl.gjorgdy.discord.commands.MyCommand;

public class LinkRoleCommand extends ListenerAdapter implements MyCommand {

    public SlashCommandData getData() {
        return Commands.slash("link_role", "Link a role from Discord with the Mainframe")
                .addOption(OptionType.ROLE, "role", "Id of role to link", true)
                .addOption(OptionType.STRING, "name", "Name of role in the Mainframe", true)
                .addOption(OptionType.BOOLEAN, "exists", "If this role already exists in the Mainframe", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("link_role")) return;

        Role role;
        String name;
        boolean exists;
        try {
            role = event.getOption("role").getAsRole();
            name = event.getOption("name").getAsString();
            exists = event.getOption("exists").getAsBoolean();
        } catch (NullPointerException e) {
            event.reply("Received an error trying to run this command : \n" + e).setEphemeral(true).complete();
            return;
        }
        // Get identifier
        Identifier roleIdentifier = Functions.createIdentifier(role);
        // Get handler
        RoleHandler rh = Mainframe.ROLES;
        // Try connecting to an existing role
        if (exists) {
            try {
                rh.addLink(rh.getFilter(name), roleIdentifier);
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

}
