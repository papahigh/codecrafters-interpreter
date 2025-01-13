package parser;


import static java.util.Optional.ofNullable;

public class ASTPrinter implements Expression.Visitor<String> {

    public String print(Expression expression) {
        return ofNullable(expression).map(it -> it.accept(this)).orElse("");
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
