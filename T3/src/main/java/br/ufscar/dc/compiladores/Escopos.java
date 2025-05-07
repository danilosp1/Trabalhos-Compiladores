// ESSA CLASSE EH BASICAMENTE UMA PILHA DE TABELAS DE SIMBOLOS
// ELA VAI SERVIR PARA DEFINIR QUAL O ESCOPO DE UM INDENTIFICADOR, EX: VERIFICAR SE O IDENTIFICADOR FOI DECLARADO DENTRO DE UMA FUNCAO, NO ESCOPO GLOBAL, NA MAIN, ETC
// ELA VAI FAZER ISSO EMPILHANDO E DESEMPILHANDO OS ESCOPOS ATUAIS DURANTE A ANALISE DO PROGRAMA

package main.java.br.ufscar.dc.compiladores;

import java.util.Stack;

public class Escopos {
    private Stack<TabelaDeSimbolos> pilha = new Stack<>();

    public void empilharNovoEscopo() {
        pilha.push(new TabelaDeSimbolos());
    }

    public void desempilharEscopo() {
        pilha.pop();
    }

    public TabelaDeSimbolos escopoAtual() {
        return pilha.peek();
    }

    public TabelaDeSimbolos.Entrada buscar(String nome) {
        for (int i = pilha.size() - 1; i >= 0; i--) {
            TabelaDeSimbolos ts = pilha.get(i);
            if (ts.existe(nome)) {
                return ts.buscar(nome);
            }
        }
        return null;
    }
}
