package nl.gjorgdy.objects;

import nl.gjorgdy.objects.connections.Connection;

import java.util.List;

public class User {

    private final long userId;
    private final List<Role> roles;
    private final List<Connection> connections;
    private String displayName;

    public User(long userId, String displayName, List<Role> roles, List<Connection> connections) {
        this.userId = userId;
        this.displayName = displayName;
        this.roles = roles;
        this.connections = connections;
    }

    public long getUserId() {
        return userId;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
