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

            CharStream cs = CharStreams.fromFileName(arquivoEntrada);
            NossaLexer lex = new NossaLexer(cs);

            Token t = null;
            while ((t = lex.nextToken()).getType() != Token.EOF) {
                // String nomeToken = NossaLexer.VOCABULARY.getDisplayName(t.getType());
                Vocabulary vocab = lex.getVocabulary();
                String nomeToken = vocab.getDisplayName(t.getType());
                
                String mensagem;
                switch (nomeToken) {
                    case "ERRO":
                        // símbolo não reconhecido
                        mensagem = "Linha " + t.getLine() + ": " + t.getText() + " - simbolo nao identificado";
                        break;
                    case "CADEIA_NAO_FECHADA":
                        mensagem = "Linha " + t.getLine() + ": cadeia nao fechada";
                        break;
                    case "COMENTARIO_NAO_FECHADO":
                        mensagem = "Linha " + t.getLine() + ": comentario nao fechado";
                        break;
                    default:
                        // saída normal de token
                        mensagem = "<'" + t.getText() + "'," + nomeToken + ">";
                        escritor.write(mensagem);
                        escritor.newLine();
                        System.out.println(mensagem);
                        continue;  // volta ao while
                }
                // se chegou aqui, é algum erro: imprime e interrompe
                System.out.println(mensagem);
                escritor.write(mensagem);
                escritor.newLine();
                break;
            }            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
