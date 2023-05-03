package nl.gjorgdy.database.records;

import nl.gjorgdy.Main;
import nl.gjorgdy.database.records.identifiers.Identifier;
import nl.gjorgdy.database.records.identifiers.ObjectIdIdentifier;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;

public record UserRecord(
        @Nullable ObjectId _id,
        @NotNull String display_name,
        @NotNull Identifier[] roles,
        @NotNull Map<String, Identifier> connections,
        @NotNull Date timestamp
) implements RecordInterface {

    @Override
    public List<String> toStringList() {
        List<String> list = new ArrayList<>();
        list.add("User > " + _id.toHexString());
        list.add("├ Display Name : " + display_name);
        if (roles.length > 1) {
            list.add("├ Roles");
            for (Identifier role : roles) {
                for (String line : Main.MONGODB.roleHandler.get(role).toStringList()) {
                    list.add("│  " + line);
                }
            }
        }
        if (connections.size() > 0) {
            list.add("├ Connections");
            for (Identifier connection : connections.values()) {
                list.add("│  " + "Connection > " + connection.type().toString());
                list.add("│  ├ " + " ID : " + connection.id().toString());
            }
        }
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String formattedDate = formatter.format(timestamp);
        list.add("├ Timestamp : " + formattedDate);
        return list;
    }

    @Override
    public ObjectIdIdentifier databaseIdentifier() {
        return new ObjectIdIdentifier(Identifier.Types.mainframe_user, _id);
    }

    @Override
    public Identifier[] identifiers() {
        if (connections == null || connections.size() == 0) {
            return new Identifier[]{databaseIdentifier()};
        } else {
            int length = connections.size();
            Identifier[] result = Arrays.copyOf(connections.values().toArray(new Identifier[0]), length + 1);
            result[length] = databaseIdentifier();
            return result;
        }
    }

}
