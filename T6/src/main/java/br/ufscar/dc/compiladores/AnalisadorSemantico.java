package main.java.br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.VideoLangBaseVisitor;
import br.ufscar.dc.compiladores.VideoLangParser;

import java.util.*;

public class AnalisadorSemantico extends VideoLangBaseVisitor<Void> {

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

    private final Map<String, String> tabelaSimbolos = new HashMap<>(); // id -> tipo
    private final List<ErroSemantico> erros = new ArrayList<>();

    // Flags para garantir declarações únicas
    private boolean duracaoCenaDefinida = false;
    private boolean audioDefinido = false;
    private boolean renderizacaoDefinida = false;

    public List<ErroSemantico> getErros() {
        return erros;
    }

    @Override
    public Void visitDeclaracao(VideoLangParser.DeclaracaoContext ctx) {
        String tipo = ctx.getChild(1).getText(); // 'imagem' ou 'audio'
        String id = ctx.ID().getText();

        if (tabelaSimbolos.containsKey(id)) {
            erros.add(new ErroSemantico(ctx.getStart().getLine(),
                    "Identificador '" + id + "' já declarado anteriormente."));
        } else {
            tabelaSimbolos.put(id, tipo);
        }

        return null;
    }

    @Override
    public Void visitUsarImagem(VideoLangParser.UsarImagemContext ctx) {
        String id = ctx.ID().getText();

        if (!tabelaSimbolos.containsKey(id)) {
            erros.add(new ErroSemantico(ctx.getStart().getLine(), "Imagem '" + id + "' não foi declarada."));
        } else if (!tabelaSimbolos.get(id).equals("imagem")) {
            erros.add(new ErroSemantico(ctx.getStart().getLine(), "Identificador '" + id + "' não é do tipo imagem."));
        }
        return super.visitUsarImagem(ctx);
    }

    @Override
    public Void visitAdicionarAudio(VideoLangParser.AdicionarAudioContext ctx) {
        if (audioDefinido) {
            erros.add(new ErroSemantico(ctx.getStart().getLine(), "A trilha sonora já foi adicionada."));
        }
        audioDefinido = true;
        
        String id = ctx.ID().getText();

        if (!tabelaSimbolos.containsKey(id)) {
            erros.add(new ErroSemantico(ctx.getStart().getLine(), "Áudio '" + id + "' não foi declarado."));
        } else if (!tabelaSimbolos.get(id).equals("audio")) {
            erros.add(new ErroSemantico(ctx.getStart().getLine(), "Identificador '" + id + "' não é do tipo áudio."));
        }

        // Valida o volume dentro do bloco
        for (var attr : ctx.audioAtributo()) {
            if (attr.getText().startsWith("com")) {
                int volume = Integer.parseInt(attr.INT(0).getText());
                if (volume < 0 || volume > 100) {
                    erros.add(new ErroSemantico(ctx.getStart().getLine(), "Volume deve estar entre 0 e 100."));
                }
            }
        }
        return null;
    }

    @Override
    public Void visitRenderizar(VideoLangParser.RenderizarContext ctx) {
        if (renderizacaoDefinida) {
            erros.add(new ErroSemantico(ctx.getStart().getLine(), "A configuração de renderização já foi definida."));
        }
        renderizacaoDefinida = true;
        return null;
    }
}
