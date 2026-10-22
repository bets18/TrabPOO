package view;

// o que a View avisa quando o jogador clica em algo. O Controller implementa esta interface;
// assim a View não depende do Controller, só desta "porta".
public interface AcoesDoJogador {
    void colunaClicada(String coluna);

    void paradaClicada(String parada);

    void passarVezClicado();
}
