lexer grammar NossaLexer;

ALGORITMO: 'algoritmo';

DECLARE: 'declare';

LITERAL: 'literal';

ESCREVA: 'escreva';

INTEIRO: 'inteiro';

LEIA: 'leia';

FIM_ALGORITMO: 'fim_algoritmo';

COLON: ':';

LPAREN: '(';

RPAREN: ')';

COMMA: ',';

IDENT: [a-zA-Z_][a-zA-Z_0-9]*;

fragment
ESC_SEQ:
    '\\\'';

CADEIA: '"' (ESC_SEQ | ~('\n'|'\''|'\\'))* '"' | '\'' (ESC_SEQ | ~('\n'|'\''|'\\'))* '\'';

COMENTARIO: '{' .*? '}' -> skip;

WS: [ \t\r\n]+ -> skip;