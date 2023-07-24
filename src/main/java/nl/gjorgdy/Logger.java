package nl.gjorgdy;

public class Logger {

    private final String moduleName;

    public Logger(String moduleName) {
        this.moduleName = moduleName;
    }

    public void log(String string) {
        String out =
                "\u001B[37m" +
                "[" +
                moduleName +
                "] " +
                string;
        System.err.println(out);
    }

    public void alert(String string) {
        String out =
                "\u001B[33m" +
                "[" +
                moduleName +
                "] " +
                string;
        System.err.println(out);
    }

    public void error(String string) {
        String out =
                "\u001B[31m" +
                "[" +
                moduleName +
                "] " +
                string;
        System.err.println(out);
    }

}
