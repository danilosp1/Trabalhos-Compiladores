package main.java.br.ufscar.dc.compiladores;

import java.util.*;
import org.antlr.v4.runtime.Token;
import br.ufscar.dc.compiladores.NossaGramaticaParser;
import br.ufscar.dc.compiladores.NossaGramaticaBaseVisitor;

public class AnalisadorSemantico extends NossaGramaticaBaseVisitor<Void> {

    // Categorias de símbolos para a tabela de símbolos
    private enum CategoriaSimbolo {
        VARIAVEL, CONSTANTE, TIPO, FUNCAO, PROCEDIMENTO, PARAMETRO
    }

    // Classe base para tipos da linguagem LA
    private abstract static class TipoLA {
        public abstract String getNome();
        public abstract boolean eNumerico(); // Se é inteiro ou real
        public abstract boolean eEquivalente(TipoLA outro); // Para compatibilidade geral

        // Tipos primitivos estáticos
        public static final Primitivo INTEIRO = new Primitivo("inteiro");
        public static final Primitivo REAL = new Primitivo("real");
        public static final Primitivo LITERAL = new Primitivo("literal");
        public static final Primitivo LOGICO = new Primitivo("logico");
        public static final Primitivo TIPO_INDEFINIDO = new Primitivo("tipo_indefinido"); // Para erros
    }

    // Representa tipos primitivos
    private static class Primitivo extends TipoLA {
        private final String nome;
        private Primitivo(String nome) { this.nome = nome; }
        @Override public String getNome() { return nome; }
        @Override public boolean eNumerico() { return this == INTEIRO || this == REAL; }
        @Override public boolean eEquivalente(TipoLA outro) {
            if (this == TIPO_INDEFINIDO || outro == TIPO_INDEFINIDO) return true; // Evita cascata de erros
            if (this.eNumerico() && outro.eNumerico()) return true; // int e real equivalentes em certos contextos
            return this == outro; // Ex: literal == literal
        }
    }

    // Representa tipos ponteiro
    private static class Ponteiro extends TipoLA {
        private final TipoLA tipoApontado; // Tipo base do ponteiro
        public Ponteiro(TipoLA tipoApontado) { this.tipoApontado = tipoApontado; }
        public TipoLA getTipoApontado() { return tipoApontado; }
        @Override public String getNome() { return "^" + (tipoApontado != null ? tipoApontado.getNome() : "indefinido"); }
        @Override public boolean eNumerico() { return false; }
        @Override public boolean eEquivalente(TipoLA outro) {
            if (outro == TIPO_INDEFINIDO) return true;
            if (!(outro instanceof Ponteiro)) return false;
            Ponteiro outroPonteiro = (Ponteiro) outro;
            // Ponteiros equivalentes se apontam para tipos base equivalentes
            if (this.tipoApontado == TipoLA.TIPO_INDEFINIDO || outroPonteiro.tipoApontado == TipoLA.TIPO_INDEFINIDO) return true;
            return this.tipoApontado.eEquivalente(outroPonteiro.tipoApontado);
        }
    }

    // Representa tipos registro
    private static class Registro extends TipoLA {
        public final String nomeOriginalDoTipo; // Nome dado na declaração "tipo Nome : registro..."
        public final Map<String, Simbolo> campos;
        public Registro(String nomeOriginal, Map<String, Simbolo> campos) {
            this.nomeOriginalDoTipo = nomeOriginal;
            this.campos = new LinkedHashMap<>(campos); // Preserva ordem dos campos
        }
        @Override public String getNome() { return nomeOriginalDoTipo != null ? nomeOriginalDoTipo : "registro_anonimo"; }
        @Override public boolean eNumerico() { return false; }
        @Override public boolean eEquivalente(TipoLA outro) {
            if (outro == TIPO_INDEFINIDO) return true;
            if (!(outro instanceof Registro)) return false;
            Registro outroReg = (Registro) outro;
            // Equivalência baseada no nome original do tipo registro
            if (this.nomeOriginalDoTipo != null && outroReg.nomeOriginalDoTipo != null) {
                return this.nomeOriginalDoTipo.equals(outroReg.nomeOriginalDoTipo);
            }
            return false; // Registros anônimos ou com nomes diferentes não são equivalentes
        }
    }

    // Representa uma entrada na tabela de símbolos
    private static class Simbolo {
        String nome;
        TipoLA tipo;
        CategoriaSimbolo categoria;
        int linhaDeclaracao;
        List<TipoLA> tiposParametros; // Para funções/procedimentos
        Map<String, Simbolo> camposDoTipoRegistro; // Para TIPO registro

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

    private final Deque<Map<String, Simbolo>> pilhaEscopos = new ArrayDeque<>(); // Gerencia escopos aninhados
    private TipoLA tipoRetornoFuncaoAtual = null; // Para verificar 'retorne' em funções
    private boolean dentroDeFuncaoOuProcedimento = false; // Para verificar uso de 'retorne'
    private String nomeFuncaoProcedimentoAtual = null; // Para mensagens de erro mais claras

    private final List<String> erros = new ArrayList<>(); // Armazena erros semânticos

    // Retorna lista de erros, ordenada e sem duplicatas
    public List<String> getErros() {
        List<String> distinctErrors = new ArrayList<>(new LinkedHashSet<>(erros));
        // Ordena erros por linha, depois por mensagem para consistência
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

    // Adiciona símbolo ao escopo atual, checando redeclaração
    private void adicionarSimbolo(String nome, TipoLA tipo, CategoriaSimbolo categoria, Token tokenNome) {
        Map<String, Simbolo> escopoAtual = pilhaEscopos.peek();
        if (escopoAtual.containsKey(nome)) { // Verifica se já existe no escopo atual
            adicionarErro(tokenNome, "identificador " + nome + " ja declarado anteriormente");
        } else {
            escopoAtual.put(nome, new Simbolo(nome, tipo, categoria, tokenNome.getLine()));
        }
    }

    private void adicionarSimbolo(Simbolo simbolo) { // Sobrecarga para símbolo já criado
        Map<String, Simbolo> escopoAtual = pilhaEscopos.peek();
        if (escopoAtual.containsKey(simbolo.nome)) {
            adicionarErro(simbolo.linhaDeclaracao, "identificador " + simbolo.nome + " ja declarado anteriormente");
        } else {
            escopoAtual.put(simbolo.nome, simbolo);
        }
    }

    // Busca símbolo na pilha de escopos
    private Simbolo buscarSimbolo(String nome) {
        for (Map<String, Simbolo> escopo : pilhaEscopos) {
            if (escopo.containsKey(nome)) return escopo.get(nome);
        }
        return null; // Não encontrado
    }

    @Override
    public Void visitPrograma(NossaGramaticaParser.ProgramaContext ctx) {
        entrarEscopo(); // Escopo global
        visitDeclaracoes(ctx.declaracoes()); // Declarações globais (tipos, procs, funcs)
        if (ctx.corpo() != null) { // Corpo do algoritmo principal
            visitCorpo(ctx.corpo()); // Declarações locais e comandos do algoritmo
        }
        sairEscopo();
        return null;
    }

    @Override
    public Void visitCorpo(NossaGramaticaParser.CorpoContext ctx) {
        // Declarações locais do algoritmo (adicionadas ao escopo global/atual)
        for(NossaGramaticaParser.Declaracao_localContext declCtx : ctx.declaracao_local()) {
            visitDeclaracao_local(declCtx);
        }
        for(NossaGramaticaParser.CmdContext cmdCtx : ctx.cmd()) {
            visit(cmdCtx);
        }
        return null;
    }

    // Resolve um nó de tipo para um TipoLA (registro, ponteiro, etc.)
    private TipoLA resolverTipoNo(NossaGramaticaParser.TipoContext tipoCtx) {
        if (tipoCtx.registro() != null) { // Definição de registro
            entrarEscopo(); // Escopo temporário para campos do registro
            Map<String, Simbolo> campos = new LinkedHashMap<>();
            for (NossaGramaticaParser.VariavelContext varCampoCtx : tipoCtx.registro().variavel()) {
                TipoLA tipoCampo = resolverTipoNo(varCampoCtx.tipo());
                for (NossaGramaticaParser.IdentificadorContext identCampoCtx : varCampoCtx.identificador()) {
                    String nomeCampo = identCampoCtx.IDENT(0).getText();
                    Token tokenNomeCampo = identCampoCtx.IDENT(0).getSymbol();
                    if (campos.containsKey(nomeCampo)) { // Verifica duplicata de campo
                        adicionarErro(tokenNomeCampo, "identificador " + nomeCampo + " ja declarado neste registro");
                    } else {
                        campos.put(nomeCampo, new Simbolo(nomeCampo, tipoCampo, CategoriaSimbolo.VARIAVEL, tokenNomeCampo.getLine()));
                    }
                }
            }
            sairEscopo();
            return new Registro(null, campos); // Nome original será definido se for "tipo Nome : registro"
        } else if (tipoCtx.tipo_estendido() != null) { // Tipo estendido (primitivo, ponteiro, tipo nomeado)
            return resolverTipoEstendido(tipoCtx.tipo_estendido());
        }
        adicionarErro(tipoCtx.getStart(), "tipo desconhecido ou mal formado: " + tipoCtx.getText());
        return TipoLA.TIPO_INDEFINIDO;
    }

    // Resolve um tipo estendido (pode ser tipo básico, tipo nomeado ou ponteiro para um destes)
    private TipoLA resolverTipoEstendido(NossaGramaticaParser.Tipo_estendidoContext teCtx) {
        NossaGramaticaParser.Tipo_basico_identContext tbiCtx = teCtx.tipo_basico_ident();
        TipoLA tipoBase;

        if (tbiCtx.tipo_basico() != null) { // Tipo básico (literal, inteiro, etc.)
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
        } else { // Identificador (referência a um tipo nomeado)
            String nomeIdentTipo = tbiCtx.IDENT().getText();
            Simbolo simboloTipo = buscarSimbolo(nomeIdentTipo);
            if (simboloTipo == null) { // Tipo nomeado não declarado
                adicionarErro(tbiCtx.IDENT().getSymbol(), "tipo " + nomeIdentTipo + " nao declarado");
                tipoBase = TipoLA.TIPO_INDEFINIDO;
            } else if (simboloTipo.categoria != CategoriaSimbolo.TIPO) { // Identificador não é um tipo
                adicionarErro(tbiCtx.IDENT().getSymbol(), "identificador " + nomeIdentTipo + " nao e um tipo");
                tipoBase = TipoLA.TIPO_INDEFINIDO;
            } else {
                tipoBase = simboloTipo.tipo; // Tipo encontrado
            }
        }

        if (teCtx.PONTEIRO() != null) { // Se for ponteiro (ex: ^inteiro)
            return (tipoBase == TipoLA.TIPO_INDEFINIDO) ? TipoLA.TIPO_INDEFINIDO : new Ponteiro(tipoBase);
        }
        return tipoBase;
    }

    @Override
    public Void visitDeclaracao_local(NossaGramaticaParser.Declaracao_localContext ctx) {
        if (ctx.CONSTANTE() != null) { // Declaração de constante
            String nomeConst = ctx.IDENT().getText();
            Token tokenNomeConst = ctx.IDENT().getSymbol();
            TipoLA tipoDeclaradoConst = resolverTipoBasico(ctx.tipo_basico());
            TipoLA tipoValor = verificarTipoValorConstante(ctx.valor_constante());

            if (tipoDeclaradoConst != TipoLA.TIPO_INDEFINIDO && tipoValor != TipoLA.TIPO_INDEFINIDO) {
                if (!saoCompativeisAtribuicao(tipoDeclaradoConst, tipoValor)) { // Verifica compatibilidade
                    adicionarErro(tokenNomeConst, "atribuicao nao compativel para " + nomeConst);
                }
            }
            adicionarSimbolo(nomeConst, tipoDeclaradoConst, CategoriaSimbolo.CONSTANTE, tokenNomeConst);

        } else if (ctx.TIPO() != null) { // Declaração de tipo (ex: "tipo MeuReg: registro...")
            String nomeNovoTipo = ctx.IDENT().getText();
            Token tokenNomeNovoTipo = ctx.IDENT().getSymbol();
            TipoLA tipoDefinido = resolverTipoNo(ctx.tipo());

            if (tipoDefinido instanceof Registro) { // Se for registro, atribui o nome declarado
                tipoDefinido = new Registro(nomeNovoTipo, ((Registro) tipoDefinido).campos);
            }

            Simbolo simboloDoTipo = new Simbolo(nomeNovoTipo, tipoDefinido, CategoriaSimbolo.TIPO, tokenNomeNovoTipo.getLine());
            if (tipoDefinido instanceof Registro) {
                simboloDoTipo.camposDoTipoRegistro = ((Registro) tipoDefinido).campos; // Guarda campos no símbolo
            }
            adicionarSimbolo(simboloDoTipo);

        } else if (ctx.variavel() != null) { // Declaração de variável
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

        if (ctx.PROCEDIMENTO() != null) { // Declaração de procedimento
            nomeSubRotina = ctx.IDENT().getText(); tokenNome = ctx.IDENT().getSymbol();
            categoria = CategoriaSimbolo.PROCEDIMENTO;
        } else { // Declaração de função
            nomeSubRotina = ctx.IDENT().getText(); tokenNome = ctx.IDENT().getSymbol();
            categoria = CategoriaSimbolo.FUNCAO;
            tipoRetorno = resolverTipoEstendido(ctx.tipo_estendido()); // Funções têm tipo de retorno
        }

        Simbolo simboloSubRotina = new Simbolo(nomeSubRotina, tipoRetorno, categoria, tokenNome.getLine());
        if (ctx.parametros() != null) { // Coleta tipos dos parâmetros
            for (NossaGramaticaParser.ParametroContext paramCtx : ctx.parametros().parametro()) {
                TipoLA tipoDoParam = resolverTipoEstendido(paramCtx.tipo_estendido());
                for (int i = 0; i < paramCtx.identificador().size(); i++) { // Para a,b:inteiro
                    simboloSubRotina.tiposParametros.add(tipoDoParam);
                }
            }
        }
        adicionarSimbolo(simboloSubRotina); // Adiciona ao escopo atual (global)

        entrarEscopo(); // Novo escopo para a sub-rotina
        boolean oldDentroDeFuncaoOuProcedimento = dentroDeFuncaoOuProcedimento;
        TipoLA oldTipoRetornoFuncaoAtual = tipoRetornoFuncaoAtual;
        String oldNomeFuncaoProcedimentoAtual = nomeFuncaoProcedimentoAtual;

        dentroDeFuncaoOuProcedimento = true;
        nomeFuncaoProcedimentoAtual = nomeSubRotina;
        if (categoria == CategoriaSimbolo.FUNCAO) {
            this.tipoRetornoFuncaoAtual = tipoRetorno;
        } else {
            this.tipoRetornoFuncaoAtual = null; // Procedimento não tem tipo de retorno para 'retorne expr'
        }

        if (ctx.parametros() != null) visitParametros(ctx.parametros()); // Adiciona params ao novo escopo
        ctx.declaracao_local().forEach(this::visitDeclaracao_local); // Adiciona locais ao novo escopo
        ctx.cmd().forEach(this::visit); // Processa comandos da sub-rotina

        this.tipoRetornoFuncaoAtual = oldTipoRetornoFuncaoAtual; // Restaura estado para escopo externo
        this.dentroDeFuncaoOuProcedimento = oldDentroDeFuncaoOuProcedimento;
        this.nomeFuncaoProcedimentoAtual = oldNomeFuncaoProcedimentoAtual;
        sairEscopo();
        return null;
    }

    @Override
    public Void visitParametros(NossaGramaticaParser.ParametrosContext ctx) {
        for (NossaGramaticaParser.ParametroContext paramCtx : ctx.parametro()) {
            visitParametro(paramCtx); // Adiciona cada parâmetro ao escopo da sub-rotina
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

    // Verifica compatibilidade para chamada de função/procedimento (parâmetro vs argumento)
    private boolean saoCompativeisParametro(TipoLA parametroDeclarado, TipoLA tipoArgumento) {
        if (parametroDeclarado == TipoLA.TIPO_INDEFINIDO || tipoArgumento == TipoLA.TIPO_INDEFINIDO) return true;
        if (parametroDeclarado instanceof Ponteiro) { // Parâmetro ponteiro
            // Argumento deve ser ponteiro para tipo base compatível
            return (tipoArgumento instanceof Ponteiro) &&
                    ((Ponteiro) parametroDeclarado).getTipoApontado().eEquivalente(((Ponteiro) tipoArgumento).getTipoApontado());
        }
        if (parametroDeclarado instanceof Registro) { // Parâmetro registro
            return (tipoArgumento instanceof Registro) && parametroDeclarado.eEquivalente(tipoArgumento); // Mesmo tipo nomeado
        }
        return parametroDeclarado == tipoArgumento; // Tipos primitivos devem ser exatos
    }

    // Verifica compatibilidade para atribuição (LHS <- RHS)
    private boolean saoCompativeisAtribuicao(TipoLA tipoEsquerdo, TipoLA tipoDireito) {
        if (tipoEsquerdo == TipoLA.TIPO_INDEFINIDO || tipoDireito == TipoLA.TIPO_INDEFINIDO) return false;

        if (tipoEsquerdo instanceof Ponteiro) { // Atribuição a ponteiro
            return (tipoDireito instanceof Ponteiro) &&
                    ((Ponteiro) tipoEsquerdo).getTipoApontado().eEquivalente(((Ponteiro) tipoDireito).getTipoApontado());
        }

        if (tipoEsquerdo.eNumerico() && tipoDireito.eNumerico()) { // Atribuição numérica
            if (tipoEsquerdo == TipoLA.REAL && tipoDireito == TipoLA.INTEIRO) return true; // real <- inteiro (permitido)
            return tipoEsquerdo == tipoDireito; // real <- real, inteiro <- inteiro
        }

        if (tipoEsquerdo == TipoLA.LITERAL && tipoDireito == TipoLA.LITERAL) return true;
        if (tipoEsquerdo == TipoLA.LOGICO && tipoDireito == TipoLA.LOGICO) return true;
        if (tipoEsquerdo instanceof Registro && tipoDireito instanceof Registro) { // Atribuição de registros
            return tipoEsquerdo.eEquivalente(tipoDireito); // Devem ser do mesmo tipo nomeado
        }
        return false;
    }

    // Verifica tipo de expressão lógica (com 'ou')
    public TipoLA verificarTipoExpressao(NossaGramaticaParser.ExpressaoContext ctx) {
        if (ctx == null) return TipoLA.TIPO_INDEFINIDO;
        TipoLA tipoResultante = verificarTipoTermoLogico(ctx.termo_logico(0));
        for (int i = 0; i < ctx.op_logico_1().size(); i++) { // Para cada 'ou'
            TipoLA tipoDireita = verificarTipoTermoLogico(ctx.termo_logico(i + 1));
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else if (!(tipoResultante == TipoLA.LOGICO && tipoDireita == TipoLA.LOGICO)) { // 'ou' requer lógicos
                tipoResultante = TipoLA.TIPO_INDEFINIDO; // Incompatível, resulta em indefinido
            } else {
                tipoResultante = TipoLA.LOGICO;
            }
        }
        return tipoResultante;
    }

    // Verifica tipo de termo lógico (com 'e')
    private TipoLA verificarTipoTermoLogico(NossaGramaticaParser.Termo_logicoContext ctx) {
        TipoLA tipoResultante = verificarTipoFatorLogico(ctx.fator_logico(0));
        for (int i = 0; i < ctx.op_logico_2().size(); i++) { // Para cada 'e'
            TipoLA tipoDireita = verificarTipoFatorLogico(ctx.fator_logico(i + 1));
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else if (!(tipoResultante == TipoLA.LOGICO && tipoDireita == TipoLA.LOGICO)) { // 'e' requer lógicos
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else {
                tipoResultante = TipoLA.LOGICO;
            }
        }
        return tipoResultante;
    }

    // Verifica tipo de fator lógico (com 'nao')
    private TipoLA verificarTipoFatorLogico(NossaGramaticaParser.Fator_logicoContext ctx) {
        TipoLA tipoParcela = verificarTipoParcelaLogica(ctx.parcela_logica());
        if (ctx.NAO_LOGICO() != null) { // Se houver 'nao'
            if (tipoParcela == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;
            if (tipoParcela != TipoLA.LOGICO) { // 'nao' requer lógico
                return TipoLA.TIPO_INDEFINIDO;
            }
            return TipoLA.LOGICO;
        }
        return tipoParcela;
    }

    // Verifica tipo de parcela lógica (booleano ou expressão relacional)
    private TipoLA verificarTipoParcelaLogica(NossaGramaticaParser.Parcela_logicaContext ctx) {
        if (ctx.VERDADEIRO() != null || ctx.FALSO() != null) return TipoLA.LOGICO;
        return verificarTipoExpRelacional(ctx.exp_relacional()); // Senão, é exp. relacional
    }

    // Verifica tipo de expressão relacional (ex: a > b)
    private TipoLA verificarTipoExpRelacional(NossaGramaticaParser.Exp_relacionalContext ctx) {
        TipoLA tipoEsq = verificarTipoExpAritmetica(ctx.exp_aritmetica(0));
        if (ctx.op_relacional() != null) { // Se houver operador relacional
            TipoLA tipoDir = verificarTipoExpAritmetica(ctx.exp_aritmetica(1));
            if (tipoEsq == TipoLA.TIPO_INDEFINIDO || tipoDir == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;

            boolean compatible = false; // Verifica compatibilidade para operação relacional
            if (tipoEsq.eNumerico() && tipoDir.eNumerico()) compatible = true;
            else if (tipoEsq == TipoLA.LITERAL && tipoDir == TipoLA.LITERAL) compatible = true;
            else if ((ctx.op_relacional().IGUAL() != null || ctx.op_relacional().DIFERENTE() != null)) { // Para '=' ou '<>'
                if (tipoEsq == TipoLA.LOGICO && tipoDir == TipoLA.LOGICO) compatible = true;
                else if (tipoEsq instanceof Ponteiro && tipoDir instanceof Ponteiro) { // Ponteiros comparáveis se apontam para tipos equivalentes
                    if (((Ponteiro)tipoEsq).getTipoApontado().eEquivalente(((Ponteiro)tipoDir).getTipoApontado())) {
                        compatible = true;
                    }
                }
            }

            if (!compatible) return TipoLA.TIPO_INDEFINIDO; // Tipos incompatíveis
            return TipoLA.LOGICO; // Resultado de exp. relacional é sempre lógico
        }
        return tipoEsq; // Se não for relacional, é o tipo da exp. aritmética (ex: 'se var_logica')
    }

    // Verifica tipo de expressão aritmética (com '+' ou '-')
    private TipoLA verificarTipoExpAritmetica(NossaGramaticaParser.Exp_aritmeticaContext ctx) {
        TipoLA tipoResultante = verificarTipoTermo(ctx.termo(0));
        for (int i = 0; i < ctx.op1().size(); i++) { // Para cada '+' ou '-'
            TipoLA tipoDireita = verificarTipoTermo(ctx.termo(i + 1));
            Token opToken = ctx.op1(i).getStart();
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else if (opToken.getText().equals("+") && tipoResultante == TipoLA.LITERAL && tipoDireita == TipoLA.LITERAL) {
                tipoResultante = TipoLA.LITERAL; // Concatenação de literais
            } else if (tipoResultante.eNumerico() && tipoDireita.eNumerico()) { // Operandos numéricos
                tipoResultante = (tipoResultante == TipoLA.REAL || tipoDireita == TipoLA.REAL) ? TipoLA.REAL : TipoLA.INTEIRO; // Real prevalece
            } else {
                tipoResultante = TipoLA.TIPO_INDEFINIDO; // Incompatível (ex: literal + logico)
            }
        }
        return tipoResultante;
    }

    // Verifica tipo de termo aritmético (com '*' ou '/')
    private TipoLA verificarTipoTermo(NossaGramaticaParser.TermoContext ctx) {
        TipoLA tipoResultante = verificarTipoFator(ctx.fator(0));
        for (int i = 0; i < ctx.op2().size(); i++) { // Para cada '*' ou '/'
            TipoLA tipoDireita = verificarTipoFator(ctx.fator(i + 1));
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else if (tipoResultante.eNumerico() && tipoDireita.eNumerico()) {
                if (ctx.op2(i).DIVISAO() != null) { // Operador '/'
                    if (tipoResultante == TipoLA.INTEIRO && tipoDireita == TipoLA.INTEIRO) { // int / int -> int (divisão inteira)
                        tipoResultante = TipoLA.INTEIRO;
                    } else { // Se houver real, resultado é real
                        tipoResultante = TipoLA.REAL;
                    }
                } else { // Operador '*'
                    tipoResultante = (tipoResultante == TipoLA.REAL || tipoDireita == TipoLA.REAL) ? TipoLA.REAL : TipoLA.INTEIRO; // Real prevalece
                }
            } else {
                tipoResultante = TipoLA.TIPO_INDEFINIDO; // Incompatível
            }
        }
        return tipoResultante;
    }

    // Verifica tipo de fator aritmético (com '%')
    private TipoLA verificarTipoFator(NossaGramaticaParser.FatorContext ctx) {
        TipoLA tipoResultante = verificarTipoParcela(ctx.parcela(0));
        for (int i = 0; i < ctx.op3().size(); i++) { // Para cada '%' (mod)
            TipoLA tipoDireita = verificarTipoParcela(ctx.parcela(i + 1));
            if (tipoResultante == TipoLA.TIPO_INDEFINIDO || tipoDireita == TipoLA.TIPO_INDEFINIDO) {
                tipoResultante = TipoLA.TIPO_INDEFINIDO;
            } else if (tipoResultante == TipoLA.INTEIRO && tipoDireita == TipoLA.INTEIRO) { // '%' requer inteiros
                tipoResultante = TipoLA.INTEIRO;
            } else {
                tipoResultante = TipoLA.TIPO_INDEFINIDO; // Incompatível
            }
        }
        return tipoResultante;
    }

    // Verifica tipo de parcela (base da expressão, pode ter '-' unário)
    private TipoLA verificarTipoParcela(NossaGramaticaParser.ParcelaContext ctx) {
        TipoLA tipoBase;
        if (ctx.parcela_unario() != null) { // Parcela como identificador, chamada de func, literal numérico, (expr)
            tipoBase = verificarTipoParcelaUnario(ctx.parcela_unario());
        } else { // Parcela como &identificador ou literal cadeia
            tipoBase = verificarTipoParcelaNaoUnario(ctx.parcela_nao_unario());
        }

        if (ctx.op_unario() != null && ctx.op_unario().MENOS() != null) { // Se houver '-' unário
            if (tipoBase == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;
            if (!tipoBase.eNumerico()) { // '-' unário requer operando numérico
                return TipoLA.TIPO_INDEFINIDO;
            }
            // Tipo não muda com '-' (ex: -int é int)
        }
        return tipoBase;
    }

    // Verifica tipo de parcela unária
    private TipoLA verificarTipoParcelaUnario(NossaGramaticaParser.Parcela_unarioContext ctx) {
        if (ctx.identificador() != null) { // Identificador (var, const, ^ptr, reg.campo)
            return getTipoDeIdentificadorContext(ctx.identificador(), ctx.PONTEIRO() != null); // PONTEIRO() é o '^' antes do ident
        }
        if (ctx.IDENT() != null) { // Chamada de função: func(params)
            String nomeFunc = ctx.IDENT().getText();
            Simbolo funcSimbolo = buscarSimbolo(nomeFunc);

            if (funcSimbolo == null) { // Função não declarada
                adicionarErro(ctx.IDENT().getSymbol(), "identificador " + nomeFunc + " nao declarado");
                ctx.expressao().forEach(this::verificarTipoExpressao); // Verifica args para outros erros
                return TipoLA.TIPO_INDEFINIDO;
            }
            if (funcSimbolo.categoria != CategoriaSimbolo.FUNCAO) { // Não é função
                adicionarErro(ctx.IDENT().getSymbol(), "identificador " + nomeFunc + " nao e uma funcao");
                ctx.expressao().forEach(this::verificarTipoExpressao);
                return TipoLA.TIPO_INDEFINIDO;
            }

            // Verifica compatibilidade de parâmetros
            List<NossaGramaticaParser.ExpressaoContext> argsCtx = ctx.expressao();
            List<TipoLA> tiposArgs = new ArrayList<>();
            argsCtx.forEach(argExpr -> tiposArgs.add(verificarTipoExpressao(argExpr))); // Tipos dos argumentos

            if (argsCtx.size() != funcSimbolo.tiposParametros.size()) { // Número de args incorreto
                adicionarErro(ctx.IDENT().getSymbol(), "incompatibilidade de parametros na chamada de " + nomeFunc);
            } else {
                for (int i = 0; i < funcSimbolo.tiposParametros.size(); i++) { // Verifica tipo de cada arg
                    if (!saoCompativeisParametro(funcSimbolo.tiposParametros.get(i), tiposArgs.get(i))) {
                        adicionarErro(ctx.IDENT().getSymbol(), "incompatibilidade de parametros na chamada de " + nomeFunc);
                        break;
                    }
                }
            }
            return funcSimbolo.tipo; // Tipo de retorno da função
        }
        if (ctx.NUM_INT() != null) return TipoLA.INTEIRO;
        if (ctx.NUM_REAL() != null) return TipoLA.REAL;
        if (ctx.LPAREN() != null) return verificarTipoExpressao(ctx.expressao(0)); // (expressao)

        return TipoLA.TIPO_INDEFINIDO;
    }

    // Verifica tipo de parcela não unária
    private TipoLA verificarTipoParcelaNaoUnario(NossaGramaticaParser.Parcela_nao_unarioContext ctx) {
        if (ctx.ENDERECO() != null) { // &identificador
            TipoLA tipoIdent = getTipoDeIdentificadorContext(ctx.identificador(), false); // Tipo do identificador
            return (tipoIdent == TipoLA.TIPO_INDEFINIDO) ? TipoLA.TIPO_INDEFINIDO : new Ponteiro(tipoIdent); // Retorna ^(tipo do ident)
        }
        if (ctx.CADEIA() != null) return TipoLA.LITERAL;
        return TipoLA.TIPO_INDEFINIDO;
    }

    // Obtém tipo de um identificador (var, campo de registro, ^ptr)
    private TipoLA getTipoDeIdentificadorContext(NossaGramaticaParser.IdentificadorContext identCtx, boolean desreferenciadoPeloOperador) {
        String nomeBase = identCtx.IDENT(0).getText();
        Token tokenBase = identCtx.IDENT(0).getSymbol();
        Simbolo simbolo = buscarSimbolo(nomeBase);

        // Constrói path completo do identificador para mensagens de erro (ex: var.campo1.campo2)
        String identifierPathForError = identCtx.IDENT(0).getText();
        for(int k=1; k < identCtx.IDENT().size(); k++) {
            identifierPathForError += "." + identCtx.IDENT(k).getText();
        }
        if (desreferenciadoPeloOperador) { // Adiciona '^' se aplicável
            identifierPathForError = "^" + identifierPathForError;
        }

        if (simbolo == null) { // Símbolo base não declarado
            // Adapta mensagem se for acesso a campo de var não declarada (vinhocaro.tipoVinho)
            if (identCtx.IDENT().size() > 1) {
                adicionarErro(tokenBase, "identificador " + identifierPathForError + " nao declarado");
            } else {
                adicionarErro(tokenBase, "identificador " + nomeBase + " nao declarado");
            }
            return TipoLA.TIPO_INDEFINIDO;
        }

        // Verifica uso incorreto de nome de TIPO ou PROCEDIMENTO como variável
        if (simbolo.categoria == CategoriaSimbolo.TIPO || simbolo.categoria == CategoriaSimbolo.PROCEDIMENTO) {
            // Permite se for parte de uma chamada de função/procedimento
            boolean isPartOfCall = (identCtx.getParent() instanceof NossaGramaticaParser.Parcela_unarioContext &&
                    ((NossaGramaticaParser.Parcela_unarioContext)identCtx.getParent()).LPAREN() != null) ||
                    (identCtx.getParent() instanceof NossaGramaticaParser.CmdChamadaContext);
            if(!isPartOfCall) { // Se não for chamada, é erro
                adicionarErro(tokenBase, "identificador " + nomeBase + " e um " + simbolo.categoria.toString().toLowerCase() + " e nao pode ser usado neste contexto");
                return TipoLA.TIPO_INDEFINIDO;
            }
        }

        TipoLA tipoAtual = simbolo.tipo;

        if (desreferenciadoPeloOperador) { // Desreferência explícita: ^ptr
            if (tipoAtual == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;
            if (!(tipoAtual instanceof Ponteiro)) { // Tenta desreferenciar não-ponteiro
                adicionarErro(identCtx.getStart(), "tentativa de desreferenciar um nao-ponteiro " + identifierPathForError);
                return TipoLA.TIPO_INDEFINIDO;
            }
            tipoAtual = ((Ponteiro) tipoAtual).getTipoApontado(); // Tipo vira o tipo apontado
        }

        for (int i = 1; i < identCtx.IDENT().size(); i++) { // Acesso a campos: rec.campo1.campo2
            if (tipoAtual == TipoLA.TIPO_INDEFINIDO) return TipoLA.TIPO_INDEFINIDO;

            if (!(tipoAtual instanceof Registro)) { // Tenta acessar campo de não-registro
                String prefix = identCtx.IDENT(0).getText();
                for(int k=1; k < i; k++) { prefix += "." + identCtx.IDENT(k).getText(); } // Constrói o prefixo problemático
                adicionarErro(identCtx.IDENT(i-1).getSymbol(), "identificador " + prefix + " nao e um registro");
                return TipoLA.TIPO_INDEFINIDO;
            }
            Registro rTipo = (Registro) tipoAtual;
            String nomeCampo = identCtx.IDENT(i).getText(); // Nome do campo

            Simbolo campoSimbolo = (rTipo.campos != null) ? rTipo.campos.get(nomeCampo) : null;
            if (campoSimbolo == null) { // Campo não existe
                String idCompostoErrado = identCtx.IDENT(0).getText();
                for(int k=1; k<=i; k++) { idCompostoErrado += "." + identCtx.IDENT(k).getText(); } // Path completo até campo errado
                adicionarErro(identCtx.IDENT(i).getSymbol(), "identificador " + idCompostoErrado + " nao declarado");
                return TipoLA.TIPO_INDEFINIDO;
            }
            tipoAtual = campoSimbolo.tipo; // Atualiza tipo para o do campo
        }

        if (identCtx.dimensao() != null && !identCtx.dimensao().exp_aritmetica().isEmpty()) { // Acesso a array: var[indice]
            // tipoAtual já é o tipo do elemento. Verifica uso incorreto de ponteiro.
            if (tipoAtual instanceof Ponteiro && !desreferenciadoPeloOperador){
                adicionarErro(identCtx.getStart(), "acesso a ponteiro como array nao permitido diretamente, use ^ponteiro para desreferenciar se for o caso");
                return TipoLA.TIPO_INDEFINIDO;
            }
        }
        return tipoAtual;
    }

    @Override
    public Void visitCmdLeia(NossaGramaticaParser.CmdLeiaContext ctx) {
        for (NossaGramaticaParser.IdentificadorContext identCtx : ctx.identificador()) {
            getTipoDeIdentificadorContext(identCtx, false); // Verifica se ident existe
            Simbolo s = buscarSimbolo(identCtx.IDENT(0).getText());
            if (s != null && s.categoria == CategoriaSimbolo.CONSTANTE) { // Não pode ler em constante
                adicionarErro(identCtx.IDENT(0).getSymbol(), "nao e possivel ler em constante " + s.nome);
            }
        }
        return null;
    }

    @Override
    public Void visitCmdEscreva(NossaGramaticaParser.CmdEscrevaContext ctx) {
        ctx.expressao().forEach(this::verificarTipoExpressao); // Verifica tipo de cada expressão
        return null;
    }

    @Override
    public Void visitCmdSe(NossaGramaticaParser.CmdSeContext ctx) {
        TipoLA tipoCond = verificarTipoExpressao(ctx.expressao()); // Condição do 'se' deve ser lógica
        if (tipoCond != TipoLA.TIPO_INDEFINIDO && tipoCond != TipoLA.LOGICO) {
            adicionarErro(ctx.expressao().getStart(), "expressao do comando 'se' deve ser logica");
        }
        return super.visitCmdSe(ctx); // Deixa ANTLR visitar comandos do if/else
    }

    @Override
    public Void visitCmdAtribuicao(NossaGramaticaParser.CmdAtribuicaoContext ctx) {
        boolean lhsDesreferenciado = ctx.PONTEIRO() != null; // Se há '^' no LHS
        TipoLA tipoLHS = getTipoDeIdentificadorContext(ctx.identificador(), lhsDesreferenciado);
        TipoLA tipoRHS = verificarTipoExpressao(ctx.expressao());

        String lhsText = (lhsDesreferenciado ? "^" : "") + ctx.identificador().getText();

        // Verifica atribuição a constante (se LHS é identificador simples)
        if (!lhsDesreferenciado && ctx.identificador().IDENT().size() == 1 && ctx.identificador().dimensao().exp_aritmetica().isEmpty()) {
            Simbolo s = buscarSimbolo(ctx.identificador().IDENT(0).getText());
            if (s != null && s.categoria == CategoriaSimbolo.CONSTANTE) {
                adicionarErro(ctx.ATRIBUICAO().getSymbol(), "atribuicao nao compativel para " + lhsText);
                return null;
            }
        }

        if (!saoCompativeisAtribuicao(tipoLHS, tipoRHS)) { // Verifica compatibilidade de tipos
            if (tipoLHS != TipoLA.TIPO_INDEFINIDO) { // Só reporta se LHS não for já um erro de "não declarado"
                adicionarErro(ctx.ATRIBUICAO().getSymbol(), "atribuicao nao compativel para " + lhsText);
            }
        }
        return null;
    }

    @Override
    public Void visitCmdChamada(NossaGramaticaParser.CmdChamadaContext ctx) {
        String nomeSub = ctx.IDENT().getText();
        Simbolo simboloSub = buscarSimbolo(nomeSub);

        if (simboloSub == null) { // Sub-rotina não declarada
            adicionarErro(ctx.IDENT().getSymbol(), "identificador " + nomeSub + " nao declarado");
            return null;
        }
        if (simboloSub.categoria != CategoriaSimbolo.PROCEDIMENTO && simboloSub.categoria != CategoriaSimbolo.FUNCAO) {
            adicionarErro(ctx.IDENT().getSymbol(), "identificador " + nomeSub + " nao e um procedimento ou funcao");
            return null;
        }

        // Verifica parâmetros
        List<NossaGramaticaParser.ExpressaoContext> argsCtx = ctx.expressao();
        List<TipoLA> tiposArgs = new ArrayList<>();
        argsCtx.forEach(argExpr -> tiposArgs.add(verificarTipoExpressao(argExpr)));

        List<TipoLA> tiposParamsEsperados = simboloSub.tiposParametros != null ? simboloSub.tiposParametros : Collections.emptyList();

        if (argsCtx.size() != tiposParamsEsperados.size()) { // Número de args incorreto
            adicionarErro(ctx.IDENT().getSymbol(), "incompatibilidade de parametros na chamada de " + nomeSub);
        } else {
            for (int i = 0; i < tiposParamsEsperados.size(); i++) { // Verifica tipo de cada arg
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
        if (!dentroDeFuncaoOuProcedimento) { // 'retorne' no escopo do algoritmo principal
            adicionarErro(ctx.RETORNE().getSymbol(), "comando retorne nao permitido nesse escopo");
            return null;
        }
        if (tipoRetornoFuncaoAtual == null) { // 'retorne' dentro de um procedimento
            adicionarErro(ctx.RETORNE().getSymbol(), "comando retorne nao permitido nesse escopo");
            return null;
        }

        // Dentro de uma FUNÇÃO, verifica tipo do retorno
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
        Simbolo simboloVar = buscarSimbolo(nomeVar); // Var de controle do loop
        if (simboloVar == null) adicionarErro(tokenVar, "identificador " + nomeVar + " nao declarado");
        else if (simboloVar.categoria != CategoriaSimbolo.VARIAVEL) adicionarErro(tokenVar, "identificador " + nomeVar + " em comando 'para' deve ser uma variavel");
        else if (simboloVar.tipo != TipoLA.TIPO_INDEFINIDO && !simboloVar.tipo.eNumerico()) adicionarErro(tokenVar, "variavel de controle " + nomeVar + " em comando 'para' deve ser numerica");

        // Limites do loop devem ser numéricos
        TipoLA tIni = verificarTipoExpAritmetica(ctx.exp_aritmetica(0));
        TipoLA tFim = verificarTipoExpAritmetica(ctx.exp_aritmetica(1));
        if (tIni != TipoLA.TIPO_INDEFINIDO && !tIni.eNumerico()) adicionarErro(ctx.exp_aritmetica(0).getStart(), "expressao inicial do comando 'para' deve ser numerica");
        if (tFim != TipoLA.TIPO_INDEFINIDO && !tFim.eNumerico()) adicionarErro(ctx.exp_aritmetica(1).getStart(), "expressao final do comando 'para' deve ser numerica");

        return super.visitCmdPara(ctx); // Visita comandos do loop
    }

    @Override public Void visitCmdEnquanto(NossaGramaticaParser.CmdEnquantoContext ctx) {
        TipoLA tipoCond = verificarTipoExpressao(ctx.expressao()); // Condição deve ser lógica
        if (tipoCond != TipoLA.TIPO_INDEFINIDO && tipoCond != TipoLA.LOGICO) {
            adicionarErro(ctx.expressao().getStart(), "expressao do comando 'enquanto' deve ser logica");
        }
        return super.visitCmdEnquanto(ctx); // Visita comandos do loop
    }

    @Override public Void visitCmdFaca(NossaGramaticaParser.CmdFacaContext ctx) {
        for(NossaGramaticaParser.CmdContext cmdCtx : ctx.cmd()) visit(cmdCtx); // Visita comandos do loop
        TipoLA tipoCond = verificarTipoExpressao(ctx.expressao()); // Condição do 'ate' deve ser lógica
        if (tipoCond != TipoLA.TIPO_INDEFINIDO && tipoCond != TipoLA.LOGICO) {
            adicionarErro(ctx.expressao().getStart(), "expressao do comando 'faca..ate' deve ser logica");
        }
        return null;
    }

    @Override public Void visitCmdCaso(NossaGramaticaParser.CmdCasoContext ctx) {
        TipoLA tipoExp = verificarTipoExpAritmetica(ctx.exp_aritmetica()); // Expressão do 'caso' deve ser inteira
        if (tipoExp != TipoLA.TIPO_INDEFINIDO && tipoExp != TipoLA.INTEIRO) {
            adicionarErro(ctx.exp_aritmetica().getStart(), "expressao do comando 'caso' deve ser inteira");
        }
        return super.visitCmdCaso(ctx); // Visita itens e comandos do 'senao'
    }
}