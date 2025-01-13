package doctor;

import scanner.Token;

import static scanner.TokenType.EOF;

public sealed interface Doctor permits ConsoleDoctor {

    void diagnostics();

    default void error(int line, String message) {
        report(line, "", message);
    }

    default void error(Token token, String message) {
        if (token.type() == EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), " at'" + token.lexeme() + "'", message);
        }
    }

    void report(int line, String where, String message);

    static Doctor console() {
        return new ConsoleDoctor();
    }
}
