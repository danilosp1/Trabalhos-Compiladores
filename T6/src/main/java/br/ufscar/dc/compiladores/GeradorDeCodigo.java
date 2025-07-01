package main.java.br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.VideoLangParser;
import br.ufscar.dc.compiladores.VideoLangBaseVisitor;
import java.util.List;
import java.util.ArrayList;


import java.util.*;
import org.antlr.v4.runtime.tree.TerminalNode;

public class GeradorDeCodigo extends VideoLangBaseVisitor<Void> {

    private final StringBuilder codigo = new StringBuilder();
    private final Map<String, String> imagens = new HashMap<>();
    private final Map<String, String> audios = new HashMap<>();
    private final List<String> clipes = new ArrayList<>();
    private final List<String> textos = new ArrayList<>();
    private String trilhaSonora = null;
    private String resolucao = "size=(1080, 1080)";
    private String arquivoSaida = "\"video_final.mp4\"";
    private int cenaDuracao = 0;

    public String getCodigoGerado() {
        return codigo.toString();
    }

    @Override
    public Void visitProgram(VideoLangParser.ProgramContext ctx) {
        codigo.append("from moviepy.editor import *\n")
              .append("from moviepy.video.fx import fadein, fadeout, blur, resize\n\n");

        visitChildren(ctx);

        // Compor clipes
        codigo.append("\n# Composição dos clipes\n");
        codigo.append("elementos = []\n");
        clipes.forEach(c -> codigo.append("elementos.append(").append(c).append(")\n"));
        textos.forEach(t -> codigo.append("elementos.append(").append(t).append(")\n"));

        codigo.append("video = CompositeVideoClip(elementos, ").append(resolucao).append(")\n");

        // Áudio
        if (trilhaSonora != null) {
            codigo.append("video = video.set_audio(").append(trilhaSonora).append(")\n");
        }

        // Exportar vídeo
        codigo.append("video.write_videofile(").append(arquivoSaida).append(", fps=24)\n");
        return null;
    }

    @Override
    public Void visitDeclaracao(VideoLangParser.DeclaracaoContext ctx) {
        String tipo = ctx.getChild(1).getText(); // imagem ou audio
        String id = ctx.ID().getText();
        String caminho = ctx.STRING().getText().replace("'", "\"");

        if (tipo.equals("imagem")) {
            imagens.put(id, caminho);
        } else {
            audios.put(id, caminho);
        }
        return null;
    }

    @Override
    public Void visitCena(VideoLangParser.CenaContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitCriarTexto(VideoLangParser.CriarTextoContext ctx) {
        String conteudo = ctx.STRING().getText().replace("'", "\"");
        String fonte = "\"Arial\"";
        int tamanho = 48;
        String cor = "\"white\"";
        String posicao = "\"center\"";
        int inicio = 0, duracao = 3;

        for (var atributo : ctx.textoAtributo()) {
            String nome = atributo.getStart().getText();
            if (nome.equals("fonte")) {
                fonte = atributo.STRING().getText().replace("'", "\"");
            } else if (nome.equals("tamanho_fonte")) {
                tamanho = Integer.parseInt(atributo.INT().getText());
            } else if (nome.equals("cor")) {
                cor = atributo.STRING().getText().replace("'", "\"");
            } else if (nome.equals("posicao")) {
                String x = atributo.POSICAO(0).getText();
                String y = atributo.POSICAO(1).getText();
                posicao = "(" + toPythonPos(x) + ", " + toPythonPos(y) + ")";
            } else if (nome.equals("inicio")) {
                inicio = Integer.parseInt(atributo.INT(0).getText());
                duracao = Integer.parseInt(atributo.INT(1).getText());
            }
        }

        String nomeVar = "texto_" + textos.size();
        textos.add(nomeVar);

        codigo.append(String.format(
            "%s = TextClip(text=%s, fontsize=%d, color=%s, font=%s)\n",
            nomeVar, conteudo, tamanho, cor, fonte));
        codigo.append(String.format("%s = %s.set_position(%s).set_start(%d).set_duration(%d)\n\n",
            nomeVar, nomeVar, posicao, inicio, duracao));

        return null;
    }

    @Override
    public Void visitUsarImagem(VideoLangParser.UsarImagemContext ctx) {
        String id = ctx.ID().getText();
        String caminho = imagens.getOrDefault(id, "\"arquivo_nao_encontrado.jpg\"");
        String nomeVar = "clip_" + clipes.size();
        clipes.add(nomeVar);

        String posicao = "\"center\"";
        int inicio = 0, duracao = 3;
        List<String> efeitos = new ArrayList<>();

        for (var attr : ctx.usarAtributo()) {
            if (attr.getText().startsWith("posicao")) {
                String x = attr.POSICAO(0).getText();
                String y = attr.POSICAO(1).getText();
                posicao = "(" + toPythonPos(x) + ", " + toPythonPos(y) + ")";
            } else if (attr.getText().startsWith("inicio")) {
                inicio = Integer.parseInt(attr.INT(0).getText());
                duracao = Integer.parseInt(attr.INT(1).getText());
            } else if (attr.getText().startsWith("efeito")) {
                for (var e : attr.efeitoLista().efeito()) {
                    String nome = e.STRING().getText().replace("'", "");
                    int tempo = Integer.parseInt(e.INT().getText());
                    efeitos.add(applyEffect(nome, tempo));
                }
            }
        }

        codigo.append(String.format("%s = ImageClip(%s).set_position(%s).set_start(%d).set_duration(%d)\n",
            nomeVar, caminho, posicao, inicio, duracao));

        for (String fx : efeitos) {
            codigo.append(String.format("%s = %s\n", nomeVar, fx.replace("{clip}", nomeVar)));
        }

        codigo.append("\n");
        return null;
    }

    @Override
    public Void visitAdicionarAudio(VideoLangParser.AdicionarAudioContext ctx) {
        String id = ctx.ID().getText();
        int volume = Integer.parseInt(ctx.INT().getText());
        String caminho = audios.getOrDefault(id, "\"trilha.mp3\"");
        String nome = "audio_" + id;

        codigo.append(String.format("%s = AudioFileClip(%s).volumex(%.2f)\n", nome, caminho, volume / 100.0));
        trilhaSonora = nome;
        return null;
    }

    @Override
    public Void visitRenderizar(VideoLangParser.RenderizarContext ctx) {
        arquivoSaida = ctx.STRING().getText().replace("'", "\"");
        int w = Integer.parseInt(ctx.INT(0).getText());
        int h = Integer.parseInt(ctx.INT(1).getText());
        resolucao = String.format("size=(%d, %d)", w, h);
        return null;
    }

    // ======= Utilitários =======

    private String toPythonPos(String pos) {
        return switch (pos) {
            case "centro" -> "'center'";
            case "esquerda" -> "'left'";
            case "direita" -> "'right'";
            case "topo" -> "'top'";
            case "baixo" -> "'bottom'";
            default -> "'center'";
        };
    }

    private String applyEffect(String nome, int tempo) {
        return switch (nome) {
            case "fade_in" -> "{clip}.fx(fadein, " + tempo + ")";
            case "fade_out" -> "{clip}.fx(fadeout, " + tempo + ")";
            case "blur_in" -> "{clip}.fx(blur.blur, " + tempo + ")";
            case "blur_out" -> "{clip}.fx(blur.blur, " + tempo + ")";
            case "zoom_in" -> "{clip}.resize(lambda t: 1 + 0.1 * t)";
            case "zoom_out" -> "{clip}.resize(lambda t: 1 - 0.1 * t)";
            default -> "{clip}  # efeito desconhecido";
        };
    }
}
