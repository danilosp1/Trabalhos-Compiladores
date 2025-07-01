package br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.VideoLangLexer;
import br.ufscar.dc.compiladores.VideoLangParser;
import main.java.br.ufscar.dc.compiladores.GeradorDeCodigo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import org.antlr.v4.runtime.misc.ParseCancellationException;


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

            // Erro personalizado (opcional)
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    throw new ParseCancellationException("Erro de sintaxe na linha " + line + ":" + charPositionInLine + " - " + msg);
                }
            });

            ParseTree tree = parser.program();
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
            System.err.println("Erro de compilação: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
