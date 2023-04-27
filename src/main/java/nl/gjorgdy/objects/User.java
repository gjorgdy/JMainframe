package nl.gjorgdy.objects;

import nl.gjorgdy.Main;
import nl.gjorgdy.objects.connections.Connection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    private final ObjectId userId;
    private final List<Role> roles;
    private final Map<Connection.Type, Connection> connections;
    private String displayName;

    public User(Document document) {
        this.userId = document.getObjectId("_id");
        this.displayName = document.getString("display_name");
        // Create an empty list for all roles
        this.roles = new ArrayList<>();
        // Add all roles
        for (ObjectId roleId : document.getList("roles", ObjectId.class)) {
            this.roles.add(Main.MONGO_DB.getRole(roleId));
        }
        this.connections = new HashMap<>();
        for (Document connection : document.getList("connections", Document.class)) {

        }
    }

    public User(ObjectId userId, String displayName, List<Role> roles, Map<Connection.Type, Connection> connections) {
        this.userId = userId;
        this.displayName = displayName;
        this.roles = roles;
        this.connections = connections;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public Connection getConnection(String type) {
        return connections.getOrDefault(type, null);
    }

    public boolean addConnection(Connection connection) {
        connections.put(connection.getType(), connection);
        // TODO: update user
        return true;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
