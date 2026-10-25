package view;

// cliques que a View repassa. O Controller implementa essa interface, assim a View
// não precisa conhecer o Controller
public interface AcoesDoJogador {
    void colunaClicada(String coluna);

    void paradaClicada(String parada);

    // clique em cima do peão que não está jogando (só a Altani tem 2)
    void peaoClicado(int peao);

    void passarVezClicado();
}
