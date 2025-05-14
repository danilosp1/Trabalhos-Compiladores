// Generated from /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/Trabalho-Compiladores/T3/src/main/antlr4/br/ufscar/dc/compiladores/NossaGramatica.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link NossaGramaticaParser}.
 */
public interface NossaGramaticaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#programa}.
	 * @param ctx the parse tree
	 */
	void enterPrograma(NossaGramaticaParser.ProgramaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#programa}.
	 * @param ctx the parse tree
	 */
	void exitPrograma(NossaGramaticaParser.ProgramaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#declaracoes}.
	 * @param ctx the parse tree
	 */
	void enterDeclaracoes(NossaGramaticaParser.DeclaracoesContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#declaracoes}.
	 * @param ctx the parse tree
	 */
	void exitDeclaracoes(NossaGramaticaParser.DeclaracoesContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#decl_local_global}.
	 * @param ctx the parse tree
	 */
	void enterDecl_local_global(NossaGramaticaParser.Decl_local_globalContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#decl_local_global}.
	 * @param ctx the parse tree
	 */
	void exitDecl_local_global(NossaGramaticaParser.Decl_local_globalContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#declaracao_local}.
	 * @param ctx the parse tree
	 */
	void enterDeclaracao_local(NossaGramaticaParser.Declaracao_localContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#declaracao_local}.
	 * @param ctx the parse tree
	 */
	void exitDeclaracao_local(NossaGramaticaParser.Declaracao_localContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#variavel}.
	 * @param ctx the parse tree
	 */
	void enterVariavel(NossaGramaticaParser.VariavelContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#variavel}.
	 * @param ctx the parse tree
	 */
	void exitVariavel(NossaGramaticaParser.VariavelContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#identificador}.
	 * @param ctx the parse tree
	 */
	void enterIdentificador(NossaGramaticaParser.IdentificadorContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#identificador}.
	 * @param ctx the parse tree
	 */
	void exitIdentificador(NossaGramaticaParser.IdentificadorContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#dimensao}.
	 * @param ctx the parse tree
	 */
	void enterDimensao(NossaGramaticaParser.DimensaoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#dimensao}.
	 * @param ctx the parse tree
	 */
	void exitDimensao(NossaGramaticaParser.DimensaoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#tipo}.
	 * @param ctx the parse tree
	 */
	void enterTipo(NossaGramaticaParser.TipoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#tipo}.
	 * @param ctx the parse tree
	 */
	void exitTipo(NossaGramaticaParser.TipoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#tipo_basico}.
	 * @param ctx the parse tree
	 */
	void enterTipo_basico(NossaGramaticaParser.Tipo_basicoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#tipo_basico}.
	 * @param ctx the parse tree
	 */
	void exitTipo_basico(NossaGramaticaParser.Tipo_basicoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#tipo_basico_ident}.
	 * @param ctx the parse tree
	 */
	void enterTipo_basico_ident(NossaGramaticaParser.Tipo_basico_identContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#tipo_basico_ident}.
	 * @param ctx the parse tree
	 */
	void exitTipo_basico_ident(NossaGramaticaParser.Tipo_basico_identContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#tipo_estendido}.
	 * @param ctx the parse tree
	 */
	void enterTipo_estendido(NossaGramaticaParser.Tipo_estendidoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#tipo_estendido}.
	 * @param ctx the parse tree
	 */
	void exitTipo_estendido(NossaGramaticaParser.Tipo_estendidoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#registro}.
	 * @param ctx the parse tree
	 */
	void enterRegistro(NossaGramaticaParser.RegistroContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#registro}.
	 * @param ctx the parse tree
	 */
	void exitRegistro(NossaGramaticaParser.RegistroContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#valor_constante}.
	 * @param ctx the parse tree
	 */
	void enterValor_constante(NossaGramaticaParser.Valor_constanteContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#valor_constante}.
	 * @param ctx the parse tree
	 */
	void exitValor_constante(NossaGramaticaParser.Valor_constanteContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#declaracao_global}.
	 * @param ctx the parse tree
	 */
	void enterDeclaracao_global(NossaGramaticaParser.Declaracao_globalContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#declaracao_global}.
	 * @param ctx the parse tree
	 */
	void exitDeclaracao_global(NossaGramaticaParser.Declaracao_globalContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#parametro}.
	 * @param ctx the parse tree
	 */
	void enterParametro(NossaGramaticaParser.ParametroContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#parametro}.
	 * @param ctx the parse tree
	 */
	void exitParametro(NossaGramaticaParser.ParametroContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#parametros}.
	 * @param ctx the parse tree
	 */
	void enterParametros(NossaGramaticaParser.ParametrosContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#parametros}.
	 * @param ctx the parse tree
	 */
	void exitParametros(NossaGramaticaParser.ParametrosContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#corpo}.
	 * @param ctx the parse tree
	 */
	void enterCorpo(NossaGramaticaParser.CorpoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#corpo}.
	 * @param ctx the parse tree
	 */
	void exitCorpo(NossaGramaticaParser.CorpoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#exp_relacional}.
	 * @param ctx the parse tree
	 */
	void enterExp_relacional(NossaGramaticaParser.Exp_relacionalContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#exp_relacional}.
	 * @param ctx the parse tree
	 */
	void exitExp_relacional(NossaGramaticaParser.Exp_relacionalContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#exp_aritmetica}.
	 * @param ctx the parse tree
	 */
	void enterExp_aritmetica(NossaGramaticaParser.Exp_aritmeticaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#exp_aritmetica}.
	 * @param ctx the parse tree
	 */
	void exitExp_aritmetica(NossaGramaticaParser.Exp_aritmeticaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#expressao}.
	 * @param ctx the parse tree
	 */
	void enterExpressao(NossaGramaticaParser.ExpressaoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#expressao}.
	 * @param ctx the parse tree
	 */
	void exitExpressao(NossaGramaticaParser.ExpressaoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#cmd}.
	 * @param ctx the parse tree
	 */
	void enterCmd(NossaGramaticaParser.CmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#cmd}.
	 * @param ctx the parse tree
	 */
	void exitCmd(NossaGramaticaParser.CmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#cmdLeia}.
	 * @param ctx the parse tree
	 */
	void enterCmdLeia(NossaGramaticaParser.CmdLeiaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#cmdLeia}.
	 * @param ctx the parse tree
	 */
	void exitCmdLeia(NossaGramaticaParser.CmdLeiaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#cmdEscreva}.
	 * @param ctx the parse tree
	 */
	void enterCmdEscreva(NossaGramaticaParser.CmdEscrevaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#cmdEscreva}.
	 * @param ctx the parse tree
	 */
	void exitCmdEscreva(NossaGramaticaParser.CmdEscrevaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#cmdSe}.
	 * @param ctx the parse tree
	 */
	void enterCmdSe(NossaGramaticaParser.CmdSeContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#cmdSe}.
	 * @param ctx the parse tree
	 */
	void exitCmdSe(NossaGramaticaParser.CmdSeContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#cmdCaso}.
	 * @param ctx the parse tree
	 */
	void enterCmdCaso(NossaGramaticaParser.CmdCasoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#cmdCaso}.
	 * @param ctx the parse tree
	 */
	void exitCmdCaso(NossaGramaticaParser.CmdCasoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#cmdPara}.
	 * @param ctx the parse tree
	 */
	void enterCmdPara(NossaGramaticaParser.CmdParaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#cmdPara}.
	 * @param ctx the parse tree
	 */
	void exitCmdPara(NossaGramaticaParser.CmdParaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#cmdEnquanto}.
	 * @param ctx the parse tree
	 */
	void enterCmdEnquanto(NossaGramaticaParser.CmdEnquantoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#cmdEnquanto}.
	 * @param ctx the parse tree
	 */
	void exitCmdEnquanto(NossaGramaticaParser.CmdEnquantoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#cmdFaca}.
	 * @param ctx the parse tree
	 */
	void enterCmdFaca(NossaGramaticaParser.CmdFacaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#cmdFaca}.
	 * @param ctx the parse tree
	 */
	void exitCmdFaca(NossaGramaticaParser.CmdFacaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#cmdAtribuicao}.
	 * @param ctx the parse tree
	 */
	void enterCmdAtribuicao(NossaGramaticaParser.CmdAtribuicaoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#cmdAtribuicao}.
	 * @param ctx the parse tree
	 */
	void exitCmdAtribuicao(NossaGramaticaParser.CmdAtribuicaoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#cmdChamada}.
	 * @param ctx the parse tree
	 */
	void enterCmdChamada(NossaGramaticaParser.CmdChamadaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#cmdChamada}.
	 * @param ctx the parse tree
	 */
	void exitCmdChamada(NossaGramaticaParser.CmdChamadaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#cmdRetorne}.
	 * @param ctx the parse tree
	 */
	void enterCmdRetorne(NossaGramaticaParser.CmdRetorneContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#cmdRetorne}.
	 * @param ctx the parse tree
	 */
	void exitCmdRetorne(NossaGramaticaParser.CmdRetorneContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#selecao}.
	 * @param ctx the parse tree
	 */
	void enterSelecao(NossaGramaticaParser.SelecaoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#selecao}.
	 * @param ctx the parse tree
	 */
	void exitSelecao(NossaGramaticaParser.SelecaoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#item_selecao}.
	 * @param ctx the parse tree
	 */
	void enterItem_selecao(NossaGramaticaParser.Item_selecaoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#item_selecao}.
	 * @param ctx the parse tree
	 */
	void exitItem_selecao(NossaGramaticaParser.Item_selecaoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#constantes}.
	 * @param ctx the parse tree
	 */
	void enterConstantes(NossaGramaticaParser.ConstantesContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#constantes}.
	 * @param ctx the parse tree
	 */
	void exitConstantes(NossaGramaticaParser.ConstantesContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#numero_intervalo}.
	 * @param ctx the parse tree
	 */
	void enterNumero_intervalo(NossaGramaticaParser.Numero_intervaloContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#numero_intervalo}.
	 * @param ctx the parse tree
	 */
	void exitNumero_intervalo(NossaGramaticaParser.Numero_intervaloContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#op_unario}.
	 * @param ctx the parse tree
	 */
	void enterOp_unario(NossaGramaticaParser.Op_unarioContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#op_unario}.
	 * @param ctx the parse tree
	 */
	void exitOp_unario(NossaGramaticaParser.Op_unarioContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#termo}.
	 * @param ctx the parse tree
	 */
	void enterTermo(NossaGramaticaParser.TermoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#termo}.
	 * @param ctx the parse tree
	 */
	void exitTermo(NossaGramaticaParser.TermoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#fator}.
	 * @param ctx the parse tree
	 */
	void enterFator(NossaGramaticaParser.FatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#fator}.
	 * @param ctx the parse tree
	 */
	void exitFator(NossaGramaticaParser.FatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#termo_logico}.
	 * @param ctx the parse tree
	 */
	void enterTermo_logico(NossaGramaticaParser.Termo_logicoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#termo_logico}.
	 * @param ctx the parse tree
	 */
	void exitTermo_logico(NossaGramaticaParser.Termo_logicoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#fator_logico}.
	 * @param ctx the parse tree
	 */
	void enterFator_logico(NossaGramaticaParser.Fator_logicoContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#fator_logico}.
	 * @param ctx the parse tree
	 */
	void exitFator_logico(NossaGramaticaParser.Fator_logicoContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#parcela_logica}.
	 * @param ctx the parse tree
	 */
	void enterParcela_logica(NossaGramaticaParser.Parcela_logicaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#parcela_logica}.
	 * @param ctx the parse tree
	 */
	void exitParcela_logica(NossaGramaticaParser.Parcela_logicaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#op1}.
	 * @param ctx the parse tree
	 */
	void enterOp1(NossaGramaticaParser.Op1Context ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#op1}.
	 * @param ctx the parse tree
	 */
	void exitOp1(NossaGramaticaParser.Op1Context ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#op2}.
	 * @param ctx the parse tree
	 */
	void enterOp2(NossaGramaticaParser.Op2Context ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#op2}.
	 * @param ctx the parse tree
	 */
	void exitOp2(NossaGramaticaParser.Op2Context ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#op3}.
	 * @param ctx the parse tree
	 */
	void enterOp3(NossaGramaticaParser.Op3Context ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#op3}.
	 * @param ctx the parse tree
	 */
	void exitOp3(NossaGramaticaParser.Op3Context ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#op_relacional}.
	 * @param ctx the parse tree
	 */
	void enterOp_relacional(NossaGramaticaParser.Op_relacionalContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#op_relacional}.
	 * @param ctx the parse tree
	 */
	void exitOp_relacional(NossaGramaticaParser.Op_relacionalContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#op_logico_1}.
	 * @param ctx the parse tree
	 */
	void enterOp_logico_1(NossaGramaticaParser.Op_logico_1Context ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#op_logico_1}.
	 * @param ctx the parse tree
	 */
	void exitOp_logico_1(NossaGramaticaParser.Op_logico_1Context ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#op_logico_2}.
	 * @param ctx the parse tree
	 */
	void enterOp_logico_2(NossaGramaticaParser.Op_logico_2Context ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#op_logico_2}.
	 * @param ctx the parse tree
	 */
	void exitOp_logico_2(NossaGramaticaParser.Op_logico_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#parcela}.
	 * @param ctx the parse tree
	 */
	void enterParcela(NossaGramaticaParser.ParcelaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#parcela}.
	 * @param ctx the parse tree
	 */
	void exitParcela(NossaGramaticaParser.ParcelaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#parcela_unario}.
	 * @param ctx the parse tree
	 */
	void enterParcela_unario(NossaGramaticaParser.Parcela_unarioContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#parcela_unario}.
	 * @param ctx the parse tree
	 */
	void exitParcela_unario(NossaGramaticaParser.Parcela_unarioContext ctx);
	/**
	 * Enter a parse tree produced by {@link NossaGramaticaParser#parcela_nao_unario}.
	 * @param ctx the parse tree
	 */
	void enterParcela_nao_unario(NossaGramaticaParser.Parcela_nao_unarioContext ctx);
	/**
	 * Exit a parse tree produced by {@link NossaGramaticaParser#parcela_nao_unario}.
	 * @param ctx the parse tree
	 */
	void exitParcela_nao_unario(NossaGramaticaParser.Parcela_nao_unarioContext ctx);
}