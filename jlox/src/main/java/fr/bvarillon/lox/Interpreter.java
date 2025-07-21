package fr.bvarillon.lox;

import java.util.List;

/**
 * Interpreter
 */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    public void interpret_prompt(List<Stmt> stmts) {
        try {
            for(Stmt stmt : stmts){
                if(stmt instanceof Stmt.Expression){
                    System.out.println(evaluate(((Stmt.Expression)stmt).expression));
                } else {
                    execute(stmt);
                }
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    public void interpret(List<Stmt> stmts) {
        try {
            for(Stmt stmt : stmts){
                execute(stmt);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    public Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    public void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Object visit(Expr.Literal expr){
        return expr.value;
    }

    @Override
    public Object visit(Expr.Binary expr){
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);


        switch(expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, left, right);
                return (double)left - (double)right;
            case STAR:
                checkNumberOperand(expr.operator, left, right);
                return (double)left * (double)right;
            case SLASH:
                checkNumberOperand(expr.operator, left, right);
                if((double)right == (double)0) throw new RuntimeError(expr.operator, "Right operand must be different from 0.");
                return (double)left / (double)right;
            case PLUS:
                if (left instanceof String || right instanceof String) {
                return stringify(left) + stringify(right);
                }
                if (left instanceof Double && right instanceof Double) {
                return (double)left + (double)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case GREATER:
                checkNumberOperand(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double)left >= (double)right;
            case LOWER:
                checkNumberOperand(expr.operator, left, right);
                return (double)left < (double)right;
            case LOWER_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL:
                return !isEqual(left,right);
            case EQUAL_EQUAL:
                return isEqual(left,right);
            case COMMA:
                evaluate(expr.left);
                return evaluate(expr.right);
        }
        return null;
    }

    @Override
    public Object visit(Expr.Ternary expr){
        return isThruthy(evaluate(expr.condition)) ? evaluate(expr.left) : evaluate(expr.right);
    }

    @Override
    public Object visit(Expr.Grouping expr){
        return evaluate(expr.expression);
    }

    @Override
    public Object visit(Expr.Unary expr){
        Object right = evaluate(expr.right);

        switch(expr.operator.type){
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case BANG:
                return !isThruthy(right);
        }
        return null;
    }

    @Override
    public Object visit(Expr.Var expr){
        return environment.get(expr.name);
    }

    @Override
    public Object visit(Expr.Assign expr){
        Object value = evaluate(expr.value);
        environment.assign(expr.name,value);
        return value;
    }

    @Override
    public Void visit(Stmt.Print stmt){
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visit(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }
     @Override 
    public Void visit(Stmt.Var stmt) {
        Object value = null;
        
        if(stmt.initializer != null){
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme,value);
        return null;
    }

    @Override
    public Void visit(Stmt.Block block){
        execute_block(block.statements, new Environment(environment));
        return null;
    }

    private void execute_block(List<Stmt> statements, Environment env){
        Environment previous = environment;
        try {
            this.environment = env;
            for (Stmt stmt : statements){
                execute(stmt);
            }
        } finally {
            this.environment = previous;
        }
    }

    private boolean isThruthy(Object obj) {
        if(obj == null) return false;
        if(obj instanceof Boolean) return (boolean)obj;
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null) return false;
        return left.equals(right);
    }
    private String stringify(Object value) {
        if(value == null) return "nil";

        if(value instanceof Double) {
            String text = value.toString();
            if(text.endsWith(".0")){
                text = text.substring(0, text.length()-2);
            }
            return text;
        }

        return value.toString();
    }

    private void checkNumberOperand(Token token, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(token, "Operand must be a number.");
    }
    private void checkNumberOperand(Token token, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(token, "Operands must be a numbers.");
    }
}
