package nl.gjorgdy;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    final public Dotenv cfg = Dotenv.load();
    final public String prefix;

    public Config(String prefix) {
        this.prefix = prefix;
    }

    public String get(String key) {
        String constructedKey = prefix + "_" + key;
        String value = cfg.get(constructedKey);
        if (value == null)
            throw new RuntimeException("Key \"" + constructedKey + "\" is not set in .env");
        else
            return value;
    }

}
