package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// parada do mapa, onde os peões andam
class Parada extends Local {
    private final TipoParada tipo;
    private final List<Jogador> peoes = new ArrayList<>();
    private final Map<Jogador, Integer> yurtsPorDono = new HashMap<>(); // dono -> quantos yurts
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

    // só pro yurt que o Tolui já começa em Karakorum. Karakorum não aceita yurt
    // construído, então esse não passa pela checagem de espaço
    void adicionarYurtInicial(Jogador dono) {
        yurtsPorDono.put(dono, yurtsPorDono.getOrDefault(dono, 0) + 1);
        quantidadeYurts++;
    }

    boolean possuiYurtDe(Jogador jogador) {
        return yurtsPorDono.containsKey(jogador);
    }

    // um item por yurt (se alguém tem 2 aqui, aparece 2 vezes)
    List<Jogador> getDonosDosYurts() {
        List<Jogador> donos = new ArrayList<>();
        for (Map.Entry<Jogador, Integer> entrada : yurtsPorDono.entrySet()) {
            for (int i = 0; i < entrada.getValue(); i++) {
                donos.add(entrada.getKey());
            }
        }
        return donos;
    }

    int getQuantidadeYurts() {
        return quantidadeYurts;
    }
}
