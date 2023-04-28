package nl.gjorgdy.database.records;

import nl.gjorgdy.database.records.identifiers.Identifier;
import org.bson.types.ObjectId;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public record User (
        ObjectId _id,
        String display_name,
        Role[] roles,
        Identifier[] connections
) implements DatabaseRecord {

    @Override
    public List<String> toStringList() {
        List<String> list = new ArrayList<>();
        list.add("User > " + _id.toHexString());
        list.add("├ Display Name : " + display_name);
        list.add("├ Roles");
        for (Role role : roles) {
            for (String line : role.toStringList()) {
                list.add("│  " + line);
            }
        }
        list.add("├ Connections");
        for (Identifier connection : connections) {
            list.add("│  " + "Connection > " + connection.type().toString());
            list.add("│  ├ " + " ID : " + connection.id().toString());
        }
        return list;
    }

}
