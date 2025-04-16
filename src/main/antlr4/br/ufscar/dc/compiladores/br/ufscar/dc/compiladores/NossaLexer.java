// Generated from /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/NossaLexica/src/main/antlr4/br/ufscar/dc/compiladores/NossaLexer.g4 by ANTLR 4.13.2
package br.ufscar.dc.compiladores;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class NossaLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ALGORITMO=1, DECLARE=2, LITERAL=3, ESCREVA=4, INTEIRO=5, LEIA=6, FIM_ALGORITMO=7, 
		COLON=8, LPAREN=9, RPAREN=10, COMMA=11, IDENT=12, CADEIA=13, COMENTARIO=14, 
		WS=15;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"ALGORITMO", "DECLARE", "LITERAL", "ESCREVA", "INTEIRO", "LEIA", "FIM_ALGORITMO", 
			"COLON", "LPAREN", "RPAREN", "COMMA", "IDENT", "ESC_SEQ", "CADEIA", "COMENTARIO", 
			"WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'algoritmo'", "'declare'", "'literal'", "'escreva'", "'inteiro'", 
			"'leia'", "'fim_algoritmo'", "':'", "'('", "')'", "','"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ALGORITMO", "DECLARE", "LITERAL", "ESCREVA", "INTEIRO", "LEIA", 
			"FIM_ALGORITMO", "COLON", "LPAREN", "RPAREN", "COMMA", "IDENT", "CADEIA", 
			"COMENTARIO", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public NossaLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "NossaLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u000f\u0096\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001"+
		"\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0005\u000b"+
		"i\b\u000b\n\u000b\f\u000bl\t\u000b\u0001\f\u0001\f\u0001\f\u0001\r\u0001"+
		"\r\u0001\r\u0005\rt\b\r\n\r\f\rw\t\r\u0001\r\u0001\r\u0001\r\u0001\r\u0005"+
		"\r}\b\r\n\r\f\r\u0080\t\r\u0001\r\u0003\r\u0083\b\r\u0001\u000e\u0001"+
		"\u000e\u0005\u000e\u0087\b\u000e\n\u000e\f\u000e\u008a\t\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0004\u000f\u0091\b\u000f"+
		"\u000b\u000f\f\u000f\u0092\u0001\u000f\u0001\u000f\u0001\u0088\u0000\u0010"+
		"\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r"+
		"\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\u0000\u001b\r"+
		"\u001d\u000e\u001f\u000f\u0001\u0000\u0004\u0003\u0000AZ__az\u0004\u0000"+
		"09AZ__az\u0003\u0000\n\n\'\'\\\\\u0003\u0000\t\n\r\r  \u009c\u0000\u0001"+
		"\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005"+
		"\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001"+
		"\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000"+
		"\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000"+
		"\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000"+
		"\u0000\u0000\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000"+
		"\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000"+
		"\u0000\u0000\u0001!\u0001\u0000\u0000\u0000\u0003+\u0001\u0000\u0000\u0000"+
		"\u00053\u0001\u0000\u0000\u0000\u0007;\u0001\u0000\u0000\u0000\tC\u0001"+
		"\u0000\u0000\u0000\u000bK\u0001\u0000\u0000\u0000\rP\u0001\u0000\u0000"+
		"\u0000\u000f^\u0001\u0000\u0000\u0000\u0011`\u0001\u0000\u0000\u0000\u0013"+
		"b\u0001\u0000\u0000\u0000\u0015d\u0001\u0000\u0000\u0000\u0017f\u0001"+
		"\u0000\u0000\u0000\u0019m\u0001\u0000\u0000\u0000\u001b\u0082\u0001\u0000"+
		"\u0000\u0000\u001d\u0084\u0001\u0000\u0000\u0000\u001f\u0090\u0001\u0000"+
		"\u0000\u0000!\"\u0005a\u0000\u0000\"#\u0005l\u0000\u0000#$\u0005g\u0000"+
		"\u0000$%\u0005o\u0000\u0000%&\u0005r\u0000\u0000&\'\u0005i\u0000\u0000"+
		"\'(\u0005t\u0000\u0000()\u0005m\u0000\u0000)*\u0005o\u0000\u0000*\u0002"+
		"\u0001\u0000\u0000\u0000+,\u0005d\u0000\u0000,-\u0005e\u0000\u0000-.\u0005"+
		"c\u0000\u0000./\u0005l\u0000\u0000/0\u0005a\u0000\u000001\u0005r\u0000"+
		"\u000012\u0005e\u0000\u00002\u0004\u0001\u0000\u0000\u000034\u0005l\u0000"+
		"\u000045\u0005i\u0000\u000056\u0005t\u0000\u000067\u0005e\u0000\u0000"+
		"78\u0005r\u0000\u000089\u0005a\u0000\u00009:\u0005l\u0000\u0000:\u0006"+
		"\u0001\u0000\u0000\u0000;<\u0005e\u0000\u0000<=\u0005s\u0000\u0000=>\u0005"+
		"c\u0000\u0000>?\u0005r\u0000\u0000?@\u0005e\u0000\u0000@A\u0005v\u0000"+
		"\u0000AB\u0005a\u0000\u0000B\b\u0001\u0000\u0000\u0000CD\u0005i\u0000"+
		"\u0000DE\u0005n\u0000\u0000EF\u0005t\u0000\u0000FG\u0005e\u0000\u0000"+
		"GH\u0005i\u0000\u0000HI\u0005r\u0000\u0000IJ\u0005o\u0000\u0000J\n\u0001"+
		"\u0000\u0000\u0000KL\u0005l\u0000\u0000LM\u0005e\u0000\u0000MN\u0005i"+
		"\u0000\u0000NO\u0005a\u0000\u0000O\f\u0001\u0000\u0000\u0000PQ\u0005f"+
		"\u0000\u0000QR\u0005i\u0000\u0000RS\u0005m\u0000\u0000ST\u0005_\u0000"+
		"\u0000TU\u0005a\u0000\u0000UV\u0005l\u0000\u0000VW\u0005g\u0000\u0000"+
		"WX\u0005o\u0000\u0000XY\u0005r\u0000\u0000YZ\u0005i\u0000\u0000Z[\u0005"+
		"t\u0000\u0000[\\\u0005m\u0000\u0000\\]\u0005o\u0000\u0000]\u000e\u0001"+
		"\u0000\u0000\u0000^_\u0005:\u0000\u0000_\u0010\u0001\u0000\u0000\u0000"+
		"`a\u0005(\u0000\u0000a\u0012\u0001\u0000\u0000\u0000bc\u0005)\u0000\u0000"+
		"c\u0014\u0001\u0000\u0000\u0000de\u0005,\u0000\u0000e\u0016\u0001\u0000"+
		"\u0000\u0000fj\u0007\u0000\u0000\u0000gi\u0007\u0001\u0000\u0000hg\u0001"+
		"\u0000\u0000\u0000il\u0001\u0000\u0000\u0000jh\u0001\u0000\u0000\u0000"+
		"jk\u0001\u0000\u0000\u0000k\u0018\u0001\u0000\u0000\u0000lj\u0001\u0000"+
		"\u0000\u0000mn\u0005\\\u0000\u0000no\u0005\'\u0000\u0000o\u001a\u0001"+
		"\u0000\u0000\u0000pu\u0005\"\u0000\u0000qt\u0003\u0019\f\u0000rt\b\u0002"+
		"\u0000\u0000sq\u0001\u0000\u0000\u0000sr\u0001\u0000\u0000\u0000tw\u0001"+
		"\u0000\u0000\u0000us\u0001\u0000\u0000\u0000uv\u0001\u0000\u0000\u0000"+
		"vx\u0001\u0000\u0000\u0000wu\u0001\u0000\u0000\u0000x\u0083\u0005\"\u0000"+
		"\u0000y~\u0005\'\u0000\u0000z}\u0003\u0019\f\u0000{}\b\u0002\u0000\u0000"+
		"|z\u0001\u0000\u0000\u0000|{\u0001\u0000\u0000\u0000}\u0080\u0001\u0000"+
		"\u0000\u0000~|\u0001\u0000\u0000\u0000~\u007f\u0001\u0000\u0000\u0000"+
		"\u007f\u0081\u0001\u0000\u0000\u0000\u0080~\u0001\u0000\u0000\u0000\u0081"+
		"\u0083\u0005\'\u0000\u0000\u0082p\u0001\u0000\u0000\u0000\u0082y\u0001"+
		"\u0000\u0000\u0000\u0083\u001c\u0001\u0000\u0000\u0000\u0084\u0088\u0005"+
		"{\u0000\u0000\u0085\u0087\t\u0000\u0000\u0000\u0086\u0085\u0001\u0000"+
		"\u0000\u0000\u0087\u008a\u0001\u0000\u0000\u0000\u0088\u0089\u0001\u0000"+
		"\u0000\u0000\u0088\u0086\u0001\u0000\u0000\u0000\u0089\u008b\u0001\u0000"+
		"\u0000\u0000\u008a\u0088\u0001\u0000\u0000\u0000\u008b\u008c\u0005}\u0000"+
		"\u0000\u008c\u008d\u0001\u0000\u0000\u0000\u008d\u008e\u0006\u000e\u0000"+
		"\u0000\u008e\u001e\u0001\u0000\u0000\u0000\u008f\u0091\u0007\u0003\u0000"+
		"\u0000\u0090\u008f\u0001\u0000\u0000\u0000\u0091\u0092\u0001\u0000\u0000"+
		"\u0000\u0092\u0090\u0001\u0000\u0000\u0000\u0092\u0093\u0001\u0000\u0000"+
		"\u0000\u0093\u0094\u0001\u0000\u0000\u0000\u0094\u0095\u0006\u000f\u0000"+
		"\u0000\u0095 \u0001\u0000\u0000\u0000\t\u0000jsu|~\u0082\u0088\u0092\u0001"+
		"\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}