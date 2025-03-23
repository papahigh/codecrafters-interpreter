package parser;

import scanner.Token;

import java.util.List;

public sealed interface Expression {

    <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {
        R visit(TernaryExpression it);
        R visit(AssignExpression it);
        R visit(BinaryExpression it);
        R visit(CallExpression it);
        R visit(FunctionExpression it);
        R visit(GroupingExpression it);
        R visit(LogicalExpression it);
        R visit(LiteralExpression it);
        R visit(UnaryExpression it);
        R visit(VariableExpression it);
    }

    record TernaryExpression(Expression condition, Expression thenBranch, Expression elseBranch) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record AssignExpression(Token name, Expression value) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record BinaryExpression(Expression left, Token operator, Expression right) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record CallExpression(Expression callee, Token paren, List<Expression> arguments) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record FunctionExpression(Token name, List<Token> parameters, List<Statement> body) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record GroupingExpression(Expression expression) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record LogicalExpression(Expression left, Token operator, Expression right) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record LiteralExpression(Object value) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record UnaryExpression(Token operator, Expression right) implements Expression {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    
        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    record VariableExpression(Token name) implements Expression {
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
