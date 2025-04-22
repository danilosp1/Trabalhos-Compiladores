package br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.NossaLexer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.antlr.v4.runtime.*;

public class Main {
    public static void main(String args[]) throws Exception {

        if (args.length != 2) {
            System.out.println("\nErro ao executar o programa! =(");
            System.out.println("Utilize: java -jar target/NossaLexica-1.0-SNAPSHOT.jar src/main/java/br/ufscar/dc/compiladores/entradaJander1.txt src/main/java/br/ufscar/dc/compiladores/saidaJander1.txt\n");
            return;
        }

        String arquivoEntrada = args[0];
        String arquivoSaida = args[1];

        System.out.println("\nArquivo de Entrada eh: " + arquivoEntrada);
        System.out.println("Arquivo de Saída eh: " + arquivoSaida + "\n");

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(arquivoSaida))) {

            CharStream cs = CharStreams.fromFileName(args[0]);
            NossaLexer lex = new NossaLexer(cs);

            Token t = null;
            while ((t = lex.nextToken()).getType() != Token.EOF) {
                // String nomeToken = NossaLexer.VOCABULARY.getDisplayName(t.getType());
                Vocabulary vocab = lex.getVocabulary();
                String nomeToken = vocab.getDisplayName(t.getType());

                if (nomeToken.equals("ERRO")) {
                    String mensagem = "Erro na linha " + t.getLine() + ": " + t.getText();
                    System.out.println(mensagem);
                    escritor.write(mensagem);
                    escritor.newLine();
                    break;
                } else if (nomeToken.equals("CADEIA_NAO_FECHADA")) {
                    String mensagem = "Cadeia não fechada na linha " + t.getLine();
                    System.out.println(mensagem);
                    escritor.write(mensagem);
                    escritor.newLine();
                    break;
                } else {
                    String mensagem = "<'" + t.getText() + "'," + nomeToken + ">";
                    System.out.println(mensagem);
                    escritor.write(mensagem);
                    escritor.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
