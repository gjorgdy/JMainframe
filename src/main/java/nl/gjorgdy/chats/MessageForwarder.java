package nl.gjorgdy.chats;

import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.HashMap;

public class MessageForwarder{

    private final ObjectId global = new ObjectId();
    private final HashMap<ObjectId, MessageHandler> messageHandlers = new HashMap<>();

    public MessageForwarder() throws IOException {
        messageHandlers.put(global, new MessageHandler(new Identifier[0]));
    }

    public void registerChannel(Identifier[] channelIdentifiers) {

    }

    public void send(Identifier channelIdentifier, Message msg) {
        messageHandlers.get(global).send(msg);
    }

    public void start() {
        for (MessageHandler messageHandler : messageHandlers.values()) {
            messageHandler.start();
        }
    }

}
