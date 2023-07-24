package nl.gjorgdy.events;

import com.sun.tools.javac.Main;
import nl.gjorgdy.Mainframe;
import nl.gjorgdy.database.identifiers.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Events implements UserListener, RoleListener {

    private final List<UserListener> userListeners;
    private final List<RoleListener> roleListeners;
    private final ExecutorService executor;

    public Events() {
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
        Mainframe.logger.log("Updated display name of role '" + displayName + "'");
        roleListeners.forEach( rl ->
                executor.submit(() -> rl.onRoleDisplayNameUpdate(roleIdentifiers, displayName))
        );
    }

    @Override
    public void onRoleParentUpdate(List<Identifier> roleIdentifiers, List<Identifier> parentRoleIdentifiers, boolean additive) {
        Mainframe.logger.log("Updated parent of role '" + roleIdentifiers.get(0) + "'");
        roleListeners.forEach( rl ->
                executor.submit(() -> rl.onRoleParentUpdate(roleIdentifiers, parentRoleIdentifiers, additive))
        );
    }

    @Override
    public void onRolePermissionUpdate(List<Identifier> roleIdentifiers, String permission, boolean additive) {
        Mainframe.logger.log("Updated permissions of role '" + roleIdentifiers.get(0) + "'");
        roleListeners.forEach( rl ->
                executor.submit(() -> rl.onRolePermissionUpdate(roleIdentifiers, permission, additive))
        );
    }

    @Override
    public void onUserDisplayNameUpdate(List<Identifier> userIdentifiers, String newDisplayName) {
        Mainframe.logger.log("Updated display name of user '" + userIdentifiers.get(0) + "', '" + newDisplayName + "' ");
        userListeners.forEach( ul ->
            executor.submit(() -> ul.onUserDisplayNameUpdate(userIdentifiers, newDisplayName))
        );
    }

    @Override
    public void onUserConnectionUpdate(List<Identifier> userIdentifiers, Identifier connection, boolean additive) {
        Mainframe.logger.log("Updated connections of user '" + userIdentifiers.get(0) + "' ");
        userListeners.forEach( ul ->
                executor.submit(() -> ul.onUserConnectionUpdate(userIdentifiers, connection, additive))
        );
    }

    @Override
    public void onUserRoleUpdate(List<Identifier> userIdentifiers, List<Identifier> roleIdentifiers, boolean additive) {
        Mainframe.logger.log("Updated roles of user '" + userIdentifiers.get(0) + "' ");
        userListeners.forEach( ul ->
                executor.submit(() -> ul.onUserRoleUpdate(userIdentifiers, roleIdentifiers, additive))
        );
    }

}
