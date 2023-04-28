package nl.gjorgdy.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import nl.gjorgdy.database.codecs.IdentifierArrayCodec;
import nl.gjorgdy.database.codecs.IdentifierCodec;
import nl.gjorgdy.database.codecs.RoleArrayCodec;
import nl.gjorgdy.database.records.Role;
import nl.gjorgdy.database.records.User;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class MongoDB extends Thread {

    private final MongoClientSettings settings;
    private MongoCollection<User> userCollection;
    private MongoCollection<Role> roleCollection;

    public MongoDB() {
        String username = "mainframe";
        String password = "VUnu9DkTD9CUVKNT76Sg82bY5tDDnL";
        String ip = "hexasis.eu:27017";
        String uri = "mongodb://" + username + ":" + password + "@" + ip;

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(new IdentifierCodec(), new IdentifierArrayCodec(), new RoleArrayCodec()),
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder()
                                .register(Role.class)
                                .register(User.class)
                                .build()
                )
        );

        settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .codecRegistry(codecRegistry)
                .build();
    }

    @Override
    public void start() {
        super.start();
        // Block main thread till Database has connected
        while (isNotReady()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void run() {
        // Keep the database connected
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("mainframe");
            userCollection = database.getCollection("users", User.class);
            roleCollection = database.getCollection("roles", Role.class);
            // Ping every n seconds
            while (true) {
                try {
                    // TODO: implement data sync
                    // Send a ping to confirm a successful connection
                    Bson command = new BsonDocument("ping", new BsonInt64(1));
                    Document commandResult = database.runCommand(command);
                    //System.out.println("[MongoDB] Successfully pinged database");
                    Thread.sleep(10000);
                } catch (MongoException |InterruptedException e) {
                    System.err.println(e);
                }
            }
        }
    }

    public boolean isNotReady() {
        return userCollection == null || roleCollection == null;
    }

    public Role getRole(ObjectId roleId) {
        return roleCollection.find(new Document("_id", roleId)).first();
    }

    public User testInsertUser(User user) {
        userCollection.deleteMany(new BsonDocument());
        InsertOneResult result = userCollection.insertOne(user);
        System.out.print(result.getInsertedId());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return userCollection.find(new Document("_id", result.getInsertedId())).first();
    }

    public Role testInsertRole(Role role) {
        roleCollection.deleteMany(new BsonDocument());
        InsertOneResult result = roleCollection.insertOne(role);
        System.out.print(result.getInsertedId());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return roleCollection.find(new Document("_id", result.getInsertedId())).first();
    }
}
