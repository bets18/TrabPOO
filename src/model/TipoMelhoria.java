package model;

// símbolo desenhado na peça de melhoria. As 5 de tesouro valem como um tesouro virtual
// daquele tipo, e cada uma conta como um símbolo diferente nas cartas secretas
enum TipoMelhoria {
    MOEDA(null),
    ESPADA(null),
    YURT(null),
    PUNHO(null),
    CAVALOS(null), // 2 movimentos
    PELE(TipoTesouro.PELE),
    FERRO(TipoTesouro.FERRO),
    CARNE(TipoTesouro.CARNE),
    GRAOS(TipoTesouro.GRAOS),
    LA(TipoTesouro.LA);

    private final TipoTesouro tesouro;

    TipoMelhoria(TipoTesouro tesouro) {
        this.tesouro = tesouro;
    }

    // null se a melhoria não for de tesouro
    TipoTesouro getTesouro() {
        return tesouro;
    }

    // o que a carta secreta do Cavaleiro conta
    boolean temCavaloOuPunho() {
        return this == CAVALOS || this == PUNHO;
    }
}
