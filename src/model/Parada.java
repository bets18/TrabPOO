package model;

import java.util.ArrayList;
import java.util.List;

// espaço de movimento dos peões
class Parada extends Local {
    private final TipoParada tipo;
    private final List<Jogador> peoes = new ArrayList<>();
    private final List<Jogador> donosDosYurts = new ArrayList<>();

    Parada(String nome, Regiao regiao, TipoParada tipo) {
        super(nome, regiao);
        this.tipo = tipo;
    }

    TipoParada getTipo() {
        return tipo;
    }

    boolean temEspacoParaPeao() {
        return peoes.size() < tipo.getCapacidadePeoes();
    }

    void adicionarPeao(Jogador jogador) {
        if (!temEspacoParaPeao()) {
            throw new IllegalStateException("A parada " + getNome() + " não tem espaço para outro peão.");
        }
        peoes.add(jogador);
    }

    void removerPeao(Jogador jogador) {
        peoes.remove(jogador);
    }

    boolean temEspacoParaYurt() {
        return donosDosYurts.size() < tipo.getCapacidadeYurts();
    }

    void adicionarYurt(Jogador dono) {
        if (!temEspacoParaYurt()) {
            throw new IllegalStateException("A parada " + getNome() + " não tem espaço para outro yurt.");
        }
        donosDosYurts.add(dono);
    }

    boolean possuiYurtDe(Jogador jogador) {
        return donosDosYurts.contains(jogador);
    }

    int getQuantidadeYurts() {
        return donosDosYurts.size();
    }
}
