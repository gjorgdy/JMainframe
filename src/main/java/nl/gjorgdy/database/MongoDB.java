package nl.gjorgdy.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import nl.gjorgdy.objects.Identifier;
import nl.gjorgdy.objects.Role;
import nl.gjorgdy.objects.User;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class MongoDB extends Thread {

    private final MongoClientSettings settings;
    private MongoDatabase database;
    private MongoCollection<User> userCollection;
    private MongoCollection<Role> roleCollection;

    public MongoDB() {
        String username = "mainframe";
        String password = "VUnu9DkTD9CUVKNT76Sg82bY5tDDnL";
        String ip = "hexasis.eu:27017";
        String uri = "mongodb://" + username + ":" + password + "@" + ip;

        settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .build();
    }

    @Override
    public void run() {
        // Keep the database connected
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            database = mongoClient.getDatabase("mainframe");
            userCollection = database.getCollection("users", User.class);
            roleCollection = database.getCollection("roles", Role.class);
            // Ping every n seconds
            while (true) {
                try {
                    // TODO: implement data sync
                    // Send a ping to confirm a successful connection
                    Bson command = new BsonDocument("ping", new BsonInt64(1));
                    Document commandResult = database.runCommand(command);
                    System.out.println("[MongoDB] Successfully pinged database");
                    Thread.sleep(10000);
                } catch (MongoException |InterruptedException e) {
                    System.err.println(e);
                }
            }
        }
    }

    public boolean isReady() {
        return userCollection != null && roleCollection != null;
    }

    public Role getRole(ObjectId roleId) {
        return roleCollection.find(new Document("_id", roleId)).first();
    }

    public User insertUser(User user) {
        userCollection.insertOne(user);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return userCollection.find(new Document("display_name", user.getDisplayName())).first();
    }
}
