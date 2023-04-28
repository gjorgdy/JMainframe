package nl.gjorgdy.database.records;

import nl.gjorgdy.database.records.identifiers.Identifier;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record Role (
        ObjectId _id,
        String display_name,
        @Nullable Role[] parent_roles,
        @Nullable Identifier[] connections
) implements DatabaseRecord {
    @Override
    public List<String> toStringList() {
        List<String> list = new ArrayList<>();
        list.add("Role > " + _id.toHexString());
        list.add("├ Display name : " + display_name);
        list.add("├ Parent roles");
        if (parent_roles != null) {
            for (Role parent_role : parent_roles) {
                for (String line : parent_role.toStringList()) {
                    list.add("│  " + line);
                }
            }
        }
        list.add("├ Connections");
        if (connections != null) {
            for (Identifier connection : connections) {
                list.add("│  ├ " + connection.type().toString() + " > " + connection.id().toString());
            }
        }
        return list;
    }
}
