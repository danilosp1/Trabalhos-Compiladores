grammar VideoLang;

// REGRAS SINTATICAS
program: (declaracao | cena | renderizar)+;

declaracao: 'carregar' ('imagem' | 'audio') ID 'de' STRING;

cena: 'cena' '{' cenaCorpo+ '}';

cenaCorpo
    : criarTexto
    | usarImagem
    | adicionarAudio
    | duracaoCena
    ;

// Regra para a duração total da cena/vídeo
duracaoCena: 'duracao' ':' INT 's';

criarTexto: 'criar' 'texto' STRING '{' textoAtributo+ '}';
textoAtributo
    : 'fonte' ':' STRING
    | 'tamanho_fonte' ':' INT
    | 'cor' ':' STRING
    | 'posicao' ':' '(' coord ',' coord ')'
    | 'inicio' 'em' INT 's' 'por' INT 's'
    ;

usarImagem: 'usar' 'imagem' ID '{' usarAtributo+ '}';
usarAtributo
    : 'posicao' ':' '(' coord ',' coord ')'
    | 'inicio' 'em' INT 's' 'por' INT 's'
    | 'efeito' ':' efeitoLista
    ;

// Regra para uma coordenada, que pode ser uma palavra-chave ou um valor customizado
coord: POSICAO | STRING;

efeitoLista: efeito (',' efeito)*;
efeito: STRING INT 's';

adicionarAudio: 'adicionar' 'audio' ID 'com' 'volume' INT '%';

renderizar: 'renderizar' 'para' STRING 'com' 'resolucao' '(' INT ',' INT ')';

// REGRAS LEXICAS

// REGRAS ESPECÍFICAS (PALAVRAS-CHAVE) VÊM PRIMEIRO
POSICAO: 'centro' | 'esquerda' | 'direita' | 'topo' | 'baixo';

// REGRAS GENÉRICAS VÊM DEPOIS
ID: [a-zA-Z_][a-zA-Z0-9_]*;

STRING
    : '\'' ( ~['\\] | '\\' . )*? '\''
    | '"' ( ~["\\] | '\\' . )*? '"'
    ;

INT: [0-9]+;
WS: [ \t\r\n]+ -> skip;