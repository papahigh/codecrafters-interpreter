package parser;

import scanner.Token;

import java.util.List;

public sealed interface Statement {

    <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {
        R visit(BlockStatement it);
        R visit(ExpressionStatement it);
        R visit(FunctionStatement it);
        R visit(IfStatement it);
        R visit(PrintStatement it);
        R visit(ReturnStatement it);
        R visit(VarStatement it);
        R visit(WhileStatement it);
    }

    record BlockStatement(List<Statement> statements) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record ExpressionStatement(Expression expression) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record FunctionStatement(Token name, List<Token> parameters, List<Statement> body) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record IfStatement(Expression condition, Statement thenBranch, Statement elseBranch) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record PrintStatement(Expression expression) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record ReturnStatement(Token keyword, Expression value) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record VarStatement(Token name, Expression initializer) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record WhileStatement(Expression condition, Statement body) implements Statement {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }
}
