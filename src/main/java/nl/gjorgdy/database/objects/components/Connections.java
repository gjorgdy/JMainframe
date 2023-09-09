package nl.gjorgdy.database.objects.components;

import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.database.identifiers.LongIdentifier;
import nl.gjorgdy.database.identifiers.Types;
import nl.gjorgdy.database.objects.DatabaseObject;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Connections extends DatabaseComponent {

    public Connections(DatabaseObject parent, String key) throws NotRegisteredException {
        super(parent, key);

        List<Document> identifierDocuments = parent.getDocument().getList(key, Document.class);
    }

    @Nullable
    public List<Identifier> get(Types type) throws NotRegisteredException {
        return parent.getDocument()
                .getList(key, Document.class)
                .parallelStream()
                .map(document -> {
                    System.out.println(document.toJson());
                    Identifier identifier = Identifier.fromDocument(document);
                    System.out.println(identifier);
                    return identifier;
                })
                .toList();
    }

    @Nullable
    public Identifier getFirst(Types type) throws NotRegisteredException {
        List<Identifier> identifiers = get(type);
        return identifiers != null && identifiers.size() > 1 ? identifiers.get(0) : null;
    }

}
