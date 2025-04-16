package br.ufscar.dc.compiladores;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String input = "se 123 + abc";
        CharStream stream = CharStreams.fromString(input);

        NossaLexer lexer = new NossaLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        tokens.fill(); // Carrega todos os tokens

        for (Token token : tokens.getTokens()) {
            System.out.println(token.toString() + " - " +
                    lexer.getVocabulary().getDisplayName(token.getType()));
        }
    }
}