// ESSA CLASSE VAI SER RESPONSAVEL POR ARMAZENAR EM UM HASHMAP: 
// OS IDENTIFICADORES (FUNCAO, VARIAVEL, CONSTANTE, ETC)
// SEU NOMES E 
// SEUS TIPOS (INTEIRO, STRING, DECIMAL, ETC)

package main.java.br.ufscar.dc.compiladores;

import java.util.*;

public class TabelaDeSimbolos {
    public enum Categoria {
        VARIAVEL, CONSTANTE, TIPO, FUNCAO, PROCEDIMENTO
    }

    public static class Entrada {
        public String nome;
        public String tipo;
        public Categoria categoria;

        public Entrada(String nome, String tipo, Categoria categoria) {
            this.nome = nome;
            this.tipo = tipo;
            this.categoria = categoria;
        }
    }

    private Map<String, Entrada> simbolos = new LinkedHashMap<>();

    public boolean existe(String nome) {
        return simbolos.containsKey(nome);
    }

    public boolean existeNoEscopoAtual(String nome) {
        return simbolos.containsKey(nome);
    }

    public void adicionar(String nome, String tipo, Categoria categoria) {
        simbolos.put(nome, new Entrada(nome, tipo, categoria));
    }

    public Entrada buscar(String nome) {
        return simbolos.get(nome);
    }

    public Collection<Entrada> getEntradas() {
        return simbolos.values();
    }
}