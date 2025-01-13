package parser;

import doctor.Doctor;
import scanner.Token;
import scanner.TokenType;

import java.util.List;
import java.util.function.Supplier;

import static scanner.TokenType.*;

public class Parser {

    private final List<Token> tokens;
    private final Doctor doctor;

    private int current = 0;

    public Parser(List<Token> tokens, Doctor doctor) {
        this.tokens = tokens;
        this.doctor = doctor;
    }

    public Expression parse() {
        try {
            return expression();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Expression expression() {
        return ternary();
    }

    private Expression ternary() {
        var expression = equality();
        if (match(QUESTION)) {
            var thenBranch = expression();
            consume(COLON, "Expect ':' in ternary operator");
            var elseBranch = expression();
            expression = new Expression.TernaryExpression(expression, thenBranch, elseBranch);
        }
        return expression;
    }

    private Expression equality() {
        return binary(this::comparison, BANG_EQUAL, EQUAL_EQUAL);
    }

    private Expression comparison() {
        return binary(this::term, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
    }

    private Expression term() {
        return binary(this::factor, MINUS, PLUS);
    }

    private Expression factor() {
        return binary(this::unary, SLASH, STAR);
    }

    private Expression binary(Supplier<Expression> leftSupplier, TokenType... operators) {
        var expression = leftSupplier.get();
        while (match(operators)) {
            var operator = previous();
            var right = leftSupplier.get();
            expression = new Expression.BinaryExpression(expression, operator, right);
        }
        return expression;
    }

    private Expression unary() {
        if (match(BANG, MINUS)) {
            var operator = previous();
            var right = unary();
            return new Expression.UnaryExpression(operator, right);
        }
        return primary();
    }

    private Expression primary() {
        if (match(FALSE)) return new Expression.LiteralExpression(false);
        if (match(TRUE)) return new Expression.LiteralExpression(true);
        if (match(NIL)) return new Expression.LiteralExpression(null);
        if (match(NUMBER, STRING)) return new Expression.LiteralExpression(previous().literal());

        if (match(LEFT_PAREN)) {
            var expression = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression");
            return new Expression.GroupingExpression(expression);
        }

        throw error(peek(), "Expect expression.");
    }


    private void synchronize() {
        advance();
        while (!isEOF()) {
            if (previous().type() == SEMICOLON) return;
            switch (peek().type()) {
                case CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> {
                    return;
                }
            }
            advance();
        }
    }

    private boolean match(TokenType... types) {
        for (var type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isEOF()) return false;
        return peek().type() == type;
    }

    private Token advance() {
        if (!isEOF()) current++;
        return peek();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isEOF() {
        return peek().type() == EOF;
    }

    private ParseError error(Token token, String message) {
        doctor.error(token, message);
        return new ParseError();
    }

    private static class ParseError extends RuntimeException {
    }
}
