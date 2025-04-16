package br.ufscar.dc.compiladores;

import java.io.IOException;

import br.ufscar.dc.compiladores.NossaLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import br.ufscar.dc.compiladores.NossaLexer;

public class Main {
    public static void main(String args[]) throws Exception {

        try {
            CharStream cs = CharStreams.fromFileName(args[0]);
            NossaLexer lex = new NossaLexer(cs);

            Token t = null;
            while ((t = lex.nextToken()).getType() != Token.EOF) {
                String nomeToken = NossaLexer.VOCABULARY.getDisplayName(t.getType());

                if(nomeToken.equals("ERRO")) {
                    System.out.println("Erro na linha "+t.getLine()+": "+t.getText());
                    break;
                    } else if(nomeToken.equals("CADEIA_NAO_FECHADA")) {
                        System.out.println("Cadeia não fechada na linha "+t.getLine());
                        break;
                    } else {
                System.out.println("<" + nomeToken + "," + t.getText() + ">");
                            }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
