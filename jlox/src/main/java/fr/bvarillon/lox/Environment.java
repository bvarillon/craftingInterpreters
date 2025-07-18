package fr.bvarillon.lox;

import static fr.bvarillon.lox.TokenType.values;

import java.util.HashMap;
import java.util.Map;

/**
 * Environment
 */
public class Environment {
    private final Map<String,Object> values = new HashMap<>(); 

    void define(String name, Object value) {
        values.put(name, value);
    }

    Object get(Token name) {
        if(values.containsKey(name.lexeme)){
            return values.get(name.lexeme);
        }

        throw new RuntimeError(name, "Undefine variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value){
        if(values.containsKey(name.lexeme)){
            values.put(name.lexeme, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
