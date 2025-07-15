package br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.VideoLangBaseVisitor;
import br.ufscar.dc.compiladores.VideoLangParser;

import java.io.File;
import java.util.*;

public class AnalisadorSemantico extends VideoLangBaseVisitor<Void> {

    // Listas de constantes para validação
    private static final List<String> EXTENSOES_IMAGEM = Arrays.asList("jpg", "jpeg", "png", "bmp", "gif");
    private static final List<String> EXTENSOES_AUDIO = Arrays.asList("mp3", "wav", "ogg", "aac", "flac");
    private static final List<String> CORES_VALIDAS = Arrays.asList(
        "branco", "verde", "amarelo", "azul", "vermelho", "marrom", "roxo", "preto"
    );
    private static final List<String> POSICOES_VALIDAS = Arrays.asList(
        "centro", "esquerda", "direita", "topo", "baixo"
    );
    private static final List<String> EFEITOS_VALIDOS = Arrays.asList(
        "CrossFadeIn", "CrossFadeOut"
    );

    public static class ErroSemantico {
        public final int linha;
        public final String mensagem;

        public ErroSemantico(int linha, String mensagem) {
            this.linha = linha;
            this.mensagem = mensagem;
        }

        @Override
        public String toString() {
            return "Linha " + linha + ": " + mensagem;
        }
    }

    private final Map<String, String> tabelaSimbolos = new HashMap<>();
    private final List<ErroSemantico> erros = new ArrayList<>();

    // Estrutura para rastrear os tempos de início das imagens e evitar sobreposições
    private final Set<Integer> temposDeInicioImagens = new HashSet<>();


    private boolean duracaoCenaDefinida = false;
    private boolean audioDefinido = false;
    private boolean renderizacaoDefinida = false;

    public List<ErroSemantico> getErros() {
        return erros;
    }

    private String unquote(String text) {
        if (text == null || text.length() < 2) return text;
        return text.substring(1, text.length() - 1);
    }
    
    private Optional<String> getFileExtension(String filename) {
        return Optional.ofNullable(filename)
          .filter(f -> f.contains("."))
          .map(f -> f.substring(filename.lastIndexOf(".") + 1).toLowerCase());
    }

    @Override
    public Void visitDeclaracao(VideoLangParser.DeclaracaoContext ctx) {
        String tipoDeclarado = ctx.getChild(1).getText();
        String id = ctx.ID().getText();
        String caminhoString = ctx.STRING().getText();
        int linha = ctx.getStart().getLine();

        if (tabelaSimbolos.containsKey(id)) {
            erros.add(new ErroSemantico(linha, "Identificador '" + id + "' já declarado anteriormente."));
            return null;
        } else {
            tabelaSimbolos.put(id, tipoDeclarado);
        }

        String caminhoArquivo = unquote(caminhoString);
        File arquivo = new File(caminhoArquivo);

        if (!arquivo.exists()) {
            erros.add(new ErroSemantico(linha, "Arquivo '" + caminhoArquivo + "' não encontrado."));
        } else {
            Optional<String> extensaoOpt = getFileExtension(caminhoArquivo);
            if (extensaoOpt.isPresent()) {
                String extensao = extensaoOpt.get();
                if (tipoDeclarado.equals("imagem") && !EXTENSOES_IMAGEM.contains(extensao)) {
                    erros.add(new ErroSemantico(linha, "Arquivo '" + caminhoArquivo + "' não é um tipo de imagem válido."));
                } else if (tipoDeclarado.equals("audio") && !EXTENSOES_AUDIO.contains(extensao)) {
                    erros.add(new ErroSemantico(linha, "Arquivo '" + caminhoArquivo + "' não é um tipo de áudio válido."));
                }
            } else {
                erros.add(new ErroSemantico(linha, "Arquivo '" + caminhoArquivo + "' não possui uma extensão de arquivo."));
            }
        }
        return null;
    }

    @Override
    public Void visitCriarTexto(VideoLangParser.CriarTextoContext ctx) {
        for (var atributo : ctx.textoAtributo()) {
            int linha = atributo.getStart().getLine();
            if (atributo.COR() != null) {
                String cor = unquote(atributo.STRING().getText());
                if (!CORES_VALIDAS.contains(cor.toLowerCase())) {
                    erros.add(new ErroSemantico(linha, "Cor '" + cor + "' não é uma cor válida."));
                }
            }
            else if (atributo.POSICAO() != null) {
                String posicao = unquote(atributo.STRING().getText());
                if (!POSICOES_VALIDAS.contains(posicao.toLowerCase())) {
                    erros.add(new ErroSemantico(linha, "Posição '" + posicao + "' não é uma posição válida."));
                }
            }
        }
        return super.visitCriarTexto(ctx);
    }

    @Override
    public Void visitUsarImagem(VideoLangParser.UsarImagemContext ctx) {
        String id = ctx.ID().getText();
        int linha = ctx.getStart().getLine();

        if (!tabelaSimbolos.containsKey(id)) {
            erros.add(new ErroSemantico(linha, "Imagem '" + id + "' não foi declarada."));
        } else if (!tabelaSimbolos.get(id).equals("imagem")) {
            erros.add(new ErroSemantico(linha, "Identificador '" + id + "' não é do tipo imagem."));
        }

        // Itera sobre todos os atributos para validar posição, efeito e tempo
        for (var atributo : ctx.usarAtributo()) {
            int linhaAtributo = atributo.getStart().getLine();
            
            if (atributo.POSICAO() != null) {
                String posicao = unquote(atributo.STRING().getText());
                if (!POSICOES_VALIDAS.contains(posicao.toLowerCase())) {
                    erros.add(new ErroSemantico(linhaAtributo, "Posição '" + posicao + "' não é uma posição válida."));
                }
            } 
            else if (atributo.EFEITO() != null) {
                String efeito = unquote(atributo.efeito().STRING().getText());
                if (!EFEITOS_VALIDOS.contains(efeito)) {
                    erros.add(new ErroSemantico(linhaAtributo, "Efeito '" + efeito + "' não é um efeito válido."));
                }
            }
            // Adiciona a verificação de tempo de início
            else if (atributo.INICIO() != null) {
                int tempoInicio = Integer.parseInt(atributo.INT(0).getText());
                
                // Verifica se o tempo de início já foi usado
                if (temposDeInicioImagens.contains(tempoInicio)) {
                    erros.add(new ErroSemantico(linhaAtributo, "Já existe uma imagem iniciando no tempo " + tempoInicio + "s."));
                } else {
                    // Se não foi usado, adiciona ao conjunto para futuras verificações
                    temposDeInicioImagens.add(tempoInicio);
                }
            }
        }

        return super.visitUsarImagem(ctx);
    }
    
    @Override
    public Void visitAdicionarAudio(VideoLangParser.AdicionarAudioContext ctx) { 
        int linha = ctx.getStart().getLine();
        if (audioDefinido) {
            erros.add(new ErroSemantico(linha, "A trilha sonora já foi adicionada."));
        }
        audioDefinido = true;
        
        String id = ctx.ID().getText();
        if (!tabelaSimbolos.containsKey(id)) {
            erros.add(new ErroSemantico(linha, "Áudio '" + id + "' não foi declarado."));
        } else if (!tabelaSimbolos.get(id).equals("audio")) {
            erros.add(new ErroSemantico(linha, "Identificador '" + id + "' não é do tipo áudio."));
        }

        for (var attr : ctx.audioAtributo()) {
            if (attr.getText().startsWith("com")) {
                int volume = Integer.parseInt(attr.INT(0).getText());
                if (volume < 0 || volume > 100) {
                    erros.add(new ErroSemantico(linha, "Volume deve estar entre 0 e 100."));
                }
            }
        }
        return null;
    }
    
    @Override
    public Void visitRenderizar(VideoLangParser.RenderizarContext ctx) { 
        int linha = ctx.getStart().getLine();
        if (renderizacaoDefinida) {
            erros.add(new ErroSemantico(linha, "A configuração de renderização já foi definida."));
        }
        renderizacaoDefinida = true;
        return null;
    }
}