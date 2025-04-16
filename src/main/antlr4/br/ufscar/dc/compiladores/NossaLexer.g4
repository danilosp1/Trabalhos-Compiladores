lexer grammar NossaLexer;

PALAVRA_CHAVE:
    'DECLARACOES' | 'ALGORITMO' | 'INT' | 'REAL' | 'ATRIBUIR' | 'A' | 'LER' | 'IMPRIMIR' | 'SE' | 'ENTAO' | 'ENQUANTO' | 'INICIO' | 'FIM' | 'E' | 'OU';

fragment
DIGITO: '0'..'9';

NUMINT:
    ('+'|'-')? DIGITO+;

NUMREAL:
    ('+'|'-')? DIGITO+ '.' DIGITO+;

VARIAVEL:
    [a-zA-Z][a-zA-Z0-0]*;

CADEIA:
    '\'' (ESC_SEQ | ~('\n'|'\''|'\\'))* '\'';

fragment
ESC_SEQ:
    '\\\'';

COMENTARIO:
    '%' ~('\n'|'\r')* '\r'? '\n' { skip(); };

WS: (' '|'\t'|'\r'|'\n') { skip(); };

OP_REL	:	'>' | '>=' | '<' | '<=' | '<>' | '='
	;
OP_ARIT	:	'+' | '-' | '*' | '/'
	;
DELIM	:	':'
	;
ABREPAR :	'('
	;
FECHAPAR:	')'
	;

CADEIA_NAO_FECHADA: '\'' (ESC_SEQ | ~('\n'|'\''|'\\'))* '\n';
ERRO: .;