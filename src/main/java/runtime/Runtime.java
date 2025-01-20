package runtime;

import doctor.Doctor;
import doctor.RuntimeError;
import parser.Expression;
import parser.Statement;
import scanner.Token;
import scanner.TokenType;

import java.util.ArrayList;
import java.util.List;


public class Runtime implements Expression.Visitor<Object>, Statement.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = new Environment(globals);
    private final Doctor doctor;

    public Runtime(Doctor doctor) {
        this.doctor = doctor;
    }

    {
        globals.define("clock", new Callable() {

            @Override
            public Object call(Runtime runtime, List<Object> args) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    public void run(Expression expression) {
        try {
            var value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            doctor.runtimeError(error);
        }
    }

    public void run(Iterable<Statement> statements) {
        try {
            for (var it : statements)
                it.accept(this);
        } catch (RuntimeError error) {
            doctor.runtimeError(error);
        }
    }

    @Override
    public Object visit(Expression.TernaryExpression it) {
        var condition = evaluate(it.condition());
        return isTruthy(condition) ? evaluate(it.thenBranch()) : evaluate(it.elseBranch());
    }

    @Override
    public Object visit(Expression.AssignExpression it) {
        var value = evaluate(it.value());
        environment.assign(it.name(), value);
        return value;
    }

    @Override
    public Object visit(Expression.BinaryExpression it) {
        var left = evaluate(it.left());
        var right = evaluate(it.right());
        return switch (it.operator().type()) {
            case MINUS -> number(it.operator(), left) - number(it.operator(), right);
            case STAR -> number(it.operator(), left) * number(it.operator(), right);
            case SLASH -> {
                var divisor = number(it.operator(), right);
                if (divisor == 0.0) throw new RuntimeError(it.operator(), "Division by zero");
                yield number(it.operator(), left) / divisor;
            }
            case PLUS -> {
                if (left instanceof Double d && right instanceof Double e) {
                    yield d + e;
                } else if (left instanceof String a && right instanceof String b) {
                    yield a + b;
                } else {
                    throw new RuntimeError(it.operator(), "Operands must be two numbers or two strings.");
                }
            }
            case GREATER -> number(it.operator(), left) > number(it.operator(), right);
            case GREATER_EQUAL -> number(it.operator(), left) >= number(it.operator(), right);
            case LESS -> number(it.operator(), left) < number(it.operator(), right);
            case LESS_EQUAL -> number(it.operator(), left) <= number(it.operator(), right);
            case EQUAL_EQUAL -> isEqual(left, right);
            case BANG_EQUAL -> !isEqual(left, right);
            default -> null;
        };
    }

    @Override
    public Object visit(Expression.CallExpression it) {
        var callee = evaluate(it.callee());
        var arguments = new ArrayList<>(it.arguments().size());

        for (var argument : it.arguments()) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof Callable c))
            throw new RuntimeError(it.paren(), "Can only call functions and classes.");

        if (c.length() != arguments.size())
            throw new RuntimeError(it.paren(), "Expected %s arguments but got %s.".formatted(c.length(), arguments.size()));

        return c.call(this, arguments);
    }

    @Override
    public Object visit(Expression.FunctionExpression it) {
        return new Callable.DefaultCallable(it.name(), it.parameters(), it.body(), environment);
    }

    @Override
    public Object visit(Expression.GroupingExpression it) {
        return evaluate(it.expression());
    }

    @Override
    public Object visit(Expression.LogicalExpression it) {
        Object left = evaluate(it.left());
        if (it.operator().type() == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }
        return evaluate(it.right());
    }

    @Override
    public Object visit(Expression.LiteralExpression it) {
        return it.value();
    }

    @Override
    public Object visit(Expression.UnaryExpression it) {
        var right = evaluate(it.right());
        return switch (it.operator().type()) {
            case MINUS -> -number(it.operator(), right);
            case BANG -> !isTruthy(right);
            default -> null;
        };
    }

    @Override
    public Object visit(Expression.VariableExpression it) {
        return environment.get(it.name());
    }

    @Override
    public Void visit(Statement.BlockStatement it) {
        executeBlock(it.statements(), environment.fork());
        return null;
    }

    @Override
    public Void visit(Statement.ExpressionStatement it) {
        evaluate(it.expression());
        return null;
    }

    @Override
    public Void visit(Statement.FunctionStatement it) {
        var callable = new Callable.DefaultCallable(it.name(), it.parameters(), it.body(), environment);
        environment.define(it.name().lexeme(), callable);
        return null;
    }

    @Override
    public Void visit(Statement.IfStatement it) {
        if (isTruthy(evaluate(it.condition()))) {
            it.thenBranch().accept(this);
        } else if (it.elseBranch() != null) {
            it.elseBranch().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Statement.PrintStatement it) {
        var content = evaluate(it.expression());
        System.out.println(stringify(content));
        return null;
    }

    @Override
    public Void visit(Statement.ReturnStatement it) {
        var value = (Object) null;
        if (it.value() != null) {
            value = evaluate(it.value());
        }
        throw new Callable.Return(value);
    }

    @Override
    public Void visit(Statement.VarStatement it) {
        Object value = null;
        if (it.initializer() != null) {
            value = evaluate(it.initializer());
        }
        environment.define(it.name(), value);
        return null;
    }

    @Override
    public Void visit(Statement.WhileStatement it) {
        while (isTruthy(evaluate(it.condition()))) {
            it.body().accept(this);
        }
        return null;
    }

    void executeBlock(List<Statement> statements, Environment environment) {
        var previous = this.environment;
        try {
            this.environment = environment;
            for (var s : statements)
                s.accept(this);
        } finally {
            this.environment = previous;
        }
    }

    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    private String stringify(Object value) {
        if (value == null) return "nil";
        if (value instanceof Double) {
            var text = value.toString();
            if (text.endsWith(".0"))
                text = text.substring(0, text.length() - 2);
            return text;
        }
        return value.toString();
    }

    private double number(Token operator, Object operand) {
        if (operand instanceof Double it) return it;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        return switch (object) {
            case Boolean it -> it;
            case Double it -> it != 0.0;
            case String _ -> true;
            default -> false;
        };
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }
}
