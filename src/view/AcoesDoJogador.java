package view;

// cliques que a View repassa. O Controller implementa essa interface, assim a View
// não precisa conhecer o Controller
public interface AcoesDoJogador {
    // na cartela: ativa a coluna ou, se o Jochi tem melhoria esperando, coloca ela ali
    void colunaClicada(String coluna);

    void paradaClicada(String parada);

    // botão direito numa parada
    void yurtClicado(String parada);

    // clique em cima do peão que não está jogando (só a Altani tem 2)
    void peaoClicado(int peao);

    // ícone de tributo (pega tributo) ou cabeça do Khan (move o Khan)
    void provinciaClicada(String provincia);

    // cabeça do Khan ao lado da área de melhorias
    void areaDoKhanClicada();

    void cidadeClicada(String cidade);

    // pergaminho do conselheiro (o nome é o da parada dele)
    void conselheiroClicado(String parada);

    // espaço da área de melhorias (0 a 4)
    void melhoriaDaAreaClicada(int espaco);

    void pilhaDeMelhoriasClicada();

    // tesouro escolhido na janela de cartas (ataque, entrega ou bônus)
    void tesouroEscolhido(String tesouro);

    // habilidade do Ogedei: pegar o tesouro pagando uma moeda
    void tesouroComMoedaEscolhido(String tesouro);

    void bonusClicado(String bonus);

    void cancelarSelecaoClicado();

    // habilidade do Chagatai: trocar 1 cavalo por 1 espada
    void habilidadeClicada();

    void verCartelaClicado();

    void verCartasClicado();

    void passarVezClicado();
}
