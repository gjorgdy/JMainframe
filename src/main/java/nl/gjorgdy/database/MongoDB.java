package nl.gjorgdy.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.github.cdimascio.dotenv.Dotenv;
import nl.gjorgdy.Config;
import nl.gjorgdy.database.handlers.ChannelHandler;
import nl.gjorgdy.database.handlers.RoleHandler;
import nl.gjorgdy.database.handlers.ServerHandler;
import nl.gjorgdy.database.handlers.UserHandler;

import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.database.identifiers.LongIdentifier;
import nl.gjorgdy.database.identifiers.ObjectIDIdentifier;
import nl.gjorgdy.database.identifiers.StringIdentifier;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.List;

public class MongoDB {

    private final MongoClient mongoClient;
    public final UserHandler userHandler;
    public final MongoCollection<Document> userCollection;
    public final RoleHandler roleHandler;
    public final ChannelHandler channelHandler;
    public final ServerHandler serverHandler;
    public final MongoCollection<Document> serverCollection;
    public CodecRegistry codecRegistry;

    public MongoDB() {
        // DOTENV loader
        Config cfg = new Config("MONGO");

        String username = cfg.get("USERNAME");
        String password = cfg.get("PASSWORD");
        String ip = cfg.get("IP");
        String database = cfg.get("DATABASE");
        String uri = constructURI(username, password, ip);

        codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder()
                                .register(LongIdentifier.class)
                                .register(ObjectIDIdentifier.class)
                                .register(StringIdentifier.class)
                                .register(List.class)
                                .build()
                )
        );
        // Settings
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .codecRegistry(codecRegistry)
                .build();
        // Get the Mainframe database
        mongoClient = MongoClients.create(settings);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
        // Get user collection and create handler
        userCollection = mongoDatabase.getCollection("users");
        userHandler = new UserHandler(userCollection);
        // Get role collection and create handler
        roleHandler = new RoleHandler(mongoDatabase.getCollection("roles"));
        // Get channel collection and create handler
        channelHandler = new ChannelHandler(mongoDatabase.getCollection("channels"));
        // Get server collection and create handler
        serverCollection = mongoDatabase.getCollection("servers");
        serverHandler = new ServerHandler(serverCollection);
    }

    public static String constructURI(String username, String password, String ip) {
        return "mongodb://" + username + ":" + password + "@" + ip;
    }

    public void shutdown() {
        mongoClient.close();
    }
}
