package nl.gjorgdy.database.handlers;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.handlers.generic.DisplayNameHandler;
import nl.gjorgdy.database.records.ServerRecord;
import nl.gjorgdy.database.records.identifiers.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerHandler extends DisplayNameHandler<ServerRecord> {

    public ServerHandler(MongoCollection<ServerRecord> mongoCollection) {
        super(mongoCollection);
    }

    public ServerRecord register(String displayName, ServerRecord.Settings settings, Identifier connection) throws InvalidDisplayNameException {
        // Check if display name is already in use
        if (isDisplayNameUsed(displayName)) {
            throw new InvalidDisplayNameException();
        }
        // Create a new user
        ServerRecord serverRecord = new ServerRecord(null, displayName, settings, connection);
        // Insert into database
        insert(serverRecord).wasAcknowledged();
        // Return the record
        return serverRecord;
    }

    public ServerRecord updateSettings(ServerRecord serverRecord, ServerRecord.Settings settings) {
        // Update value in database
        setValue(serverRecord.filter(), "settings", settings);
        // Reload cache
        return get(serverRecord.databaseIdentifier());
    }
}
