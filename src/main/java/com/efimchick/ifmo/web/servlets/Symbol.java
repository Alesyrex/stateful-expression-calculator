package com.efimchick.ifmo.web.servlets;

public enum Symbol {
    OPEN_BRACKET('('),
    CLOSE_BRACKET(')'),
    PLUS('+'),
    MINUS('-'),
    MULTIPLE('*'),
    DIVIDE('/'),
    LETTER_A('a'),
    LETTER_Z('z'),
    NUMBER_0('0'),
    NUMBER_9('9');

    private final char symbol;

    Symbol(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }
}
