package fr.bvarillon.lox;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
    R visit(Assign EXPR);
    R visit(Binary EXPR);
    R visit(Call EXPR);
    R visit(Grouping EXPR);
    R visit(Literal EXPR);
    R visit(Logical EXPR);
    R visit(Unary EXPR);
    R visit(Var EXPR);
    R visit(Ternary EXPR);
    }

// Class Assign
    static class Assign extends Expr {
    Assign(Token name, Expr value) {
        this.name = name;
        this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Token name;
        final Expr value;
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

// Class Call
    static class Call extends Expr {
    Call(Expr callee, Token paren, List<Expr> arguments) {
        this.callee = callee;
        this.paren = paren;
        this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Expr callee;
        final Token paren;
        final List<Expr> arguments;
    }

// Class Grouping
    static class Grouping extends Expr {
    Grouping(Expr expression) {
        this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Expr expression;
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

// Class Logical
    static class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
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

// Class Var
    static class Var extends Expr {
    Var(Token name) {
        this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Token name;
    }

// Class Ternary
    static class Ternary extends Expr {
    Ternary(Expr condition, Expr left, Expr right) {
        this.condition = condition;
        this.left = left;
        this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Expr condition;
        final Expr left;
        final Expr right;
    }


    abstract <R> R accept(Visitor<R> visitor);

}
