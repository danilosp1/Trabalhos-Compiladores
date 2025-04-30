grammar NossaGramatica;

// ==================== REGRAS LÉXICAS ====================

// --- Palavras-chave e símbolos ---
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

// Identificadores
IDENT           : [a-zA-Z_][a-zA-Z_0-9]*;

// --- sequências de escape para strings ---
fragment ESC_SEQ: '\\' [btnfr"'\\];

// --- CADEIA de caracteres (string) ---
CADEIA
    : '"' ( ESC_SEQ | ~[\\"\r\n] )* '"'
    | '\'' ( ESC_SEQ | ~[\\'\r\n] )* '\'';

CADEIA_NAO_FECHADA
    : '"' (~[\\"\r\n])*
    | '\'' (~[\\'\r\n])*;

// --- COMENTÁRIO ---
COMENTARIO: '{' ~[\r\n}]* '}' -> skip;
COMENTARIO_NAO_FECHADO: '{' ~[\r\n}]*;

// --- NÚMEROS ---
NUM_REAL : [0-9]+ '.' [0-9]+;
NUM_INT  : [0-9]+;

// --- WS ---
WS: [ \t\r\n]+ -> skip;

// --- ERRO ---
ERRO: .;

// ==================== REGRAS SINTÁTICAS ====================

// --- Estrutura principal do programa ---
programa : declaracoes ALGORITMO corpo FIM_ALGORITMO EOF;

// --- Blocos de declarações ---
declaracoes : decl_local_global*;

decl_local_global : declaracao_local | declaracao_global;

declaracao_local : DECLARE variavel
                 | CONSTANTE IDENT COLON tipo_basico IGUAL valor_constante
                 | TIPO IDENT COLON tipo;

// --- Declarações locais (variáveis, constantes, tipos) ---
variavel : identificador (COMMA identificador)* COLON tipo;

identificador : IDENT (PONTO IDENT)* dimensao;

dimensao : (LCOLCHETE exp_aritmetica RCOLCHETE)*;

// --- Tipos e estruturas de dados ---
tipo : registro | tipo_estendido;

tipo_basico : LITERAL | INTEIRO | REAL | LOGICO;

tipo_basico_ident : tipo_basico | IDENT;

tipo_estendido : PONTEIRO? tipo_basico_ident;

registro : REGISTRO variavel* FIM_REGISTRO;

// --- Expressões e valores ---
valor_constante : CADEIA | NUM_INT | NUM_REAL | VERDADEIRO | FALSO;

declaracao_global : PROCEDIMENTO IDENT LPAREN parametros? RPAREN declaracao_local* cmd* FIM_PROCEDIMENTO
                  | FUNCAO IDENT LPAREN parametros? RPAREN COLON tipo_estendido declaracao_local* cmd* FIM_FUNCAO;

parametro : VAR? identificador (COMMA identificador)* COLON tipo_estendido;

parametros : parametro (COMMA parametro)*;

corpo : declaracao_local* cmd*;

exp_relacional : exp_aritmetica (op_relacional exp_aritmetica)?;

exp_aritmetica : termo (op1 termo)*;

expressao : termo_logico (op_logico_1 termo_logico)*;

// --- Comandos da linguagem ---
cmd : cmdLeia | cmdEscreva | cmdSe | cmdCaso | cmdPara | cmdEnquanto | cmdFaca | cmdAtribuicao | cmdChamada | cmdRetorne;

cmdLeia : LEIA LPAREN PONTEIRO? identificador (COMMA PONTEIRO? identificador)* RPAREN;

cmdEscreva : ESCREVA LPAREN expressao (COMMA expressao)* RPAREN;

cmdSe : SE expressao ENTAO cmd* (SENAO cmd*)? FIM_SE;

cmdCaso : CASO exp_aritmetica SEJA selecao (SENAO cmd*)? FIM_CASO;

cmdPara : PARA IDENT ATRIBUICAO exp_aritmetica ATE exp_aritmetica FACA cmd* FIM_PARA;

cmdEnquanto : ENQUANTO expressao FACA cmd* FIM_ENQUANTO;

cmdFaca : FACA cmd* ATE expressao;

cmdAtribuicao : PONTEIRO? identificador ATRIBUICAO expressao;

cmdChamada : IDENT LPAREN expressao (COMMA expressao)* RPAREN;

cmdRetorne : RETORNE expressao;

selecao: item_selecao*;

item_selecao : constantes COLON cmd*;

constantes : numero_intervalo (COMMA numero_intervalo)*;

numero_intervalo : op_unario? NUM_INT (ENTRE op_unario? NUM_INT)?;

op_unario : MENOS;

// --- Componentes de expressões ---
termo : fator (op2 fator)*;

fator : parcela (op3 parcela)*;

termo_logico : fator_logico (op_logico_2 fator_logico)*;

fator_logico : NAO_LOGICO? parcela_logica;

parcela_logica : (VERDADEIRO | FALSO) | exp_relacional;

// --- Operadores ---
op1 : MAIS | MENOS;

op2 : MULT | DIVISAO;

op3 : MOD;

op_relacional : IGUAL | DIFERENTE | MAIOR_IGUAL | MENOR_IGUAL | MAIOR | MENOR;

op_logico_1 : OU_LOGICO;

op_logico_2 : E_LOGICO;

// --- Elementos básicos ---
parcela : op_unario? parcela_unario | parcela_nao_unario;

parcela_unario : PONTEIRO? identificador
               | IDENT LPAREN expressao (COMMA expressao)* RPAREN
               | NUM_INT
               | NUM_REAL
               | LPAREN expressao RPAREN;

parcela_nao_unario : ENDERECO identificador | CADEIA;