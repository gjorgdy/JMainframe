package nl.gjorgdy.events.handlers;

import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.events.Event;

import java.util.ArrayList;
import java.util.List;

public class Events {

    public final Handler SERVERS = new Handler();
    public final Handler USERS = new Handler();
    public final Handler ROLES = new Handler();
    public final Handler CHANNELS = new Handler();

    public static class Handler {

        List<Listener> listeners = new ArrayList<>();

        public void register(Listener listener) {
            listeners.add(listener);
        }

        public void send(Event event) {
            listeners.parallelStream().forEach(listener -> listener.onEvent(event));
        }

    }

    public interface Listener {
        void onEvent(Event event);
    }

    public record DisplayNameUpdate (
        List<Identifier> identifiers,
        String updatedDisplayName
    ) implements Event { }

}
