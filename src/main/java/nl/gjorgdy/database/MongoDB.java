package nl.gjorgdy.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import nl.gjorgdy.database.codecs.IdentifierArrayCodec;
import nl.gjorgdy.database.codecs.IdentifierCodec;
import nl.gjorgdy.database.codecs.IdentifierMapCodec;
import nl.gjorgdy.database.codecs.StringArrayCodec;
import nl.gjorgdy.database.handlers.ChannelHandler;
import nl.gjorgdy.database.handlers.RoleHandler;
import nl.gjorgdy.database.handlers.ServerHandler;
import nl.gjorgdy.database.handlers.UserHandler;
import nl.gjorgdy.database.records.ChannelRecord;
import nl.gjorgdy.database.records.RoleRecord;
import nl.gjorgdy.database.records.ServerRecord;
import nl.gjorgdy.database.records.UserRecord;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

public class MongoDB extends Thread {

    private final MongoClientSettings settings;
    private boolean status = false;
    private MongoClient mongoClient;
    public UserHandler userHandler;
    public RoleHandler roleHandler;
    public ChannelHandler channelHandler;
    public ServerHandler serverHandler;

    public MongoDB() {
        String username = "mainframe";
        String password = "VUnu9DkTD9CUVKNT76Sg82bY5tDDnL";
        String ip = "hexasis.eu:27017";
        String uri = "mongodb://" + username + ":" + password + "@" + ip;

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(new IdentifierMapCodec(), new IdentifierCodec(), new IdentifierArrayCodec(), new StringArrayCodec()),
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder()
                                .register(RoleRecord.class)
                                .register(UserRecord.class)
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
        // Block main thread until Database has connected
        while (!status) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void close() {
        mongoClient.close();
    }

    @Override
    public void run() {
        // Keep the database connected
        while (true) {
            try (MongoClient mongoClient = MongoClients.create(settings)) {
                this.mongoClient = mongoClient;
                // Get the Mainframe database
                MongoDatabase database = mongoClient.getDatabase("mainframe");
                // Get user collection and create handler
                MongoCollection<UserRecord> userCollection = database.getCollection("users", UserRecord.class);
                userHandler = new UserHandler(userCollection);
                // Get role collection and create handler
                MongoCollection<RoleRecord> roleCollection = database.getCollection("roles", RoleRecord.class);
                roleHandler = new RoleHandler(roleCollection);
                // Get channel collection and create handler
                MongoCollection<ChannelRecord> channelCollection = database.getCollection("channels", ChannelRecord.class);
                channelHandler = new ChannelHandler(roleCollection);
                // Get server collection and create handler
                MongoCollection<ServerRecord> serverCollection = database.getCollection("servers", ServerRecord.class);
                serverHandler = new ServerHandler(serverCollection);
                // Ping to check connection
                try {
                    while (true) {
                        // Send a ping to confirm a successful connection
                        Bson command = new BsonDocument("ping", new BsonInt64(1));
                        Document commandResult = database.runCommand(command);
                        // If reached, connection established
                        status = true;
                        // Sleep for 10 seconds
                        Thread.sleep(10000);
                    }
                } catch (MongoException | InterruptedException e) {
                    System.err.println(e);
                    // Set ready state to false
                    status = false;
                }
            }
        }
    }

    public String getStatus() {
        return status ? "Online" : "Offline";
    }
}
