package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// espaço de movimento dos peões
class Parada extends Local {
    private final TipoParada tipo;
    private final List<Jogador> peoes = new ArrayList<>();
    private final Map<Jogador, Integer> yurtsPorDono = new HashMap<>(); // dono -> quantidade
    private int quantidadeYurts = 0;

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
        return quantidadeYurts < tipo.getCapacidadeYurts();
    }

    void adicionarYurt(Jogador dono) {
        if (!temEspacoParaYurt()) {
            throw new IllegalStateException("A parada " + getNome() + " não tem espaço para outro yurt.");
        }
        yurtsPorDono.put(dono, yurtsPorDono.getOrDefault(dono, 0) + 1);
        quantidadeYurts++;
    }

    boolean possuiYurtDe(Jogador jogador) {
        return yurtsPorDono.containsKey(jogador);
    }

    int getQuantidadeYurts() {
        return quantidadeYurts;
    }
}
