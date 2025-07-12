package fr.bvarillon.lox;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
    R visit(Binary EXPR);
    R visit(Grouping EXPR);
    R visit(Literal EXPR);
    R visit(Unary EXPR);
    }

// Class Binary
    static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Expr left;
        final Token operator;
        final Expr right;
    }

// Class Grouping
    static class Grouping extends Expr {
    Grouping(Expr exppression) {
        this.exppression = exppression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Expr exppression;
    }

// Class Literal
    static class Literal extends Expr {
    Literal(Object value) {
        this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Object value;
    }

// Class Unary
    static class Unary extends Expr {
    Unary(Token operator, Expr right) {
        this.operator = operator;
        this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Token operator;
        final Expr right;
    }


    abstract <R> R accept(Visitor<R> visitor);

}
