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

    private static boolean syntaxErrorOccurred = false; // Evita múltiplos erros sintáticos/léxicos
    private static BufferedWriter escritor = null;

    // Listener para tratamento customizado de erros léxicos e sintáticos
    public static class MyCustomErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg,
                                RecognitionException e) {

            if (syntaxErrorOccurred) { // Processa apenas o primeiro erro encontrado
                return;
            }
            syntaxErrorOccurred = true;

            Token t = (Token) offendingSymbol;
            String tokenText = t.getText();
            String primeiraLinhaErro = "";

            Vocabulary vocab = (recognizer instanceof Parser) ? ((Parser)recognizer).getVocabulary() : ((Lexer)recognizer).getVocabulary();
            String symbolicName = vocab.getDisplayName(t.getType()); // Nome do tipo de token com erro

            int reportLine = line;
            if (tokenText.equals("<EOF>")) {
                tokenText = "EOF";
            }

            // Formata a mensagem de erro baseado no tipo do token
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
                } else { // Fallback para console se escritor falhar
                    System.err.println("Erro ao escrever no arquivo: BufferedWriter é nulo.");
                    System.out.println(primeiraLinhaErro);
                    System.out.println("Fim da compilacao");
                }
            } catch (IOException ioe) {
                System.err.println("Erro ao escrever mensagem de erro no arquivo: " + ioe.getMessage());
                System.out.println(primeiraLinhaErro);
                System.out.println("Fim da compilacao");
            }
            throw new ParseCancellationException("Ocorreu um erro de sintaxe/lexico"); // Para o parsing
        }
    }

    public static void main(String args[]) {
        if (args.length != 2) { // Validação de argumentos de linha de comando
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

            parser.removeErrorListeners(); // Remove listeners padrão
            parser.addErrorListener(new MyCustomErrorListener()); // Adiciona listener customizado

            try {
                ParseTree tree = parser.programa(); // Inicia análise sintática

                // Se chegou aqui, não houve erro léxico/sintático que parou o parsing
                AnalisadorSemantico analisador = new AnalisadorSemantico();
                analisador.visit(tree); // Executa análise semântica

                List<String> errosSemanticos = analisador.getErros();

                if (!errosSemanticos.isEmpty()) { // Escreve erros semânticos, se houver
                    for (String erro : errosSemanticos) {
                        writer.write(erro);
                        writer.newLine();
                    }
                }

                // "Fim da compilacao" é sempre escrito se o parsing foi bem-sucedido
                writer.write("Fim da compilacao");
                writer.newLine();
                writer.flush();

            } catch (ParseCancellationException pce) {
                // Erro léxico/sintático já foi tratado e escrito pelo listener
            } catch (RecognitionException re) {
                // Fallback para outros erros ANTLR (raro se o listener funciona)
                if (!syntaxErrorOccurred) {
                    String errorLine = "Linha " + re.getOffendingToken().getLine() + ": erro sintatico critico proximo a " + re.getOffendingToken().getText();
                    writer.write(errorLine);
                    writer.newLine();
                    writer.write("Fim da compilacao");
                    writer.newLine();
                    writer.flush();
                }
            }

        } catch (IOException e) { // Erros de I/O ao abrir/escrever arquivos
            System.err.println("Erro de I/O: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) { // Outros erros inesperados
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            if (escritor != null) { // Tenta registrar um erro genérico no arquivo de saída
                try {
                    escritor.write("Erro inesperado durante a compilacao: " + e.getMessage());
                    escritor.newLine();
                    escritor.write("Fim da compilacao");
                    escritor.newLine();
                    escritor.flush();
                } catch (IOException ioe) {
                    // Ignora falha ao escrever erro genérico
                }
            }
        } finally {
            escritor = null;
        }
    }
}