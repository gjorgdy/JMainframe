package nl.gjorgdy.database.objects.components;

import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.database.Functions;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.objects.DatabaseObject;
import nl.gjorgdy.events.handlers.Events;

public class DisplayName extends DatabaseComponent {

    public DisplayName(DatabaseObject parent, String key) {
        super(parent, key);
    }

    public String get() throws NotRegisteredException {
        return getEntry(String.class);
    }

    protected void set(String displayName) throws NotRegisteredException {
        UpdateResult ur = setValue(
            Functions.format(displayName)
        );
        sendEvent( new Events.DisplayNameUpdate(
            parent.getIdentifiers(),
            displayName
        ) );
    }

}
