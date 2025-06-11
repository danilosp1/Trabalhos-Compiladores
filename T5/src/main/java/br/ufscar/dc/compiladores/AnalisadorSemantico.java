package main.java.br.ufscar.dc.compiladores;

import java.util.*;
import org.antlr.v4.runtime.Token;
import br.ufscar.dc.compiladores.NossaGramaticaParser;
import br.ufscar.dc.compiladores.NossaGramaticaBaseVisitor;

public class AnalisadorSemantico extends NossaGramaticaBaseVisitor<Void> {

    public enum CategoriaSimbolo {
        VARIAVEL, CONSTANTE, TIPO, FUNCAO, PROCEDIMENTO, PARAMETRO
    }

    public abstract static class TipoLA {
        public abstract String getNome();
        public abstract boolean eNumerico();
        public abstract boolean eEquivalente(TipoLA outro);

        public static final Primitivo INTEIRO = new Primitivo("inteiro");
        public static final Primitivo REAL = new Primitivo("real");
        public static final Primitivo LITERAL = new Primitivo("literal");
        public static final Primitivo LOGICO = new Primitivo("logico");
        public static final Primitivo TIPO_INDEFINIDO = new Primitivo("tipo_indefinido");
    }

    public static class Primitivo extends TipoLA {
        private final String nome;
        private Primitivo(String nome) { this.nome = nome; }
        @Override public String getNome() { return nome; }
        @Override public boolean eNumerico() { return this == INTEIRO || this == REAL; }
        @Override public boolean eEquivalente(TipoLA outro) {
            if (this == TIPO_INDEFINIDO || outro == TIPO_INDEFINIDO) return true;
            if (this.eNumerico() && outro.eNumerico()) return true;
            return this == outro;
        }
    }

    public static class Ponteiro extends TipoLA {
        public final TipoLA tipoApontado;
        public Ponteiro(TipoLA tipoApontado) { this.tipoApontado = tipoApontado; }
        public TipoLA getTipoApontado() { return tipoApontado; }
        @Override public String getNome() { return "^" + (tipoApontado != null ? tipoApontado.getNome() : "indefinido"); }
        @Override public boolean eNumerico() { return false; }
        @Override public boolean eEquivalente(TipoLA outro) {
            if (outro == TIPO_INDEFINIDO) return true;
            if (!(outro instanceof Ponteiro)) return false;
            Ponteiro outroPonteiro = (Ponteiro) outro;
            if (this.tipoApontado == TipoLA.TIPO_INDEFINIDO || outroPonteiro.tipoApontado == TipoLA.TIPO_INDEFINIDO) return true;
            return this.tipoApontado.eEquivalente(outroPonteiro.tipoApontado);
        }
    }

    public static class Registro extends TipoLA {
        public final String nomeOriginalDoTipo;
        public final Map<String, Simbolo> campos;
        public Registro(String nomeOriginal, Map<String, Simbolo> campos) {
            this.nomeOriginalDoTipo = nomeOriginal;
            this.campos = new LinkedHashMap<>(campos);
        }
        @Override public String getNome() { return nomeOriginalDoTipo != null ? nomeOriginalDoTipo : "registro_anonimo"; }
        @Override public boolean eNumerico() { return false; }
        @Override public boolean eEquivalente(TipoLA outro) {
            if (outro == TIPO_INDEFINIDO) return true;
            if (!(outro instanceof Registro)) return false;
            Registro outroReg = (Registro) outro;
            if (this.nomeOriginalDoTipo != null && outroReg.nomeOriginalDoTipo != null) {
                return this.nomeOriginalDoTipo.equals(outroReg.nomeOriginalDoTipo);
            }
            return false;
        }
    }

    public static class Simbolo {
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

    public static class TabelaDeSimbolos {
        private Deque<Map<String, Simbolo>> pilhaEscoposGlobal;
        private AnalisadorSemantico analisadorRef;

        public TabelaDeSimbolos(AnalisadorSemantico analisador) {
            this.pilhaEscoposGlobal = analisador.pilhaEscopos;
            this.analisadorRef = analisador;
        }

        public TipoLA getTipoDeVariavelDeclarada(NossaGramaticaParser.VariavelContext ctx) {
            return analisadorRef.resolverTipoNo(ctx.tipo());
        }

        public TipoLA getTipoDeSimbolo(String nome) {
            Simbolo s = analisadorRef.buscarSimbolo(nome);
            return (s != null) ? s.tipo : TipoLA.TIPO_INDEFINIDO;
        }

        public TipoLA getTipoDeIdentificador(NossaGramaticaParser.IdentificadorContext identCtx, boolean desreferenciadoPeloOperador) {
            return analisadorRef.getTipoDeIdentificadorContext(identCtx, desreferenciadoPeloOperador);
        }

        public TipoLA verificarTipoExpressao(NossaGramaticaParser.ExpressaoContext ctx) {
            return analisadorRef.verificarTipoExpressao(ctx);
        }

        public TipoLA verificarTipoExpAritmetica(NossaGramaticaParser.Exp_aritmeticaContext ctx) {
            return analisadorRef.verificarTipoExpAritmetica(ctx);
        }

        public TipoLA getTipoDeParametro(NossaGramaticaParser.ParametroContext paramCtx) {
            return analisadorRef.resolverTipoEstendido(paramCtx.tipo_estendido());
        }

        public TipoLA getTipoDeSimboloNoRegistro(String nomeTipoRegistro, String nomeCampo) {
            Simbolo tipoRegSimbolo = analisadorRef.buscarSimbolo(nomeTipoRegistro);
            if (tipoRegSimbolo != null && tipoRegSimbolo.tipo instanceof Registro) {
                Registro reg = (Registro) tipoRegSimbolo.tipo;
                if (reg.campos.containsKey(nomeCampo)) {
                    return reg.campos.get(nomeCampo).tipo;
                }
            }
            return TipoLA.TIPO_INDEFINIDO;
        }

        public Simbolo buscarSimboloGlobal(String nome) {
            if (!analisadorRef.pilhaEscopos.isEmpty()) {
                Map<String, AnalisadorSemantico.Simbolo> escopoGlobal;
                if (analisadorRef.pilhaEscopos.size() == 1) {
                    escopoGlobal = analisadorRef.pilhaEscopos.peek();
                } else {
                    escopoGlobal = analisadorRef.pilhaEscopos.getLast();
                }
                if (escopoGlobal.containsKey(nome)) {
                    return escopoGlobal.get(nome);
                }
            }
            return null;
        }
    }

    private final Deque<Map<String, Simbolo>> pilhaEscopos = new ArrayDeque<>();
    private TabelaDeSimbolos tabelaCompleta;
    private TipoLA tipoRetornoFuncaoAtual = null;
    private boolean dentroDeFuncaoOuProcedimento = false;
    private String nomeFuncaoProcedimentoAtual = null;
    private final List<String> erros = new ArrayList<>();

    public List<String> getErros() {
        List<String> distinctErrors = new ArrayList<>(new LinkedHashSet<>(erros));
        Collections.sort(distinctErrors, (e1, e2) -> {
            try {
                int linha1 = Integer.parseInt(e1.substring(e1.indexOf(" ") + 1, e1.indexOf(":")));
                int linha2 = Integer.parseInt(e2.substring(e2.indexOf(" ") + 1, e2.indexOf(":")));
                if (linha1 != linha2) {
                    return Integer.compare(linha1, linha2);
                }
                return e1.compareTo(e2);
            } catch (Exception ex) {
                return e1.compareTo(e2);
            }
        });
        return distinctErrors;
    }

    private void adicionarErro(Token token, String mensagem) {
        if (token == null) return;
        adicionarErro(token.getLine(), mensagem);
    }

    private void adicionarErro(int linha, String mensagem) {
        erros.add("Linha " + linha + ": " + mensagem);
    }

    private void entrarEscopo() { pilhaEscopos.push(new LinkedHashMap<>()); }
    private void sairEscopo() { pilhaEscopos.pop(); }

    private void adicionarSimbolo(String nome, TipoLA tipo, CategoriaSimbolo categoria, Token tokenNome) {
        Map<String, Simbolo> escopoAtual = pilhaEscopos.peek();
        if (escopoAtual.containsKey(nome)) {
            adicionarErro(tokenNome, "identificador " + nome + " ja declarado anteriormente");
        } else {
            escopoAtual.put(nome, new Simbolo(nome, tipo, categoria, tokenNome.getLine()));
        }
    }

    private void adicionarSimbolo(Simbolo simbolo) {
        Map<String, Simbolo> escopoAtual = pilhaEscopos.peek();
        if (escopoAtual.containsKey(simbolo.nome)) {
            adicionarErro(simbolo.linhaDeclaracao, "identificador " + simbolo.nome + " ja declarado anteriormente");
        } else {
            escopoAtual.put(simbolo.nome, simbolo);
        }
    }

    public Simbolo buscarSimbolo(String nome) {
        for (Map<String, Simbolo> escopo : pilhaEscopos) {
            if (escopo.containsKey(nome)) return escopo.get(nome);
        }
        return null;
    }

    @Override
    public Void visitPrograma(NossaGramaticaParser.ProgramaContext ctx) {
        entrarEscopo();
        this.tabelaCompleta = new TabelaDeSimbolos(this);

        visitDeclaracoes(ctx.declaracoes());
        if (ctx.corpo() != null) {
            visitCorpo(ctx.corpo());
        }
        return null;
    }

    public TabelaDeSimbolos getTabelaDeSimbolosCompleta() {
        return this.tabelaCompleta;
    }


    @Override
    public Void visitCorpo(NossaGramaticaParser.CorpoContext ctx) {
        for(NossaGramaticaParser.Declaracao_localContext declCtx : ctx.declaracao_local()) {
            visitDeclaracao_local(declCtx);
        }
        for(NossaGramaticaParser.CmdContext cmdCtx : ctx.cmd()) {
            visit(cmdCtx);
        }
        return null;
    }

    public TipoLA resolverTipoNo(NossaGramaticaParser.TipoContext tipoCtx) {
        if (tipoCtx.registro() != null) {
            entrarEscopo();
            Map<String, Simbolo> campos = new LinkedHashMap<>();
            for (NossaGramaticaParser.VariavelContext varCampoCtx : tipoCtx.registro().variavel()) {
                TipoLA tipoCampo = resolverTipoNo(varCampoCtx.tipo());
                for (NossaGramaticaParser.IdentificadorContext identCampoCtx : varCampoCtx.identificador()) {
                    String nomeCampo = identCampoCtx.IDENT(0).getText();
                    Token tokenNomeCampo = identCampoCtx.IDENT(0).getSymbol();
                    if (campos.containsKey(nomeCampo)) {
                        adicionarErro(tokenNomeCampo, "identificador " + nomeCampo + " ja declarado neste registro");
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
        adicionarErro(tipoCtx.getStart(), "tipo desconhecido ou mal formado: " + tipoCtx.getText());
        return TipoLA.TIPO_INDEFINIDO;
    }

    public TipoLA resolverTipoEstendido(NossaGramaticaParser.Tipo_estendidoContext teCtx) {
        NossaGramaticaParser.Tipo_basico_identContext tbiCtx = teCtx.tipo_basico_ident();
        TipoLA tipoBase;

        if (tbiCtx.tipo_basico() != null) {
            String nomeTipoBase = tbiCtx.tipo_basico().getText();
            switch (nomeTipoBase) {
                case "literal": tipoBase = TipoLA.LITERAL; break;
                case "inteiro": tipoBase = TipoLA.INTEIRO; break;
                case "real":    tipoBase = TipoLA.REAL;    break;
                case "logico":  tipoBase = TipoLA.LOGICO;  break;
                default:
                    adicionarErro(tbiCtx.tipo_basico().getStart(), "tipo base desconhecido " + nomeTipoBase);
                    tipoBase = TipoLA.TIPO_INDEFINIDO; break;
            }
        } else {
            String nomeIdentTipo = tbiCtx.IDENT().getText();
            Simbolo simboloTipo = buscarSimbolo(nomeIdentTipo);
            if (simboloTipo == null) {
                adicionarErro(tbiCtx.IDENT().getSymbol(), "tipo " + nomeIdentTipo + " nao declarado");
                tipoBase = TipoLA.TIPO_INDEFINIDO;
            } else if (simboloTipo.categoria != CategoriaSimbolo.TIPO) {
                adicionarErro(tbiCtx.IDENT().getSymbol(), "identificador " + nomeIdentTipo + " nao e um tipo");
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

            if (tipoDeclaradoConst != TipoLA.TIPO_INDEFINIDO && tipoValor != TipoLA.TIPO_INDEFINIDO) {
                if (!saoCompativeisAtribuicao(tipoDeclaradoConst, tipoValor)) {
                    adicionarErro(tokenNomeConst, "atribuicao nao compativel para " + nomeConst);
                }
            }
            adicionarSimbolo(nomeConst, tipoDeclaradoConst, CategoriaSimbolo.CONSTANTE, tokenNomeConst);

        } else if (ctx.TIPO() != null) {
            String nomeNovoTipo = ctx.IDENT().getText();
            Token tokenNomeNovoTipo = ctx.IDENT().getSymbol();
            TipoLA tipoDefinido = resolverTipoNo(ctx.tipo());

            if (tipoDefinido instanceof Registro) {
                tipoDefinido = new Registro(nomeNovoTipo, ((Registro) tipoDefinido).campos);
            }

            Simbolo simboloDoTipo = new Simbolo(nomeNovoTipo, tipoDefinido, CategoriaSimbolo.TIPO, tokenNomeNovoTipo.getLine());
            if (tipoDefinido instanceof Registro) {
                simboloDoTipo.camposDoTipoRegistro = ((Registro) tipoDefinido).campos;
            }
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
        adicionarErro(ctx.getStart(), "tipo basico desconhecido: " + nome);
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
            nomeSubRotina = ctx.IDENT().getText(); tokenNome = ctx.IDENT().getSymbol();
            categoria = CategoriaSimbolo.PROCEDIMENTO;
        } else {
            nomeSubRotina = ctx.IDENT().getText(); tokenNome = ctx.IDENT().getSymbol();
            categoria = CategoriaSimbolo.FUNCAO;
            tipoRetorno = resolverTipoEstendido(ctx.tipo_estendido());
        }

        Simbolo simboloSubRotina = new Simbolo(nomeSubRotina, tipoRetorno, categoria, tokenNome.getLine());
        if (ctx.parametros() != null) {
            for (NossaGramaticaParser.ParametroContext paramCtx : ctx.parametros().parametro()) {
                TipoLA tipoDoParam = resolverTipoEstendido(paramCtx.tipo_estendido());
                for (int i = 0; i < paramCtx.identificador().size(); i++) {
                    simboloSubRotina.tiposParametros.add(tipoDoParam);
                }
            }
        }
        adicionarSimbolo(simboloSubRotina);

        entrarEscopo();
        boolean oldDentroDeFuncaoOuProcedimento = dentroDeFuncaoOuProcedimento;
        TipoLA oldTipoRetornoFuncaoAtual = tipoRetornoFuncaoAtual;
        String oldNomeFuncaoProcedimentoAtual = nomeFuncaoProcedimentoAtual;

        dentroDeFuncaoOuProcedimento = true;
        nomeFuncaoProcedimentoAtual = nomeSubRotina;
        if (categoria == CategoriaSimbolo.FUNCAO) {
            this.tipoRetornoFuncaoAtual = tipoRetorno;
        } else {
            this.tipoRetornoFuncaoAtual = null;
        }

        if (ctx.parametros() != null) visitParametros(ctx.parametros());
        ctx.declaracao_local().forEach(this::visitDeclaracao_local);
        ctx.cmd().forEach(this::visit);

        this.tipoRetornoFuncaoAtual = oldTipoRetornoFuncaoAtual;
        this.dentroDeFuncaoOuProcedimento = oldDentroDeFuncaoOuProcedimento;
        this.nomeFuncaoProcedimentoAtual = oldNomeFuncaoProcedimentoAtual;
        sairEscopo();
        return null;
    }

    @Override
    public Void visitParametros(NossaGramaticaParser.ParametrosContext ctx) {
        for (NossaGramaticaParser.ParametroContext paramCtx : ctx.parametro()) {
            visitParametro(paramCtx);
        }
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

    private boolean saoCompativeisParametro(TipoLA parametroDeclarado, TipoLA tipoArgumento) {
        if (parametroDeclarado == TipoLA.TIPO_INDEFINIDO || tipoArgumento == TipoLA.TIPO_INDEFINIDO) return true;
        if (parametroDeclarado instanceof Ponteiro) {
            return (tipoArgumento instanceof Ponteiro) &&
                    ((Ponteiro) parametroDeclarado).getTipoApontado().eEquivalente(((Ponteiro) tipoArgumento).getTipoApontado());
        }
        if (parametroDeclarado instanceof Registro) {
            return (tipoArgumento instanceof Registro) && parametroDeclarado.eEquivalente(tipoArgumento);
        }
        if (parametroDeclarado.eNumerico() && tipoArgumento.eNumerico()) return true;
        return parametroDeclarado == tipoArgumento;
    }

    private boolean saoCompativeisAtribuicao(TipoLA tipoEsquerdo, TipoLA tipoDireito) {
        if (tipoEsquerdo == TipoLA.TIPO_INDEFINIDO || tipoDireito == TipoLA.TIPO_INDEFINIDO) return false;

        if (tipoEsquerdo instanceof Ponteiro) {
            return (tipoDireito instanceof Ponteiro) &&
                    ((Ponteiro) tipoEsquerdo).getTipoApontado().eEquivalente(((Ponteiro) tipoDireito).getTipoApontado());
        }

        if (tipoEsquerdo.eNumerico() && tipoDireito.eNumerico()) {
            if (tipoEsquerdo == TipoLA.REAL && tipoDireito == TipoLA.INTEIRO) return true;
            return tipoEsquerdo == tipoDireito;
        }

        if (tipoEsquerdo == TipoLA.LITERAL && tipoDireito == TipoLA.LITERAL) return true;
        if (tipoEsquerdo == TipoLA.LOGICO && tipoDireito == TipoLA.LOGICO) return true;
        if (tipoEsquerdo instanceof Registro && tipoDireito instanceof Registro) {
            return tipoEsquerdo.eEquivalente(tipoDireito);
        }
        return false;
    }

    public TipoLA verificarTipoExpressao(NossaGramaticaParser.ExpressaoContext ctx) {
        if (ctx == null) return TipoLA.TIPO_INDEFINIDO;
        TipoLA tipoResultante = verificarTipoTermoLogico(ctx.termo_logico(0));
        for (int i = 0; i < ctx.op_logico_1().size(); i++) {
            TipoLA tipoDireita = verificarTipoTermoLogico(ctx.termo_logico(i + 1));
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else if (!(tipoResultante == TipoLA.LOGICO && tipoDireita == TipoLA.LOGICO)) {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else {
                tipoResultante = TipoLA.LOGICO;
            }
        }
        return tipoResultante;
    }

    private TipoLA verificarTipoTermoLogico(NossaGramaticaParser.Termo_logicoContext ctx) {
        TipoLA tipoResultante = verificarTipoFatorLogico(ctx.fator_logico(0));
        for (int i = 0; i < ctx.op_logico_2().size(); i++) {
            TipoLA tipoDireita = verificarTipoFatorLogico(ctx.fator_logico(i + 1));
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else if (!(tipoResultante == TipoLA.LOGICO && tipoDireita == TipoLA.LOGICO)) {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else {
                tipoResultante = TipoLA.LOGICO;
            }
        }
        return tipoResultante;
    }

    private TipoLA verificarTipoFatorLogico(NossaGramaticaParser.Fator_logicoContext ctx) {
        TipoLA tipoParcela = verificarTipoParcelaLogica(ctx.parcela_logica());
        if (ctx.NAO_LOGICO() != null) {
            if (tipoParcela == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;
            if (tipoParcela != TipoLA.LOGICO) {
                return TipoLA.TIPO_INDEFINIDO;
            }
            return TipoLA.LOGICO;
        }
        return tipoParcela;
    }

    private TipoLA verificarTipoParcelaLogica(NossaGramaticaParser.Parcela_logicaContext ctx) {
        if (ctx.VERDADEIRO() != null || ctx.FALSO() != null) return TipoLA.LOGICO;
        if (ctx.LPAREN() != null) return verificarTipoExpressao(ctx.expressao());
        return verificarTipoExpRelacional(ctx.exp_relacional());
    }

    private TipoLA verificarTipoExpRelacional(NossaGramaticaParser.Exp_relacionalContext ctx) {
        TipoLA tipoEsq = verificarTipoExpAritmetica(ctx.exp_aritmetica(0));
        if (ctx.op_relacional() != null) {
            TipoLA tipoDir = verificarTipoExpAritmetica(ctx.exp_aritmetica(1));
            if (tipoEsq == TipoLA.TIPO_INDEFINIDO || tipoDir == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;

            boolean compatible = false;
            if (tipoEsq.eNumerico() && tipoDir.eNumerico()) compatible = true;
            else if (tipoEsq == TipoLA.LITERAL && tipoDir == TipoLA.LITERAL) compatible = true;
            else if ((ctx.op_relacional().IGUAL() != null || ctx.op_relacional().DIFERENTE() != null)) {
                if (tipoEsq == TipoLA.LOGICO && tipoDir == TipoLA.LOGICO) compatible = true;
                else if (tipoEsq instanceof Ponteiro && tipoDir instanceof Ponteiro) {
                    if (((Ponteiro)tipoEsq).getTipoApontado().eEquivalente(((Ponteiro)tipoDir).getTipoApontado())) {
                        compatible = true;
                    }
                }
            }

            if (!compatible) return TipoLA.TIPO_INDEFINIDO;
            return TipoLA.LOGICO;
        }
        return tipoEsq;
    }

    public TipoLA verificarTipoExpAritmetica(NossaGramaticaParser.Exp_aritmeticaContext ctx) {
        TipoLA tipoResultante = verificarTipoTermo(ctx.termo(0));
        for (int i = 0; i < ctx.op1().size(); i++) {
            TipoLA tipoDireita = verificarTipoTermo(ctx.termo(i + 1));
            Token opToken = ctx.op1(i).getStart();
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else if (opToken.getText().equals("+") && tipoResultante == TipoLA.LITERAL && tipoDireita == TipoLA.LITERAL) {
                tipoResultante = TipoLA.LITERAL;
            } else if (tipoResultante.eNumerico() && tipoDireita.eNumerico()) {
                tipoResultante = (tipoResultante == TipoLA.REAL || tipoDireita == TipoLA.REAL) ? TipoLA.REAL : TipoLA.INTEIRO;
            } else {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            }
        }
        return tipoResultante;
    }

    private TipoLA verificarTipoTermo(NossaGramaticaParser.TermoContext ctx) {
        TipoLA tipoResultante = verificarTipoFator(ctx.fator(0));
        for (int i = 0; i < ctx.op2().size(); i++) {
            TipoLA tipoDireita = verificarTipoFator(ctx.fator(i + 1));
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else if (tipoResultante.eNumerico() && tipoDireita.eNumerico()) {
                if (ctx.op2(i).DIVISAO() != null) {
                    tipoResultante = TipoLA.REAL;
                } else {
                    tipoResultante = (tipoResultante == TipoLA.REAL || tipoDireita == TipoLA.REAL) ? TipoLA.REAL : TipoLA.INTEIRO;
                }
            } else {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            }
        }
        return tipoResultante;
    }

    private TipoLA verificarTipoFator(NossaGramaticaParser.FatorContext ctx) {
        TipoLA tipoResultante = verificarTipoParcela(ctx.parcela(0));
        for (int i = 0; i < ctx.op3().size(); i++) {
            TipoLA tipoDireita = verificarTipoParcela(ctx.parcela(i + 1));
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else if (tipoResultante == TipoLA.INTEIRO && tipoDireita == TipoLA.INTEIRO) {
                tipoResultante = TipoLA.INTEIRO;
            } else {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            }
        }
        return tipoResultante;
    }

    private TipoLA verificarTipoParcela(NossaGramaticaParser.ParcelaContext ctx) {
        TipoLA tipoBase;
        if (ctx.parcela_unario() != null) {
            tipoBase = verificarTipoParcelaUnario(ctx.parcela_unario());
        } else {
            tipoBase = verificarTipoParcelaNaoUnario(ctx.parcela_nao_unario());
        }

        if (ctx.op_unario() != null && ctx.op_unario().MENOS() != null) {
            if (tipoBase == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;
            if (!tipoBase.eNumerico()) {
                return TipoLA.TIPO_INDEFINIDO;
            }
        }
        return tipoBase;
    }

    private TipoLA verificarTipoParcelaUnario(NossaGramaticaParser.Parcela_unarioContext ctx) {
        if (ctx.identificador() != null) {
            return getTipoDeIdentificadorContext(ctx.identificador(), ctx.PONTEIRO() != null);
        }
        if (ctx.IDENT() != null) {
            String nomeFunc = ctx.IDENT().getText();
            Simbolo funcSimbolo = buscarSimbolo(nomeFunc);

            if (funcSimbolo == null) {
                adicionarErro(ctx.IDENT().getSymbol(), "identificador " + nomeFunc + " nao declarado");
                if(ctx.expressao() != null) ctx.expressao().forEach(this::verificarTipoExpressao);
                return TipoLA.TIPO_INDEFINIDO;
            }
            if (funcSimbolo.categoria != CategoriaSimbolo.FUNCAO) {
                adicionarErro(ctx.IDENT().getSymbol(), "identificador " + nomeFunc + " nao e uma funcao");
                if(ctx.expressao() != null) ctx.expressao().forEach(this::verificarTipoExpressao);
                return TipoLA.TIPO_INDEFINIDO;
            }

            List<NossaGramaticaParser.ExpressaoContext> argsCtx = ctx.expressao() != null ? ctx.expressao() : Collections.emptyList();
            List<TipoLA> tiposArgs = new ArrayList<>();
            argsCtx.forEach(argExpr -> tiposArgs.add(verificarTipoExpressao(argExpr)));

            if (argsCtx.size() != funcSimbolo.tiposParametros.size()) {
                adicionarErro(ctx.IDENT().getSymbol(), "incompatibilidade de parametros na chamada de " + nomeFunc);
            } else {
                for (int i = 0; i < funcSimbolo.tiposParametros.size(); i++) {
                    if (!saoCompativeisParametro(funcSimbolo.tiposParametros.get(i), tiposArgs.get(i))) {
                        adicionarErro(ctx.IDENT().getSymbol(), "incompatibilidade de parametros na chamada de " + nomeFunc);
                        break;
                    }
                }
            }
            return funcSimbolo.tipo;
        }
        if (ctx.NUM_INT() != null) return TipoLA.INTEIRO;
        if (ctx.NUM_REAL() != null) return TipoLA.REAL;
        if (ctx.LPAREN() != null && ctx.expressao() != null && !ctx.expressao().isEmpty()) return verificarTipoExpressao(ctx.expressao(0));

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

    public TipoLA getTipoDeIdentificadorContext(NossaGramaticaParser.IdentificadorContext identCtx, boolean desreferenciadoPeloOperador) {
        String nomeBase = identCtx.IDENT(0).getText();
        Token tokenBase = identCtx.IDENT(0).getSymbol();
        Simbolo simbolo = buscarSimbolo(nomeBase);

        String identifierPathForError = identCtx.IDENT(0).getText();
        for(int k=1; k < identCtx.IDENT().size(); k++) {
            identifierPathForError += "." + identCtx.IDENT(k).getText();
        }
        if (desreferenciadoPeloOperador) {
            identifierPathForError = "^" + identifierPathForError;
        }

        if (simbolo == null) {
            if (identCtx.IDENT().size() > 1) {
                adicionarErro(tokenBase, "identificador " + identifierPathForError + " nao declarado");
            } else {
                adicionarErro(tokenBase, "identificador " + nomeBase + " nao declarado");
            }
            return TipoLA.TIPO_INDEFINIDO;
        }

        if (simbolo.categoria == CategoriaSimbolo.TIPO || simbolo.categoria == CategoriaSimbolo.PROCEDIMENTO) {
            boolean isPartOfCall = (identCtx.getParent() instanceof NossaGramaticaParser.Parcela_unarioContext &&
                    ((NossaGramaticaParser.Parcela_unarioContext)identCtx.getParent()).LPAREN() != null) ||
                    (identCtx.getParent() instanceof NossaGramaticaParser.CmdChamadaContext);
            if(!isPartOfCall) {
                adicionarErro(tokenBase, "identificador " + nomeBase + " e um " + simbolo.categoria.toString().toLowerCase() + " e nao pode ser usado neste contexto");
                return TipoLA.TIPO_INDEFINIDO;
            }
        }

        TipoLA tipoAtual = simbolo.tipo;

        if (desreferenciadoPeloOperador) {
            if (tipoAtual == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;
            if (!(tipoAtual instanceof Ponteiro)) {
                adicionarErro(identCtx.getStart(), "tentativa de desreferenciar um nao-ponteiro " + identifierPathForError);
                return TipoLA.TIPO_INDEFINIDO;
            }
            tipoAtual = ((Ponteiro) tipoAtual).getTipoApontado();
        }

        for (int i = 1; i < identCtx.IDENT().size(); i++) {
            if (tipoAtual == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;

            if (!(tipoAtual instanceof Registro)) {
                String prefix = identCtx.IDENT(0).getText();
                for(int k=1; k < i; k++) { prefix += "." + identCtx.IDENT(k).getText(); }
                adicionarErro(identCtx.IDENT(i-1).getSymbol(), "identificador " + prefix + " nao e um registro");
                return TipoLA.TIPO_INDEFINIDO;
            }
            Registro rTipo = (Registro) tipoAtual;
            String nomeCampo = identCtx.IDENT(i).getText();

            Simbolo campoSimbolo = (rTipo.campos != null) ? rTipo.campos.get(nomeCampo) : null;
            if (campoSimbolo == null) {
                String idCompostoErrado = identCtx.IDENT(0).getText();
                for(int k=1; k<=i; k++) { idCompostoErrado += "." + identCtx.IDENT(k).getText(); }
                adicionarErro(identCtx.IDENT(i).getSymbol(), "identificador " + idCompostoErrado + " nao declarado");
                return TipoLA.TIPO_INDEFINIDO;
            }
            tipoAtual = campoSimbolo.tipo;
        }

        if (identCtx.dimensao() != null && !identCtx.dimensao().exp_aritmetica().isEmpty()) {
            if (tipoAtual instanceof Ponteiro && !desreferenciadoPeloOperador){
                adicionarErro(identCtx.getStart(), "acesso a ponteiro como array nao permitido diretamente, use ^ponteiro para desreferenciar se for o caso");
                return TipoLA.TIPO_INDEFINIDO;
            }
            for(NossaGramaticaParser.Exp_aritmeticaContext expDim : identCtx.dimensao().exp_aritmetica()){
                TipoLA tipoIndice = verificarTipoExpAritmetica(expDim);
                if(tipoIndice != TipoLA.TIPO_INDEFINIDO && tipoIndice != TipoLA.INTEIRO){
                    adicionarErro(expDim.getStart(), "indice de array deve ser inteiro");
                }
            }
        }
        return tipoAtual;
    }

    @Override
    public Void visitCmdLeia(NossaGramaticaParser.CmdLeiaContext ctx) {
        for (NossaGramaticaParser.IdentificadorContext identCtx : ctx.identificador()) {
            boolean ehPonteiro = ctx.PONTEIRO(ctx.identificador().indexOf(identCtx)) != null;
            TipoLA tipoIdent = getTipoDeIdentificadorContext(identCtx, ehPonteiro);
            if(tipoIdent == TipoLA.TIPO_INDEFINIDO) continue;

            String nomeBase = identCtx.IDENT(0).getText();
            if (identCtx.IDENT().size() == 1 && identCtx.dimensao().exp_aritmetica().isEmpty() && !ehPonteiro) {
                Simbolo s = buscarSimbolo(nomeBase);
                if (s != null && s.categoria == CategoriaSimbolo.CONSTANTE) {
                    adicionarErro(identCtx.IDENT(0).getSymbol(), "nao e possivel ler em constante " + s.nome);
                }
            }
        }
        return null;
    }

    @Override
    public Void visitCmdEscreva(NossaGramaticaParser.CmdEscrevaContext ctx) {
        ctx.expressao().forEach(this::verificarTipoExpressao);
        return null;
    }

    @Override
    public Void visitCmdSe(NossaGramaticaParser.CmdSeContext ctx) {
        TipoLA tipoCond = verificarTipoExpressao(ctx.expressao());
        if (tipoCond != TipoLA.TIPO_INDEFINIDO && tipoCond != TipoLA.LOGICO) {
            adicionarErro(ctx.expressao().getStart(), "expressao do comando 'se' deve ser logica");
        }
        ctx.cmd().forEach(this::visit);
        if (ctx.SENAO() != null && ctx.cmd().size() == ctx.parent.getChildCount() - 4) {
        }
        return null;
    }

    @Override
    public Void visitCmdAtribuicao(NossaGramaticaParser.CmdAtribuicaoContext ctx) {
        boolean lhsDesreferenciado = ctx.PONTEIRO() != null;
        TipoLA tipoLHS = getTipoDeIdentificadorContext(ctx.identificador(), lhsDesreferenciado);
        TipoLA tipoRHS = verificarTipoExpressao(ctx.expressao());

        String lhsText = (lhsDesreferenciado ? "^" : "") + ctx.identificador().getText();

        if (!lhsDesreferenciado && ctx.identificador().IDENT().size() == 1 && ctx.identificador().dimensao().exp_aritmetica().isEmpty()) {
            Simbolo s = buscarSimbolo(ctx.identificador().IDENT(0).getText());
            if (s != null && s.categoria == CategoriaSimbolo.CONSTANTE) {
                adicionarErro(ctx.ATRIBUICAO().getSymbol(), "atribuicao nao compativel para " + lhsText);
                return null;
            }
        }

        if (!saoCompativeisAtribuicao(tipoLHS, tipoRHS)) {
            if (tipoLHS != TipoLA.TIPO_INDEFINIDO) {
                adicionarErro(ctx.ATRIBUICAO().getSymbol(), "atribuicao nao compativel para " + lhsText);
            }
        }
        return null;
    }

    @Override
    public Void visitCmdChamada(NossaGramaticaParser.CmdChamadaContext ctx) {
        String nomeSub = ctx.IDENT().getText();
        Simbolo simboloSub = buscarSimbolo(nomeSub);

        if (simboloSub == null) {
            adicionarErro(ctx.IDENT().getSymbol(), "identificador " + nomeSub + " nao declarado");
            return null;
        }
        if (simboloSub.categoria != CategoriaSimbolo.PROCEDIMENTO && simboloSub.categoria != CategoriaSimbolo.FUNCAO) {
            adicionarErro(ctx.IDENT().getSymbol(), "identificador " + nomeSub + " nao e um procedimento ou funcao");
            return null;
        }

        List<NossaGramaticaParser.ExpressaoContext> argsCtx = ctx.expressao() != null ? ctx.expressao() : Collections.emptyList();
        List<TipoLA> tiposArgs = new ArrayList<>();
        argsCtx.forEach(argExpr -> tiposArgs.add(verificarTipoExpressao(argExpr)));

        List<TipoLA> tiposParamsEsperados = simboloSub.tiposParametros != null ? simboloSub.tiposParametros : Collections.emptyList();

        if (argsCtx.size() != tiposParamsEsperados.size()) {
            adicionarErro(ctx.IDENT().getSymbol(), "incompatibilidade de parametros na chamada de " + nomeSub);
        } else {
            for (int i = 0; i < tiposParamsEsperados.size(); i++) {
                if (!saoCompativeisParametro(tiposParamsEsperados.get(i), tiposArgs.get(i))) {
                    adicionarErro(ctx.IDENT().getSymbol(), "incompatibilidade de parametros na chamada de " + nomeSub);
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public Void visitCmdRetorne(NossaGramaticaParser.CmdRetorneContext ctx) {
        if (!dentroDeFuncaoOuProcedimento) {
            adicionarErro(ctx.RETORNE().getSymbol(), "comando retorne nao permitido nesse escopo");
            return null;
        }
        if (tipoRetornoFuncaoAtual == null) {
            adicionarErro(ctx.RETORNE().getSymbol(), "comando retorne nao permitido nesse escopo");
            return null;
        }

        TipoLA tipoRetornado = verificarTipoExpressao(ctx.expressao());
        if (!saoCompativeisAtribuicao(tipoRetornoFuncaoAtual, tipoRetornado)) {
            if (tipoRetornoFuncaoAtual != TipoLA.TIPO_INDEFINIDO && tipoRetornado != TipoLA.TIPO_INDEFINIDO) {
                adicionarErro(ctx.expressao().getStart(), "tipo de retorno incompativel para funcao " + nomeFuncaoProcedimentoAtual);
            }
        }
        return null;
    }

    @Override public Void visitCmdPara(NossaGramaticaParser.CmdParaContext ctx) {
        String nomeVar = ctx.IDENT().getText(); Token tokenVar = ctx.IDENT().getSymbol();
        Simbolo simboloVar = buscarSimbolo(nomeVar);
        if (simboloVar == null) adicionarErro(tokenVar, "identificador " + nomeVar + " nao declarado");
        else if (simboloVar.categoria != CategoriaSimbolo.VARIAVEL) adicionarErro(tokenVar, "identificador " + nomeVar + " em comando 'para' deve ser uma variavel");
        else if (simboloVar.tipo != TipoLA.TIPO_INDEFINIDO && !simboloVar.tipo.eNumerico()) adicionarErro(tokenVar, "variavel de controle " + nomeVar + " em comando 'para' deve ser numerica");

        TipoLA tIni = verificarTipoExpAritmetica(ctx.exp_aritmetica(0));
        TipoLA tFim = verificarTipoExpAritmetica(ctx.exp_aritmetica(1));
        if (tIni != TipoLA.TIPO_INDEFINIDO && !tIni.eNumerico()) adicionarErro(ctx.exp_aritmetica(0).getStart(), "expressao inicial do comando 'para' deve ser numerica");
        if (tFim != TipoLA.TIPO_INDEFINIDO && !tFim.eNumerico()) adicionarErro(ctx.exp_aritmetica(1).getStart(), "expressao final do comando 'para' deve ser numerica");

        ctx.cmd().forEach(this::visit);
        return null;
    }

    @Override public Void visitCmdEnquanto(NossaGramaticaParser.CmdEnquantoContext ctx) {
        TipoLA tipoCond = verificarTipoExpressao(ctx.expressao());
        if (tipoCond != TipoLA.TIPO_INDEFINIDO && tipoCond != TipoLA.LOGICO) {
            adicionarErro(ctx.expressao().getStart(), "expressao do comando 'enquanto' deve ser logica");
        }
        ctx.cmd().forEach(this::visit);
        return null;
    }

    @Override public Void visitCmdFaca(NossaGramaticaParser.CmdFacaContext ctx) {
        ctx.cmd().forEach(this::visit);
        TipoLA tipoCond = verificarTipoExpressao(ctx.expressao());
        if (tipoCond != TipoLA.TIPO_INDEFINIDO && tipoCond != TipoLA.LOGICO) {
            adicionarErro(ctx.expressao().getStart(), "expressao do comando 'faca..ate' deve ser logica");
        }
        return null;
    }

    @Override public Void visitCmdCaso(NossaGramaticaParser.CmdCasoContext ctx) {
        TipoLA tipoExp = verificarTipoExpAritmetica(ctx.exp_aritmetica());
        if (tipoExp != TipoLA.TIPO_INDEFINIDO && tipoExp != TipoLA.INTEIRO) {
            adicionarErro(ctx.exp_aritmetica().getStart(), "expressao do comando 'caso' deve ser inteira");
        }
        ctx.selecao().item_selecao().forEach(item -> item.cmd().forEach(this::visit));
        if(ctx.cmd() != null) ctx.cmd().forEach(this::visit);
        return null;
    }
}