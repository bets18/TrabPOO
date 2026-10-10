package model;

// Área entre as rotas que fornece um único tipo de tributo (o ícone impresso no tabuleiro)
class Provincia extends Local {
    static final int MAX_TRIBUTOS = 3;

    private final TipoTributo tipo;
    private final boolean doKhan; // províncias com o ícone da cabeça do Khan
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

    // retorna false se a província já estiver no máximo (3 peças); nesse caso nada muda
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
        quantidade;
        return true;
    }
}
