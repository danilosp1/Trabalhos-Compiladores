lexer grammar NossaLexer;

PALAVRA_CHAVE : 'se' | 'então' | 'senão';
NUMERO : [0-9]+;
IDENTIFICADOR : [a-zA-Z_][a-zA-Z0-9_]*;
ESPACO : [ \t\r\n]+ -> skip;
OPERADOR : '+' | '-' | '*' | '/';