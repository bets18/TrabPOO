package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Jogador {
    static final int YURTS_INICIAIS = 12;

    private final Herdeiro herdeiro;
    private final Map<TipoTributo, Integer> tributos = new HashMap<>();
    private final Map<TipoTesouro, Integer> tesouros = new HashMap<>();
    private int yurtsNoEstoque = YURTS_INICIAIS;
    private int votos = 0;
    // onde está cada peão (só a Altani tem 2). O índice da lista é o número do peão
    private final List<Parada> posicoes = new ArrayList<>();
    private int peaoAtivo = 0; // peão que joga neste turno
    private final Set<Coluna> colunasComFicha = new HashSet<>();

    Jogador(Herdeiro herdeiro) {
        this.herdeiro = herdeiro;
        int peoes = herdeiro == Herdeiro.ALTANI ? 2 : 1;
        for (int i = 0; i < peoes; i++) {
            posicoes.add(null);
        }
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

    // se não tiver peças suficientes devolve false e não gasta nada
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

    // a branca sempre pode ser ativada, porque é ela que devolve as fichas
    boolean podeAtivar(Coluna coluna) {
        return coluna == Coluna.BRANCA || !colunasComFicha.contains(coluna);
    }

    void colocarFicha(Coluna coluna) {
        colunasComFicha.add(coluna);
    }

    void recuperarFichas() {
        colunasComFicha.clear();
    }

    // posição do peão ativo
    Parada getPosicao() {
        return posicoes.get(peaoAtivo);
    }

    void setPosicao(Parada posicao) {
        posicoes.set(peaoAtivo, posicao);
    }

    int getNumeroDePeoes() {
        return posicoes.size();
    }

    Parada getPosicaoDoPeao(int peao) {
        return posicoes.get(peao);
    }

    void setPosicaoDoPeao(int peao, Parada posicao) {
        posicoes.set(peao, posicao);
    }

    int getPeaoAtivo() {
        return peaoAtivo;
    }

    void setPeaoAtivo(int peao) {
        peaoAtivo = peao;
    }
}
