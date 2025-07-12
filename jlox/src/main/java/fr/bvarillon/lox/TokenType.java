package fr.bvarillon.lox;

public enum TokenType {
    // Single character
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR, DOUBLE_DOT, QUESTION,

    // One or two charecters 
    BANG, BANG_EQUAL,
    EQUAl, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LOWER, LOWER_EQUAL, 


    // Literals
    STRING, NUMBER, IDENTIFIER,

    // Keywords
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF
}
