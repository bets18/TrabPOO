package model;

// peça de melhoria: o símbolo e a cor do fundo, que diz em qual coluna ela entra
class Melhoria {
    private final TipoMelhoria tipo;
    private final Coluna cor; // VERMELHA, VERDE ou AMARELA

    Melhoria(TipoMelhoria tipo, Coluna cor) {
        if (cor == Coluna.BRANCA) {
            throw new IllegalArgumentException("Não existe melhoria de fundo branco.");
        }
        this.tipo = tipo;
        this.cor = cor;
    }

    TipoMelhoria getTipo() {
        return tipo;
    }

    Coluna getCor() {
        return cor;
    }

    // é assim que a API passa a melhoria pra View, ex: "MOEDA:AMARELA"
    String getNome() {
        return tipo.name() + ":" + cor.name();
    }

    @Override
    public String toString() {
        return getNome();
    }
}
