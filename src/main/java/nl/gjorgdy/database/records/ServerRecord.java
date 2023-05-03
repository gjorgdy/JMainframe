package nl.gjorgdy.database.records;

import nl.gjorgdy.database.records.identifiers.Identifier;
import nl.gjorgdy.database.records.identifiers.ObjectIdIdentifier;
import org.bson.types.ObjectId;

import java.util.List;

public record ServerRecord(
        ObjectId _id,
        String display_name,
        Settings settings,
        Identifier connection
) implements RecordInterface {

    @Override
    public List<String> toStringList() {
        return null;
    }

    @Override
    public Identifier[] identifiers() {
        return new Identifier[]{
            databaseIdentifier(),
            connection
        };
    }

    @Override
    public ObjectIdIdentifier databaseIdentifier() {
        return new ObjectIdIdentifier(Identifier.Types.mainframe_server, _id);
    }

    public record Settings(
        boolean sync_display_names,
        boolean sync_roles
    ) {}

}
