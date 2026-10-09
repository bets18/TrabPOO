package model;

import java.util.HashMap;
import java.util.Map;

class Jogador {
    // hashmap guardando as pecas de tributo (ivan q pediu)
    Map<TipoTributo, Integer> tributos;

    // construtor vazio para os testes funcionarem (new Jogador())
    Jogador() {
        this.tributos = new HashMap<>();
        
        // comeca com 0 de tudo
        tributos.put(TipoTributo.MOEDA, 0);
        tributos.put(TipoTributo.ESPADA, 0);
        tributos.put(TipoTributo.YURT, 0);
    }

    void adicionarTributos(TipoTributo tipo, int quantidade) {
        // atualiza o hashmap
        int atual = tributos.get(tipo);
        tributos.put(tipo, atual + quantidade);
    }
    
    int getQuantidadeTributo(TipoTributo tipo) {
        return tributos.getOrDefault(tipo, 0);
    }
}