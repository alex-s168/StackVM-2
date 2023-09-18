package me.alex_s168.stackvm2.ir.token

enum class TokenType {

    IDENTIFIER,
    PARENTHESIS_OPEN,
    PARENTHESIS_CLOSE,
    SQUARE_BRACKET_OPEN,
    SQUARE_BRACKET_CLOSE,
    CURLY_BRACKET_OPEN,
    CURLY_BRACKET_CLOSE,
    ANGLE_BRACKET_OPEN,
    ANGLE_BRACKET_CLOSE,
    APOSTROPHE,
    COMMA,
    COLON,
    ASSIGNMENT,
    END_OF_STATEMENT,
    END_OF_LINE,
    STAR,
    AND,
    MODULO

}