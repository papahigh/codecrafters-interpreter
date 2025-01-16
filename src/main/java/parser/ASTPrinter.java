package parser;


import static java.util.Optional.ofNullable;

public class ASTPrinter implements Expression.Visitor<String>, Statement.Visitor<String> {

    public String print(Statement statement) {
        return ofNullable(statement).map(it -> it.accept(this)).orElse("");
    }

    public String print(Expression expression) {
        return ofNullable(expression).map(it -> it.accept(this)).orElse("");
    }

    @Override
    public String visit(Expression.TernaryExpression it) {
        return parenthesize("ternary", it.condition(), it.thenBranch(), it.elseBranch());
    }

    @Override
    public String visit(Expression.BinaryExpression it) {
        return parenthesize(it.operator().lexeme(), it.left(), it.right());
    }

    @Override
    public String visit(Expression.GroupingExpression it) {
        return parenthesize("group", it.expression());
    }

    @Override
    public String visit(Expression.LiteralExpression it) {
        if (it.value() == null) return "nil";
        return it.value().toString();
    }

    @Override
    public String visit(Expression.UnaryExpression it) {
        return parenthesize(it.operator().lexeme(), it.right());
    }

    @Override
    public String visit(Expression.VariableExpression it) {
        return parenthesize(it.name().lexeme());
    }

    @Override
    public String visit(Statement.ExpressionStatement it) {
        return it.expression().accept(this);
    }

    @Override
    public String visit(Statement.PrintStatement it) {
        return parenthesize("print", it.expression());
    }

    @Override
    public String visit(Statement.VarStatement it) {
        return parenthesize(it.name().lexeme(), it.initializer());
    }

    private String parenthesize(String name, Expression... expression) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for (var child : expression) {
            builder.append(" ");
            builder.append(child.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }
}
