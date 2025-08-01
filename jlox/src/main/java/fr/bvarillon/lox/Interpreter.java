package fr.bvarillon.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interpreter
 */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr,Integer> locals = new HashMap<>();


    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity(){return 0;}

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments){
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString(){return "<native fun>";}
        });
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

    public void resolve(Expr expr, int depth){
        locals.put(expr, depth);
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
                return (double)left / (double)right;
            case PLUS:
                if (left instanceof String && right instanceof String) {
                return (String)left + (String)right;
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
        }
        return null;
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
    public Object visit(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR){
            if(isThruthy(left)) return isThruthy(left);
        } else {
            if(!isThruthy(left)) return isThruthy(left);
        }

        return isThruthy(evaluate(expr.right));
    }

    @Override
    public Object visit(Expr.Var expr){
        return lookupVariable(expr.name, expr);
    }

    @Override
    public Object visit(Expr.Assign expr){
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null){
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Object visit(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        
        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)){
            throw new RuntimeError(expr.paren, "Can only call functions and classes");
        }
        LoxCallable function = (LoxCallable) callee;
        if (arguments.size() != function.arity()){
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but found " + arguments.size() + ".");
        }
        return function.call(this, arguments);
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

    @Override
    public Void visit(Stmt.If stmt){
        if(isThruthy(evaluate(stmt.condition)))
            execute(stmt.thenStmt);
        else if (stmt.elseStmt != null)
            execute(stmt.elseStmt);
        return null;
    }

    @Override
    public Void visit(Stmt.While stmt) {
        while(isThruthy(evaluate(stmt.condition)))
            execute(stmt.body);
        return null;
    }

    @Override
    public Void visit(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt,environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visit(Stmt.Return stmt) {
        Object value = null;
        if(stmt.value != null){
            value = evaluate(stmt.value);
        }

        throw new Return(value);
    }

    public void execute_block(List<Stmt> statements, Environment env){
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

    private Object lookupVariable(Token name, Expr expr){
        Integer distance =  locals.get(expr);
        if (distance != null){
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }
}
