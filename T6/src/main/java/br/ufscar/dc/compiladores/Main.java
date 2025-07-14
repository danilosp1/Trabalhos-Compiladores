package br.ufscar.dc.compiladores;

import main.java.br.ufscar.dc.compiladores.AnalisadorSemantico;
import main.java.br.ufscar.dc.compiladores.GeradorDeCodigo;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    // Flag para indicar se um erro já foi reportado
    private static boolean erroEncontrado = false;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java Main <arquivo_entrada> <arquivo_saida>");
            return;
        }

        String arquivoEntrada = args[0];
        String arquivoSaida = args[1];

        // Usamos um try-with-resources para garantir que o writer seja fechado.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoSaida))) {
            CharStream input = CharStreams.fromFileName(arquivoEntrada);
            VideoLangLexer lexer = new VideoLangLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // Carrega todos os tokens para podermos inspecioná-los
            tokens.fill();

            // --- VERIFICAÇÃO DE ERROS LÉXICOS ---
            for (Token token : tokens.getTokens()) {
                String tokenName = lexer.getVocabulary().getSymbolicName(token.getType());
                String erroMsg = null;

                // 1. Verifica se uma STRING não foi fechada
                if (token.getType() == VideoLangLexer.STRING) {
                    String textoToken = token.getText();
                    // Se a string não termina com aspas, ela não foi fechada
                    if (!textoToken.endsWith("\"") && !textoToken.endsWith("'")) {
                        erroMsg = "Linha " + token.getLine() + ": string nao fechada";
                    }
                }
                // 2. Verifica se um caractere é inválido
                else if ("ERRO_CARACTERE_INVALIDO".equals(tokenName)) {
                    erroMsg = "Linha " + token.getLine() + ": " + token.getText() + " - simbolo nao identificado";
                }

                // Se encontramos um erro, reportamos e paramos
                if (erroMsg != null) {
                    reportarErro(writer, erroMsg);
                    return; // Sai do método main
                }
            }

            // Se chegamos aqui, não há erros léxicos. Prosseguimos para o parser.
            VideoLangParser parser = new VideoLangParser(tokens);

            // Configura o error listener para erros sintáticos
            parser.removeErrorListeners();
            MyCustomErrorListener mcel = new MyCustomErrorListener(writer);
            parser.addErrorListener(mcel);

            ParseTree tree = parser.program();
            
            // Se um erro sintático foi encontrado pelo listener, a flag estará true
            if (erroEncontrado) {
                return;
            }

            // --- ANÁLISE SEMÂNTICA ---
            AnalisadorSemantico semantico = new AnalisadorSemantico();
            semantico.visit(tree);

            if (!semantico.getErros().isEmpty()) {
                for (AnalisadorSemantico.ErroSemantico erro : semantico.getErros()) {
                    reportarErro(writer, erro.toString());
                }
                // Como pode haver múltiplos erros semânticos, a finalização é feita fora do loop
                writer.write("Fim da compilacao\n");
                return;
            }
            
            // --- GERAÇÃO DE CÓDIGO (se não houver erros) ---
            GeradorDeCodigo gerador = new GeradorDeCodigo();
            gerador.visit(tree);
            writer.write(gerador.getCodigoGerado());
            System.out.println("Código Python gerado com sucesso em: " + arquivoSaida);

        } catch (IOException e) {
            System.err.println("Erro ao ler ou escrever arquivos: " + e.getMessage());
        } catch (ParseCancellationException e) {
            // Este catch agora é apenas para garantir a parada do parser.
            // A mensagem de erro já foi escrita pelo listener.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Função auxiliar para reportar erros (escreve no arquivo e imprime no console)
    private static void reportarErro(BufferedWriter writer, String mensagem) throws IOException {
        // Imprime no console (stderr)
        System.err.println(mensagem);
        // Escreve no arquivo de saída
        writer.write(mensagem + "\n");
        erroEncontrado = true;
    }

    // Listener customizado para erros SINTÁTICOS
    public static class MyCustomErrorListener extends BaseErrorListener {
        private final BufferedWriter writer;

        public MyCustomErrorListener(BufferedWriter writer) {
            this.writer = writer;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            if (!erroEncontrado) { // Reporta apenas o primeiro erro sintático
                String erroMsg = "Linha " + line + ": erro sintatico proximo a " + ((Token)offendingSymbol).getText();
                try {
                    reportarErro(writer, erroMsg);
                    writer.write("Fim da compilacao\n");
                } catch (IOException ioException) {
                    // Ignora
                }
            }
            // Lança a exceção para parar o parsing imediatamente
            throw new ParseCancellationException();
        }
    }
}