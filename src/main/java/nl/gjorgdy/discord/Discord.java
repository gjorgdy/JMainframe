package nl.gjorgdy.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import nl.gjorgdy.Config;
import nl.gjorgdy.Logger;
import nl.gjorgdy.Mainframe;
import nl.gjorgdy.discord.commands.SlashCommands;
import nl.gjorgdy.discord.listeners.MemberListener;
import nl.gjorgdy.discord.listeners.mainframe.MainframeUserListener;

import java.util.EnumSet;

public class Discord {

    private final JDABuilder builder;
    private JDA bot;
    public static Logger logger = new Logger("Discord");

    public Discord() {
        // Get token
        Config cfg = new Config("DISCORD");
        String token = cfg.get("TOKEN");
        // Construct the bot itself
        builder = JDABuilder.createDefault(token);
        builder.setActivity(Activity.listening("inspirational quotes"));
        builder.setEnabledIntents(EnumSet.allOf(GatewayIntent.class));
        // Register Discord event listeners
        builder.addEventListeners(new MemberListener());
        // Cache all users
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
    }

    public void start() {
        try {
            bot = builder.build().awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.log("Started bot");
        // Register Mainframe user listeners
        Mainframe.Events.addListener(new MainframeUserListener(bot));
        // Start loader thread
        new Loader(bot).start();
        // Register command listener
        bot.addEventListener(new SlashCommands(bot));
        // Set activity
    }

    public void shutdown() {
        bot.shutdown();
    }

    public boolean isReady() {
        return bot.getStatus() == JDA.Status.CONNECTED;
    }

    public String getStatus() {
        return isReady() ? "Online" : "Offline";
    }
}
