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
        generateExpressions();
    }

    private static void generateExpressions() throws IOException {
        var outputLocation = "src/main/java/parser/Expression.java";
        var expressionTypes = List.of(
                entry("BinaryExpression", "Expression left, Token operator, Expression right"),
                entry("GroupingExpression", "Expression expression"),
                entry("LiteralExpression", "Object value"),
                entry("UnaryExpression", "Token operator, Expression right")
        );

        try (var writer = Files.newBufferedWriter(Path.of(outputLocation))) {
            // language=java
            var code = """
                    package parser;
                    
                    import scanner.Token;
                    
                    public sealed interface Expression {
                    
                        <R> R accept(Visitor<R> visitor);
                    
                    // $VISITOR
                    // $EXPRESSIONS
                    }
                    """
                    .replace("// $VISITOR", renderVisitor(expressionTypes))
                    .replace("// $EXPRESSIONS", renderTypes(expressionTypes));
            writer.write(code);
        }
    }

    private static String renderVisitor(List<Map.Entry<String, String>> expressionTypes) {
        var methods = expressionTypes.stream()
                .map(it -> "R visit($ it);".replace("$", it.getKey()))
                .collect(Collectors.joining("\n"));
        // language=java
        return """
                interface Visitor<R> {
                // $METHODS
                }
                """
                .replace("// $METHODS", methods.indent(4).stripTrailing())
                .indent(4);
    }

    private static String renderTypes(List<Map.Entry<String, String>> expressionTypes) {
        return expressionTypes.stream()
                .map(it ->
                        """
                                record $name($components) implements Expression {
                                    @Override
                                    public <R> R accept(Visitor<R> visitor) {
                                        return visitor.visit(this);
                                    }
                                }
                                """
                                .replace("$name", it.getKey())
                                .replace("$components", it.getValue())
                                .indent(4)
                                .stripTrailing()
                ).collect(Collectors.joining("\n\n"));
    }
}
