// ESSA CLASSE QUE VAI FAZER A COORDENACAO E O USO DOS ESCOPOS E FAZER AS VERIFICAÇÕES
package main.java.br.ufscar.dc.compiladores;

import java.util.*;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import br.ufscar.dc.compiladores.NossaGramaticaParser;
import br.ufscar.dc.compiladores.NossaGramaticaBaseVisitor;

public class AnalisadorSemantico extends NossaGramaticaBaseVisitor<Void> {

    private enum CategoriaSimbolo {
        VARIAVEL, CONSTANTE, TIPO, FUNCAO, PROCEDIMENTO, PARAMETRO
    }

    private abstract static class TipoLA {
        public abstract String getNome();
        public abstract boolean eNumerico();
        public abstract boolean eEquivalente(TipoLA outro);

        public static final Primitivo INTEIRO = new Primitivo("inteiro");
        public static final Primitivo REAL = new Primitivo("real");
        public static final Primitivo LITERAL = new Primitivo("literal");
        public static final Primitivo LOGICO = new Primitivo("logico");
        public static final Primitivo TIPO_INDEFINIDO = new Primitivo("tipo_indefinido");
    }

    private static class Primitivo extends TipoLA {
        private final String nome;
        private Primitivo(String nome) { this.nome = nome; }
        @Override public String getNome() { return nome; }
        @Override public boolean eNumerico() { return this == INTEIRO || this == REAL; }
        @Override public boolean eEquivalente(TipoLA outro) {
            if (this == TipoLA.TIPO_INDEFINIDO || outro == TipoLA.TIPO_INDEFINIDO) return true;
            if (this.eNumerico() && outro.eNumerico()) return true;
            return this == outro;
        }
    }

    private static class Ponteiro extends TipoLA {
        private final TipoLA tipoApontado;
        public Ponteiro(TipoLA tipoApontado) { this.tipoApontado = tipoApontado; }
        public TipoLA getTipoApontado() { return tipoApontado; }
        @Override public String getNome() { return "^" + tipoApontado.getNome(); }
        @Override public boolean eNumerico() { return false; }
        @Override public boolean eEquivalente(TipoLA outro) {
            if (outro == TipoLA.TIPO_INDEFINIDO) return true;
            if (!(outro instanceof Ponteiro)) return false;
            return this.tipoApontado.eEquivalente(((Ponteiro) outro).tipoApontado);
        }
    }

    private static class Registro extends TipoLA {
        public final String nomeOriginalDoTipo;
        public final Map<String, Simbolo> campos;
        public Registro(String nomeOriginal, Map<String, Simbolo> campos) {
            this.nomeOriginalDoTipo = nomeOriginal;
            this.campos = new LinkedHashMap<>(campos);
        }
        @Override public String getNome() { return nomeOriginalDoTipo != null ? nomeOriginalDoTipo : "registro_anonimo"; }
        @Override public boolean eNumerico() { return false; }
        @Override public boolean eEquivalente(TipoLA outro) {
            if (outro == TipoLA.TIPO_INDEFINIDO) return true;
            if (!(outro instanceof Registro)) return false;
            Registro outroReg = (Registro) outro;
            if (this.nomeOriginalDoTipo != null && outroReg.nomeOriginalDoTipo != null) {
                return this.nomeOriginalDoTipo.equals(outroReg.nomeOriginalDoTipo);
            }
            return false;
        }
    }

    private static class Simbolo {
        String nome;
        TipoLA tipo;
        CategoriaSimbolo categoria;
        int linhaDeclaracao;
        List<TipoLA> tiposParametros;
        Map<String, Simbolo> camposDoTipoRegistro;

        Simbolo(String nome, TipoLA tipo, CategoriaSimbolo categoria, int linhaDeclaracao) {
            this.nome = nome;
            this.tipo = tipo;
            this.categoria = categoria;
            this.linhaDeclaracao = linhaDeclaracao;
            if (categoria == CategoriaSimbolo.FUNCAO || categoria == CategoriaSimbolo.PROCEDIMENTO) {
                this.tiposParametros = new ArrayList<>();
            }
        }
    }

    private final Deque<Map<String, Simbolo>> pilhaEscopos = new ArrayDeque<>();
    private TipoLA tipoRetornoFuncaoAtual = null;

    // Estruturas para coletar e filtrar erros
    private Map<Integer, String> errosPrioritariosPorLinha = new HashMap<>(); // mensagem de erro prioritário
    private Set<String> errosDetalhadosAdicionados = new HashSet<>(); // Para erros de expressão - evita duplicatas exatas

    public List<String> getErros() {
        List<String> listaFinalErros = new ArrayList<>();

        // Adiciona erros detalhados que não estão em linhas com erros prioritários
        for (String erroDetalhado : errosDetalhadosAdicionados) {
            try {
                int linhaErroDetalhado = Integer.parseInt(erroDetalhado.substring(erroDetalhado.indexOf(" ") + 1, erroDetalhado.indexOf(":")));
                if (!errosPrioritariosPorLinha.containsKey(linhaErroDetalhado)) {
                    listaFinalErros.add(erroDetalhado);
                }
            } catch (Exception e) { // Se falhar ao parsear a linha, adiciona de qualquer forma
                listaFinalErros.add(erroDetalhado);
            }
        }

        // Adiciona todos os erros prioritários
        listaFinalErros.addAll(errosPrioritariosPorLinha.values());

        // Ordena a lista final pela linha do erro
        Collections.sort(listaFinalErros, (e1, e2) -> {
            try {
                int linha1 = Integer.parseInt(e1.substring(e1.indexOf(" ") + 1, e1.indexOf(":")));
                int linha2 = Integer.parseInt(e2.substring(e2.indexOf(" ") + 1, e2.indexOf(":")));
                return Integer.compare(linha1, linha2);
            } catch (Exception e) {
                return e1.compareTo(e2);
            }
        });

        return listaFinalErros;
    }

    // Adiciona um erro que deve ter prioridade sobre outros na mesma linha (exemplo erro de atribuição)
    private void adicionarErroPrioritario(Token token, String mensagem) {
        if (token == null) return;
        adicionarErroPrioritario(token.getLine(), mensagem);
    }

    private void adicionarErroPrioritario(int linha, String mensagem) {
        String erroCompleto = "Linha " + linha + ": " + mensagem;
        // O primeiro erro prioritário para uma linha é o que fica.
        if (!errosPrioritariosPorLinha.containsKey(linha)) {
            errosPrioritariosPorLinha.put(linha, erroCompleto);
        }
    }

    // Adiciona um erro mais detalhado (exemplo de uma expressão) - suprimido se houver um erro prioritário na linha
    private void adicionarErroDetalhado(Token token, String mensagem) {
        if (token == null) return;
        String erroCompleto = "Linha " + token.getLine() + ": " + mensagem;
        errosDetalhadosAdicionados.add(erroCompleto); // Adiciona ao set para evitar duplicatas exatas
    }

    private void entrarEscopo() { pilhaEscopos.push(new LinkedHashMap<>()); }
    private void sairEscopo() { pilhaEscopos.pop(); }

    private void adicionarSimbolo(String nome, TipoLA tipo, CategoriaSimbolo categoria, Token tokenNome) {
        Map<String, Simbolo> escopoAtual = pilhaEscopos.peek();
        if (escopoAtual.containsKey(nome)) {
            adicionarErroPrioritario(tokenNome, "identificador " + nome + " ja declarado anteriormente");
        } else {
            Simbolo s = new Simbolo(nome, tipo, categoria, tokenNome.getLine());
            escopoAtual.put(nome, s);
        }
    }

    private void adicionarSimbolo(Simbolo simbolo) {
        Map<String, Simbolo> escopoAtual = pilhaEscopos.peek();
        if (escopoAtual.containsKey(simbolo.nome)) {
            adicionarErroPrioritario(simbolo.linhaDeclaracao, "identificador " + simbolo.nome + " ja declarado anteriormente");
        } else {
            escopoAtual.put(simbolo.nome, simbolo);
        }
    }

    private Simbolo buscarSimbolo(String nome) {
        for (Map<String, Simbolo> escopo : pilhaEscopos) {
            if (escopo.containsKey(nome)) return escopo.get(nome);
        }
        return null;
    }

    @Override
    public Void visitPrograma(NossaGramaticaParser.ProgramaContext ctx) {
        entrarEscopo();
        visitDeclaracoes(ctx.declaracoes());
        if (ctx.corpo() != null) visitCorpo(ctx.corpo());
        sairEscopo();
        return null;
    }

    @Override
    public Void visitDeclaracoes(NossaGramaticaParser.DeclaracoesContext ctx) {
        return super.visitDeclaracoes(ctx);
    }

    @Override
    public Void visitCorpo(NossaGramaticaParser.CorpoContext ctx) {
        for(NossaGramaticaParser.Declaracao_localContext declCtx : ctx.declaracao_local()) visitDeclaracao_local(declCtx);
        for(NossaGramaticaParser.CmdContext cmdCtx : ctx.cmd()) visit(cmdCtx);
        return null;
    }

    private TipoLA resolverTipoNo(NossaGramaticaParser.TipoContext tipoCtx) {
        if (tipoCtx.registro() != null) {
            entrarEscopo();
            Map<String, Simbolo> campos = new LinkedHashMap<>();
            for (NossaGramaticaParser.VariavelContext varCampoCtx : tipoCtx.registro().variavel()) {
                TipoLA tipoCampo = resolverTipoNo(varCampoCtx.tipo());
                for (NossaGramaticaParser.IdentificadorContext identCampoCtx : varCampoCtx.identificador()) {
                    String nomeCampo = identCampoCtx.IDENT(0).getText();
                    Token tokenNomeCampo = identCampoCtx.IDENT(0).getSymbol();
                    if (campos.containsKey(nomeCampo)) {
                        adicionarErroPrioritario(tokenNomeCampo, "identificador " + nomeCampo + " ja declarado neste registro");
                    } else {
                        campos.put(nomeCampo, new Simbolo(nomeCampo, tipoCampo, CategoriaSimbolo.VARIAVEL, tokenNomeCampo.getLine()));
                    }
                }
            }
            sairEscopo();
            return new Registro(null, campos);
        } else if (tipoCtx.tipo_estendido() != null) {
            return resolverTipoEstendido(tipoCtx.tipo_estendido());
        }
        return TipoLA.TIPO_INDEFINIDO;
    }

    private TipoLA resolverTipoEstendido(NossaGramaticaParser.Tipo_estendidoContext teCtx) {
        NossaGramaticaParser.Tipo_basico_identContext tbiCtx = teCtx.tipo_basico_ident();
        TipoLA tipoBase;
        if (tbiCtx.tipo_basico() != null) {
            String nomeTipoBase = tbiCtx.tipo_basico().getText();
            switch (nomeTipoBase) {
                case "literal": tipoBase = TipoLA.LITERAL; break;
                case "inteiro": tipoBase = TipoLA.INTEIRO; break;
                case "real":    tipoBase = TipoLA.REAL;    break;
                case "logico":  tipoBase = TipoLA.LOGICO;  break;
                default:        tipoBase = TipoLA.TIPO_INDEFINIDO; break;
            }
        } else {
            String nomeIdentTipo = tbiCtx.IDENT().getText();
            Simbolo simboloTipo = buscarSimbolo(nomeIdentTipo);
            if (simboloTipo == null || simboloTipo.categoria != CategoriaSimbolo.TIPO) {
                adicionarErroPrioritario(tbiCtx.IDENT().getSymbol(), "tipo " + nomeIdentTipo + " nao declarado");
                tipoBase = TipoLA.TIPO_INDEFINIDO;
            } else {
                tipoBase = simboloTipo.tipo;
            }
        }
        if (teCtx.PONTEIRO() != null) {
            return (tipoBase == TipoLA.TIPO_INDEFINIDO) ? TipoLA.TIPO_INDEFINIDO : new Ponteiro(tipoBase);
        }
        return tipoBase;
    }

    @Override
    public Void visitDeclaracao_local(NossaGramaticaParser.Declaracao_localContext ctx) {
        if (ctx.CONSTANTE() != null) {
            String nomeConst = ctx.IDENT().getText();
            Token tokenNomeConst = ctx.IDENT().getSymbol();
            TipoLA tipoDeclaradoConst = resolverTipoBasico(ctx.tipo_basico());
            TipoLA tipoValor = verificarTipoValorConstante(ctx.valor_constante());
            if (tipoDeclaradoConst != tipoValor && tipoValor != TipoLA.TIPO_INDEFINIDO && tipoDeclaradoConst != TipoLA.TIPO_INDEFINIDO) {
                adicionarErroPrioritario(ctx.valor_constante().getStart(), "tipo de valor constante incompativel com " + tipoDeclaradoConst.getNome());
            }
            adicionarSimbolo(nomeConst, tipoDeclaradoConst, CategoriaSimbolo.CONSTANTE, tokenNomeConst);
        } else if (ctx.TIPO() != null) {
            String nomeNovoTipo = ctx.IDENT().getText();
            Token tokenNomeNovoTipo = ctx.IDENT().getSymbol();
            TipoLA tipoDefinido = resolverTipoNo(ctx.tipo());
            if (tipoDefinido instanceof Registro && ((Registro) tipoDefinido).nomeOriginalDoTipo == null) {
                tipoDefinido = new Registro(nomeNovoTipo, ((Registro) tipoDefinido).campos);
            }
            Simbolo simboloDoTipo = new Simbolo(nomeNovoTipo, tipoDefinido, CategoriaSimbolo.TIPO, tokenNomeNovoTipo.getLine());
            if (tipoDefinido instanceof Registro) simboloDoTipo.camposDoTipoRegistro = ((Registro) tipoDefinido).campos;
            adicionarSimbolo(simboloDoTipo);
        } else if (ctx.variavel() != null) {
            NossaGramaticaParser.VariavelContext varCtx = ctx.variavel();
            TipoLA tipoDasVariaveis = resolverTipoNo(varCtx.tipo());
            for (NossaGramaticaParser.IdentificadorContext identCtx : varCtx.identificador()) {
                adicionarSimbolo(identCtx.IDENT(0).getText(), tipoDasVariaveis, CategoriaSimbolo.VARIAVEL, identCtx.IDENT(0).getSymbol());
            }
        }
        return null;
    }

    private TipoLA resolverTipoBasico(NossaGramaticaParser.Tipo_basicoContext ctx) {
        String nome = ctx.getText();
        if (nome.equals("literal")) return TipoLA.LITERAL;
        if (nome.equals("inteiro")) return TipoLA.INTEIRO;
        if (nome.equals("real")) return TipoLA.REAL;
        if (nome.equals("logico")) return TipoLA.LOGICO;
        return TipoLA.TIPO_INDEFINIDO;
    }

    private TipoLA verificarTipoValorConstante(NossaGramaticaParser.Valor_constanteContext ctx) {
        if (ctx.CADEIA() != null) return TipoLA.LITERAL;
        if (ctx.NUM_INT() != null) return TipoLA.INTEIRO;
        if (ctx.NUM_REAL() != null) return TipoLA.REAL;
        if (ctx.VERDADEIRO() != null || ctx.FALSO() != null) return TipoLA.LOGICO;
        return TipoLA.TIPO_INDEFINIDO;
    }

    @Override
    public Void visitDeclaracao_global(NossaGramaticaParser.Declaracao_globalContext ctx) {
        String nomeSubRotina; Token tokenNome; CategoriaSimbolo categoria; TipoLA tipoRetorno = null;
        if (ctx.PROCEDIMENTO() != null) {
            nomeSubRotina = ctx.IDENT().getText(); tokenNome = ctx.IDENT().getSymbol(); categoria = CategoriaSimbolo.PROCEDIMENTO;
        } else {
            nomeSubRotina = ctx.IDENT().getText(); tokenNome = ctx.IDENT().getSymbol(); categoria = CategoriaSimbolo.FUNCAO;
            tipoRetorno = resolverTipoEstendido(ctx.tipo_estendido());
        }
        Simbolo simboloSubRotina = new Simbolo(nomeSubRotina, tipoRetorno, categoria, tokenNome.getLine());
        if (ctx.parametros() != null) {
            for (NossaGramaticaParser.ParametroContext paramCtx : ctx.parametros().parametro()) {
                TipoLA tipoDoParam = resolverTipoEstendido(paramCtx.tipo_estendido());
                for (int i = 0; i < paramCtx.identificador().size(); i++) simboloSubRotina.tiposParametros.add(tipoDoParam);
            }
        }
        adicionarSimbolo(simboloSubRotina);
        entrarEscopo();
        if (categoria == CategoriaSimbolo.FUNCAO) this.tipoRetornoFuncaoAtual = tipoRetorno;
        if (ctx.parametros() != null) visitParametros(ctx.parametros());
        ctx.declaracao_local().forEach(this::visitDeclaracao_local);
        ctx.cmd().forEach(this::visit);
        this.tipoRetornoFuncaoAtual = null;
        sairEscopo();
        return null;
    }

    @Override
    public Void visitParametros(NossaGramaticaParser.ParametrosContext ctx) {
        for (NossaGramaticaParser.ParametroContext paramCtx : ctx.parametro()) visitParametro(paramCtx);
        return null;
    }

    @Override
    public Void visitParametro(NossaGramaticaParser.ParametroContext paramCtx) {
        TipoLA tipoParam = resolverTipoEstendido(paramCtx.tipo_estendido());
        if (tipoParam != TipoLA.TIPO_INDEFINIDO) {
            for (NossaGramaticaParser.IdentificadorContext identCtx : paramCtx.identificador()) {
                adicionarSimbolo(identCtx.IDENT(0).getText(), tipoParam, CategoriaSimbolo.PARAMETRO, identCtx.IDENT(0).getSymbol());
            }
        }
        return null;
    }

    public TipoLA verificarTipoExpressao(NossaGramaticaParser.ExpressaoContext ctx) {
        if (ctx == null) return TipoLA.TIPO_INDEFINIDO;
        TipoLA tipoResultante = verificarTipoTermoLogico(ctx.termo_logico(0));
        for (int i = 0; i < ctx.op_logico_1().size(); i++) {
            TipoLA tipoDireita = verificarTipoTermoLogico(ctx.termo_logico(i + 1));
            Token opToken = ctx.op_logico_1(i).OU_LOGICO().getSymbol();
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) tipoResultante = TipoLA.TIPO_INDEFINIDO;
            else if (!(tipoResultante.eEquivalente(TipoLA.LOGICO) && tipoDireita.eEquivalente(TipoLA.LOGICO))) {
                adicionarErroDetalhado(opToken, "operacao logica '" + opToken.getText() + "' requer operandos logicos");
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else tipoResultante = TipoLA.LOGICO;
        }
        return tipoResultante;
    }

    private TipoLA verificarTipoTermoLogico(NossaGramaticaParser.Termo_logicoContext ctx) {
        TipoLA tipoResultante = verificarTipoFatorLogico(ctx.fator_logico(0));
        for (int i = 0; i < ctx.op_logico_2().size(); i++) {
            TipoLA tipoDireita = verificarTipoFatorLogico(ctx.fator_logico(i + 1));
            Token opToken = ctx.op_logico_2(i).E_LOGICO().getSymbol();
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) tipoResultante = TipoLA.TIPO_INDEFINIDO;
            else if (!(tipoResultante.eEquivalente(TipoLA.LOGICO) && tipoDireita.eEquivalente(TipoLA.LOGICO))) {
                adicionarErroDetalhado(opToken, "operacao logica '" + opToken.getText() + "' requer operandos logicos");
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else tipoResultante = TipoLA.LOGICO;
        }
        return tipoResultante;
    }

    private TipoLA verificarTipoFatorLogico(NossaGramaticaParser.Fator_logicoContext ctx) {
        TipoLA tipoParcela = verificarTipoParcelaLogica(ctx.parcela_logica());
        if (ctx.NAO_LOGICO() != null) {
            if (tipoParcela == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;
            if (!tipoParcela.eEquivalente(TipoLA.LOGICO)) {
                adicionarErroDetalhado(ctx.NAO_LOGICO().getSymbol(), "operador 'nao' requer operando logico");
                return TipoLA.TIPO_INDEFINIDO;
            }
            return TipoLA.LOGICO;
        }
        return tipoParcela;
    }

    private TipoLA verificarTipoParcelaLogica(NossaGramaticaParser.Parcela_logicaContext ctx) {
        if (ctx.VERDADEIRO() != null || ctx.FALSO() != null) return TipoLA.LOGICO;
        return verificarTipoExpRelacional(ctx.exp_relacional());
    }

    private TipoLA verificarTipoExpRelacional(NossaGramaticaParser.Exp_relacionalContext ctx) {
        TipoLA tipoEsq = verificarTipoExpAritmetica(ctx.exp_aritmetica(0));
        if (ctx.op_relacional() != null) {
            TipoLA tipoDir = verificarTipoExpAritmetica(ctx.exp_aritmetica(1));
            Token opToken = ctx.op_relacional().getStart();
            if (tipoEsq == TipoLA.TIPO_INDEFINIDO || tipoDir == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;
            boolean num = tipoEsq.eNumerico() && tipoDir.eNumerico();
            boolean lit = tipoEsq == TipoLA.LITERAL && tipoDir == TipoLA.LITERAL;
            boolean log = (opToken.getText().equals("=") || opToken.getText().equals("<>")) && tipoEsq == TipoLA.LOGICO && tipoDir == TipoLA.LOGICO;
            boolean pont = (opToken.getText().equals("=") || opToken.getText().equals("<>")) && (tipoEsq instanceof Ponteiro && tipoDir instanceof Ponteiro && ((Ponteiro)tipoEsq).getTipoApontado().eEquivalente(((Ponteiro)tipoDir).getTipoApontado()));
            if (!num && !lit && !log && !pont) {
                adicionarErroDetalhado(opToken, "tipos incompativeis para operacao relacional: " + tipoEsq.getNome() + " " + opToken.getText() + " " + tipoDir.getNome());
                return TipoLA.TIPO_INDEFINIDO;
            }
            return TipoLA.LOGICO;
        }
        return tipoEsq;
    }

    private TipoLA verificarTipoExpAritmetica(NossaGramaticaParser.Exp_aritmeticaContext ctx) {
        TipoLA tipoResultante = verificarTipoTermo(ctx.termo(0));
        for (int i = 0; i < ctx.op1().size(); i++) {
            TipoLA tipoDireita = verificarTipoTermo(ctx.termo(i + 1));
            Token opToken = ctx.op1(i).MAIS() != null ? ctx.op1(i).MAIS().getSymbol() : ctx.op1(i).MENOS().getSymbol();
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) tipoResultante = TipoLA.TIPO_INDEFINIDO;
            else if ( (opToken.getText().equals("+") && tipoResultante == TipoLA.LITERAL && tipoDireita == TipoLA.LITERAL) ) tipoResultante = TipoLA.LITERAL;
            else if (!tipoResultante.eNumerico() || !tipoDireita.eNumerico()) {
                adicionarErroDetalhado(opToken, "operacao aritmetica '" + opToken.getText() + "' requer operandos numericos (ou literais para '+')");
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else tipoResultante = (tipoResultante == TipoLA.REAL || tipoDireita == TipoLA.REAL) ? TipoLA.REAL : TipoLA.INTEIRO;
        }
        return tipoResultante;
    }

    private TipoLA verificarTipoTermo(NossaGramaticaParser.TermoContext ctx) {
        TipoLA tipoResultante = verificarTipoFator(ctx.fator(0));
        for (int i = 0; i < ctx.op2().size(); i++) {
            TipoLA tipoDireita = verificarTipoFator(ctx.fator(i + 1));
            Token opToken = ctx.op2(i).MULT() != null ? ctx.op2(i).MULT().getSymbol() : ctx.op2(i).DIVISAO().getSymbol();
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) tipoResultante = TipoLA.TIPO_INDEFINIDO;
            else if (!tipoResultante.eNumerico() || !tipoDireita.eNumerico()) {
                adicionarErroDetalhado(opToken, "operacao aritmetica '" + opToken.getText() + "' requer operandos numericos");
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else tipoResultante = (ctx.op2(i).DIVISAO() != null || tipoResultante == TipoLA.REAL || tipoDireita == TipoLA.REAL) ? TipoLA.REAL : TipoLA.INTEIRO;
        }
        return tipoResultante;
    }

    private TipoLA verificarTipoFator(NossaGramaticaParser.FatorContext ctx) {
        TipoLA tipoResultante = verificarTipoParcela(ctx.parcela(0));
        for (int i = 0; i < ctx.op3().size(); i++) {
            TipoLA tipoDireita = verificarTipoParcela(ctx.parcela(i + 1));
            Token opToken = ctx.op3(i).MOD().getSymbol();
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) tipoResultante = TipoLA.TIPO_INDEFINIDO;
            else if (tipoResultante != TipoLA.INTEIRO || tipoDireita != TipoLA.INTEIRO) {
                adicionarErroDetalhado(opToken, "operacao '" + opToken.getText() + "' requer operandos inteiros");
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else tipoResultante = TipoLA.INTEIRO;
        }
        return tipoResultante;
    }

    private TipoLA verificarTipoParcela(NossaGramaticaParser.ParcelaContext ctx) {
        TipoLA tipoBase = (ctx.parcela_unario() != null) ? verificarTipoParcelaUnario(ctx.parcela_unario()) : verificarTipoParcelaNaoUnario(ctx.parcela_nao_unario());
        if (ctx.op_unario() != null && ctx.op_unario().MENOS() != null) { // Negativo
            if (tipoBase == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;
            if (!tipoBase.eNumerico()) {
                adicionarErroDetalhado(ctx.op_unario().MENOS().getSymbol(), "operador unario '-' requer operando numerico");
                return TipoLA.TIPO_INDEFINIDO;
            }
        }
        return tipoBase;
    }

    private TipoLA verificarTipoParcelaUnario(NossaGramaticaParser.Parcela_unarioContext ctx) {
        if (ctx.identificador() != null) return getTipoDeIdentificadorContext(ctx.identificador(), ctx.PONTEIRO() != null);
        if (ctx.IDENT() != null) { // Chamada de função
            String nomeFunc = ctx.IDENT().getText();
            Simbolo funcSimbolo = buscarSimbolo(nomeFunc);
            if (funcSimbolo == null ) {
                adicionarErroPrioritario(ctx.IDENT().getSymbol(), "identificador " + nomeFunc + " nao declarado");
                ctx.expressao().forEach(this::verificarTipoExpressao); return TipoLA.TIPO_INDEFINIDO;
            }
            if(funcSimbolo.categoria != CategoriaSimbolo.FUNCAO){
                adicionarErroDetalhado(ctx.IDENT().getSymbol(), "identificador " + nomeFunc + " nao e uma funcao");
                ctx.expressao().forEach(this::verificarTipoExpressao); return TipoLA.TIPO_INDEFINIDO;
            }
            List<NossaGramaticaParser.ExpressaoContext> args = ctx.expressao();
            if (funcSimbolo.tiposParametros == null) funcSimbolo.tiposParametros = new ArrayList<>();
            if (args.size() != funcSimbolo.tiposParametros.size()) {
                adicionarErroDetalhado(ctx.IDENT().getSymbol(), "numero incorreto de parametros para funcao " + nomeFunc);
            }
            for(int i=0; i < Math.min(args.size(), funcSimbolo.tiposParametros.size()); i++) {
                TipoLA argT = verificarTipoExpressao(args.get(i));
                TipoLA paramT = funcSimbolo.tiposParametros.get(i);
                if(paramT != TipoLA.TIPO_INDEFINIDO && argT != TipoLA.TIPO_INDEFINIDO && !saoCompativeisAtribuicao(paramT, argT)) {
                    adicionarErroDetalhado(args.get(i).getStart(), "tipo de parametro " + (i+1) + " incompativel na chamada da funcao " + nomeFunc +
                            ". Esperado " + paramT.getNome() + " mas obteve " + argT.getNome());
                }
            }
            return funcSimbolo.tipo;
        }
        if (ctx.NUM_INT() != null) return TipoLA.INTEIRO;
        if (ctx.NUM_REAL() != null) return TipoLA.REAL;
        if (ctx.LPAREN() != null) return verificarTipoExpressao(ctx.expressao(0));
        return TipoLA.TIPO_INDEFINIDO;
    }

    private TipoLA verificarTipoParcelaNaoUnario(NossaGramaticaParser.Parcela_nao_unarioContext ctx) {
        if (ctx.ENDERECO() != null) {
            TipoLA tipoIdent = getTipoDeIdentificadorContext(ctx.identificador(), false);
            return (tipoIdent == TipoLA.TIPO_INDEFINIDO) ? TipoLA.TIPO_INDEFINIDO : new Ponteiro(tipoIdent);
        }
        if (ctx.CADEIA() != null) return TipoLA.LITERAL;
        return TipoLA.TIPO_INDEFINIDO;
    }

    private TipoLA getTipoDeIdentificadorContext(NossaGramaticaParser.IdentificadorContext identCtx, boolean desreferenciadoPeloOperador) {
        String nomeBase = identCtx.IDENT(0).getText();
        Token tokenBase = identCtx.IDENT(0).getSymbol();
        Simbolo simbolo = buscarSimbolo(nomeBase);
        if (simbolo == null) {
            adicionarErroPrioritario(tokenBase, "identificador " + nomeBase + " nao declarado");
            return TipoLA.TIPO_INDEFINIDO;
        }
        if (simbolo.categoria == CategoriaSimbolo.TIPO || simbolo.categoria == CategoriaSimbolo.PROCEDIMENTO) {
            boolean isCall = (identCtx.getParent() instanceof NossaGramaticaParser.Parcela_unarioContext && ((NossaGramaticaParser.Parcela_unarioContext)identCtx.getParent()).LPAREN() != null) ||
                    (identCtx.getParent() instanceof NossaGramaticaParser.CmdChamadaContext);
            if (!isCall) {
                adicionarErroDetalhado(tokenBase, "identificador " + nomeBase + " e um " + simbolo.categoria.toString().toLowerCase() + " e nao pode ser usado neste contexto");
                return TipoLA.TIPO_INDEFINIDO;
            }
        }
        TipoLA tipoAtual = simbolo.tipo;
        for (int i = 1; i < identCtx.IDENT().size(); i++) {
            if (tipoAtual == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;
            if (!(tipoAtual instanceof Registro)) {
                String prefix = identCtx.IDENT().subList(0, i).stream().map(TerminalNode::getText).collect(Collectors.joining("."));
                adicionarErroDetalhado(identCtx.IDENT(i-1).getSymbol(), "identificador " + prefix + " nao e um registro");
                return TipoLA.TIPO_INDEFINIDO;
            }
            Registro rTipo = (Registro) tipoAtual; String campoNome = identCtx.IDENT(i).getText(); Token campoToken = identCtx.IDENT(i).getSymbol();
            Simbolo campoSimbolo = (rTipo.campos != null) ? rTipo.campos.get(campoNome) : null;
            if (campoSimbolo == null) {
                adicionarErroDetalhado(campoToken, "campo " + campoNome + " nao existe no tipo " + rTipo.getNome());
                return TipoLA.TIPO_INDEFINIDO;
            }
            tipoAtual = campoSimbolo.tipo;
        }
        if (desreferenciadoPeloOperador) {
            if (tipoAtual == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;
            if (!(tipoAtual instanceof Ponteiro)) {
                adicionarErroDetalhado(tokenBase, "tentativa de desreferenciar um nao-ponteiro " + identCtx.getText());
                return TipoLA.TIPO_INDEFINIDO;
            }
            return ((Ponteiro) tipoAtual).getTipoApontado();
        }
        if (identCtx.dimensao() != null && !identCtx.dimensao().exp_aritmetica().isEmpty()) {
            adicionarErroDetalhado(identCtx.dimensao().getStart(), "acesso a array (dimensao) nao implementado");
            return TipoLA.TIPO_INDEFINIDO;
        }
        return tipoAtual;
    }

    @Override
    public Void visitCmdLeia(NossaGramaticaParser.CmdLeiaContext ctx) {
        for (NossaGramaticaParser.IdentificadorContext identCtx : ctx.identificador()) {
            getTipoDeIdentificadorContext(identCtx, false);
        }
        return null;
    }

    @Override
    public Void visitCmdEscreva(NossaGramaticaParser.CmdEscrevaContext ctx) {
        for (NossaGramaticaParser.ExpressaoContext exprCtx : ctx.expressao()) verificarTipoExpressao(exprCtx);
        return null;
    }

    @Override
    public Void visitCmdSe(NossaGramaticaParser.CmdSeContext ctx) {
        TipoLA tipoCond = verificarTipoExpressao(ctx.expressao());
        if (tipoCond != TipoLA.TIPO_INDEFINIDO && tipoCond != TipoLA.LOGICO) {
            adicionarErroPrioritario(ctx.expressao().getStart(), "expressao do comando 'se' deve ser logica");
        }
        for(NossaGramaticaParser.CmdContext cmd : ctx.cmd()) visit(cmd);
        return null;
    }

    @Override
    public Void visitCmdCaso(NossaGramaticaParser.CmdCasoContext ctx) {
        TipoLA tipoExp = verificarTipoExpAritmetica(ctx.exp_aritmetica());
        if (tipoExp != TipoLA.TIPO_INDEFINIDO && tipoExp != TipoLA.INTEIRO) {
            adicionarErroPrioritario(ctx.exp_aritmetica().getStart(), "expressao do comando 'caso' deve ser inteira");
        }
        if(ctx.selecao() != null) visitSelecao(ctx.selecao());
        if(ctx.SENAO() != null && ctx.cmd() != null && !ctx.cmd().isEmpty()){
            for(NossaGramaticaParser.CmdContext cmdCtx : ctx.cmd()) visit(cmdCtx);
        }
        return null;
    }

    @Override
    public Void visitSelecao(NossaGramaticaParser.SelecaoContext ctx) {
        for(NossaGramaticaParser.Item_selecaoContext item : ctx.item_selecao()) visitItem_selecao(item);
        return null;
    }

    @Override
    public Void visitItem_selecao(NossaGramaticaParser.Item_selecaoContext ctx) {
        visitConstantes(ctx.constantes()); // Verifica estrutura, não tipo dos NUM_INT que é garantido
        for(NossaGramaticaParser.CmdContext cmdCtx : ctx.cmd()) visit(cmdCtx);
        return null;
    }

    @Override public Void visitConstantes(NossaGramaticaParser.ConstantesContext ctx){ return super.visitConstantes(ctx); }

    @Override
    public Void visitCmdPara(NossaGramaticaParser.CmdParaContext ctx) {
        String nomeVar = ctx.IDENT().getText(); Simbolo simboloVar = buscarSimbolo(nomeVar); Token tokenVar = ctx.IDENT().getSymbol();
        if (simboloVar == null) adicionarErroPrioritario(tokenVar, "identificador " + nomeVar + " nao declarado");
        else if (simboloVar.categoria != CategoriaSimbolo.VARIAVEL) adicionarErroPrioritario(tokenVar, "identificador " + nomeVar + " em comando 'para' deve ser uma variavel");
        else if (simboloVar.tipo != TipoLA.TIPO_INDEFINIDO && !simboloVar.tipo.eNumerico()) adicionarErroPrioritario(tokenVar, "variavel de controle " + nomeVar + " em comando 'para' deve ser numerica");

        TipoLA tIni = verificarTipoExpAritmetica(ctx.exp_aritmetica(0)); TipoLA tFim = verificarTipoExpAritmetica(ctx.exp_aritmetica(1));
        if (tIni != TipoLA.TIPO_INDEFINIDO && !tIni.eNumerico()) adicionarErroPrioritario(ctx.exp_aritmetica(0).getStart(), "expressao inicial do comando 'para' deve ser numerica");
        if (tFim != TipoLA.TIPO_INDEFINIDO && !tFim.eNumerico()) adicionarErroPrioritario(ctx.exp_aritmetica(1).getStart(), "expressao final do comando 'para' deve ser numerica");
        for(NossaGramaticaParser.CmdContext cmdCtx : ctx.cmd()) visit(cmdCtx);
        return null;
    }

    @Override
    public Void visitCmdEnquanto(NossaGramaticaParser.CmdEnquantoContext ctx) {
        TipoLA tipoCond = verificarTipoExpressao(ctx.expressao());
        if (tipoCond != TipoLA.TIPO_INDEFINIDO && tipoCond != TipoLA.LOGICO) {
            adicionarErroPrioritario(ctx.expressao().getStart(), "expressao do comando 'enquanto' deve ser logica");
        }
        for(NossaGramaticaParser.CmdContext cmdCtx : ctx.cmd()) visit(cmdCtx);
        return null;
    }

    @Override
    public Void visitCmdFaca(NossaGramaticaParser.CmdFacaContext ctx) {
        for(NossaGramaticaParser.CmdContext cmdCtx : ctx.cmd()) visit(cmdCtx);
        TipoLA tipoCond = verificarTipoExpressao(ctx.expressao());
        if (tipoCond != TipoLA.TIPO_INDEFINIDO && tipoCond != TipoLA.LOGICO) {
            adicionarErroPrioritario(ctx.expressao().getStart(), "expressao do comando 'faca..ate' deve ser logica");
        }
        return null;
    }

    @Override
    public Void visitCmdChamada(NossaGramaticaParser.CmdChamadaContext ctx) {
        String nome = ctx.IDENT().getText(); Simbolo simbolo = buscarSimbolo(nome); Token tokenNome = ctx.IDENT().getSymbol();
        if(simbolo == null) {
            adicionarErroPrioritario(tokenNome, "identificador " + nome + " nao declarado");
            ctx.expressao().forEach(this::verificarTipoExpressao); return null;
        }
        if(simbolo.categoria != CategoriaSimbolo.PROCEDIMENTO && simbolo.categoria != CategoriaSimbolo.FUNCAO) {
            adicionarErroPrioritario(tokenNome, "identificador " + nome + " nao e um procedimento ou funcao");
            ctx.expressao().forEach(this::verificarTipoExpressao); return null;
        }
        List<NossaGramaticaParser.ExpressaoContext> args = ctx.expressao();
        if (simbolo.tiposParametros == null) simbolo.tiposParametros = new ArrayList<>();
        if (args.size() != simbolo.tiposParametros.size()) {
            adicionarErroPrioritario(tokenNome, "numero incorreto de parametros para " + simbolo.categoria.toString().toLowerCase() + " " + nome);
        }
        for(int i=0; i < Math.min(args.size(), simbolo.tiposParametros.size()); i++) {
            TipoLA tArg = verificarTipoExpressao(args.get(i)); TipoLA tParam = simbolo.tiposParametros.get(i);
            if(tParam != TipoLA.TIPO_INDEFINIDO && tArg != TipoLA.TIPO_INDEFINIDO && !saoCompativeisAtribuicao(tParam, tArg)) {
                adicionarErroPrioritario(args.get(i).getStart(), "tipo de parametro " + (i+1) + " incompativel na chamada de " + nome +
                        ". Esperado " + tParam.getNome() + " mas obteve " + tArg.getNome());
            }
        }
        return null;
    }

    @Override
    public Void visitCmdRetorne(NossaGramaticaParser.CmdRetorneContext ctx) {
        if (tipoRetornoFuncaoAtual == null) {
            adicionarErroPrioritario(ctx.RETORNE().getSymbol(), "comando retorne nao permitido fora de uma funcao");
        } else {
            TipoLA tipoRetornado = verificarTipoExpressao(ctx.expressao());
            if (tipoRetornoFuncaoAtual != TipoLA.TIPO_INDEFINIDO) {
                boolean compativel = (tipoRetornado != TipoLA.TIPO_INDEFINIDO) && saoCompativeisAtribuicao(tipoRetornoFuncaoAtual, tipoRetornado);
                if (!compativel) {
                    String msg = "tipo de retorno incompativel com a declaracao da funcao. Esperado " +
                            tipoRetornoFuncaoAtual.getNome() + " mas obteve " +
                            (tipoRetornado == TipoLA.TIPO_INDEFINIDO ? "tipo_indefinido_na_expressao" : tipoRetornado.getNome());
                    adicionarErroPrioritario(ctx.expressao().getStart(), msg);
                }
            }
        }
        return null;
    }

    @Override
    public Void visitCmdAtribuicao(NossaGramaticaParser.CmdAtribuicaoContext ctx) {
        boolean lhsDesref = ctx.PONTEIRO() != null; String nomeLHSVar = ctx.identificador().IDENT(0).getText(); Token tokenAtr = ctx.ATRIBUICAO().getSymbol();
        TipoLA tipoLHS = getTipoDeIdentificadorContext(ctx.identificador(), lhsDesref);
        TipoLA tipoRHS = verificarTipoExpressao(ctx.expressao());
        Simbolo simboloLHSBase = buscarSimbolo(ctx.identificador().IDENT(0).getText());
        boolean erroFatalLHS = false;
        if (simboloLHSBase != null && simboloLHSBase.categoria == CategoriaSimbolo.CONSTANTE && ctx.identificador().IDENT().size() == 1 && !lhsDesref) {
            adicionarErroPrioritario(tokenAtr, "identificador " + nomeLHSVar + " e constante e nao pode ser modificado");
            erroFatalLHS = true;
        }
        if (tipoLHS == TipoLA.TIPO_INDEFINIDO) erroFatalLHS = true; // Erro de LHS não declarado já é prioritário

        if (!erroFatalLHS) { // Se LHS é válido e não constante
            boolean compativel = (tipoRHS != TipoLA.TIPO_INDEFINIDO) && saoCompativeisAtribuicao(tipoLHS, tipoRHS);
            if (!compativel) {
                adicionarErroPrioritario(tokenAtr, "atribuicao nao compativel para " + nomeLHSVar);
            }
        }
        return null;
    }

    private boolean saoCompativeisAtribuicao(TipoLA esquerdo, TipoLA direito) {
        if (esquerdo == TipoLA.TIPO_INDEFINIDO || direito == TipoLA.TIPO_INDEFINIDO) return false;
        if (esquerdo instanceof Ponteiro) {
            return (direito instanceof Ponteiro) && ((Ponteiro) esquerdo).getTipoApontado().eEquivalente(((Ponteiro) direito).getTipoApontado());
        }
        if (esquerdo.eNumerico() && direito.eNumerico()) return !(esquerdo == TipoLA.INTEIRO && direito == TipoLA.REAL);
        if (esquerdo == TipoLA.LITERAL && direito == TipoLA.LITERAL) return true;
        if (esquerdo == TipoLA.LOGICO && direito == TipoLA.LOGICO) return true;
        if (esquerdo instanceof Registro && direito instanceof Registro) return esquerdo.eEquivalente(direito);
        return false;
    }
}