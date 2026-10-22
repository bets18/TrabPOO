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

    // o yurt inicial do Tolui em Karakorum: Karakorum não aceita yurts construídos, mas este
    // vem da preparação (pág. 20) e só serve para o Tolui pular Karakorum ao se mover
    void adicionarYurtInicial(Jogador dono) {
        yurtsPorDono.put(dono, yurtsPorDono.getOrDefault(dono, 0) + 1);
        quantidadeYurts++;
    }

    boolean possuiYurtDe(Jogador jogador) {
        return yurtsPorDono.containsKey(jogador);
    }

    // um elemento por yurt (o mesmo dono aparece duas vezes se tiver 2 yurts aqui)
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
