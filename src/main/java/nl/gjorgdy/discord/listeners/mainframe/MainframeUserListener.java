package nl.gjorgdy.discord.listeners.mainframe;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.records.ServerRecord;
import nl.gjorgdy.database.records.UserRecord;
import nl.gjorgdy.database.records.identifiers.Identifier;
import nl.gjorgdy.events.UserListener;

public class MainframeUserListener implements UserListener {

    private final JDA bot;

    public MainframeUserListener(JDA bot) {
        this.bot = bot;
    }

    @Override
    public void onUserDisplayNameUpdate(UserRecord userRecord) {
        // Check guilds
        for (ServerRecord serverRecord : Main.MONGODB.serverHandler.getAll()) {
            if (serverRecord.connection().type() == Identifier.Types.discord_guild && serverRecord.settings().sync_display_names()) {
                // Get guild
                Guild guild = bot.getGuildById((long) serverRecord.connection().id());
                // Get discord user id
                long discordUserId = (long) userRecord.connections().get(Identifier.Types.discord_user).id();
                // Get discord member instance
                Member discordMember = guild.retrieveMemberById(discordUserId).complete();
                // set the display name
                if (discordMember.getNickname() == null || !discordMember.getNickname().equals(userRecord.display_name())) {
                    discordMember.modifyNickname(userRecord.display_name()).complete();
                }
            }
        }
    }
}
