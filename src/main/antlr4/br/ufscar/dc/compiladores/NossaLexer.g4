lexer grammar NossaLexer;

// ——— Palavras-chave e símbolos ———
ALGORITMO       : 'algoritmo';
DECLARE         : 'declare';
LITERAL         : 'literal';
ESCREVA         : 'escreva';
INTEIRO         : 'inteiro';
REAL            : 'real';
LEIA            : 'leia';
FIM_ALGORITMO   : 'fim_algoritmo';

// ATRIBUICAO      : '<-';
COLON           : ':';
LPAREN          : '(';
RPAREN          : ')';
COMMA           : ',';

MAIS            : '+';
MENOS           : '-';
MULT            : '*';
DIVISAO         : '/';

// identificadores
IDENT           : [a-zA-Z_][a-zA-Z_0-9]*;


// ——— sequências de escape para strings ———
fragment ESC_SEQ: '\\' [btnfr"'\\];    // \b, \t, \n, \f, \r, \', \" e \\


// ——— CADEIA de caracteres (string) ———
// cadeia correta (fecha na mesma linha)
CADEIA
    : '"' ( ESC_SEQ | ~[\\"\r\n] )* '"'
    | '\'' ( ESC_SEQ | ~[\\'\r\n] )* '\''
    ;


// cadeia que não fecha na mesma linha
CADEIA_NAO_FECHADA
    : '"' (~[\\"\r\n])*            // abre " e não encontra " antes do EOL
    | '\'' (~[\\'\r\n])*           // abre ' e não encontra ' antes do EOL
    ;


// ——— COMENTÁRIO ———
// comentário correto (skip)
COMENTARIO: '{' ~[\r\n}]* '}' -> skip;


// comentário não fechado na mesma linha
COMENTARIO_NAO_FECHADO: '{' ~[\r\n}]*;               // abre { e não fecha antes do EOL


// ——— WS ———
WS: [ \t\r\n]+ -> skip;


// ——— ERRO — captura qualquer outro caractere inválido ———
ERRO: .;
