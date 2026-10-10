package model;

// capacidades de cada tipo de parada (manual, págs. 7 e 11)
enum TipoParada {
    SIMPLES(1, 1),
    DUPLA(2, 2),                        // paradas de conselheiro: cabem 2 peões e 2 yurts
    KARAKORUM(Integer.MAX_VALUE, 0);    // cabem todos os peões e não pode ter yurts

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
