package doctor;

public sealed interface Doctor permits ConsoleDoctor {

    void diagnostics();

    default void error(int line, String message) {
        report(line, "", message);
    }

    void report(int line, String where, String message);

    static Doctor console() {
        return new ConsoleDoctor();
    }
}
