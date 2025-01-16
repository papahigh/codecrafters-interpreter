package parser;

import scanner.Token;

public sealed interface Expression {

    <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {
        R visit(TernaryExpression it);
        R visit(AssignExpression it);
        R visit(BinaryExpression it);
        R visit(GroupingExpression it);
        R visit(LiteralExpression it);
        R visit(UnaryExpression it);
        R visit(VariableExpression it);
    }

    record TernaryExpression(Expression condition, Expression thenBranch, Expression elseBranch) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    record AssignExpression(Token name, Expression value) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    record BinaryExpression(Expression left, Token operator, Expression right) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    record GroupingExpression(Expression expression) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    record LiteralExpression(Object value) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    record UnaryExpression(Token operator, Expression right) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    record VariableExpression(Token name) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }
}
