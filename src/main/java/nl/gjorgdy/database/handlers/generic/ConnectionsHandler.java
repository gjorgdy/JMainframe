package nl.gjorgdy.database.handlers.generic;

import com.mongodb.client.MongoCollection;
import nl.gjorgdy.database.exceptions.UserAlreadyRegisteredException;
import nl.gjorgdy.database.records.RecordInterface;
import nl.gjorgdy.database.records.identifiers.Identifier;

public class ConnectionsHandler<T extends RecordInterface> extends DisplayNameHandler<T> {

    public ConnectionsHandler(MongoCollection<T> mongoCollection) {
        super(mongoCollection);
    }

    public T addConnection(T record, Identifier connection) throws UserAlreadyRegisteredException {
        if (get(connection) != null) {
            throw new UserAlreadyRegisteredException();
        }
        // Add connection
        addArrayValue(record.filter(), "connections", connection);
        // Reload record
        return get(record.databaseIdentifier());
    }

    public T removeConnection(T record, Identifier connection) {
        // Add connection
        pullArrayValue(record.filter(), "connections", connection);
        // Reload record
        return get(record.databaseIdentifier());
    }

}
