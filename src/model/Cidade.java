package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// cidade com tesouros. Só pode ser atacada depois de revelada e antes de ser conquistada
class Cidade extends Local {
    static final int TESOUROS_AO_REVELAR = 4;

    private final Map<TipoTesouro, Integer> tesouros = new HashMap<>(); // tipo -> quantas peças
    private int quantidadeTesouros = 0;
    private boolean revelada = false;
    private Jogador conquistador = null;

    Cidade(String nome, Regiao regiao) {
        super(nome, regiao);
    }

    // tira as 4 primeiras peças da pilha e coloca na cidade
    void revelar(List<TipoTesouro> pilhaDeTesouros) {
        revelada = true;
        for (int i = 0; i < TESOUROS_AO_REVELAR && !pilhaDeTesouros.isEmpty(); i++) {
            TipoTesouro tipo = pilhaDeTesouros.remove(0);
            tesouros.put(tipo, tesouros.getOrDefault(tipo, 0) + 1);
            quantidadeTesouros++;
        }
    }

    boolean isRevelada() {
        return revelada;
    }

    boolean estaConquistada() {
        return conquistador != null;
    }

    Jogador getConquistador() {
        return conquistador;
    }

    void conquistar(Jogador jogador) {
        conquistador = jogador;
    }

    boolean podeSerAtacada() {
        return revelada && !estaConquistada() && quantidadeTesouros > 0;
    }

    boolean possuiTesouro(TipoTesouro tipo) {
        return tesouros.getOrDefault(tipo, 0) > 0;
    }

    boolean removerTesouro(TipoTesouro tipo) {
        if (!possuiTesouro(tipo)) {
            return false;
        }
        tesouros.put(tipo, tesouros.get(tipo) - 1);
        quantidadeTesouros--;
        return true;
    }

    int getQuantidadeTesouros() {
        return quantidadeTesouros;
    }
}
