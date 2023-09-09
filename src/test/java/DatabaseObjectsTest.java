import nl.gjorgdy.Mainframe;
import nl.gjorgdy.database.MongoDB;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.database.identifiers.Types;
import nl.gjorgdy.database.objects.Server;
import nl.gjorgdy.database.objects.User;
import org.bson.assertions.Assertions;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class DatabaseObjectsTest {

    @Test
    public void testServer() {

        try {
            Mainframe mf = new Mainframe();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MongoDB db = new MongoDB();

        try {
            Server server = new Server(db.serverCollection, new ObjectId("64a19502c56d90231b9ad571"));

            String displayName = server.displayName.get();
            System.out.println(displayName);
            Assertions.isTrue("Name", displayName.equals("El mainframe Developmento"));

            Identifier identifier = server.identifiers.getFirst(Types.discord_guild);
            System.out.println(identifier);
            Assertions.isTrue("Identifier", identifier != null);

        } catch (NotRegisteredException e) {
            Assertions.fail(e.getMessage());
        }

    }

    @Test
    public void testUser() {

        try {
            Mainframe mf = new Mainframe();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MongoDB db = new MongoDB();

        try {
            User user = new User(db.userCollection, new ObjectId("64a1895494b3202877001853"));

            String displayName = user.displayName.get();
            System.out.println(displayName);
            Assertions.isTrue("Name", displayName.equals("gjorgdy"));

            List<Identifier> identifier = user.identifiers.get(Types.discord_user);
            System.out.println(identifier);
            Assertions.isTrue("Identifier", identifier != null && identifier.size() > 0);

        } catch (NotRegisteredException e) {
            Assertions.fail(e.getMessage());
        }


    }

}
