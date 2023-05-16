package nl.gjorgdy.discord.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.gjorgdy.Main;
import nl.gjorgdy.chats.Message;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.discord.Functions;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageListener extends ListenerAdapter {

    private final List<Long> channelIds;

    public MessageListener(List<Long> channelIds) {
        this.channelIds = channelIds;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (channelIds.contains(event.getChannel().getIdLong())) {
            // Get objects
            Identifier authorIdentifier = Functions.createIdentifier(event.getAuthor());
            Identifier channelIdentifier = Functions.createIdentifier(event.getChannel());
            // Construct message
            Message mainframeMessage = new Message(
                    // Create an identifier for user
                    authorIdentifier,
                    // Create an identifier for channel
                    channelIdentifier,
                    // Get the contents of this message
                    event.getMessage().getContentRaw()
            );
            // Forward message
            Main.MESSAGE_FORWARDER.send(channelIdentifier, mainframeMessage);
        }
    }

}
