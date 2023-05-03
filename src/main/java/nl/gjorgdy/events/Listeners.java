package nl.gjorgdy.events;

import nl.gjorgdy.chats.Message;
import nl.gjorgdy.database.records.RoleRecord;
import nl.gjorgdy.database.records.UserRecord;
import nl.gjorgdy.database.records.identifiers.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Listeners implements UserListener, RoleListener {

    private final List<UserListener> userListeners;

    public Listeners() {
        userListeners = new ArrayList<>();
    }

    public void addListener(UserListener userListener) {
        userListeners.add(userListener);
    }


    @Override
    public void onUserDisplayNameUpdate(UserRecord userRecord) {
        UserListener.super.onUserDisplayNameUpdate(userRecord);
    }

    @Override
    public void onRoleDisplayNameUpdate(RoleRecord roleRecord) {
        RoleListener.super.onRoleDisplayNameUpdate(roleRecord);
    }

    @Override
    public void onUserConnectionUpdate(UserRecord userRecord, Identifier connection, boolean additive) {
        UserListener.super.onUserConnectionUpdate(userRecord, connection, additive);
    }

    @Override
    public void onUserRoleUpdate(UserRecord userRecord, RoleRecord roleRecord, boolean additive) {
        UserListener.super.onUserRoleUpdate(userRecord, roleRecord, additive);
    }

    @Override
    public void onMessage(Message msg) {
        for (UserListener userListener : userListeners) {
            userListener.onMessage(msg);
        }
    }
}
