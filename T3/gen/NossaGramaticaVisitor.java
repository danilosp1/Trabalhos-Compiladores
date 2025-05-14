// Generated from /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/Trabalho-Compiladores/T3/src/main/antlr4/br/ufscar/dc/compiladores/NossaGramatica.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link NossaGramaticaParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface NossaGramaticaVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#programa}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrograma(NossaGramaticaParser.ProgramaContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#declaracoes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaracoes(NossaGramaticaParser.DeclaracoesContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#decl_local_global}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecl_local_global(NossaGramaticaParser.Decl_local_globalContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#declaracao_local}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaracao_local(NossaGramaticaParser.Declaracao_localContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#variavel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariavel(NossaGramaticaParser.VariavelContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#identificador}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentificador(NossaGramaticaParser.IdentificadorContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#dimensao}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimensao(NossaGramaticaParser.DimensaoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#tipo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTipo(NossaGramaticaParser.TipoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#tipo_basico}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTipo_basico(NossaGramaticaParser.Tipo_basicoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#tipo_basico_ident}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTipo_basico_ident(NossaGramaticaParser.Tipo_basico_identContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#tipo_estendido}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTipo_estendido(NossaGramaticaParser.Tipo_estendidoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#registro}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegistro(NossaGramaticaParser.RegistroContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#valor_constante}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValor_constante(NossaGramaticaParser.Valor_constanteContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#declaracao_global}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaracao_global(NossaGramaticaParser.Declaracao_globalContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#parametro}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParametro(NossaGramaticaParser.ParametroContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#parametros}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParametros(NossaGramaticaParser.ParametrosContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#corpo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCorpo(NossaGramaticaParser.CorpoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#exp_relacional}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExp_relacional(NossaGramaticaParser.Exp_relacionalContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#exp_aritmetica}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExp_aritmetica(NossaGramaticaParser.Exp_aritmeticaContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#expressao}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressao(NossaGramaticaParser.ExpressaoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmd(NossaGramaticaParser.CmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#cmdLeia}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmdLeia(NossaGramaticaParser.CmdLeiaContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#cmdEscreva}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmdEscreva(NossaGramaticaParser.CmdEscrevaContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#cmdSe}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmdSe(NossaGramaticaParser.CmdSeContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#cmdCaso}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmdCaso(NossaGramaticaParser.CmdCasoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#cmdPara}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmdPara(NossaGramaticaParser.CmdParaContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#cmdEnquanto}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmdEnquanto(NossaGramaticaParser.CmdEnquantoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#cmdFaca}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmdFaca(NossaGramaticaParser.CmdFacaContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#cmdAtribuicao}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmdAtribuicao(NossaGramaticaParser.CmdAtribuicaoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#cmdChamada}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmdChamada(NossaGramaticaParser.CmdChamadaContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#cmdRetorne}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmdRetorne(NossaGramaticaParser.CmdRetorneContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#selecao}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelecao(NossaGramaticaParser.SelecaoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#item_selecao}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitItem_selecao(NossaGramaticaParser.Item_selecaoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#constantes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantes(NossaGramaticaParser.ConstantesContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#numero_intervalo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumero_intervalo(NossaGramaticaParser.Numero_intervaloContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#op_unario}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOp_unario(NossaGramaticaParser.Op_unarioContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#termo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermo(NossaGramaticaParser.TermoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#fator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFator(NossaGramaticaParser.FatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#termo_logico}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermo_logico(NossaGramaticaParser.Termo_logicoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#fator_logico}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFator_logico(NossaGramaticaParser.Fator_logicoContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#parcela_logica}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParcela_logica(NossaGramaticaParser.Parcela_logicaContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#op1}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOp1(NossaGramaticaParser.Op1Context ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#op2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOp2(NossaGramaticaParser.Op2Context ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#op3}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOp3(NossaGramaticaParser.Op3Context ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#op_relacional}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOp_relacional(NossaGramaticaParser.Op_relacionalContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#op_logico_1}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOp_logico_1(NossaGramaticaParser.Op_logico_1Context ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#op_logico_2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOp_logico_2(NossaGramaticaParser.Op_logico_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#parcela}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParcela(NossaGramaticaParser.ParcelaContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#parcela_unario}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParcela_unario(NossaGramaticaParser.Parcela_unarioContext ctx);
	/**
	 * Visit a parse tree produced by {@link NossaGramaticaParser#parcela_nao_unario}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParcela_nao_unario(NossaGramaticaParser.Parcela_nao_unarioContext ctx);
}