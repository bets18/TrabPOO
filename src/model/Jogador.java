package model;

import java.util.HashMap;
import java.util.Map;

class Jogador {
    static final int YURTS_INICIAIS = 12;

    private final Herdeiro herdeiro;
    private final Map<TipoTributo, Integer> tributos = new HashMap<>();
    private final Map<TipoTesouro, Integer> tesouros = new HashMap<>();
    private int yurtsNoEstoque = YURTS_INICIAIS;
    private int votos = 0;
    private Parada posicao;

    Jogador(Herdeiro herdeiro) {
        this.herdeiro = herdeiro;
        for (TipoTributo tipo : TipoTributo.values()) {
            tributos.put(tipo, 0);
        }
    }

    Herdeiro getHerdeiro() {
        return herdeiro;
    }

    void adicionarTributos(TipoTributo tipo, int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade de tributos não pode ser negativa.");
        }
        tributos.put(tipo, tributos.get(tipo) + quantidade);
    }

    // Retorna false (sem alterar nada) se o jogador não tiver peças suficientes
    boolean gastarTributos(TipoTributo tipo, int quantidade) {
        int atual = tributos.get(tipo);
        if (quantidade < 0 || atual < quantidade) {
            return false;
        }
        tributos.put(tipo, atual - quantidade);
        return true;
    }

    int getQuantidadeTributo(TipoTributo tipo) {
        return tributos.get(tipo);
    }

    void adicionarTesouro(TipoTesouro tipo) {
        tesouros.put(tipo, tesouros.getOrDefault(tipo, 0) + 1);
    }

    int getQuantidadeTesouro(TipoTesouro tipo) {
        return tesouros.getOrDefault(tipo, 0);
    }

    int getYurtsNoEstoque() {
        return yurtsNoEstoque;
    }

    boolean retirarYurtDoEstoque() {
        if (yurtsNoEstoque == 0) {
            return false;
        }
        yurtsNoEstoque--;
        return true;
    }

    int getVotos() {
        return votos;
    }

    void adicionarVotos(int quantidade) {
        votos += quantidade;
    }

    Parada getPosicao() {
        return posicao;
    }

    void setPosicao(Parada posicao) {
        this.posicao = posicao;
    }
}
