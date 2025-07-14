grammar VideoLang;

// REGRAS SINTATICAS
// (Nenhuma mudança aqui, permanece igual)
program: (declaracao | cena | renderizar)+;
declaracao: CARREGAR (IMAGEM | AUDIO) ID DE STRING;
cena: CENA '{' cenaCorpo+ '}';
cenaCorpo
    : criarTexto
    | usarImagem
    | adicionarAudio
    | duracaoCena
    ;
duracaoCena: DURACAO ':' INT 's';
criarTexto: CRIAR TEXTO STRING '{' textoAtributo+ '}';
textoAtributo
    : FONTE ':' STRING
    | TAMANHO_FONTE ':' INT
    | COR ':' STRING
    | POSICAO ':' STRING
    | INICIO EM INT 's' POR INT 's'
    ;
usarImagem: USAR IMAGEM ID '{' usarAtributo+ '}';
usarAtributo
    : POSICAO ':' STRING
    | INICIO EM INT 's' POR INT 's'
    | EFEITO ':' efeito
    ;
efeito: STRING POR INT 's';
adicionarAudio: ADICIONAR AUDIO ID '{' audioAtributo+ '}';
audioAtributo
    : INICIO EM INT 's' POR INT 's'
    | COM VOLUME INT '%'
    ;
renderizar: RENDERIZAR PARA STRING COM INT FPS E RESOLUCAO INT 'x' INT;


// REGRAS LEXICAS

// --- Palavras-Chave ---
// Definir explicitamente as keywords evita que elas sejam confundidas com IDs.
CARREGAR: 'carregar';
IMAGEM: 'imagem';
AUDIO: 'audio';
DE: 'de';
CENA: 'cena';
DURACAO: 'duracao';
CRIAR: 'criar';
TEXTO: 'texto';
FONTE: 'fonte';
TAMANHO_FONTE: 'tamanho_fonte';
COR: 'cor';
POSICAO: 'posicao';
INICIO: 'inicio';
EM: 'em';
POR: 'por';
USAR: 'usar';
EFEITO: 'efeito';
ADICIONAR: 'adicionar';
COM: 'com';
VOLUME: 'volume';
RENDERIZAR: 'renderizar';
PARA: 'para';
FPS: 'fps';
E: 'e';
RESOLUCAO: 'resolucao';

// --- Tokens de Dados e Símbolos ---
ID: [a-zA-Z_][a-zA-Z0-9_]*;

// REGRA UNIFICADA PARA STRING
STRING
    : '\'' ( ~['\\\r\n] | '\\' . )* ( '\'' | EOF )
    | '"' ( ~["\\\r\n] | '\\' . )* ( '"' | EOF )
    ;

INT: [0-9]+;

// --- Tokens Especiais e de Erro ---
// A regra de erro STRING_NAO_FECHADA é removida daqui.
// A lógica será tratada no Main.java.

LINE_COMMENT: '//' ~[\r\n]* -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;
WS: [ \t\r\n]+ -> skip;

// Mantemos esta para capturar caracteres como '~', '`', etc.
ERRO_CARACTERE_INVALIDO: . ;