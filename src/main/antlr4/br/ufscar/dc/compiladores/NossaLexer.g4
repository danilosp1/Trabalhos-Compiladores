lexer grammar NossaLexer;

// ——— Palavras-chave e símbolos ———
ALGORITMO       : 'algoritmo';
DECLARE         : 'declare';
LITERAL         : 'literal';
ESCREVA         : 'escreva';
LEIA            : 'leia';
FIM_ALGORITMO   : 'fim_algoritmo';

// Comandos basicos
SE              : 'se';
ENTAO           : 'entao';
SENAO           : 'senao';
FIM_SE          : 'fim_se';
CASO            : 'caso';
SEJA            : 'seja';
FIM_CASO        : 'fim_caso';
ENTRE           : '..';
PARA            : 'para';
ATE             : 'ate';
FACA            : 'faca';
FIM_PARA        : 'fim_para';
ENQUANTO        : 'enquanto';
FIM_ENQUANTO    : 'fim_enquanto';
REGISTRO        : 'registro';
FIM_REGISTRO    : 'fim_registro';
TIPO            : 'tipo';
PROCEDIMENTO    : 'procedimento';
FIM_PROCEDIMENTO: 'fim_procedimento';
FUNCAO          : 'funcao';
RETORNE         : 'retorne';
FIM_FUNCAO      : 'fim_funcao';

// Tipos primitivos
INTEIRO         : 'inteiro';
REAL            : 'real';
LOGICO          : 'logico';
VAR             : 'var';
CONSTANTE       : 'constante';

FALSO           : 'falso';
VERDADEIRO      : 'verdadeiro';

ATRIBUICAO      : '<-';

PONTEIRO        : '^';
ENDERECO        : '&';

PONTO           : '.';

// Símbolos e Operadores
COLON           : ':';
LPAREN          : '(';
RPAREN          : ')';
COMMA           : ',';
LCOLCHETE       : '[';
RCOLCHETE       : ']';

MAIS            : '+';
MENOS           : '-';
MULT            : '*';
DIVISAO         : '/';
MOD             : '%';


// Operadores relacionais
IGUAL           : '=';
DIFERENTE       : '<>';
MENOR           : '<';
MENOR_IGUAL     : '<=';
MAIOR           : '>';
MAIOR_IGUAL     : '>=';

// Operadores lógicos
E_LOGICO        : 'e';
OU_LOGICO       : 'ou';
NAO_LOGICO      : 'nao';


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


// ——— NÚMEROS ———
NUM_REAL : [0-9]+ '.' [0-9]+;
NUM_INT  : [0-9]+;


// ——— WS ———
WS: [ \t\r\n]+ -> skip;


// ——— ERRO — captura qualquer outro caractere inválido ———
ERRO: .;
