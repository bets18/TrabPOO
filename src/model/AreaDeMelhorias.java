package model;

import java.util.ArrayList;
import java.util.List;

// os 5 espaços na parte de baixo do tabuleiro e a pilha virada pra baixo. Depois de uma
// compra as peças que sobram andam pra direita e só voltam a ser repostas quando alguém
// leva o khan pra cá
class AreaDeMelhorias {
    static final int ESPACOS = 5;
    // embaixo do 2º espaço tem uma moeda impressa e embaixo do 4º, uma espada
    static final int ESPACO_DA_MOEDA = 1;
    static final int ESPACO_DA_ESPADA = 3;
    static final int TOPO_DA_PILHA = -1;

    private final Melhoria[] espacos = new Melhoria[ESPACOS];
    private final List<Melhoria> pilha;

    // a pilha já vem embaralhada; as 5 primeiras são reveladas
    AreaDeMelhorias(List<Melhoria> pilhaEmbaralhada) {
        pilha = new ArrayList<>(pilhaEmbaralhada);
        repor();
    }

    // null se o espaço estiver vazio (ou se a pilha acabou, no caso do topo)
    Melhoria espiar(int espaco) {
        if (espaco == TOPO_DA_PILHA) {
            return pilha.isEmpty() ? null : pilha.get(0);
        }
        if (espaco < 0 || espaco >= ESPACOS) {
            return null;
        }
        return espacos[espaco];
    }

    // tira a peça do espaço (ou do topo da pilha) e empurra as outras pra direita
    Melhoria retirar(int espaco) {
        Melhoria melhoria = espiar(espaco);
        if (melhoria == null) {
            return null;
        }
        if (espaco == TOPO_DA_PILHA) {
            pilha.remove(0);
        } else {
            espacos[espaco] = null;
            empurrarParaDireita();
        }
        return melhoria;
    }

    boolean espacoVazio(int espaco) {
        return espacos[espaco] == null;
    }

    int getTamanhoDaPilha() {
        return pilha.size();
    }

    // completa os espaços vazios com peças da pilha
    void repor() {
        for (int i = 0; i < ESPACOS && !pilha.isEmpty(); i++) {
            if (espacos[i] == null) {
                espacos[i] = pilha.remove(0);
            }
        }
    }

    // ainda dá pra pegar alguma melhoria (na área ou na pilha)?
    boolean temMelhoria() {
        if (!pilha.isEmpty()) {
            return true;
        }
        for (Melhoria melhoria : espacos) {
            if (melhoria != null) {
                return true;
            }
        }
        return false;
    }

    private void empurrarParaDireita() {
        List<Melhoria> restantes = new ArrayList<>();
        for (Melhoria melhoria : espacos) {
            if (melhoria != null) {
                restantes.add(melhoria);
            }
        }
        int primeiroOcupado = ESPACOS - restantes.size();
        for (int i = 0; i < ESPACOS; i++) {
            espacos[i] = i < primeiroOcupado ? null : restantes.get(i - primeiroOcupado);
        }
    }
}
