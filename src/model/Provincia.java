package model;

// área entre as rotas. Cada uma dá um tipo só de tributo (o ícone desenhado nela)
class Provincia extends Local {
    static final int MAX_TRIBUTOS = 3;

    private final TipoTributo tipo;
    private final boolean doKhan; // tem a cabeça do khan desenhada
    private int quantidade = 0;

    Provincia(String nome, Regiao regiao, TipoTributo tipo, boolean doKhan) {
        super(nome, regiao);
        this.tipo = tipo;
        this.doKhan = doKhan;
    }

    TipoTributo getTipo() {
        return tipo;
    }

    boolean isDoKhan() {
        return doKhan;
    }

    int getQuantidade() {
        return quantidade;
    }

    // false se já tem 3
    boolean adicionarTributo() {
        if (quantidade >= MAX_TRIBUTOS) {
            return false;
        }
        quantidade++;
        return true;
    }

    boolean removerTributo() {
        if (quantidade == 0) {
            return false;
        }
        quantidade--;
        return true;
    }
}
