package model;

// ações impressas na base de uma coluna do tabuleiro do herdeiro: cavalos (movimentos),
// punhos (pegar tributo), Khan, moeda virtual e, só na coluna branca, recuperar as fichas
class AcoesColuna {
    private final int cavalos;
    private final int punhos;
    private final boolean khan;
    private final boolean moedaVirtual;
    private final boolean recuperaFichas;

    AcoesColuna(int cavalos, int punhos, boolean khan, boolean moedaVirtual, boolean recuperaFichas) {
        this.cavalos = cavalos;
        this.punhos = punhos;
        this.khan = khan;
        this.moedaVirtual = moedaVirtual;
        this.recuperaFichas = recuperaFichas;
    }

    int getCavalos() {
        return cavalos;
    }

    int getPunhos() {
        return punhos;
    }

    boolean temKhan() {
        return khan;
    }

    boolean temMoedaVirtual() {
        return moedaVirtual;
    }

    boolean recuperaFichas() {
        return recuperaFichas;
    }
}
