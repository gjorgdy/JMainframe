package nl.gjorgdy.discord.listeners.mainframe;

import net.dv8tion.jda.api.JDA;
import nl.gjorgdy.events.UserListener;

public class MainframeUserListener implements UserListener {

    private final JDA bot;

    public MainframeUserListener(JDA bot) {
        this.bot = bot;
    }

}
