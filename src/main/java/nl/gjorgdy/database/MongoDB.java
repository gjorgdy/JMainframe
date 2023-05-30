package nl.gjorgdy.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import nl.gjorgdy.database.handlers.ChannelHandler;
import nl.gjorgdy.database.handlers.RoleHandler;
import nl.gjorgdy.database.handlers.ServerHandler;
import nl.gjorgdy.database.handlers.UserHandler;

import nl.gjorgdy.database.identifiers.Identifier;
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
    public CodecRegistry codecRegistry;

    public MongoDB() {
        String username = "mainframe";
        String password = "VUnu9DkTD9CUVKNT76Sg82bY5tDDnL";
        String ip = "hexasis.eu:27017";
        String uri = "mongodb://" + username + ":" + password + "@" + ip;

        codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                //CodecRegistries.fromCodecs(
                //        new IdentifierCodec(),
                //        new IdentifierArrayCodec(),
                //        new StringArrayCodec(),
                //        new ObjectIDArrayCodec()
                //)
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder()
                                .register(Identifier.class)
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
                MongoCollection<Document> userCollection = database.getCollection("users");
                userHandler = new UserHandler(userCollection);
                // Get role collection and create handler
                MongoCollection<Document> roleCollection = database.getCollection("roles");
                roleHandler = new RoleHandler(roleCollection);
                // Get channel collection and create handler
                MongoCollection<Document> channelCollection = database.getCollection("channels");
                channelHandler = new ChannelHandler(channelCollection);
                // Get server collection and create handler
                MongoCollection<Document> serverCollection = database.getCollection("servers");
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
