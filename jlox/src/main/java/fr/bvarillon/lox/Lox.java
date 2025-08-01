package fr.bvarillon.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static fr.bvarillon.lox.TokenType.*;

/**
 * Hello world!
 */
public class Lox {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;


    private static Interpreter interpreter = new Interpreter();

    public static void main(String[] args)  throws IOException {
        if (args.length > 1) {
            System.out.println("Usage jlox [scripts]");
            System.exit(64);
        } else if (args.length == 1){
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }

    }

    private static void run(String source){
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens(); 

        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        if (hadError) return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        if (hadError) return;

        // System.out.println(new AstPrinter().print(expr));
        interpreter.interpret(stmts);
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    public static void error(Token token, String message) {
        if (token.type == EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
    public static void runtimeError(RuntimeError error){
        System.out.println("RuntimeError:\n│ [line " + error.token.line + "]: " + error.token.lexeme+ "\n└ " + error.getMessage());
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where +": " + message);
        hadError = true;
    }
}
