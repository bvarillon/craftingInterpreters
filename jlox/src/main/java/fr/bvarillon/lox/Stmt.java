package fr.bvarillon.lox;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
    R visit(Block STMT);
    R visit(Expression STMT);
    R visit(Print STMT);
    R visit(Var STMT);
    }

// Class Block
    static class Block extends Stmt {
    Block(List<Stmt> statements) {
        this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final List<Stmt> statements;
    }

// Class Expression
    static class Expression extends Stmt {
    Expression(Expr expression) {
        this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Expr expression;
    }

// Class Print
    static class Print extends Stmt {
    Print(Expr expression) {
        this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Expr expression;
    }

// Class Var
    static class Var extends Stmt {
    Var(Token name, Expr initializer) {
        this.name = name;
        this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Token name;
        final Expr initializer;
    }


    abstract <R> R accept(Visitor<R> visitor);

}
