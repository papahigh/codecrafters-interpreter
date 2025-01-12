package scanner;

import java.util.Map;

import static java.util.Map.entry;
import static scanner.TokenType.*;

class TokenTypes {

    private static final Map<String, TokenType> keywords = Map.ofEntries(
            entry("and", AND),
            entry("class", CLASS),
            entry("else", ELSE),
            entry("false", FALSE),
            entry("for", FOR),
            entry("fun", FUN),
            entry("if", IF),
            entry("nil", NIL),
            entry("or", OR),
            entry("print", PRINT),
            entry("return", RETURN),
            entry("super", SUPER),
            entry("this", THIS),
            entry("true", TRUE),
            entry("var", VAR),
            entry("while", WHILE)
    );

    static TokenType identifier(String token) {
        return keywords.getOrDefault(token, IDENTIFIER);
    }
}
