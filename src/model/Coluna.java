package model;

// colunas do tabuleiro do herdeiro
enum Coluna {
    VERMELHA, VERDE, AMARELA, BRANCA;

    // as 3 coloridas recebem melhorias compradas e contam na influência das regiões
    boolean isColorida() {
        return this != BRANCA;
    }
}
