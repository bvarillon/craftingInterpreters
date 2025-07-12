package fr.bvarillon.lox;

/**
 * RuntimeError
 */
public class RuntimeError extends RuntimeException {
    final Token token;

    RuntimeError(Token token, String msg) {
        super(msg);
        this.token = token;
    }
    
}
