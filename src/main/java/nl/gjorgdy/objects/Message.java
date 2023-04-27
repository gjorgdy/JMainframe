package nl.gjorgdy.objects;

import java.io.Serializable;

public class Message implements Serializable {

    private final Identifier authorIdentifier;
    private final String content;

    public Message(Identifier authorIdentifier, String content) {
        this.authorIdentifier = authorIdentifier;
        this.content = content;

    }

    public Identifier getAuthorIdentifier() {
        return authorIdentifier;
    }

    public String getContent() {
        return content;
    }
}
