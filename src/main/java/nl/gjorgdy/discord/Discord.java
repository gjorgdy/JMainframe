package nl.gjorgdy.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Discord extends Thread {

    private final JDABuilder builder;
    private JDA bot;

    public Discord() {
        // Construct the bot itself
        builder = JDABuilder.createDefault("ODQ4OTEyNDA4MTczMjE1Nzg0.YLThSg.On5qaGcNjA4vyH06ZQ-IH1AZrXc");
        builder.setActivity(Activity.watching("Hexasis"));
        builder.setEnabledIntents(EnumSet.allOf(GatewayIntent.class));

        List<Long> channelIds = new ArrayList<>();
        channelIds.add(Long.valueOf("851846318434418718"));

        builder.addEventListeners(new MessageListener(channelIds));
    }

    @Override
    public void run() {
        bot = builder.build();
    }

}
