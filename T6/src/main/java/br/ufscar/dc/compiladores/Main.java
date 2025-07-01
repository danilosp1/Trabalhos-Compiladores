package br.ufscar.dc.compiladores;

import main.java.br.ufscar.dc.compiladores.AnalisadorSemantico; // Importar
import main.java.br.ufscar.dc.compiladores.GeradorDeCodigo;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java Main <arquivo_entrada> <arquivo_saida>");
            return;
        }

        String arquivoEntrada = args[0];
        String arquivoSaida = args[1];

        try {
            CharStream input = CharStreams.fromFileName(arquivoEntrada);
            VideoLangLexer lexer = new VideoLangLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            VideoLangParser parser = new VideoLangParser(tokens);

            // Erro personalizado
            parser.removeErrorListeners();
            MyCustomErrorListener mcel = new MyCustomErrorListener();
            parser.addErrorListener(mcel);

            ParseTree tree = parser.program();

            // Executa o analisador semântico
            AnalisadorSemantico semantico = new AnalisadorSemantico();
            semantico.visit(tree);

            // Se houver erros semânticos, imprime e para
            if (!semantico.getErros().isEmpty()) {
                for (AnalisadorSemantico.ErroSemantico erro : semantico.getErros()) {
                    System.err.println(erro);
                }
                // Adiciona a "saída" solicitada no enunciado de trabalhos da disciplina
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoSaida))) {
                    for (AnalisadorSemantico.ErroSemantico erro : semantico.getErros()) {
                        writer.write(erro.toString() + "\n");
                    }
                    writer.write("Fim da compilacao\n");
                }
                return; // Para a execução
            }

            // Se não houver erros, executa o gerador de código
            GeradorDeCodigo gerador = new GeradorDeCodigo();
            gerador.visit(tree);

            String codigoPython = gerador.getCodigoGerado();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoSaida))) {
                writer.write(codigoPython);
                System.out.println("Código Python gerado com sucesso em: " + arquivoSaida);
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler ou escrever arquivos: " + e.getMessage());
        } catch (ParseCancellationException e) {
            // Erro sintático capturado pelo MyCustomErrorListener
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoSaida))) {
                writer.write(e.getMessage() + "\n");
                writer.write("Fim da compilacao\n");
            } catch (IOException ioException) {
                // Ignora erros de escrita aqui
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Classe de ErrorListener customizada para o padrão de saída
    public static class MyCustomErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            String erroMsg = "Linha " + line + ": erro sintatico proximo a " + ((Token)offendingSymbol).getText();
            throw new ParseCancellationException(erroMsg);
        }
    }
}