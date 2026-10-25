package model;

// quantos peões e quantos yurts cabem em cada tipo de parada
enum TipoParada {
    SIMPLES(1, 1),
    DUPLA(2, 2),                        // parada de conselheiro
    KARAKORUM(Integer.MAX_VALUE, 0);    // cabe todo mundo, mas não pode ter yurt

    private final int capacidadePeoes;
    private final int capacidadeYurts;

    TipoParada(int capacidadePeoes, int capacidadeYurts) {
        this.capacidadePeoes = capacidadePeoes;
        this.capacidadeYurts = capacidadeYurts;
    }

    int getCapacidadePeoes() {
        return capacidadePeoes;
    }

    int getCapacidadeYurts() {
        return capacidadeYurts;
    }
}
