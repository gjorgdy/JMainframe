package nl.gjorgdy.events;

import nl.gjorgdy.chats.Message;
import nl.gjorgdy.database.identifiers.Identifier;

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
    public void onRoleDisplayNameUpdate(Identifier[] roleIdentifiers, String displayName) {
        RoleListener.super.onRoleDisplayNameUpdate(roleIdentifiers, displayName);
    }

    @Override
    public void onRoleParentUpdate(Identifier[] roleIdentifiers, Identifier[] parentRoleIdentifiers, boolean additive) {
        RoleListener.super.onRoleParentUpdate(roleIdentifiers, parentRoleIdentifiers, additive);
    }

    @Override
    public void onRolePermissionUpdate(Identifier[] roleIdentifiers, String permission, boolean additive) {
        RoleListener.super.onRolePermissionUpdate(roleIdentifiers, permission, additive);
    }

    @Override
    public void onUserDisplayNameUpdate(Identifier[] userIdentifiers, String newDisplayName) {
        UserListener.super.onUserDisplayNameUpdate(userIdentifiers, newDisplayName);
    }

    @Override
    public void onUserConnectionUpdate(Identifier[] userIdentifiers, Identifier connection, boolean additive) {
        UserListener.super.onUserConnectionUpdate(userIdentifiers, connection, additive);
    }

    @Override
    public void onUserRoleUpdate(Identifier[] userIdentifiers, Identifier[] roleIdentifiers, boolean additive) {
        UserListener.super.onUserRoleUpdate(userIdentifiers, roleIdentifiers, additive);
    }

    @Override
    public void onMessage(Message msg) {
        UserListener.super.onMessage(msg);
    }
}
