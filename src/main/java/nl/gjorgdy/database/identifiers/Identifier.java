package nl.gjorgdy.database.identifiers;

import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;

public interface Identifier {

    Types type();
    Object id();

    @Nullable
    static Identifier create(Types type, Object id) {
        try {
            return switch (type) {
                case mainframe_user, mainframe_role, mainframe_channel, mainframe_server ->
                        new ObjectIDIdentifier(type, (ObjectId) id);
                case discord_user, discord_role, discord_channel, discord_guild ->
                        new LongIdentifier(type, (Long) id);
                case minecraft_user, minecraft_server ->
                        new StringIdentifier(type, (String) id);
            };
        } catch (ClassCastException e) {
            return null;
        }
    }
}
