package br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.VideoLangBaseVisitor;
import br.ufscar.dc.compiladores.VideoLangParser;

import java.io.File;
import java.util.*;

public class AnalisadorSemantico extends VideoLangBaseVisitor<Void> {

    // --- INÍCIO DAS NOVAS ADIÇÕES ---
    // Listas de extensões de arquivo válidas
    private static final List<String> EXTENSOES_IMAGEM = Arrays.asList("jpg", "jpeg", "png", "bmp", "gif");
    private static final List<String> EXTENSOES_AUDIO = Arrays.asList("mp3", "wav", "ogg", "aac", "flac");
    // --- FIM DAS NOVAS ADIÇÕES ---

    public static class ErroSemantico {
        // ... (nenhuma mudança)
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
    
    // --- INÍCIO DAS NOVAS ADIÇÕES ---
    // Função auxiliar para obter a extensão de um arquivo
    private Optional<String> getFileExtension(String filename) {
        return Optional.ofNullable(filename)
          .filter(f -> f.contains("."))
          .map(f -> f.substring(filename.lastIndexOf(".") + 1).toLowerCase());
    }
    // --- FIM DAS NOVAS ADIÇÕES ---


    @Override
    public Void visitDeclaracao(VideoLangParser.DeclaracaoContext ctx) {
        String tipoDeclarado = ctx.getChild(1).getText(); // "imagem" ou "audio"
        String id = ctx.ID().getText();
        String caminhoString = ctx.STRING().getText();
        int linha = ctx.getStart().getLine();

        // 1. Verifica se o identificador já foi declarado
        if (tabelaSimbolos.containsKey(id)) {
            erros.add(new ErroSemantico(linha, "Identificador '" + id + "' já declarado anteriormente."));
            // Retorna aqui para evitar erros em cascata
            return null;
        } else {
            tabelaSimbolos.put(id, tipoDeclarado);
        }

        String caminhoArquivo = unquote(caminhoString);
        File arquivo = new File(caminhoArquivo);

        // 2. Verifica se o arquivo existe. Se não, não podemos verificar a extensão.
        if (!arquivo.exists()) {
            erros.add(new ErroSemantico(linha, "Arquivo '" + caminhoArquivo + "' não encontrado."));
        } else {
            // --- INÍCIO DA NOVA LÓGICA DE VERIFICAÇÃO DE TIPO ---
            // 3. Se o arquivo existe, verifica se a extensão é compatível com o tipo declarado
            Optional<String> extensaoOpt = getFileExtension(caminhoArquivo);
            
            if (extensaoOpt.isPresent()) {
                String extensao = extensaoOpt.get();
                if (tipoDeclarado.equals("imagem") && !EXTENSOES_IMAGEM.contains(extensao)) {
                    erros.add(new ErroSemantico(linha, "Arquivo '" + caminhoArquivo + "' não é um tipo de imagem válido."));
                } else if (tipoDeclarado.equals("audio") && !EXTENSOES_AUDIO.contains(extensao)) {
                    erros.add(new ErroSemantico(linha, "Arquivo '" + caminhoArquivo + "' não é um tipo de áudio válido."));
                }
            } else {
                // Arquivo sem extensão, também pode ser considerado um erro
                erros.add(new ErroSemantico(linha, "Arquivo '" + caminhoArquivo + "' não possui uma extensão de arquivo."));
            }
            // --- FIM DA NOVA LÓGICA DE VERIFICAÇÃO DE TIPO ---
        }

        return null;
    }

    // O restante do código permanece exatamente o mesmo
    @Override
    public Void visitUsarImagem(VideoLangParser.UsarImagemContext ctx) { //...
        String id = ctx.ID().getText();
        int linha = ctx.getStart().getLine();

        if (!tabelaSimbolos.containsKey(id)) {
            erros.add(new ErroSemantico(linha, "Imagem '" + id + "' não foi declarada."));
        } else if (!tabelaSimbolos.get(id).equals("imagem")) {
            erros.add(new ErroSemantico(linha, "Identificador '" + id + "' não é do tipo imagem."));
        }
        return super.visitUsarImagem(ctx);
    }
    
    @Override
    public Void visitAdicionarAudio(VideoLangParser.AdicionarAudioContext ctx) { //...
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
    public Void visitRenderizar(VideoLangParser.RenderizarContext ctx) { //...
        int linha = ctx.getStart().getLine();
        if (renderizacaoDefinida) {
            erros.add(new ErroSemantico(linha, "A configuração de renderização já foi definida."));
        }
        renderizacaoDefinida = true;
        return null;
    }
}