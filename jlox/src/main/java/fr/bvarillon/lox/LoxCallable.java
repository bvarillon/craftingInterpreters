package fr.bvarillon.lox;

import java.util.List;
/**
 * LoxCollable
 */
interface LoxCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
