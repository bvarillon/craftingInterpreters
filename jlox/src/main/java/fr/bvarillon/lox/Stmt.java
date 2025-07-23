package fr.bvarillon.lox;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
    R visit(Block STMT);
    R visit(Expression STMT);
    R visit(Function STMT);
    R visit(If STMT);
    R visit(While STMT);
    R visit(Print STMT);
    R visit(Return STMT);
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

// Class Function
    static class Function extends Stmt {
    Function(Token name, List<Token> params, List<Stmt> body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Token name;
        final List<Token> params;
        final List<Stmt> body;
    }

// Class If
    static class If extends Stmt {
    If(Expr condition, Stmt thenStmt, Stmt elseStmt) {
        this.condition = condition;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Expr condition;
        final Stmt thenStmt;
        final Stmt elseStmt;
    }

// Class While
    static class While extends Stmt {
    While(Expr condition, Stmt body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Expr condition;
        final Stmt body;
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

// Class Return
    static class Return extends Stmt {
    Return(Token keyword, Expr value) {
        this.keyword = keyword;
        this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

        final Token keyword;
        final Expr value;
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
