package scanner;

import doctor.Doctor;

import java.util.ArrayList;
import java.util.List;

import static scanner.TokenType.*;

public class Scanner {

    private final String source;
    private final Doctor doctor;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String source, Doctor doctor) {
        this.source = source;
        this.doctor = doctor;
    }

    public List<Token> scanTokens() {
        while (!isEOF()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '0','1','2','3','4','5','6','7','8','9' -> addNumber();
            case '"' -> addString();
            case '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isEOF()) advance();
                } else addToken(SLASH);
            }
            case ' ', '\r', '\t' -> {}
            case '\n' -> line++;
            default -> doctor.error(line, "Unexpected character: %c".formatted(c));
        }
    }

    private void addNumber() {
        while (Character.isDigit(peek())) advance();
        if (peek() == '.' && Character.isDigit(peekNext())) {
            do advance();
            while (Character.isDigit(peek()));
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void addString() {
        while (peek() != '"' && !isEOF()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isEOF()) {
            doctor.error(line, "Unterminated string.");
        } else {
            advance();
            var value = source.substring(start + 1, current - 1);
            addToken(STRING, value);
        }
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        var text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if (isEOF()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isEOF()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isEOF() {
        return current >= source.length();
    }
}
