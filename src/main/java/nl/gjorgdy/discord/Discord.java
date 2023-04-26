package nl.gjorgdy.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.EnumSet;

public class Discord {

    private final JDABuilder builder;
    private JDA bot;

    public Discord() {
        // Construct the bot itself
        builder = JDABuilder.createDefault("ODQ4OTEyNDA4MTczMjE1Nzg0.YLThSg.On5qaGcNjA4vyH06ZQ-IH1AZrXc");
        builder.setActivity(Activity.watching("Hexasis"));
        builder.setEnabledIntents(EnumSet.allOf(GatewayIntent.class));

        builder.addEventListeners(new MessageListener());
    }

    public void start() {
        bot = builder.build();
    }

}
