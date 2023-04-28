package nl.gjorgdy.database.records;

import java.util.List;

public interface DatabaseRecord {

    List<String> toStringList();

    default String toFormattedString() {
        return String.join("\n", toStringList());
    }

}
