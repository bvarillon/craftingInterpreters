package fr.bvarillon.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fr.bvarillon.lox.TokenType.*;

/**
 * Parser
 */
public class Parser {

    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()){
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration(){
        try{
            if (match(FUN)) return function("function");
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }

    }

    private Stmt function(String kind){
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if(!check(RIGHT_PAREN)){
            do {
                if(parameters.size()>255) error(peek(), "Can't have more then 255 parameters.");
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while(match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters list.");
        consume(LEFT_BRACE, "Exect '{' before " + kind + " body.");
        Stmt.Block body = (Stmt.Block)block();
        return new Stmt.Function(name, parameters, body.statements);
    }

    private Stmt varDeclaration(){
        Token name = consume(IDENTIFIER,"Expect variable name.");

        Expr initializer = null;
        if(match(EQUAl)){
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(PRINT)) return printStatement();
        if (match(LEFT_BRACE)) return block();
        if (match(IF)) return ifStatement();
        if (match(WHILE)) return whileStatement();
        if (match(FOR)) return forStatement();
        if (match(RETURN)) return returnStatement();

        return expressionStatement();
    }


    private Stmt returnStatement(){
        Token keyword = previous();
        Expr value = null;
        if(!check(SEMICOLON)){
            value = expression();
        }
        consume(SEMICOLON, "Expect ';' after return statement.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt whileStatement(){
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr cond = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(cond, body);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");
        Stmt initializer;
        if (match(SEMICOLON)){
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if(!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after condition.");

        Expr increment = null;

        if(!check(RIGHT_PAREN)){
            increment = expression();
        }

        consume(RIGHT_PAREN, "Expect ')' after 'for' clauses.");

        Stmt body = statement();

        if (increment != null) 
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));

        if (condition == null)
            condition = new Expr.Literal(true);

        Stmt loop = new Stmt.While(condition, body);
        
        if (initializer != null) {
            loop  = new Stmt.Block(Arrays.asList(initializer, loop));
        }

        return loop;
    }

    private Stmt ifStatement(){
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr cond = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt thenStmt = statement();
        Stmt elseStmt = null;

        if(match(ELSE)) {
            elseStmt = statement(); 
        }

        return new Stmt.If(cond, thenStmt, elseStmt);
    }

    private Stmt block() {
        List<Stmt> statements = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' at end of block.");
        return new Stmt.Block(statements);
    }

    private Stmt printStatement(){
        Expr expression = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Print(expression);
    }

    private Stmt expressionStatement() {
        Expr expression = expression();
        consume(SEMICOLON, "Expect ';' after expression");
        return new Stmt.Expression(expression);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if(match(EQUAl)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Var) {
                Token name = ((Expr.Var)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while(match(OR)){
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while(match(AND)){
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER,GREATER_EQUAL,LOWER,LOWER_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while(match(MINUS, PLUS)) {
            Token operator  = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while(match(STAR, SLASH)) {
            Token operator  = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(BANG,MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        } else {
            return call();
        }
    }

    private Expr call() {
        Expr expr = primary();
        
        while (true){
            if(match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr calle){
        List<Expr> arguments = new ArrayList<>();
        if(!check(RIGHT_PAREN)){
            do {
                if (arguments.size()>255) error(peek(), "Can't have more than 255 arguments.");
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
        return new Expr.Call(calle, paren, arguments);
    }

    private Expr primary(){
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(NUMBER,STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(IDENTIFIER)) { return new Expr.Var(previous());}
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression");
    }

    private boolean match(TokenType...types) {
        for (TokenType type: types) {
            if(check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current-1);
    }

    private Token consume(TokenType type, String message) {
        if(check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token.line, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while(!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch(peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }
}
