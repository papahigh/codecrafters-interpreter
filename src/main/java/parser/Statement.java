package parser;

import scanner.Token;

import java.util.List;

public sealed interface Statement {

    <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {
        R visit(BlockStatement it);
        R visit(ExpressionStatement it);
        R visit(IfStatement it);
        R visit(PrintStatement it);
        R visit(VarStatement it);
        R visit(WhileStatement it);
    }

    record BlockStatement(List<Statement> statements) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    record ExpressionStatement(Expression expression) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    record IfStatement(Expression condition, Statement thenBranch, Statement elseBranch) implements Statement {
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

    record WhileStatement(Expression condition, Statement body) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }
}
