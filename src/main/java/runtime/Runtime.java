package runtime;

import doctor.Doctor;
import doctor.RuntimeError;
import parser.Expression;
import scanner.Token;


public class Runtime implements Expression.Visitor<Object> {

    private final Doctor doctor;

    public Runtime(Doctor doctor) {
        this.doctor = doctor;
    }

    public void run(Expression expression) {
        try {
            var value = evaluate(expression);
            System.out.println(stringify(value));
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
                } else if (left instanceof String || right instanceof String) {
                    yield left.toString() + right.toString();
                } else {
                    throw new RuntimeError(it.operator(), "Invalid operand types for '+'");
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
    public Object visit(Expression.GroupingExpression it) {
        return evaluate(it.expression());
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
            case String it -> !it.isEmpty();
            default -> false;
        };
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }
}
