package br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.NossaGramaticaLexer;
import br.ufscar.dc.compiladores.NossaGramaticaParser;
import main.java.br.ufscar.dc.compiladores.AnalisadorSemantico;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class Main {

    // Flag para indicar se um erro já foi reportado
    private static boolean errorOccurred = false;
    private static BufferedWriter escritor = null; // Tornar o escritor acessível para o listener

    // Listener personalizado para tratamento de erros
    public static class MyCustomErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg,
                                RecognitionException e) {

            if (errorOccurred) { // Reporta apenas o primeiro erro
                return;
            }
            errorOccurred = true; // Marca que ocorreu um erro

            Token t = (Token) offendingSymbol;
            String tokenText = t.getText();
            String primeiraLinhaErro = "";

            // Obtém o vocabulário para nomes simbólicos
            Vocabulary vocab = (recognizer instanceof Parser) ? ((Parser)recognizer).getVocabulary() : ((Lexer)recognizer).getVocabulary();
            String symbolicName = vocab.getDisplayName(t.getType());

            // Ajuste para número de linha
            int reportLine = line;
            if (tokenText.equals("<EOF>")) {
                tokenText = "EOF"; // Representação mais limpa para EOF
            }

            // Determina a mensagem de erro com base no tipo
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
                    // Escreve no arquivo
                    escritor.write(primeiraLinhaErro);
                    escritor.newLine();
                    escritor.write("Fim da compilacao");
                    escritor.newLine();
                    escritor.flush(); // Garante que foi escrito imediatamente

                    // Também imprime no console
                    System.out.println(primeiraLinhaErro);
                    System.out.println("Fim da compilacao");
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

            // Lança exceção para parar o parsing imediatamente
            throw new ParseCancellationException("Ocorreu um erro de sintaxe");
        }
    }

    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println("\nErro ao executar o programa! =(");
            System.out.println("Utilize: java -jar target/NossaLexica-1.0-SNAPSHOT.jar <arquivo_entrada> <arquivo_saida>\n");
            return;
        }

        String arquivoEntrada = args[0];
        String arquivoSaida = args[1];

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoSaida))) {
            escritor = writer;
            errorOccurred = false;

            CharStream cs = CharStreams.fromFileName(arquivoEntrada);
            NossaGramaticaLexer lexer = new NossaGramaticaLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            NossaGramaticaParser parser = new NossaGramaticaParser(tokens);

            parser.removeErrorListeners();
            parser.addErrorListener(new MyCustomErrorListener());

            try {
                ParseTree tree = parser.programa();

                if (!errorOccurred) {
                    // Compilação bem sucedida, imprime os tokens reconhecidos
                    // System.out.println("Compilacao bem sucedida. Tokens reconhecidos:");

                    AnalisadorSemantico analisador = new AnalisadorSemantico();
                    analisador.visit(tree);

                    List<String> erros = analisador.getErros();

                    if (erros.isEmpty() == false) { // TEM ERROS SEMANTICOS
                        for (String erro : erros) {
                            writer.write(erro);
                            writer.newLine();
                            System.out.println(erro);
                        }
                        writer.write("Fim da compilacao");
                        writer.newLine();
                        System.out.println("Fim da compilacao");
                    }
                    else { // NAO TEM NENHUM ERRO
                        Vocabulary vocab = lexer.getVocabulary();
                        List<? extends Token> tokenList = tokens.getTokens();

                        for (Token t : tokenList) {
                            if (t.getType() == Token.EOF) {
                                break;
                            }

                            String tokenName = vocab.getDisplayName(t.getType());
                            String outputLine = "<'" + t.getText() + "'," + tokenName + ">";

                            writer.write(outputLine);
                            writer.newLine();
                            System.out.println(outputLine);
                        }
                        writer.flush();
                    }
                }

            } catch (ParseCancellationException pce) {
                // Erro já tratado pelo listener
            } catch (RecognitionException re) {
                if (!errorOccurred) {
                    String errorLine = "Linha " + re.getOffendingToken().getLine() + ": erro sintatico critico proximo a " + re.getOffendingToken().getText();
                    String endLine = "Fim da compilacao";

                    System.err.println("Erro de reconhecimento ANTLR nao capturado: " + re.getMessage());

                    writer.write(errorLine);
                    writer.newLine();
                    writer.write(endLine);
                    writer.newLine();

                    System.out.println(errorLine);
                    System.out.println(endLine);
                }
            }

        } catch (IOException e) {
            System.err.println("Erro de I/O: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            escritor = null;
        }
    }
}