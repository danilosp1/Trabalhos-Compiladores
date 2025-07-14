package br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.VideoLangParser;
import br.ufscar.dc.compiladores.VideoLangBaseVisitor;
import java.util.*;

public class GeradorDeCodigo extends VideoLangBaseVisitor<Void> {

    private final StringBuilder imports = new StringBuilder();
    private final StringBuilder paths = new StringBuilder();
    private final StringBuilder textClips = new StringBuilder();
    private final StringBuilder imageClips = new StringBuilder();
    private final StringBuilder audioProcessing = new StringBuilder();
    private final StringBuilder composition = new StringBuilder();

    private final List<String> nomesTextosClipes = new ArrayList<>();
    private final List<String> nomesImagensClipes = new ArrayList<>();

    private int resolucaoW = 1080;
    private int resolucaoH = 1920;
    private int fps = 24;
    private String arquivoSaida = "video_final.mp4";

    // --- INÍCIO DAS MUDANÇAS ---
    
    // Mapeamento para tradução de cores e posições
    private static final Map<String, String> CORES = new HashMap<>();
    private static final Map<String, String> POSICOES = new HashMap<>();
    
    static {
        CORES.put("branco", "white");
        CORES.put("preto", "black");
        CORES.put("vermelho", "red");
        CORES.put("verde", "green");
        CORES.put("azul", "blue");

        POSICOES.put("centro", "center");
        POSICOES.put("esquerda", "left");
        POSICOES.put("direita", "right");
        POSICOES.put("topo", "top");
        POSICOES.put("baixo", "bottom");
    }

    public String getCodigoGerado() {
        // CORRIGIDO: Import explícito dos módulos
        imports.append("from moviepy import ImageClip, AudioFileClip, CompositeVideoClip, TextClip, vfx");

        composition.append("video = CompositeVideoClip([")
                   .append(String.join(", ", nomesImagensClipes))
                   .append("], size=(").append(resolucaoW).append(", ").append(resolucaoH).append("))\n");
        
        if (!audioProcessing.toString().isEmpty()) {
            composition.append(audioProcessing);
            composition.append("video = video.with_audio(audio)\n");
        }
        
        if (!nomesTextosClipes.isEmpty()) {
            composition.append("\nfinal_video = CompositeVideoClip([video, ")
                       .append(String.join(", ", nomesTextosClipes))
                       .append("], size=(").append(resolucaoW).append(", ").append(resolucaoH).append("))\n");
        } else {
            composition.append("final_video = video\n");
        }
        
        composition.append(String.format("\nfinal_video.write_videofile(\"%s\", fps=%d)\n", arquivoSaida, fps));

        return imports.toString() + "\n\n" +
               paths.toString() + "\n" +
               textClips.toString() +
               imageClips.toString() + "\n" +
               composition.toString();
    }
    
    @Override
    public Void visitProgram(VideoLangParser.ProgramContext ctx) {
        // O import agora é gerado no final, no método getCodigoGerado
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitDeclaracao(VideoLangParser.DeclaracaoContext ctx) {
        String id = ctx.ID().getText();
        String caminho = unquote(ctx.STRING().getText());
        paths.append(String.format("%s_path = \"%s\"\n", id, caminho));
        return null;
    }

    @Override
    public Void visitCriarTexto(VideoLangParser.CriarTextoContext ctx) {
        String nomeVar = "txt_clip" + nomesTextosClipes.size();
        nomesTextosClipes.add(nomeVar);
        
        String conteudo = unquote(ctx.STRING().getText());
        String fonte = "Arial";
        int tamanho = 72;
        String corInput = "branco";
        String posInput = "centro";
        int inicio = 0, duracao = 3;

        for (var attr : ctx.textoAtributo()) {
            String key = attr.getChild(0).getText();
            if (key.equals("fonte")) fonte = unquote(attr.STRING().getText());
            else if (key.equals("tamanho_fonte")) tamanho = Integer.parseInt(attr.INT(0).getText());
            else if (key.equals("cor")) corInput = unquote(attr.STRING().getText());
            else if (key.equals("posicao")) posInput = unquote(attr.STRING().getText());
            else if (key.equals("inicio")) {
                inicio = Integer.parseInt(attr.INT(0).getText());
                duracao = Integer.parseInt(attr.INT(1).getText());
            }
        }

        // CORRIGIDO: Tradução de cor e posição
        String corFinal = CORES.getOrDefault(corInput.toLowerCase(), corInput);
        String posFinal = POSICOES.getOrDefault(posInput.toLowerCase(), posInput);

        textClips.append(String.format("%s = TextClip(\n", nomeVar));
        textClips.append(String.format("    text=\"%s\",\n", conteudo));
        textClips.append(String.format("    font=\"%s\",\n", fonte));
        textClips.append(String.format("    font_size=%d,\n", tamanho));
        textClips.append(String.format("    color='%s'\n", corFinal));
        textClips.append(String.format(").with_start(%d).with_duration(%d).with_position('%s')\n\n", inicio, duracao, posFinal));
        return null;
    }

    @Override
    public Void visitUsarImagem(VideoLangParser.UsarImagemContext ctx) {
        String recursoId = ctx.ID().getText();
        String nomeVar = "clip" + nomesImagensClipes.size();
        nomesImagensClipes.add(nomeVar);
        
        // Variáveis para armazenar os atributos e garantir a ordem de geração
        String posInput = "centro";
        int inicio = 0, duracao = 3;
        List<String> efeitos = new ArrayList<>();

        for (var attr : ctx.usarAtributo()) {
            String key = attr.getChild(0).getText();
            if (key.equals("posicao")) {
                posInput = unquote(attr.STRING().getText());
            } else if (key.equals("inicio")) {
                inicio = Integer.parseInt(attr.INT(0).getText());
                duracao = Integer.parseInt(attr.INT(1).getText());
            } else if (key.equals("efeito")) {
                String nomeEfeito = unquote(attr.efeito().STRING().getText());
                int tempoEfeito = Integer.parseInt(attr.efeito().INT().getText());
                efeitos.add(String.format("vfx.%s(%d)", nomeEfeito, tempoEfeito));
            }
        }
        
        // CORRIGIDO: Tradução da posição
        String posFinal = POSICOES.getOrDefault(posInput.toLowerCase(), posInput);
        
        // CORRIGIDO: Geração do código na ordem correta
        imageClips.append(String.format("%s = ImageClip(%s_path)\n", nomeVar, recursoId));
        imageClips.append(String.format("%s = %s.with_position('%s')\n", nomeVar, nomeVar, posFinal));
        imageClips.append(String.format("%s = %s.resized(width=%d, height=%d)\n", nomeVar, nomeVar, resolucaoW, resolucaoH));
        imageClips.append(String.format("%s = %s.with_start(%d).with_duration(%d)\n", nomeVar, nomeVar, inicio, duracao));
        
        if (!efeitos.isEmpty()) {
            imageClips.append(String.format("%s = %s.with_effects([%s])\n", nomeVar, nomeVar, String.join(", ", efeitos)));
        }
        imageClips.append("\n");

        return null;
    }

    @Override
    public Void visitAdicionarAudio(VideoLangParser.AdicionarAudioContext ctx) {
        String recursoId = ctx.ID().getText();
        int inicio = 0, duracao = -1, volume = 100;

        for (var attr : ctx.audioAtributo()) {
            if (attr.getText().startsWith("inicio")) {
                inicio = Integer.parseInt(attr.INT(0).getText());
                duracao = Integer.parseInt(attr.INT(1).getText());
            } else if (attr.getText().startsWith("com")) {
                volume = Integer.parseInt(attr.INT(0).getText());
            }
        }
        
        audioProcessing.append(String.format("\naudio = AudioFileClip(%s_path)\n", recursoId));
        if (duracao != -1) {
            // CORRIGIDO: subclip -> subclipped
            audioProcessing.append(String.format("audio = audio.subclipped(%d, %d)\n", inicio, duracao));
        }
        if (volume != 100) {
            // CORRIGIDO: volumex -> with_volume_scaled
            audioProcessing.append(String.format("audio = audio.with_volume_scaled(%.2f)\n", volume / 100.0));
        }
        return null;
    }

    @Override
    public Void visitRenderizar(VideoLangParser.RenderizarContext ctx) {
        this.arquivoSaida = unquote(ctx.STRING().getText());
        this.fps = Integer.parseInt(ctx.INT(0).getText());
        this.resolucaoW = Integer.parseInt(ctx.INT(1).getText());
        this.resolucaoH = Integer.parseInt(ctx.INT(2).getText());
        return null;
    }

    private String unquote(String text) {
        if (text == null || text.length() < 2) return text;
        return text.substring(1, text.length() - 1);
    }
}