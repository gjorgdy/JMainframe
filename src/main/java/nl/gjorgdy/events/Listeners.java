package nl.gjorgdy.events;

import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.discord.Sync;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Listeners implements UserListener, RoleListener {

    private final List<UserListener> userListeners;
    private final List<RoleListener> roleListeners;
    private final ExecutorService executor;

    public Listeners() {
        userListeners = new ArrayList<>();
        roleListeners = new ArrayList<>();
        executor = Executors.newFixedThreadPool(10);
    }

    public void addListener(UserListener userListener) {
        userListeners.add(userListener);
    }

    public void addListener(RoleListener roleListener) {
        roleListeners.add(roleListener);
    }

    @Override
    public void onRoleDisplayNameUpdate(List<Identifier> roleIdentifiers, String displayName) {
        roleListeners.forEach( rl ->
                executor.submit(() -> rl.onRoleDisplayNameUpdate(roleIdentifiers, displayName))
        );
    }

    @Override
    public void onRoleParentUpdate(List<Identifier> roleIdentifiers, List<Identifier> parentRoleIdentifiers, boolean additive) {
        roleListeners.forEach( rl ->
                executor.submit(() -> rl.onRoleParentUpdate(roleIdentifiers, parentRoleIdentifiers, additive))
        );
    }

    @Override
    public void onRolePermissionUpdate(List<Identifier> roleIdentifiers, String permission, boolean additive) {
        roleListeners.forEach( rl ->
                executor.submit(() -> rl.onRolePermissionUpdate(roleIdentifiers, permission, additive))
        );
    }

    @Override
    public void onUserDisplayNameUpdate(List<Identifier> userIdentifiers, String newDisplayName) {
        userListeners.forEach( ul ->
            executor.submit(() -> ul.onUserDisplayNameUpdate(userIdentifiers, newDisplayName))
        );
    }

    @Override
    public void onUserConnectionUpdate(List<Identifier> userIdentifiers, Identifier connection, boolean additive) {
        userListeners.forEach( ul ->
                executor.submit(() -> ul.onUserConnectionUpdate(userIdentifiers, connection, additive))
        );
    }

    @Override
    public void onUserRoleUpdate(List<Identifier> userIdentifiers, List<Identifier> roleIdentifiers, boolean additive) {
        userListeners.forEach( ul ->
                executor.submit(() -> ul.onUserRoleUpdate(userIdentifiers, roleIdentifiers, additive))
        );
    }

}
