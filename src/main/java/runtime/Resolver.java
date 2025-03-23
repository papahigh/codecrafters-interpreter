package runtime;

import doctor.Doctor;
import parser.Expression;
import parser.Statement;
import scanner.Token;

import java.util.*;


public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {

    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private final Runtime runtime;
    private final Doctor doctor;

    public Resolver(Runtime runtime, Doctor doctor) {
        this.runtime = runtime;
        this.doctor = doctor;
    }

    @Override
    public Void visit(Expression.TernaryExpression it) {
        resolve(it.condition());
        resolve(it.thenBranch());
        resolve(it.elseBranch());
        return null;
    }

    @Override
    public Void visit(Expression.AssignExpression it) {
        resolve(it.value());
        resolveLocal(it, it.name());
        return null;
    }

    @Override
    public Void visit(Expression.BinaryExpression it) {
        resolve(it.left());
        resolve(it.right());
        return null;
    }

    @Override
    public Void visit(Expression.CallExpression it) {
        resolve(it.callee());
        for (var argument : it.arguments()) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visit(Expression.FunctionExpression it) {
        resolve(it.body());
        return null;
    }

    @Override
    public Void visit(Expression.GroupingExpression it) {
        resolve(it.expression());
        return null;
    }

    @Override
    public Void visit(Expression.LogicalExpression it) {
        resolve(it.left());
        resolve(it.right());
        return null;
    }

    @Override
    public Void visit(Expression.LiteralExpression it) {
        return null;
    }

    @Override
    public Void visit(Expression.UnaryExpression it) {
        resolve(it.right());
        return null;
    }

    @Override
    public Void visit(Expression.VariableExpression it) {
        if (!scopes.isEmpty() && scopes.peek().get(it.name().lexeme()) == Boolean.FALSE) {
            doctor.error(it.name(), "Can't read local variable in its own initializer.");
        }
        resolveLocal(it, it.name());
        return null;
    }

    @Override
    public Void visit(Statement.BlockStatement it) {
        beginScope();
        resolve(it.statements());
        endScope();
        return null;
    }

    @Override
    public Void visit(Statement.ExpressionStatement it) {
        resolve(it.expression());
        return null;
    }

    @Override
    public Void visit(Statement.FunctionStatement it) {
        declare(it.name());
        define(it.name());
        resolveFunction(it);
        return null;
    }

    @Override
    public Void visit(Statement.IfStatement it) {
        resolve(it.condition());
        resolve(it.thenBranch());
        if (it.elseBranch() != null) {
            resolve(it.elseBranch());
        }
        return null;
    }

    @Override
    public Void visit(Statement.PrintStatement it) {
        resolve(it.expression());
        return null;
    }

    @Override
    public Void visit(Statement.ReturnStatement it) {
        if (it.value() != null) {
            resolve(it.value());
        }
        return null;
    }

    @Override
    public Void visit(Statement.VarStatement it) {
        declare(it.name());
        if (it.initializer() != null) {
            resolve(it.initializer());
        }
        define(it.name());
        return null;
    }

    @Override
    public Void visit(Statement.WhileStatement it) {
        resolve(it.condition());
        resolve(it.body());
        return null;
    }

    public void resolve(Iterable<Statement> statements) {
        for (var statement : statements)
            resolve(statement);
    }

    private void resolve(Statement statement) {
        statement.accept(this);
    }

    public void resolve(Expression expression) {
        expression.accept(this);
    }

    private void resolveFunction(Statement.FunctionStatement function) {
        beginScope();
        for (var param : function.parameters()) {
            declare(param);
            define(param);
        }
        resolve(function.body());
        endScope();
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;
        var scope = scopes.peek();
        if (scope.containsKey(name.lexeme())) {
            doctor.error(name, "Variable '%s' already declared in this scope.".formatted(name.lexeme()));
        }
        scope.put(name.lexeme(), false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme(), true);
    }

    private void resolveLocal(Expression expression, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme())) {
                runtime.resolve(expression, scopes.size() - 1 - i);
                return;
            }
        }
    }
}
