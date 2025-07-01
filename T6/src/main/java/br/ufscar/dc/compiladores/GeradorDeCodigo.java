package main.java.br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.VideoLangParser;
import br.ufscar.dc.compiladores.VideoLangBaseVisitor;
import java.util.*;

public class GeradorDeCodigo extends VideoLangBaseVisitor<Void> {

    private final StringBuilder imports = new StringBuilder();
    private final StringBuilder recursos = new StringBuilder();
    private final StringBuilder clipes = new StringBuilder();
    private final StringBuilder audio = new StringBuilder();
    private final StringBuilder composicao = new StringBuilder();

    private final List<String> nomesClipes = new ArrayList<>();
    private String nomeAudioFinal = null;
    private String resolucao = "size=(1080, 1080)";
    private String arquivoSaida = "\"video_final.mp4\"";
    private int duracaoTotal = -1;
    private int contadorClipes = 0;
    
    private static final Map<String, String> CORES = new HashMap<>();
    static {
        CORES.put("preto", "black");
        CORES.put("branco", "white");
        CORES.put("vermelho", "red");
        CORES.put("verde", "green");
        CORES.put("azul", "blue");
    }

    public String getCodigoGerado() {
        imports.append("from moviepy.editor import *\n")
               .append("from moviepy.video.fx.all import fadein, fadeout, blur\n\n");

        composicao.append("\n# === Cena completa ===\n");
        composicao.append("clips = [").append(String.join(", ", nomesClipes)).append("]\n");
        composicao.append("video = CompositeVideoClip(clips, ").append(resolucao).append(")");
        if (duracaoTotal > 0) {
            composicao.append(".set_duration(").append(duracaoTotal).append(")");
        }
        composicao.append("\n\n");

        composicao.append("# === Adiciona áudio ===\n");
        if (nomeAudioFinal != null) {
            composicao.append("video = video.set_audio(").append(nomeAudioFinal).append(")\n\n");
        }

        composicao.append("# === Exporta vídeo ===\n");
        composicao.append("video.write_videofile(").append(arquivoSaida).append(", fps=24)\n");

        return imports.toString() +
               "# === Recursos carregados ===\n" + recursos.toString() + "\n" +
               clipes.toString() +
               audio.toString() +
               composicao.toString();
    }

    @Override
    public Void visitDeclaracao(VideoLangParser.DeclaracaoContext ctx) {
        String tipo = ctx.getChild(1).getText();
        String id = ctx.ID().getText();
        String caminho = "\"" + unquote(ctx.STRING().getText()) + "\"";

        if (tipo.equals("imagem")) {
            recursos.append(String.format("%s = ImageClip(%s)\n", id, caminho));
        } else {
            recursos.append(String.format("%s = AudioFileClip(%s)\n", id, caminho));
        }
        return null;
    }

    @Override
    public Void visitDuracaoCena(VideoLangParser.DuracaoCenaContext ctx) {
        duracaoTotal = Integer.parseInt(ctx.INT().getText());
        return null;
    }

    @Override
    public Void visitCriarTexto(VideoLangParser.CriarTextoContext ctx) {
        String nomeVar = "texto" + contadorClipes++;
        nomesClipes.add(nomeVar);
        
        clipes.append(String.format("# === Texto %d ===\n", contadorClipes));

        String conteudo = "\"" + unquote(ctx.STRING().getText()) + "\"";
        String fonte = "Arial";
        int tamanho = 72;
        String cor = "white";
        String posicao = "('center', 'center')";
        int inicio = 0, duracao = 5;

        for (var atributo : ctx.textoAtributo()) {
            if (atributo.getStart().getText().equals("fonte")) {
                fonte = unquote(atributo.STRING().getText());
            } else if (atributo.getStart().getText().equals("tamanho_fonte")) {
                tamanho = Integer.parseInt(atributo.INT(0).getText());
            } else if (atributo.getStart().getText().equals("cor")) {
                String corInput = unquote(atributo.STRING().getText());
                cor = CORES.getOrDefault(corInput.toLowerCase(), corInput);
            } else if (atributo.getStart().getText().equals("posicao")) {
                posicao = String.format("(%s, %s)",
                    processarCoord(atributo.coord(0)),
                    processarCoord(atributo.coord(1))
                );
            } else if (atributo.getStart().getText().equals("inicio")) {
                inicio = Integer.parseInt(atributo.INT(0).getText());
                duracao = Integer.parseInt(atributo.INT(1).getText());
            }
        }

        clipes.append(String.format("%s = TextClip(\n", nomeVar));
        clipes.append(String.format("    text=%s,\n", conteudo));
        clipes.append(String.format("    fontsize=%d,\n", tamanho));
        clipes.append(String.format("    font=\"%s\",\n", fonte.replace(" ", "-")));
        clipes.append(String.format("    color='%s'\n", cor));
        clipes.append(String.format(").set_position(%s).set_duration(%d).set_start(%d)\n\n", posicao, duracao, inicio));

        return null;
    }

    @Override
    public Void visitUsarImagem(VideoLangParser.UsarImagemContext ctx) {
        String recursoId = ctx.ID().getText();
        String nomeVar = "img" + contadorClipes++;
        nomesClipes.add(nomeVar);

        clipes.append(String.format("# === Imagem %d (%s) ===\n", contadorClipes, recursoId));
        
        String posicao = "('center', 'center')";
        int inicio = 0, duracao = 5;

        for (var attr : ctx.usarAtributo()) {
            if (attr.getText().startsWith("posicao")) {
                 posicao = String.format("(%s, %s)",
                    processarCoord(attr.coord(0)),
                    processarCoord(attr.coord(1))
                );
            } else if (attr.getText().startsWith("inicio")) {
                inicio = Integer.parseInt(attr.INT(0).getText());
                duracao = Integer.parseInt(attr.INT(1).getText());
            }
        }
        
        clipes.append(String.format("%s = %s.set_position(%s).set_duration(%d).set_start(%d)\n",
            nomeVar, recursoId, posicao, duracao, inicio));

        for (var attr : ctx.usarAtributo()) {
            if (attr.getText().startsWith("efeito")) {
                for (var e : attr.efeitoLista().efeito()) {
                    String nomeEfeito = unquote(e.STRING().getText());
                    int tempoEfeito = Integer.parseInt(e.INT().getText());
                    applyEffect(clipes, nomeVar, nomeEfeito, tempoEfeito, duracao);
                }
            }
        }
        clipes.append("\n");
        return null;
    }

    @Override
    public Void visitAdicionarAudio(VideoLangParser.AdicionarAudioContext ctx) {
        String id = ctx.ID().getText();
        int volume = Integer.parseInt(ctx.INT().getText());
        
        audio.append("# === Processamento de Áudio ===\n");
        nomeAudioFinal = id + "_final";
        audio.append(String.format("%s = %s.volumex(%.2f)\n\n", nomeAudioFinal, id, volume / 100.0));
        return null;
    }
    
    @Override
    public Void visitRenderizar(VideoLangParser.RenderizarContext ctx) {
        arquivoSaida = "\"" + unquote(ctx.STRING().getText()) + "\"";
        int w = Integer.parseInt(ctx.INT(0).getText());
        int h = Integer.parseInt(ctx.INT(1).getText());
        resolucao = String.format("size=(%d, %d)", w, h);
        return null;
    }

    // ======= Utilitários =======

    private String unquote(String text) {
        if (text == null || text.length() < 2) {
            return text;
        }
        return text.substring(1, text.length() - 1);
    }

    private String processarCoord(VideoLangParser.CoordContext ctx) {
        if (ctx.POSICAO() != null) {
            return "'" + ctx.POSICAO().getText() + "'";
        }
        if (ctx.STRING() != null) {
            return "'" + unquote(ctx.STRING().getText()) + "'";
        }
        return "'center'";
    }

    private void applyEffect(StringBuilder builder, String clipVar, String nome, int tempo, int duracaoClip) {
        switch (nome) {
            case "fade_in":
                builder.append(String.format("%s = fadein(%s, %d)\n", clipVar, clipVar, tempo));
                break;
            case "fade_out":
                builder.append(String.format("%s = fadeout(%s, %d)\n", clipVar, clipVar, tempo));
                break;
            case "blur_in":
            case "blur_out":
                builder.append(String.format("%s = blur(%s, duration=%d)\n", clipVar, clipVar, tempo));
                break;
            case "zoom_in":
                builder.append(String.format("%s = %s.resize(lambda t: 1 + 0.2 * t / %d)\n", clipVar, clipVar, duracaoClip));
                break;
            case "zoom_out":
                builder.append(String.format("%s = %s.resize(lambda t: 1.2 - 0.2 * t / %d)\n", clipVar, clipVar, duracaoClip));
                break;
            default:
                builder.append(String.format("# Efeito '%s' desconhecido\n", nome));
                break;
        }
    }
}