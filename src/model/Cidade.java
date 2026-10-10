package model;

import java.util.ArrayList;
import java.util.List;

// Fonte dos tesouros. Só pode ser atacada depois de revelada e enquanto não for conquistada.
class Cidade extends Local {
    static final int TESOUROS_AO_REVELAR = 4;

    private final List<TipoTesouro> tesouros = new ArrayList<>();
    private boolean revelada = false;
    private Jogador conquistador = null;

    Cidade(String nome, Regiao regiao) {
        super(nome, regiao);
    }

    // Revela a cidade e coloca nela as 4 primeiras peças da pilha de tesouros
    void revelar(List<TipoTesouro> pilhaDeTesouros) {
        revelada = true;
        for (int i = 0; i < TESOUROS_AO_REVELAR && !pilhaDeTesouros.isEmpty(); i++) {
            tesouros.add(pilhaDeTesouros.remove(0));
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
        return revelada && !estaConquistada() && !tesouros.isEmpty();
    }

    boolean possuiTesouro(TipoTesouro tipo) {
        return tesouros.contains(tipo);
    }

    boolean removerTesouro(TipoTesouro tipo) {
        return tesouros.remove(tipo);
    }

    int getQuantidadeTesouros() {
        return tesouros.size();
    }
}
