package  fr.bvarillon.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * Resolver
 */
public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private Interpreter interpreter;
    private final Stack<Map<String,Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.None;

    private enum FunctionType {
        None,
        Function
    }

    Resolver(Interpreter interpreter){
        this.interpreter = interpreter;
    }

    public void resolve(List<Stmt> statements){
        for(Stmt stmt : statements){
            resolve(stmt);
        }
    }
    
    @Override
    public Void visit(Stmt.Block stmt){
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visit(Stmt.Var stmt){
        declare(stmt.name);
        if (stmt.initializer !=null){
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visit(Stmt.Expression stmt){
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visit(Stmt.If stmt){
        resolve(stmt.condition);
        resolve(stmt.thenStmt);
        if (stmt.elseStmt != null) resolve(stmt.elseStmt);
        return null;
    }

    @Override
    public Void visit(Stmt.Print stmt){
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visit(Stmt.Return stmt){
        if (currentFunction == FunctionType.None){
            Lox.error(stmt.keyword, "Can't return from top-level code.");
        }
        if(stmt.value != null)
            resolve(stmt.value);

        return null;
    }

    @Override
    public Void visit(Stmt.While stmt){
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visit(Expr.Binary expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visit(Expr.Call expr){
        resolve(expr.callee);

        for(Expr arg: expr.arguments){
            resolve(arg);
        }
        return null;
    }

    @Override
    public Void visit(Expr.Grouping expr){
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visit(Expr.Literal expr){
        return null;
    }

    @Override
    public Void visit(Expr.Logical expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visit(Expr.Unary expr){
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visit(Expr.Var expr){
        if(!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE){
            Lox.error(expr.name, "Can't read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visit(Expr.Assign expr){
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visit(Stmt.Function stmt){
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.Function);
        return null;
    }


    private void resolve(Stmt stmt){
        stmt.accept(this);
    }

    private void resolve(Expr expr){
        expr.accept(this);
    }

    private void resolveLocal(Expr expr, Token token){
        for (int i = scopes.size()-1; i >=0; i--){
            if (scopes.get(i).containsKey(token.lexeme)) {
                interpreter.resolve(expr, scopes.size()-1-i);
                return;
            }
        }
    }

    private void resolveFunction(Stmt.Function func, FunctionType type){
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param: func.params){
            declare(param);
            define(param);
        }
        resolve(func.body);
        endScope();
        currentFunction = enclosingFunction;
    }
    
    private void beginScope(){
        scopes.push(new HashMap<String,Boolean>());
    }

    private void endScope(){
        scopes.pop();
    }

    private void declare(Token name){
        if (scopes.isEmpty()) return;
        
        Map<String,Boolean> scope = scopes.peek();

        if(scope.containsKey(name.lexeme)){
            Lox.error(name, "Already a variable with this name in the scope.");
        }
        scope.put(name.lexeme, false);
    }

    private void define(Token name){
        if (scopes.isEmpty()) return;
        
        Map<String,Boolean> scope = scopes.peek();
        scope.put(name.lexeme, true);
    }
    
}

