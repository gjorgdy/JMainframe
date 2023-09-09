package nl.gjorgdy.database.identifiers;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public interface Identifier {

    Types type();
    Object id();
    Date linked();

    @Nullable
    static Identifier create(Types type, Object id) {
        return load(type, id, new Date());
    }

    @Nullable
    static Identifier load(Types type, Object id, Date linked) {
        try {
            return switch (type) {
                case mainframe_user, mainframe_role, mainframe_channel, mainframe_server ->
                        new ObjectIDIdentifier(type, (ObjectId) id, linked);
                case discord_user, discord_role, discord_channel, discord_guild ->
                        new LongIdentifier(type, (Long) id, linked);
                case minecraft_user, minecraft_server ->
                        new StringIdentifier(type, (String) id, linked);
            };
        } catch (ClassCastException e) {
            return null;
        }
    }

    static Identifier fromDocument(Document document) {
        Types type = Types.valueOf(document.getString("type"));
        Object id = document.get("id");
        Date linked = document.getDate("linked");
        return load(type, id, linked);
    }

    static Document toDocument(Identifier identifier) {
        String type = identifier.type().toString();
        Object id = identifier.id();
        Date linked = identifier.linked();
        return new Document()
                .append("type", type)
                .append("id", id)
                .append("linked", linked);
    }

    default Document toDocument() {
        return toDocument(this);
    }

    /**
     * Reset time of identifier
     *
     * @return identifier with current time
     */
    default Identifier resetTime() {
        return create(type(), id());
    }
}
