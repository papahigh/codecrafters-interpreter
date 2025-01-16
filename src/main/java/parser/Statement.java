package parser;

import scanner.Token;

public sealed interface Statement {

    <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {
        R visit(ExpressionStatement it);
        R visit(PrintStatement it);
        R visit(VarStatement it);
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

    record VarStatement(Token name, Expression initializer) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }
}
