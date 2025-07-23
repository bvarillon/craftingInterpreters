package fr.bvarillon.lox;

import fr.bvarillon.lox.Expr.Binary;

/**
 * AstPrinter
 */
public class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visit(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visit(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visit(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visit(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visit(Expr.Var expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visit(Expr.Ternary expr) {
        return "(? " + print(expr.condition) + " : " + print(expr.left) + " " + print(expr.right) + ")";
    }

    @Override
    public String visit(Expr.Assign expr) {
        return parenthesize(expr.name.lexeme, expr.value);
    }

    @Override
    public String visit(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visit(Expr.Call expr) {
        return parenthesize("Call", expr.callee);
    }

    private String parenthesize(String name, Expr...exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for(Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args) {
        Expr expression = 
        new Expr.Binary(
            new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, 1), 
                new Expr.Literal(123)), 
            new Token(TokenType.STAR, "*", null, 1), 
            new Expr.Grouping(
              new Expr.Literal(456)  
            ));

        System.out.println(new AstPrinter().print(expression));
    }
    
}
