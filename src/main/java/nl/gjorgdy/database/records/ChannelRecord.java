package nl.gjorgdy.database.records;

import nl.gjorgdy.database.records.identifiers.Identifier;
import nl.gjorgdy.database.records.identifiers.ObjectIdIdentifier;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public record ChannelRecord(
        ObjectId _id,
        String display_name,
        @Nullable Identifier[] allowed_roles,
        @Nullable Identifier[] connections
) implements RecordInterface {
    @Override
    public List<String> toStringList() {
        return null;
    }

    @Override
    public ObjectIdIdentifier databaseIdentifier() {
        return new ObjectIdIdentifier(Identifier.Types.mainframe_channel, _id);
    }

    @Override
    public Identifier[] identifiers() {
        if (connections == null) {
            return new Identifier[]{databaseIdentifier()};
        } else {
            int length = connections.length + 1;
            Identifier[] result = Arrays.copyOf(connections, length);
            result[length] = databaseIdentifier();
            return result;
        }
    }
}
