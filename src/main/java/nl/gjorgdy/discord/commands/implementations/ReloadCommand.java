package nl.gjorgdy.discord.commands.implementations;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import nl.gjorgdy.discord.commands.MyCommand;

public class ReloadCommand implements MyCommand {

    @Override
    public SlashCommandData getData() {
        return Commands.slash("reload", "Reload the whole bot")
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

}
