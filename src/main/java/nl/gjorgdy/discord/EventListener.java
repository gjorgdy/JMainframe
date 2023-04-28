package nl.gjorgdy.discord;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter {

    private final Discord parentClass;

    public EventListener(Discord parentClass) {
        this.parentClass = parentClass;
    }

    @Override
    public void onReady(ReadyEvent event) {
        parentClass.ready = true;
    }

}
