package parser;

public sealed interface Statement {

    <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {
        R visit(ExpressionStatement it);
        R visit(PrintStatement it);
    }

    record ExpressionStatement(Expression expression) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    record PrintStatement(Expression expression) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }
}
