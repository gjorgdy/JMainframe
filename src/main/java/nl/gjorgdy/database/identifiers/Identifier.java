package nl.gjorgdy.database.identifiers;

import org.bson.types.ObjectId;

public interface Identifier {

    enum Types {
        mainframe_user,
        mainframe_role,
        mainframe_channel,
        mainframe_server,
        discord_user,
        discord_role,
        discord_guild,
        discord_channel,
        minecraft_user,
        minecraft_server
    }

    Types type();
    Object id();
}
