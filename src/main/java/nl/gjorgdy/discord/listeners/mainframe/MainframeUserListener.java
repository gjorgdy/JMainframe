package nl.gjorgdy.discord.listeners.mainframe;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.database.identifiers.Types;
import nl.gjorgdy.discord.Functions;
import nl.gjorgdy.discord.Loader;
import nl.gjorgdy.discord.Sync;
import nl.gjorgdy.events.UserListener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainframeUserListener implements UserListener {

    private final JDA bot;
    private final ExecutorService executor;

    public MainframeUserListener(JDA bot) {
        this.bot = bot;
        executor = Executors.newFixedThreadPool(10);
    }

    @Override
    public void onUserConnectionUpdate(List<Identifier> userIdentifiers, Identifier connection, boolean additive) {
        // If not additive or a discord connection, skip event
        if (connection.type() != Types.discord_user || !additive) return;

        bot.getGuilds().forEach(guild -> {
            Member member = guild.getMemberById((Long) connection.id());
            if (member == null) return;
            new Loader(bot).loadMember(member);
        });
    }

    @Override
    public void onUserRoleUpdate(List<Identifier> userIdentifiers, List<Identifier> roleIdentifiers, boolean additive) {
        // Update roles in all guilds
        bot.getGuilds().forEach(guild -> {
            System.out.println(guild.getName());
            Member member = Functions.getGuildMember(userIdentifiers, guild);
            if (member == null) return;
            Sync.writeRoles(member, roleIdentifiers);
        });
    }

    @Override
    public void onUserDisplayNameUpdate(List<Identifier> userIdentifiers, String newDisplayName) {
        // Updates name in all enabled guilds
        bot.getGuilds().forEach(guild -> {
            // Skip if guild has display name syncing disabled
            if (!Functions.guildDoesSyncDisplayName(guild)) return;
            // Update name
            Member member = Functions.getGuildMember(userIdentifiers, guild);
            if (member == null) {
                return;
            }
            Sync.writeDisplayName(member, newDisplayName);
        });
    }
}
