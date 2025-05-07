// ESSA CLASSE QUE VAI FAZER A COORDENACAO E O USO DOS ESCOPOS E FAZER AS VERIFICAÇÕES
package main.java.br.ufscar.dc.compiladores;

import java.util.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import br.ufscar.dc.compiladores.NossaGramaticaParser;
import br.ufscar.dc.compiladores.NossaGramaticaBaseVisitor;

public class AnalisadorSemantico extends NossaGramaticaBaseVisitor<Void> {

    private static class Simbolo {
        String nome;
        String tipo;
        boolean isConstante;

        Simbolo(String nome, String tipo, boolean isConstante) {
            this.nome = nome;
            this.tipo = tipo;
            this.isConstante = isConstante;
        }
    }

    private final Deque<Map<String, Simbolo>> escopos = new ArrayDeque<>();
    private final List<String> erros = new ArrayList<>();

    public List<String> getErros() {
        return erros;
    }

    private void entrarEscopo() {
        escopos.push(new HashMap<>());
    }

    private void sairEscopo() {
        escopos.pop();
    }

    private void adicionarSimbolo(String nome, String tipo, boolean isConstante) {
        Map<String, Simbolo> escopoAtual = escopos.peek();
        if (escopoAtual != null) {
            escopoAtual.put(nome, new Simbolo(nome, tipo, isConstante));
        }
    }

    private Simbolo buscarSimbolo(String nome) {
        for (Map<String, Simbolo> escopo : escopos) {
            if (escopo.containsKey(nome)) {
                return escopo.get(nome);
            }
        }
        return null;
    }

    @Override
    public Void visitPrograma(NossaGramaticaParser.ProgramaContext ctx) {
        entrarEscopo();
        super.visitPrograma(ctx);
        sairEscopo();
        return null;
    }

    @Override
    public Void visitDeclaracao_local(NossaGramaticaParser.Declaracao_localContext ctx) {
        if (ctx.variavel() != null) {
            NossaGramaticaParser.VariavelContext varCtx = ctx.variavel();
            NossaGramaticaParser.TipoContext tipoNode = varCtx.tipo();
            String nomeDoTipoOriginalDeclarado = tipoNode.getText();
            String nomeTipoBaseParaValidacao = "";
            int linhaDoTokenTipoBase = 0;
            boolean tipoBaseValido = false;

            if (tipoNode.tipo_estendido() != null && tipoNode.tipo_estendido().tipo_basico_ident() != null) {
                NossaGramaticaParser.Tipo_basico_identContext tbiCtx = tipoNode.tipo_estendido().tipo_basico_ident();
                if (tbiCtx.tipo_basico() != null) {
                    nomeTipoBaseParaValidacao = tbiCtx.tipo_basico().getText();
                    linhaDoTokenTipoBase = tbiCtx.tipo_basico().getStart().getLine();
                    if (nomeTipoBaseParaValidacao.equals("literal") || nomeTipoBaseParaValidacao.equals("inteiro") ||
                        nomeTipoBaseParaValidacao.equals("real") || nomeTipoBaseParaValidacao.equals("logico")) {
                        tipoBaseValido = true;
                    }
                } else if (tbiCtx.IDENT() != null) {
                    nomeTipoBaseParaValidacao = tbiCtx.IDENT().getText();
                    linhaDoTokenTipoBase = tbiCtx.IDENT().getSymbol().getLine();
                    // Futura validação de tipo definido pelo usuário
                }
            } // Adicionar else if para tipoNode.registro() se necessário

            if (!tipoBaseValido && !nomeTipoBaseParaValidacao.isEmpty()) {
                erros.add("Linha " + linhaDoTokenTipoBase + ": tipo " + nomeTipoBaseParaValidacao + " nao declarado");
            }

            for (NossaGramaticaParser.IdentificadorContext identCtx : varCtx.identificador()) {
                // CORREÇÃO: Acessar o primeiro IDENT da lista para o nome base da variável
                String nomeVar = identCtx.IDENT(0).getText();
                int linhaVar = identCtx.IDENT(0).getSymbol().getLine();

                Map<String, Simbolo> escopoAtual = escopos.peek();
                if (escopoAtual != null) {
                    if (escopoAtual.containsKey(nomeVar)) {
                        erros.add("Linha " + linhaVar + ": identificador " + nomeVar + " ja declarado");
                    } else {
                        adicionarSimbolo(nomeVar, nomeDoTipoOriginalDeclarado, false);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Void visitCmdLeia(NossaGramaticaParser.CmdLeiaContext ctx) {
        for (NossaGramaticaParser.IdentificadorContext identCtx : ctx.identificador()) {
            // CORREÇÃO: Acessar o primeiro IDENT da lista
            String nome = identCtx.IDENT(0).getText();
            if (buscarSimbolo(nome) == null) {
                erros.add("Linha " + identCtx.IDENT(0).getSymbol().getLine() + ": identificador " + nome + " nao declarado");
            }
        }
        return super.visitCmdLeia(ctx);
    }
    
    private NossaGramaticaParser.IdentificadorContext encontrarIdentificadorNaExpressao(NossaGramaticaParser.ExpressaoContext exprCtx) {
        if (exprCtx == null) return null;
        try {
            // CORREÇÃO: Acessar parcela(0)
            NossaGramaticaParser.ParcelaContext parcelaCtx = exprCtx
                .termo_logico(0)
                .fator_logico(0)
                .parcela_logica()
                .exp_relacional()
                .exp_aritmetica(0)
                .termo(0)
                .fator(0)
                .parcela(0); // CORRIGIDO AQUI

            if (parcelaCtx != null) {
                if (parcelaCtx.parcela_unario() != null && parcelaCtx.parcela_unario().identificador() != null) {
                    return parcelaCtx.parcela_unario().identificador();
                }
                if (parcelaCtx.parcela_nao_unario() != null && parcelaCtx.parcela_nao_unario().identificador() != null) {
                    return parcelaCtx.parcela_nao_unario().identificador();
                }
            }
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            // Normal para literais ou expressões mais complexas.
        }
        return null;
    }

    @Override
    public Void visitCmdEscreva(NossaGramaticaParser.CmdEscrevaContext ctx) {
        for (NossaGramaticaParser.ExpressaoContext exprCtx : ctx.expressao()) {
            NossaGramaticaParser.IdentificadorContext identCtx = encontrarIdentificadorNaExpressao(exprCtx);
            if (identCtx != null) {
                // CORREÇÃO: Acessar o primeiro IDENT da lista
                String nome = identCtx.IDENT(0).getText();
                if (buscarSimbolo(nome) == null) {
                    erros.add("Linha " + identCtx.IDENT(0).getSymbol().getLine() + ": identificador " + nome + " nao declarado");
                }
            }
        }
        return super.visitCmdEscreva(ctx);
    }

    @Override
    public Void visitCmdAtribuicao(NossaGramaticaParser.CmdAtribuicaoContext ctx) {
        NossaGramaticaParser.IdentificadorContext identCtx = ctx.identificador();
        // CORREÇÃO: Acessar o primeiro IDENT da lista
        String nome = identCtx.IDENT(0).getText();
        Simbolo simbolo = buscarSimbolo(nome);

        if (simbolo == null) {
            erros.add("Linha " + identCtx.IDENT(0).getSymbol().getLine() + ": identificador " + nome + " nao declarado");
        } else {
            if (simbolo.isConstante) {
                erros.add("Linha " + identCtx.IDENT(0).getSymbol().getLine() + ": identificador " + nome + " e constante e nao pode ser modificado");
            }
            // TODO: Verificar compatibilidade de tipo
        }
        
        if (ctx.expressao() != null) {
            visitExpressao(ctx.expressao());
        }
        return null;
    }

    @Override
    public Void visitDeclaracao_global(NossaGramaticaParser.Declaracao_globalContext ctx) {
        entrarEscopo();
        super.visitDeclaracao_global(ctx);
        sairEscopo();
        return null;
    }

    @Override
    public Void visitParametro(NossaGramaticaParser.ParametroContext ctx) {
        return super.visitParametro(ctx);
    }

    @Override
    public Void visitExpressao(NossaGramaticaParser.ExpressaoContext ctx) {
        NossaGramaticaParser.IdentificadorContext identCtx = encontrarIdentificadorNaExpressao(ctx);
        if (identCtx != null) {
            // CORREÇÃO: Acessar o primeiro IDENT da lista
            String nome = identCtx.IDENT(0).getText();
            if (buscarSimbolo(nome) == null) {
                 erros.add("Linha " + identCtx.IDENT(0).getSymbol().getLine() + ": identificador " + nome + " nao declarado");
            }
        }
        return super.visitExpressao(ctx);
    }
}