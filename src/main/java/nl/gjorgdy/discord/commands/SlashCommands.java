package nl.gjorgdy.discord.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import nl.gjorgdy.discord.commands.implementations.GuildCommand;
import nl.gjorgdy.discord.commands.implementations.LinkRoleCommand;
import nl.gjorgdy.discord.commands.implementations.ParentRoleCommand;

import java.util.List;

public class SlashCommands extends ListenerAdapter {

    public SlashCommands(JDA bot) {
        getListeners().forEach(bot::addEventListener);
        // Load all commands in all guilds
        getListeners().forEach( command ->
            bot.getGuilds().forEach(guild ->
                guild.upsertCommand(command.getData()).queue()
            )
            //bot.upsertCommand(command).queue()
        );
    }

    public List<MyCommand> getListeners() {
        return List.of(
            new LinkRoleCommand(),
            new ParentRoleCommand(),
            new GuildCommand()
        );
    }


}
