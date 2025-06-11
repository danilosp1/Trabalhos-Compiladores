package main.java.br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.NossaGramaticaParser;
import br.ufscar.dc.compiladores.NossaGramaticaBaseVisitor;
import java.util.List;
import java.util.ArrayList;

public class GeradorDeCodigo extends NossaGramaticaBaseVisitor<String> {
    private StringBuilder codigo = new StringBuilder();
    private AnalisadorSemantico.TabelaDeSimbolos tabelaDeSimbolos;

    public GeradorDeCodigo(AnalisadorSemantico.TabelaDeSimbolos tabela) {
        this.tabelaDeSimbolos = tabela;
    }

    private String getCType(AnalisadorSemantico.TipoLA tipo) {
        if (tipo == AnalisadorSemantico.TipoLA.INTEIRO) return "int";
        if (tipo == AnalisadorSemantico.TipoLA.REAL) return "float";
        if (tipo == AnalisadorSemantico.TipoLA.LITERAL) return "char";
        if (tipo == AnalisadorSemantico.TipoLA.LOGICO) return "int";
        if (tipo instanceof AnalisadorSemantico.Ponteiro) {
            return getCType(((AnalisadorSemantico.Ponteiro) tipo).getTipoApontado()) + "*";
        }
        if (tipo instanceof AnalisadorSemantico.Registro) {
            if (((AnalisadorSemantico.Registro) tipo).nomeOriginalDoTipo != null) {
                return ((AnalisadorSemantico.Registro) tipo).nomeOriginalDoTipo;
            }
            return "struct";
        }
        return "void";
    }

    @Override
    public String visitPrograma(NossaGramaticaParser.ProgramaContext ctx) {
        codigo.append("#include <stdio.h>\n");
        codigo.append("#include <stdlib.h>\n");
        codigo.append("#include <string.h>\n\n");

        if (ctx.declaracoes() != null) {
            visit(ctx.declaracoes());
        }

        codigo.append("int main() {\n");
        if (ctx.corpo() != null) {
            visit(ctx.corpo());
        }
        codigo.append("\treturn 0;\n");
        codigo.append("}\n");
        return codigo.toString();
    }

    @Override
    public String visitDeclaracoes(NossaGramaticaParser.DeclaracoesContext ctx) {
        for (NossaGramaticaParser.Decl_local_globalContext declCtx : ctx.decl_local_global()) {
            visit(declCtx);
        }
        return null;
    }

    @Override
    public String visitDecl_local_global(NossaGramaticaParser.Decl_local_globalContext ctx) {
        if(ctx.declaracao_local() != null) {
            visit(ctx.declaracao_local());
        } else if (ctx.declaracao_global() != null) {
            visit(ctx.declaracao_global());
        }
        return null;
    }


    @Override
    public String visitCorpo(NossaGramaticaParser.CorpoContext ctx) {
        for (NossaGramaticaParser.Declaracao_localContext declCtx : ctx.declaracao_local()) {
            codigo.append("\t");
            visit(declCtx);
        }
        for (NossaGramaticaParser.CmdContext cmdCtx : ctx.cmd()) {
            codigo.append("\t");
            visit(cmdCtx);
            codigo.append("\n");
        }
        return null;
    }

    @Override
    public String visitDeclaracao_local(NossaGramaticaParser.Declaracao_localContext ctx) {
        if (ctx.variavel() != null) {
            visit(ctx.variavel());
        } else if (ctx.CONSTANTE() != null) {
            String constName = ctx.IDENT().getText();
            AnalisadorSemantico.TipoLA laTypeConst = tabelaDeSimbolos.getTipoDeSimbolo(constName);
            String cType = getCType(laTypeConst);
            String valor = visitValor_constante(ctx.valor_constante());
            codigo.append("const ").append(cType).append(" ").append(constName).append(" = ").append(valor).append(";\n");

        } else if (ctx.TIPO() != null) {
            String typeName = ctx.IDENT().getText();
            if (ctx.tipo().registro() != null) {
                codigo.append("typedef struct {\n");
                for (NossaGramaticaParser.VariavelContext varCtx : ctx.tipo().registro().variavel()) {
                    AnalisadorSemantico.TipoLA laType = tabelaDeSimbolos.getTipoDeSimboloNoRegistro(typeName, varCtx.identificador(0).IDENT(0).getText());
                    String cFieldType = getCType(laType);
                    for(NossaGramaticaParser.IdentificadorContext idCtx : varCtx.identificador()){
                        String fieldName = idCtx.IDENT(0).getText();
                        if (laType == AnalisadorSemantico.TipoLA.LITERAL) {
                            codigo.append("\t\tchar ").append(fieldName).append("[80];\n");
                        } else {
                            codigo.append("\t\t").append(cFieldType).append(" ").append(fieldName).append(";\n");
                        }
                    }
                }
                codigo.append("} ").append(typeName).append(";\n");
            }
        }
        return null;
    }

    @Override
    public String visitVariavel(NossaGramaticaParser.VariavelContext ctx) {
        AnalisadorSemantico.TipoLA laType = tabelaDeSimbolos.getTipoDeVariavelDeclarada(ctx);
        String cType = getCType(laType);

        if (laType instanceof AnalisadorSemantico.Registro && ((AnalisadorSemantico.Registro) laType).nomeOriginalDoTipo == null) {
            codigo.append("struct {\n");
            for(AnalisadorSemantico.Simbolo campo : ((AnalisadorSemantico.Registro) laType).campos.values()){
                String cFieldType = getCType(campo.tipo);
                if(campo.tipo == AnalisadorSemantico.TipoLA.LITERAL){
                    codigo.append("\t\tchar ").append(campo.nome).append("[80];\n");
                } else {
                    codigo.append("\t\t").append(cFieldType).append(" ").append(campo.nome).append(";\n");
                }
            }
            codigo.append("\t} ");
            for (int i = 0; i < ctx.identificador().size(); i++) {
                codigo.append(visitIdentificador(ctx.identificador(i)));
                if (i < ctx.identificador().size() - 1) {
                    codigo.append(";\n\tstruct /* anonymous */ ");
                }
            }

        } else {
            codigo.append(cType).append(" ");
            for (int i = 0; i < ctx.identificador().size(); i++) {
                String varName = visitIdentificador(ctx.identificador(i));
                if (laType == AnalisadorSemantico.TipoLA.LITERAL && ctx.identificador(i).dimensao().exp_aritmetica().isEmpty()) {
                    codigo.append(varName).append("[80]");
                } else {
                    codigo.append(varName);
                }
                if (i < ctx.identificador().size() - 1) {
                    codigo.append(", ");
                }
            }
        }
        codigo.append(";\n");
        return null;
    }

    @Override
    public String visitIdentificador(NossaGramaticaParser.IdentificadorContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(ctx.IDENT(0).getText());
        for(int i = 1; i < ctx.IDENT().size(); i++) {
            sb.append(".").append(ctx.IDENT(i).getText());
        }
        for(NossaGramaticaParser.Exp_aritmeticaContext expCtx : ctx.dimensao().exp_aritmetica()){
            sb.append("[").append(visitExp_aritmetica(expCtx)).append("]");
        }
        return sb.toString();
    }


    @Override
    public String visitCmdLeia(NossaGramaticaParser.CmdLeiaContext ctx) {
        codigo.append("scanf(\"");
        List<String> args = new ArrayList<>();
        for (int i = 0; i < ctx.identificador().size(); i++) {
            NossaGramaticaParser.IdentificadorContext idCtx = ctx.identificador(i);
            String varName = visitIdentificador(idCtx);
            AnalisadorSemantico.TipoLA tipo = tabelaDeSimbolos.getTipoDeIdentificador(idCtx, ctx.PONTEIRO(i) != null);

            if (tipo == AnalisadorSemantico.TipoLA.INTEIRO) codigo.append("%d");
            else if (tipo == AnalisadorSemantico.TipoLA.REAL) codigo.append("%f");
            else if (tipo == AnalisadorSemantico.TipoLA.LITERAL) codigo.append("%s");

            if (ctx.PONTEIRO(i) != null || tipo == AnalisadorSemantico.TipoLA.LITERAL) {
                args.add(varName);
            } else {
                args.add("&" + varName);
            }
            if (i < ctx.identificador().size() - 1) codigo.append(" ");
        }
        codigo.append("\"");
        for(String arg : args) {
            codigo.append(", ").append(arg);
        }
        codigo.append(");");
        return null;
    }

    @Override
    public String visitCmdEscreva(NossaGramaticaParser.CmdEscrevaContext ctx) {
        StringBuilder formatString = new StringBuilder();
        List<String> args = new ArrayList<>();
        for (int i = 0; i < ctx.expressao().size(); i++) {
            NossaGramaticaParser.ExpressaoContext expCtx = ctx.expressao(i);
            AnalisadorSemantico.TipoLA tipo = tabelaDeSimbolos.verificarTipoExpressao(expCtx);
            String expStr = visit(expCtx);

            if (tipo == AnalisadorSemantico.TipoLA.LITERAL && expStr.startsWith("\"") && expStr.endsWith("\"")) {
                formatString.append(expStr.substring(1, expStr.length() - 1));
            } else {
                if (tipo == AnalisadorSemantico.TipoLA.INTEIRO) formatString.append("%d");
                else if (tipo == AnalisadorSemantico.TipoLA.REAL) formatString.append("%f");
                else if (tipo == AnalisadorSemantico.TipoLA.LITERAL) formatString.append("%s");
                else if (tipo == AnalisadorSemantico.TipoLA.LOGICO) formatString.append("%d");
                args.add(expStr);
            }
        }
        codigo.append("printf(\"").append(formatString.toString()).append("\"");
        for(String arg : args) {
            codigo.append(", ").append(arg);
        }
        codigo.append(");");
        return null;
    }

    @Override
    public String visitExpressao(NossaGramaticaParser.ExpressaoContext ctx) {
        if (ctx == null) return "";
        String result = visitTermo_logico(ctx.termo_logico(0));
        for (int i = 0; i < ctx.op_logico_1().size(); i++) {
            result += " || " + visitTermo_logico(ctx.termo_logico(i + 1));
        }
        return result;
    }

    @Override
    public String visitTermo_logico(NossaGramaticaParser.Termo_logicoContext ctx) {
        String result = visitFator_logico(ctx.fator_logico(0));
        for (int i = 0; i < ctx.op_logico_2().size(); i++) {
            result += " && " + visitFator_logico(ctx.fator_logico(i + 1));
        }
        return result;
    }

    @Override
    public String visitFator_logico(NossaGramaticaParser.Fator_logicoContext ctx) {
        String result = visitParcela_logica(ctx.parcela_logica());
        if (ctx.NAO_LOGICO() != null) {
            result = "!(" + result + ")";
        }
        return result;
    }

    @Override
    public String visitParcela_logica(NossaGramaticaParser.Parcela_logicaContext ctx) {
        if (ctx.VERDADEIRO() != null) return "1";
        if (ctx.FALSO() != null) return "0";
        if (ctx.LPAREN() != null) return "(" + visitExpressao(ctx.expressao()) + ")";
        return visitExp_relacional(ctx.exp_relacional());
    }

    @Override
    public String visitExp_relacional(NossaGramaticaParser.Exp_relacionalContext ctx) {
        String esq = visitExp_aritmetica(ctx.exp_aritmetica(0));
        if (ctx.op_relacional() != null) {
            String op = "";
            if (ctx.op_relacional().IGUAL() != null) op = "==";
            else if (ctx.op_relacional().DIFERENTE() != null) op = "!=";
            else if (ctx.op_relacional().MAIOR() != null) op = ">";
            else if (ctx.op_relacional().MENOR() != null) op = "<";
            else if (ctx.op_relacional().MAIOR_IGUAL() != null) op = ">=";
            else if (ctx.op_relacional().MENOR_IGUAL() != null) op = "<=";
            String dir = visitExp_aritmetica(ctx.exp_aritmetica(1));
            AnalisadorSemantico.TipoLA tipoEsq = tabelaDeSimbolos.verificarTipoExpAritmetica(ctx.exp_aritmetica(0));
            AnalisadorSemantico.TipoLA tipoDir = tabelaDeSimbolos.verificarTipoExpAritmetica(ctx.exp_aritmetica(1));
            if (tipoEsq == AnalisadorSemantico.TipoLA.LITERAL && tipoDir == AnalisadorSemantico.TipoLA.LITERAL) {
                if (op.equals("==")) return "strcmp(" + esq + ", " + dir + ") == 0";
                if (op.equals("!=")) return "strcmp(" + esq + ", " + dir + ") != 0";
            }
            return esq + " " + op + " " + dir;
        }
        return esq;
    }

    @Override
    public String visitExp_aritmetica(NossaGramaticaParser.Exp_aritmeticaContext ctx) {
        String result = visitTermo(ctx.termo(0));
        for (int i = 0; i < ctx.op1().size(); i++) {
            result += " " + ctx.op1(i).getText() + " " + visitTermo(ctx.termo(i + 1));
        }
        return result;
    }

    @Override
    public String visitTermo(NossaGramaticaParser.TermoContext ctx) {
        String result = visitFator(ctx.fator(0));
        for (int i = 0; i < ctx.op2().size(); i++) {
            result += " " + ctx.op2(i).getText() + " " + visitFator(ctx.fator(i + 1));
        }
        return result;
    }

    @Override
    public String visitFator(NossaGramaticaParser.FatorContext ctx) {
        String result = visitParcela(ctx.parcela(0));
        for (int i = 0; i < ctx.op3().size(); i++) {
            result += " % " + visitParcela(ctx.parcela(i + 1));
        }
        return result;
    }

    @Override
    public String visitParcela(NossaGramaticaParser.ParcelaContext ctx) {
        String result = "";
        if (ctx.parcela_unario() != null) result = visitParcela_unario(ctx.parcela_unario());
        else result = visitParcela_nao_unario(ctx.parcela_nao_unario());

        if (ctx.op_unario() != null) {
            result = ctx.op_unario().getText() + result;
        }
        return result;
    }

    @Override
    public String visitParcela_unario(NossaGramaticaParser.Parcela_unarioContext ctx) {
        if (ctx.identificador() != null) {
            String identStr = visitIdentificador(ctx.identificador());
            if(ctx.PONTEIRO() != null) return "*" + identStr;
            return identStr;
        }
        if (ctx.IDENT() != null) {
            String funcName = ctx.IDENT().getText();
            StringBuilder args = new StringBuilder();
            if (ctx.expressao() != null) {
                for (int i = 0; i < ctx.expressao().size(); i++) {
                    args.append(visit(ctx.expressao(i)));
                    if (i < ctx.expressao().size() - 1) args.append(", ");
                }
            }
            return funcName + "(" + args.toString() + ")";
        }
        if (ctx.NUM_INT() != null) return ctx.NUM_INT().getText();
        if (ctx.NUM_REAL() != null) return ctx.NUM_REAL().getText();
        if (ctx.LPAREN() != null && ctx.expressao() != null && !ctx.expressao().isEmpty()) {
            return "(" + visit(ctx.expressao(0)) + ")";
        }
        return "";
    }

    @Override
    public String visitParcela_nao_unario(NossaGramaticaParser.Parcela_nao_unarioContext ctx) {
        if (ctx.ENDERECO() != null) return "&" + visitIdentificador(ctx.identificador());
        if (ctx.CADEIA() != null) return ctx.CADEIA().getText();
        return "";
    }


    @Override
    public String visitCmdSe(NossaGramaticaParser.CmdSeContext ctx) {
        codigo.append("if (").append(visit(ctx.expressao())).append(") {\n");
        for (NossaGramaticaParser.CmdContext cmd : ctx.cmd()) {
            if(ctx.SENAO() != null && cmd.getStart().getStartIndex() > ctx.SENAO().getSymbol().getStartIndex()) break;
            codigo.append("\t\t");
            visit(cmd);
            codigo.append("\n");
        }
        codigo.append("\t}");
        if (ctx.SENAO() != null) {
            codigo.append(" else {\n");
            boolean inElse = false;
            for (NossaGramaticaParser.CmdContext cmd : ctx.cmd()) {
                if(cmd.getStart().getStartIndex() > ctx.SENAO().getSymbol().getStartIndex()) inElse = true;
                if(inElse){
                    codigo.append("\t\t");
                    visit(cmd);
                    codigo.append("\n");
                }
            }
            codigo.append("\t}");
        }
        return null;
    }

    @Override
    public String visitCmdAtribuicao(NossaGramaticaParser.CmdAtribuicaoContext ctx) {
        String lhs = "";
        if(ctx.PONTEIRO() != null) lhs += "*";
        lhs += visitIdentificador(ctx.identificador());

        String rhs = visit(ctx.expressao());

        AnalisadorSemantico.TipoLA tipoLHS = tabelaDeSimbolos.getTipoDeIdentificador(ctx.identificador(), ctx.PONTEIRO() != null);
        if(tipoLHS == AnalisadorSemantico.TipoLA.LITERAL){
            codigo.append("strcpy(").append(lhs).append(", ").append(rhs).append(");");
        } else {
            codigo.append(lhs).append(" = ").append(rhs).append(";");
        }
        return null;
    }

    @Override
    public String visitValor_constante(NossaGramaticaParser.Valor_constanteContext ctx) {
        if (ctx.CADEIA() != null) return ctx.CADEIA().getText();
        if (ctx.NUM_INT() != null) return ctx.NUM_INT().getText();
        if (ctx.NUM_REAL() != null) return ctx.NUM_REAL().getText();
        if (ctx.VERDADEIRO() != null) return "1";
        if (ctx.FALSO() != null) return "0";
        return "";
    }

    @Override
    public String visitCmdCaso(NossaGramaticaParser.CmdCasoContext ctx) {
        codigo.append("switch(").append(visitExp_aritmetica(ctx.exp_aritmetica())).append(") {\n");
        for(NossaGramaticaParser.Item_selecaoContext item : ctx.selecao().item_selecao()){
            String[] numeros = item.constantes().getText().split(",");
            for(String numIntervalo : numeros){
                if(numIntervalo.contains("..")){
                    String[] limites = numIntervalo.split("\\.\\.");
                    int inicio = Integer.parseInt(limites[0]);
                    int fim = Integer.parseInt(limites[1]);
                    for(int k=inicio; k<=fim; k++){
                        codigo.append("\t\tcase ").append(k).append(":\n");
                    }
                } else {
                    codigo.append("\t\tcase ").append(numIntervalo).append(":\n");
                }
            }
            for(NossaGramaticaParser.CmdContext cmd : item.cmd()){
                codigo.append("\t\t\t");
                visit(cmd);
                codigo.append("\n");
            }
            codigo.append("\t\t\tbreak;\n");
        }
        if(ctx.SENAO() != null){
            codigo.append("\t\tdefault:\n");
            for(NossaGramaticaParser.CmdContext cmd : ctx.cmd()){
                codigo.append("\t\t\t");
                visit(cmd);
                codigo.append("\n");
            }
        }
        codigo.append("\t}");
        return null;
    }

    @Override
    public String visitCmdPara(NossaGramaticaParser.CmdParaContext ctx) {
        String var = ctx.IDENT().getText();
        String inicio = visitExp_aritmetica(ctx.exp_aritmetica(0));
        String fim = visitExp_aritmetica(ctx.exp_aritmetica(1));
        codigo.append("for(").append(var).append(" = ").append(inicio).append("; ").append(var).append(" <= ").append(fim).append("; ").append(var).append("++) {\n");
        for(NossaGramaticaParser.CmdContext cmd : ctx.cmd()){
            codigo.append("\t\t");
            visit(cmd);
            codigo.append("\n");
        }
        codigo.append("\t}");
        return null;
    }

    @Override
    public String visitCmdEnquanto(NossaGramaticaParser.CmdEnquantoContext ctx) {
        codigo.append("while(").append(visit(ctx.expressao())).append(") {\n");
        for(NossaGramaticaParser.CmdContext cmd : ctx.cmd()){
            codigo.append("\t\t");
            visit(cmd);
            codigo.append("\n");
        }
        codigo.append("\t}");
        return null;
    }

    @Override
    public String visitCmdFaca(NossaGramaticaParser.CmdFacaContext ctx) {
        codigo.append("do {\n");
        for(NossaGramaticaParser.CmdContext cmd : ctx.cmd()){
            codigo.append("\t\t");
            visit(cmd);
            codigo.append("\n");
        }
        codigo.append("\t} while(").append(visit(ctx.expressao())).append(");");
        return null;
    }

    @Override
    public String visitCmdChamada(NossaGramaticaParser.CmdChamadaContext ctx) {
        String funcName = ctx.IDENT().getText();
        StringBuilder args = new StringBuilder();
        if (ctx.expressao() != null) {
            for (int i = 0; i < ctx.expressao().size(); i++) {
                args.append(visit(ctx.expressao(i)));
                if (i < ctx.expressao().size() - 1) args.append(", ");
            }
        }
        codigo.append(funcName).append("(").append(args.toString()).append(");");
        return null;
    }

    @Override
    public String visitCmdRetorne(NossaGramaticaParser.CmdRetorneContext ctx) {
        codigo.append("return ").append(visit(ctx.expressao())).append(";");
        return null;
    }

    @Override
    public String visitDeclaracao_global(NossaGramaticaParser.Declaracao_globalContext ctx) {
        if (ctx.PROCEDIMENTO() != null) {
            String procName = ctx.IDENT().getText();
            codigo.append("void ").append(procName).append("(");
            if(ctx.parametros() != null) visit(ctx.parametros());
            codigo.append(") {\n");
            for(NossaGramaticaParser.Declaracao_localContext decl : ctx.declaracao_local()){
                codigo.append("\t");
                visit(decl);
            }
            for(NossaGramaticaParser.CmdContext cmd : ctx.cmd()){
                codigo.append("\t");
                visit(cmd);
                codigo.append("\n");
            }
            codigo.append("}\n\n");
        } else if (ctx.FUNCAO() != null) {
            String funcName = ctx.IDENT().getText();
            AnalisadorSemantico.Simbolo funcSimbolo = tabelaDeSimbolos.buscarSimboloGlobal(funcName);
            AnalisadorSemantico.TipoLA tipoRetorno = (funcSimbolo != null) ? funcSimbolo.tipo : AnalisadorSemantico.TipoLA.TIPO_INDEFINIDO;

            codigo.append(getCType(tipoRetorno)).append(" ").append(funcName).append("(");
            if(ctx.parametros() != null) visit(ctx.parametros());
            codigo.append(") {\n");
            for(NossaGramaticaParser.Declaracao_localContext decl : ctx.declaracao_local()){
                codigo.append("\t");
                visit(decl);
            }
            for(NossaGramaticaParser.CmdContext cmd : ctx.cmd()){
                codigo.append("\t");
                visit(cmd);
                codigo.append("\n");
            }
            codigo.append("}\n\n");
        }
        return null;
    }

    @Override
    public String visitParametros(NossaGramaticaParser.ParametrosContext ctx) {
        for(int i=0; i<ctx.parametro().size(); i++){
            visit(ctx.parametro(i));
            if(i < ctx.parametro().size() -1) codigo.append(", ");
        }
        return null;
    }

    @Override
    public String visitParametro(NossaGramaticaParser.ParametroContext ctx) {
        AnalisadorSemantico.TipoLA laTypeParam = tabelaDeSimbolos.getTipoDeParametro(ctx);
        String cType = getCType(laTypeParam);
        for(int i=0; i<ctx.identificador().size(); i++){
            String paramName = visitIdentificador(ctx.identificador(i));
            if(ctx.VAR() != null && !(laTypeParam instanceof AnalisadorSemantico.Ponteiro)){
                codigo.append(cType).append(" *").append(paramName);
            } else {
                if (laTypeParam == AnalisadorSemantico.TipoLA.LITERAL) {
                    codigo.append("char ").append(paramName).append("[]");
                } else {
                    codigo.append(cType).append(" ").append(paramName);
                }
            }
            if(i < ctx.identificador().size() -1) codigo.append(", ");
        }
        return null;
    }
}