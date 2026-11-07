package model;

// área entre as rotas. A comum dá um tipo só de tributo (o ícone desenhado nela).
// A do Khan (cabeça do Khan desenhada) não tem tributo: é só um lugar pro Khan ir,
// e as setas dela apontam pras 3 províncias que recebem tributo quando ele chega
class Provincia extends Local {
    static final int MAX_TRIBUTOS = 3;

    private final TipoTributo tipo; // null na província do Khan
    private final boolean doKhan;
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

    // false se já tem 3 ou se a província não dá tributo
    boolean adicionarTributo() {
        if (tipo == null || quantidade >= MAX_TRIBUTOS) {
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
