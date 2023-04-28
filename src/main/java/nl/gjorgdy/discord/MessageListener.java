package nl.gjorgdy.discord;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.gjorgdy.Main;
import nl.gjorgdy.Message;
import nl.gjorgdy.database.Records;
import nl.gjorgdy.database.records.identifiers.Identifier;
import nl.gjorgdy.database.records.identifiers.LongIdentifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class MessageListener extends ListenerAdapter {

    private final List<Long> channelIds;

    public MessageListener(List<Long> channelIds) {
        this.channelIds = channelIds;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (channelIds.contains(event.getChannel().getIdLong())) {
            Message mainframeMessage = new Message(
                    // Create an identifier for this user
                    new LongIdentifier(Identifier.Types.discord_user, event.getAuthor().getIdLong()),
                    // Get the contents of this message
                    event.getMessage().getContentRaw()
            );

            try {
                Main.MESSAGE_OUTPUT_STREAM.writeObject(mainframeMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
