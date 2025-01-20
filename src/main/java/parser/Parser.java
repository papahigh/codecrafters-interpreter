package parser;

import doctor.Doctor;
import doctor.RuntimeError;
import scanner.Token;
import scanner.TokenType;

import java.util.ArrayList;
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

    public Expression parseExpression() {
        try {
            return expression();
        } catch (RuntimeError | ParseError error) {
            synchronize();
            return null;
        }
    }

    public List<Statement> parseStatements() {
        var output = new ArrayList<Statement>();
        while (!isEOF()) {
            try {
                output.add(declaration());
            } catch (RuntimeError | ParseError error) {
                synchronize();
            }
        }
        return output;
    }

    private Statement declaration() {
        if (match(FUN)) return funDeclaration("function");
        if (match(VAR)) return varDeclaration();

        return statement();
    }

    private Statement funDeclaration(String kind) {
        var name = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        var parameters = new ArrayList<Token>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        var body = blockStatement();
        return new Statement.FunctionStatement(name, parameters, body);
    }

    private Statement varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expression initializer = null;
        if (match(EQUAL)) initializer = expression();

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Statement.VarStatement(name, initializer);
    }

    private Statement statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(RETURN)) return returnStatement();
        if (match(PRINT)) return printStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Statement.BlockStatement(blockStatement());

        return expressionStatement();
    }

    private Statement returnStatement() {
        var token = previous();
        var value = (Expression) null;
        if (!check(SEMICOLON)) {
            value = expression();
        }
        consume(SEMICOLON, "Expect ';' after return value.");
        return new Statement.ReturnStatement(token, value);
    }

    private Statement forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");
        var initializer = (Statement) null;
        if (!match(SEMICOLON)) {
            initializer = match(VAR) ? varDeclaration() : expressionStatement();
        }
        var condition = (Expression) null;
        if (!match(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");
        var increment = (Expression) null;
        if (!match(RIGHT_PAREN)) {
            increment = expression();
            consume(RIGHT_PAREN, "Expect ')' after clauses.");
        }
        var body = statement();


        if (increment != null) {
            body = new Statement.BlockStatement(List.of(body, new Statement.ExpressionStatement(increment)));
        }
        if (condition == null) {
            condition = new Expression.LiteralExpression(true);
        }
        var loop = new Statement.WhileStatement(condition, body);
        if (initializer == null)
            return loop;
        return new Statement.BlockStatement(List.of(initializer, loop));
    }

    private Statement ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        var condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after 'if' condition.");
        var thenBranch = statement();
        var elseBranch = (Statement) null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Statement.IfStatement(condition, thenBranch, elseBranch);
    }

    private Statement whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        var condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after 'while' condition.");
        var body = statement();
        return new Statement.WhileStatement(condition, body);
    }

    private List<Statement> blockStatement() {
        var statements = new ArrayList<Statement>();
        while (!check(RIGHT_BRACE) && !isEOF()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Statement printStatement() {
        var expression = expression();
        consume(SEMICOLON, "Expect ';' after expression");
        return new Statement.PrintStatement(expression);
    }

    private Statement expressionStatement() {
        var expression = expression();
        consume(SEMICOLON, "Expect ';' after expression");
        return new Statement.ExpressionStatement(expression);
    }

    private Expression expression() {
        return ternary();
    }

    private Expression ternary() {
        var expression = assignment();
        if (match(QUESTION)) {
            var thenBranch = expression();
            consume(COLON, "Expect ':' in ternary operator");
            var elseBranch = expression();
            expression = new Expression.TernaryExpression(expression, thenBranch, elseBranch);
        }
        return expression;
    }

    private Expression assignment() {
        var expression = or();

        if (match(EQUAL)) {
            var equals = previous();
            var value = expression();

            if (expression instanceof Expression.VariableExpression(Token name)) {
                return new Expression.AssignExpression(name, value);
            } else {
                error(equals, "Invalid assignment target.");
            }
        }
        return expression;
    }

    private Expression or() {
        var expression = and();
        if (match(OR)) {
            var operator = previous();
            var right = or();
            expression = new Expression.LogicalExpression(expression, operator, right);
        }
        return expression;
    }

    private Expression and() {
        var expression = equality();
        if (match(AND)) {
            var operator = previous();
            var right = or();
            expression = new Expression.LogicalExpression(expression, operator, right);
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

    private Expression binary(Supplier<Expression> supplier, TokenType... operators) {
        var expression = supplier.get();
        while (match(operators)) {
            var operator = previous();
            var right = supplier.get();
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
        return call();
    }

    private Expression call() {
        var expression = primary();
        while (true) {
            if (match(LEFT_PAREN)) {
                expression = finishCall(expression);
            } else {
                break;
            }
        }
        return expression;
    }

    private Expression finishCall(Expression expression) {
        var arguments = new ArrayList<Expression>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }
        var paren = consume(RIGHT_PAREN, "Expect ')' after arguments." );
        return new Expression.CallExpression(expression, paren, arguments);
    }

    private Expression primary() {
        if (match(FALSE)) return new Expression.LiteralExpression(false);
        if (match(TRUE)) return new Expression.LiteralExpression(true);
        if (match(NIL)) return new Expression.LiteralExpression(null);
        if (match(NUMBER, STRING)) return new Expression.LiteralExpression(previous().literal());
        if (match(IDENTIFIER)) return new Expression.VariableExpression(previous());

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
        if (check(type)) {
            advance();
            return previous();
        }
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
