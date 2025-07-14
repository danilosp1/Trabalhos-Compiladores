package br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.VideoLangBaseVisitor;
import br.ufscar.dc.compiladores.VideoLangParser;

import java.io.File; // <-- IMPORTAÇÃO NECESSÁRIA
import java.util.*;

public class AnalisadorSemantico extends VideoLangBaseVisitor<Void> {

    public static class ErroSemantico {
        // ... (nenhuma mudança nesta classe interna)
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

    private final Map<String, String> tabelaSimbolos = new HashMap<>(); // id -> tipo
    private final List<ErroSemantico> erros = new ArrayList<>();

    // Flags para garantir declarações únicas
    private boolean duracaoCenaDefinida = false;
    private boolean audioDefinido = false;
    private boolean renderizacaoDefinida = false;

    public List<ErroSemantico> getErros() {
        return erros;
    }

    // Função auxiliar para remover aspas de uma string
    private String unquote(String text) {
        if (text == null || text.length() < 2) return text;
        return text.substring(1, text.length() - 1);
    }

    @Override
    public Void visitDeclaracao(VideoLangParser.DeclaracaoContext ctx) {
        String tipo = ctx.getChild(1).getText();
        String id = ctx.ID().getText();
        String caminhoString = ctx.STRING().getText();
        int linha = ctx.getStart().getLine();

        // 1. Verifica se o identificador já foi declarado
        if (tabelaSimbolos.containsKey(id)) {
            erros.add(new ErroSemantico(linha, "Identificador '" + id + "' já declarado anteriormente."));
        } else {
            tabelaSimbolos.put(id, tipo);
        }

        // --- INÍCIO DA NOVA LÓGICA ---
        // 2. Verifica se o arquivo no caminho especificado existe
        String caminhoArquivo = unquote(caminhoString);
        File arquivo = new File(caminhoArquivo);

        if (!arquivo.exists()) {
            erros.add(new ErroSemantico(linha, "Arquivo '" + caminhoArquivo + "' não encontrado."));
        }
        // --- FIM DA NOVA LÓGICA ---

        return null;
    }

    // O restante da classe permanece o mesmo...

    @Override
    public Void visitUsarImagem(VideoLangParser.UsarImagemContext ctx) {
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