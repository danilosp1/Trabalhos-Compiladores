grammar VideoLang;


// REGRAS SINTATICAS


program: (declaracao | cena | renderizar)+;

declaracao: 'carregar' ('imagem' | 'audio') ID 'de' STRING;

cena: 'cena' '{' cenaCorpo+ '}';

cenaCorpo
    : criarTexto
    | usarImagem
    | adicionarAudio
    ;

criarTexto: 'criar' 'texto' STRING '{' textoAtributo+ '}';
textoAtributo
    : 'fonte' ':' STRING
    | 'tamanho_fonte' ':' INT
    | 'cor' ':' STRING
    | 'posicao' ':' '(' POSICAO ',' POSICAO ')'
    | 'inicio' 'em' INT 's' 'por' INT 's'
    ;

usarImagem: 'usar' 'imagem' ID '{' usarAtributo+ '}';
usarAtributo
    : 'posicao' ':' '(' POSICAO ',' POSICAO ')'
    | 'inicio' 'em' INT 's' 'por' INT 's'
    | 'efeito' ':' efeitoLista
    ;

efeitoLista: efeito (',' efeito)*;
efeito: STRING INT 's';

adicionarAudio: 'adicionar' 'audio' ID 'com' 'volume' INT '%';

renderizar: 'renderizar' 'para' STRING 'com' 'resolucao' '(' INT ',' INT ')';


// REGRAS LEXICAS


ID: [a-zA-Z_][a-zA-Z0-9_]*;
STRING: '\'' (~['\r\n])* '\'';
POSICAO: 'centro' | 'esquerda' | 'direita' | 'topo' | 'baixo';
INT: [0-9]+;
WS: [ \t\r\n]+ -> skip;
