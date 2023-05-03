package nl.gjorgdy.database.records;

import nl.gjorgdy.Main;
import nl.gjorgdy.database.records.identifiers.Identifier;
import nl.gjorgdy.database.records.identifiers.ObjectIdIdentifier;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record RoleRecord(
        ObjectId _id,
        String display_name,
        @NotNull String[] permissions,
        @NotNull Identifier[] parent_roles,
        @NotNull Identifier[] connections
) implements RecordInterface {
    @Override
    public List<String> toStringList() {
        List<String> list = new ArrayList<>();
        list.add("Role > " + _id.toHexString());
        list.add("├ Display name : " + display_name);
        if (parent_roles != null && parent_roles.length > 0) {
            list.add("├ Parent roles");
            for (Identifier parent_role : parent_roles) {
                for (String line : Main.MONGODB.roleHandler.get(parent_role).toStringList()) {
                    list.add("│  " + line);
                }
            }
        }
        if (connections != null && connections.length > 0) {
            list.add("├ Connections");
            for (Identifier connection : connections) {
                list.add("│  ├ " + connection.type().toString() + " > " + connection.id().toString());
            }
        }
        return list;
    }

    @Override
    public ObjectIdIdentifier databaseIdentifier() {
        return new ObjectIdIdentifier(Identifier.Types.mainframe_role, _id);
    }

    @Override
    public Identifier[] identifiers() {
        if (connections == null) {
            return new Identifier[]{databaseIdentifier()};
        } else {
            int length = connections.length;
            Identifier[] result = Arrays.copyOf(connections, length + 1);
            result[length] = databaseIdentifier();
            return result;
        }
    }

}
