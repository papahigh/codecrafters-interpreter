package parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.entry;

class Generate {

    public static void main(String[] args) throws IOException {
        generateStatements();
        generateExpressions();
    }

    private static void generateStatements() throws IOException {
        var statementTypes = List.of(
                entry("BlockStatement", "List<Statement> statements"),
                entry("ExpressionStatement", "Expression expression"),
                entry("FunctionStatement", "Token name, List<Token> parameters, List<Statement> body"),
                entry("IfStatement", "Expression condition, Statement thenBranch, Statement elseBranch"),
                entry("PrintStatement", "Expression expression"),
                entry("ReturnStatement", "Token keyword, Expression value"),
                entry("VarStatement", "Token name, Expression initializer"),
                entry("WhileStatement", "Expression condition, Statement body")
        );
        generateAST("Statement", statementTypes);
    }

    private static void generateExpressions() throws IOException {
        var expressionTypes = List.of(
                entry("TernaryExpression", "Expression condition, Expression thenBranch, Expression elseBranch"),
                entry("AssignExpression", "Token name, Expression value"),
                entry("BinaryExpression", "Expression left, Token operator, Expression right"),
                entry("CallExpression", "Expression callee, Token paren, List<Expression> arguments"),
                entry("FunctionExpression", "Token name, List<Token> parameters, List<Statement> body"),
                entry("GroupingExpression", "Expression expression"),
                entry("LogicalExpression", "Expression left, Token operator, Expression right"),
                entry("LiteralExpression", "Object value"),
                entry("UnaryExpression", "Token operator, Expression right"),
                entry("VariableExpression", "Token name")
        );

        generateAST("Expression", expressionTypes);
    }

    private static void generateAST(String baseInterface, List<Map.Entry<String, String>> astTypes) throws IOException {
        var outputLocation = "src/main/java/parser/%s.java".formatted(baseInterface);

        try (var writer = Files.newBufferedWriter(Path.of(outputLocation))) {
            // language=java
            var code = """
                    package parser;
                    
                    import scanner.Token;
                    
                    import java.util.List;
                    
                    public sealed interface BASE {
                    
                        <R> R accept(Visitor<R> visitor);
                    
                    // $VISITOR
                    // $AST_TYPES
                    }
                    """
                    // language=none
                    .replace("BASE", baseInterface)
                    .replace("// $VISITOR", renderVisitor(astTypes))
                    .replace("// $AST_TYPES", renderTypes(baseInterface, astTypes));
            writer.write(code);
        }
    }

    private static String renderVisitor(List<Map.Entry<String, String>> astTypes) {
        var methods = astTypes.stream()
                .map(it -> "R visit(T it);".replace("T", it.getKey()))
                .collect(Collectors.joining("\n"));
        return """
                interface Visitor<R> {
                // $METHODS
                }
                """
                .replace("// $METHODS", methods.indent(4).stripTrailing())
                .indent(4);
    }

    private static String renderTypes(String baseInterface, List<Map.Entry<String, String>> astTypes) {
        return astTypes.stream()
                .map(it ->
                        """
                                record $name($components) implements $base {
                                    @Override
                                    public <R> R accept(Visitor<R> visitor) {
                                        return visitor.visit(this);
                                    }
                                }
                                """
                                .replace("$base", baseInterface)
                                .replace("$name", it.getKey())
                                .replace("$components", it.getValue())
                                .indent(4)
                                .stripTrailing()
                ).collect(Collectors.joining("\n\n"));
    }
}
