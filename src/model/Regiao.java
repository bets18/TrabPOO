package model;

// cada região corresponde à coluna da mesma cor no tabuleiro do herdeiro
// (Rússia amarela, Pérsia verde e China vermelha)
enum Regiao {
    RUSSIA(Coluna.AMARELA),
    CHINA(Coluna.VERMELHA),
    PERSIA(Coluna.VERDE);

    private final Coluna coluna;

    Regiao(Coluna coluna) {
        this.coluna = coluna;
    }

    Coluna getColuna() {
        return coluna;
    }
}
