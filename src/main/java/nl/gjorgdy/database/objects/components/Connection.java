package nl.gjorgdy.database.objects.components;

import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.database.objects.DatabaseObject;
import org.jetbrains.annotations.Nullable;

public class Connection extends DatabaseComponent {

    public Connection(DatabaseObject parent, String key) {
        super(parent, key);
    }

    @Nullable
    public Identifier get() throws NotRegisteredException {
        return getEntry(Identifier.class);
    }

}
