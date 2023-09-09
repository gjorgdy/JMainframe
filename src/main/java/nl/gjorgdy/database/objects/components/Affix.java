package nl.gjorgdy.database.objects.components;

import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.objects.DatabaseObject;
import org.bson.Document;

/**
 *
 * Prefix and Suffix implementation
 *  use _ to add spaces to the affixes
 *
 */
public class Affix extends DatabaseComponent {

    static String KEY_PREFIX = "prefix";
    static String KEY_SUFFIX = "suffix";

    public Affix(DatabaseObject parent, String key) {
        super(parent, key);
    }

    public String[] get() throws NotRegisteredException {
        Document doc = getEntry();
        return new String[]{
            doc.getString(KEY_PREFIX),
            doc.getString(KEY_SUFFIX)
        };
    }

    public String apply(String in) throws NotRegisteredException {
        String[] affix = get();
        return affix[0] + in + affix[1];
    }

    public String strip(String in) throws NotRegisteredException {
        String[] affix = get();
        String prefixRegex = "^" + affix[0];
        in = in.replaceAll(prefixRegex, "");
        String suffixRegex = affix[1] + "$";
        in = in.replaceAll(suffixRegex, "");
        return affix[0] + in + affix[1];
    }

}
