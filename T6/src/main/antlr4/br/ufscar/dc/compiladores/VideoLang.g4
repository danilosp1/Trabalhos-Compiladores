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

duracaoCena: 'duracao' ':' INT 's';

criarTexto: 'criar' 'texto' STRING '{' textoAtributo+ '}';
textoAtributo
    : 'fonte' ':' STRING
    | 'tamanho_fonte' ':' INT
    | 'cor' ':' STRING
    | 'posicao' ':' STRING
    | 'inicio' 'em' INT 's' 'por' INT 's'
    ;

usarImagem: 'usar' 'imagem' ID '{' usarAtributo+ '}';
usarAtributo
    : 'posicao' ':' STRING
    | 'inicio' 'em' INT 's' 'por' INT 's'
    | 'efeito' ':' efeito
    ;

efeito: STRING 'por' INT 's'; 

adicionarAudio: 'adicionar' 'audio' ID '{' audioAtributo+ '}';
audioAtributo
    : 'inicio' 'em' INT 's' 'por' INT 's'
    | 'com' 'volume' INT '%'
    ;

renderizar: 'renderizar' 'para' STRING 'com' INT 'fps' 'e' 'resolucao' INT 'x' INT;

// REGRAS LEXICAS

ID: [a-zA-Z_][a-zA-Z0-9_]*;

STRING
    : '\'' ( ~['\\] | '\\' . )*? '\''
    | '"' ( ~["\\] | '\\' . )*? '"'
    ;

INT: [0-9]+;
LINE_COMMENT: '//' ~[\r\n]* -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;
WS: [ \t\r\n]+ -> skip;