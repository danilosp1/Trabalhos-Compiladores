package br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.NossaGramaticaLexer;
import br.ufscar.dc.compiladores.NossaGramaticaParser;
import main.java.br.ufscar.dc.compiladores.AnalisadorSemantico;
import main.java.br.ufscar.dc.compiladores.GeradorDeCodigo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class Main {

    private static boolean syntaxErrorOccurred = false;
    private static BufferedWriter escritor = null;

    public static class MyCustomErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg,
                                RecognitionException e) {

            if (syntaxErrorOccurred) {
                return;
            }
            syntaxErrorOccurred = true;

            Token t = (Token) offendingSymbol;
            String tokenText = t.getText();
            String primeiraLinhaErro = "";

            Vocabulary vocab = (recognizer instanceof Parser) ? ((Parser)recognizer).getVocabulary() : ((Lexer)recognizer).getVocabulary();
            String symbolicName = vocab.getDisplayName(t.getType());

            int reportLine = line;
            if (tokenText.equals("<EOF>")) {
                tokenText = "EOF";
            }

            switch (symbolicName) {
                case "ERRO":
                    primeiraLinhaErro = "Linha " + reportLine + ": " + tokenText + " - simbolo nao identificado";
                    break;
                case "CADEIA_NAO_FECHADA":
                    primeiraLinhaErro = "Linha " + reportLine + ": cadeia literal nao fechada";
                    break;
                case "COMENTARIO_NAO_FECHADO":
                    primeiraLinhaErro = "Linha " + reportLine + ": comentario nao fechado";
                    break;
                default:
                    primeiraLinhaErro = "Linha " + reportLine + ": erro sintatico proximo a " + tokenText;
                    break;
            }

            try {
                if (escritor != null) {
                    escritor.write(primeiraLinhaErro);
                    escritor.newLine();
                    escritor.write("Fim da compilacao");
                    escritor.newLine();
                    escritor.flush();
                } else {
                    System.err.println("Erro ao escrever no arquivo: BufferedWriter é nulo.");
                    System.out.println(primeiraLinhaErro);
                    System.out.println("Fim da compilacao");
                }
            } catch (IOException ioe) {
                System.err.println("Erro ao escrever mensagem de erro no arquivo: " + ioe.getMessage());
                System.out.println(primeiraLinhaErro);
                System.out.println("Fim da compilacao");
            }
            throw new ParseCancellationException("Ocorreu um erro de sintaxe/lexico");
        }
    }

    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println("\nErro ao executar o programa! =(");
            System.out.println("Utilize: java -jar nome_do_seu_jar.jar <arquivo_entrada> <arquivo_saida>\n");
            return;
        }

        String arquivoEntrada = args[0];
        String arquivoSaida = args[1];

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoSaida))) {
            escritor = writer;
            syntaxErrorOccurred = false;

            CharStream cs = CharStreams.fromFileName(arquivoEntrada);
            NossaGramaticaLexer lexer = new NossaGramaticaLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            NossaGramaticaParser parser = new NossaGramaticaParser(tokens);

            parser.removeErrorListeners();
            parser.addErrorListener(new MyCustomErrorListener());

            try {
                ParseTree tree = parser.programa();

                AnalisadorSemantico analisador = new AnalisadorSemantico();
                analisador.visit(tree);
                List<String> errosSemanticos = analisador.getErros();

                if (!errosSemanticos.isEmpty()) {
                    for (String erro : errosSemanticos) {
                        writer.write(erro);
                        writer.newLine();
                    }
                    writer.write("Fim da compilacao");
                    writer.newLine();
                } else {
                    AnalisadorSemantico.TabelaDeSimbolos tabelaCompleta = analisador.getTabelaDeSimbolosCompleta();
                    GeradorDeCodigo gerador = new GeradorDeCodigo(tabelaCompleta);
                    String codigoC = gerador.visit(tree);
                    writer.write(codigoC);
                }
                writer.flush();

            } catch (ParseCancellationException pce) {
            } catch (RecognitionException re) {
                if (!syntaxErrorOccurred) {
                    String errorLine = "Linha " + re.getOffendingToken().getLine() + ": erro sintatico critico proximo a " + re.getOffendingToken().getText();
                    writer.write(errorLine);
                    writer.newLine();
                    writer.write("Fim da compilacao");
                    writer.newLine();
                    writer.flush();
                }
            }

        } catch (IOException e) {
            System.err.println("Erro de I/O: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            if (escritor != null) {
                try {
                    escritor.write("Erro inesperado durante a compilacao: " + e.getMessage());
                    escritor.newLine();
                    escritor.write("Fim da compilacao");
                    escritor.newLine();
                    escritor.flush();
                } catch (IOException ioe) {
                }
            }
        } finally {
            escritor = null;
        }
    }
}