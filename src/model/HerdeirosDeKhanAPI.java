package model;

public class HerdeirosDeKhanAPI {
    private Tabuleiro tabuleiro = new Tabuleiro();

    public boolean validarMovimento(Local origem, Local destino) {
        return tabuleiro.validarMovimento(origem, destino);
    }
}
