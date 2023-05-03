package nl.gjorgdy.events;

import nl.gjorgdy.chats.Message;
import nl.gjorgdy.database.records.RoleRecord;
import nl.gjorgdy.database.records.UserRecord;
import nl.gjorgdy.database.records.identifiers.Identifier;

public interface UserListener {

// User updates
    default void onUserDisplayNameUpdate(UserRecord userRecord) {}
    default void onUserConnectionUpdate(UserRecord userRecord, Identifier connection, boolean additive) {}
    default void onUserRoleUpdate(UserRecord userRecord, RoleRecord roleRecord, boolean additive) {}
// Message events
    default void onMessage(Message msg) {}


}
