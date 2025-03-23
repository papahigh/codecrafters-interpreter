import doctor.Doctor;
import parser.ASTPrinter;
import parser.Parser;
import runtime.Resolver;
import runtime.Runtime;
import scanner.Scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: ./your_program.sh tokenize <filename>");
            System.exit(1);
        }

        String command = args[0];
        String fileName = args[1];

        switch (command) {
            case "tokenize" -> tokenize(content(fileName));
            case "parse" -> parse(content(fileName));
            case "evaluate" -> evaluate(content(fileName));
            case "run" -> run(content(fileName));
            default -> {
                System.err.println("Unknown command: " + command);
                System.exit(1);
            }
        }
    }

    private static void tokenize(String content) {
        var doctor = Doctor.console();
        var scanner = new Scanner(content, doctor);

        for (var token : scanner.scanTokens()) {
            System.out.println(token);
        }

        doctor.diagnostics();
    }

    private static void parse(String content) {
        var doctor = Doctor.console();

        var scanner = new Scanner(content, doctor);
        var parser = new Parser(scanner.scanTokens(), doctor);

        System.out.println(new ASTPrinter().print(parser.parseExpression()));

        doctor.diagnostics();
    }

    private static void evaluate(String content) {
        var doctor = Doctor.console();

        var scanner = new Scanner(content, doctor);
        var runtime = new Runtime(doctor);
        var resolver = new Resolver(runtime, doctor);

        var parser = new Parser(scanner.scanTokens(), doctor);
        var expression = parser.parseExpression();

        resolver.resolve(expression);
        runtime.run(expression);

        doctor.diagnostics();
    }

    private static void run(String content) {
        var doctor = Doctor.console();

        var scanner = new Scanner(content, doctor);
        var runtime = new Runtime(doctor);
        var resolver = new Resolver(runtime, doctor);

        var parser = new Parser(scanner.scanTokens(), doctor);
        var statements = parser.parseStatements();

        resolver.resolve(statements);
        runtime.run(statements);

        doctor.diagnostics();
    }

    private static String content(String fileName) {
        try {
            return Files.readString(Path.of(fileName));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
            return null;
        }
    }
}
