package nl.gjorgdy.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import nl.gjorgdy.Main;
import nl.gjorgdy.discord.listeners.MemberListener;
import nl.gjorgdy.discord.listeners.mainframe.MainframeUserListener;
import nl.gjorgdy.discord.listeners.MessageListener;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Discord {

    private final JDABuilder builder;
    private JDA bot;

    public Discord() {
        // Construct the bot itself
        builder = JDABuilder.createDefault("ODQ4OTEyNDA4MTczMjE1Nzg0.YLThSg.On5qaGcNjA4vyH06ZQ-IH1AZrXc");
        builder.setActivity(Activity.watching("Hexasis"));
        builder.setEnabledIntents(EnumSet.allOf(GatewayIntent.class));

        List<Long> channelIds = new ArrayList<>();
        channelIds.add(Long.valueOf("851846318434418718"));
        // Register Discord event listeners
        builder.addEventListeners(new MessageListener(channelIds));
        builder.addEventListeners(new MemberListener());
        // Cache all users
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        //builder.setEventPassthrough(true);
    }

    public void start() {
        bot = builder.build();
        while (!isReady()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // Register Mainframe user listeners
        Main.LISTENERS.addListener(new MainframeUserListener(bot));
    }

    public void shutdown() {
        bot.shutdown();
    }

    public JDA getBot() {
        return bot;
    }

    public boolean isReady() {
        return bot.getStatus() == JDA.Status.CONNECTED;
    }

    public String getStatus() {
        return isReady() ? "Online" : "Offline";
    }
}
