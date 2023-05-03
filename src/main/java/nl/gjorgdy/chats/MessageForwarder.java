package nl.gjorgdy.chats;

import nl.gjorgdy.database.records.ChannelRecord;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.HashMap;

public class MessageForwarder{

    private final ObjectId global = new ObjectId();
    private final HashMap<ObjectId, MessageHandler> messageHandlers = new HashMap<>();

    public MessageForwarder() throws IOException {
        messageHandlers.put(global, new MessageHandler(new ChannelRecord(null, "global", null, null)));
    }

    public void registerChannel(ChannelRecord channelRecord) {

    }

    public void send(ChannelRecord channelRecord, Message msg) {
        try {
            messageHandlers.get(global).send(msg);
        } catch (NullPointerException e) {
            System.err.println("Could not send message : " + e.getMessage());
        }
    }

    public void start() {
        for (MessageHandler messageHandler : messageHandlers.values()) {
            messageHandler.start();
        }
    }

}
