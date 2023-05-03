package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.handlers.generic.ConnectionsHandler;
import nl.gjorgdy.database.records.ChannelRecord;
import nl.gjorgdy.database.records.RoleRecord;
import nl.gjorgdy.database.records.identifiers.Identifier;

import javax.management.relation.Role;

public class ChannelHandler extends ConnectionsHandler<ChannelRecord> {
    public ChannelHandler(MongoCollection mongoCollection) {
        super(mongoCollection);
    }

    public ChannelRecord register(String displayName) throws InvalidDisplayNameException {
        if (isDisplayNameUsed(displayName)) {
            throw new InvalidDisplayNameException();
        }
        // Create channel record
        ChannelRecord channelRecord = new ChannelRecord(null, displayName, new Identifier[0], new Identifier[0]);
        // Insert
        insert(channelRecord);
        // Return
        return channelRecord;
    }

    public ChannelRecord addAllowedRole(ChannelRecord channelRecord, RoleRecord roleRecord) {
        // Add role
        addArrayValue(channelRecord.filter(), "allowed_roles", roleRecord);
        // return record
        return get(channelRecord.databaseIdentifier());
    }

    public ChannelRecord removeAllowedRole(ChannelRecord channelRecord, RoleRecord roleRecord) {
        // Add role
        pullArrayValue(channelRecord.filter(), "allowed_roles", roleRecord);
        // return record
        return get(channelRecord.databaseIdentifier());
    }

}
