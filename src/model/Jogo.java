package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Estado de uma partida e regras do manual. Quem decide QUANTAS ações o jogador tem
// (cavalos, punhos) é o Controller; aqui só se valida se cada ação é permitida.
class Jogo {
    static final int MIN_JOGADORES = 2;
    static final int MAX_JOGADORES = 5;
    static final int CIDADES_REVELADAS_NO_INICIO = 3;

    // Marca da trilha do Kurultai que dispara o fim do jogo, por número de jogadores (pág. 14)
    private static final Map<Integer, Integer> VOTOS_PARA_FIM = new HashMap<>();
    static {
        VOTOS_PARA_FIM.put(2, 10);
        VOTOS_PARA_FIM.put(3, 14);
        VOTOS_PARA_FIM.put(4, 16);
        VOTOS_PARA_FIM.put(5, 17);
    }

    private final Tabuleiro tabuleiro;
    private final List<Jogador> jogadores = new ArrayList<>();
    private int indiceJogadorDaVez = 0;

    private final List<Cidade> cidadesOcultas = new ArrayList<>();
    private final List<Cidade> todasAsCidades = new ArrayList<>();
    private List<TipoTesouro> pilhaDeTesouros = new ArrayList<>();

    private Provincia provinciaDoKhan = null;   // null enquanto o Khan estiver fora do tabuleiro
    private boolean khanNaAreaDeMelhorias = false;
    private int votosNoConselho = 0;            // posição da ficha neutra

    // Estado do turno atual
    private final List<Parada> paradasDoTurno = new ArrayList<>();      // inclui a parada de partida
    private final List<Parada> paradasDoMovimento = new ArrayList<>();  // só as alcançadas ao se mover
    private final Map<Cidade, Integer> tesourosTomadosNoTurno = new HashMap<>();

    // A ordem da lista é a ordem de jogo (o 1º é quem tem mais descendentes)
    Jogo(Tabuleiro tabuleiro, List<Herdeiro> herdeirosNaOrdemDeJogo) {
        if (herdeirosNaOrdemDeJogo.size() < MIN_JOGADORES || herdeirosNaOrdemDeJogo.size() > MAX_JOGADORES) {
            throw new IllegalArgumentException("O jogo deve ter de 2 a 5 jogadores.");
        }
        if (tabuleiro.getKarakorum() == null) {
            throw new IllegalStateException("O tabuleiro precisa ter Karakorum.");
        }
        this.tabuleiro = tabuleiro;

        for (int i = 0; i < herdeirosNaOrdemDeJogo.size(); i++) {
            Jogador jogador = new Jogador(herdeirosNaOrdemDeJogo.get(i));
            // 1 moeda para o 1º e o 2º a jogar, 2 moedas para os demais
            jogador.adicionarTributos(TipoTributo.MOEDA, i < 2 ? 1 : 2);
            tabuleiro.getKarakorum().adicionarPeao(jogador);
            jogador.setPosicao(tabuleiro.getKarakorum());
            jogadores.add(jogador);
        }

        // 1 peça de tributo em cada província
        for (Provincia provincia : tabuleiro.getProvincias()) {
            provincia.adicionarTributo();
        }

        iniciarTurno();
    }

    // Revela as 3 primeiras cidades com 4 tesouros cada; as demais ficam na pilha.
    // As listas já devem vir embaralhadas.
    void prepararCidades(List<Cidade> cidadesEmbaralhadas, List<TipoTesouro> tesourosEmbaralhados) {
        pilhaDeTesouros = new ArrayList<>(tesourosEmbaralhados);
        todasAsCidades.clear();
        todasAsCidades.addAll(cidadesEmbaralhadas);
        cidadesOcultas.clear();
        cidadesOcultas.addAll(cidadesEmbaralhadas);
        for (int i = 0; i < CIDADES_REVELADAS_NO_INICIO && !cidadesOcultas.isEmpty(); i++) {
            cidadesOcultas.remove(0).revelar(pilhaDeTesouros);
        }
    }

    Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    int getNumeroDeJogadores() {
        return jogadores.size();
    }

    Jogador getJogador(int indice) {
        return jogadores.get(indice);
    }

    Jogador getJogadorDaVez() {
        return jogadores.get(indiceJogadorDaVez);
    }

    void passarVez() {
        indiceJogadorDaVez = (indiceJogadorDaVez + 1) % jogadores.size();
        iniciarTurno();
    }

    private void iniciarTurno() {
        paradasDoTurno.clear();
        paradasDoTurno.add(getJogadorDaVez().getPosicao()); // tributos valem "antes, durante ou após" o movimento
        paradasDoMovimento.clear();
        tesourosTomadosNoTurno.clear();
    }

    // ---------------------------------------------------------------- Mover

    // Move o peão da vez pelas paradas do caminho (sem incluir a parada de origem).
    // Cada parada do caminho é um movimento. Para ir até ela o peão pode, se quiser, pular
    // paradas com yurts próprios, que não entram no caminho: o jogador nunca esteve nelas,
    // então não servem de base para tributos nem outras ações (manual, págs. 7, 8 e 11).
    // Pode-se passar por paradas ocupadas, mas não terminar numa parada sem espaço.
    boolean moverPeao(List<Parada> caminho, int movimentosDisponiveis) {
        if (caminho.isEmpty() || caminho.size() > movimentosDisponiveis) {
            return false;
        }
        Jogador jogador = getJogadorDaVez();
        Parada origem = jogador.getPosicao();
        Parada atual = origem;
        for (Parada proxima : caminho) {
            if (!alcancaEmUmMovimento(jogador, atual, proxima)) {
                return false;
            }
            atual = proxima;
        }

        Parada destino = atual;
        if (destino != origem && !destino.temEspacoParaPeao()) {
            return false;
        }

        origem.removerPeao(jogador);
        destino.adicionarPeao(jogador);
        jogador.setPosicao(destino);
        paradasDoTurno.addAll(caminho);
        paradasDoMovimento.addAll(caminho);
        return true;
    }

    // Verdadeiro se há rota direta ou uma sequência de rotas cujas paradas intermediárias
    // têm todas yurt do próprio jogador
    private boolean alcancaEmUmMovimento(Jogador jogador, Parada origem, Parada destino) {
        List<Parada> visitadas = new ArrayList<>();
        List<Parada> fila = new ArrayList<>();
        fila.add(origem);
        visitadas.add(origem);
        while (!fila.isEmpty()) {
            Parada atual = fila.remove(0);
            for (Parada vizinha : tabuleiro.getParadasVizinhas(atual)) {
                if (vizinha == destino) {
                    return true;
                }
                if (vizinha.possuiYurtDe(jogador) && !visitadas.contains(vizinha)) {
                    visitadas.add(vizinha);
                    fila.add(vizinha);
                }
            }
        }
        return false;
    }

    private boolean fezParadaAdjacente(Local local) {
        for (Parada parada : paradasDoTurno) {
            if (tabuleiro.saoVizinhos(parada, local)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------- Pegar tributo

    // Pega 1 peça de uma província adjacente a alguma parada feita neste turno
    boolean pegarTributo(Provincia provincia) {
        if (!fezParadaAdjacente(provincia) || !provincia.removerTributo()) {
            return false;
        }
        getJogadorDaVez().adicionarTributos(provincia.getTipo(), 1);
        return true;
    }

    // ----------------------------------------------------------- Usar o Khan

    // O Khan não pode ficar onde está. Numa província, ela e as 2 indicadas pelas setas
    // recebem 1 tributo cada (respeitando o máximo de 3).
    boolean moverKhan(Provincia destino) {
        if (!destino.isDoKhan() || destino == provinciaDoKhan) {
            return false;
        }
        provinciaDoKhan = destino;
        khanNaAreaDeMelhorias = false;
        destino.adicionarTributo();
        for (Provincia afetada : tabuleiro.getAfetadasPeloKhan(destino)) {
            afetada.adicionarTributo();
        }
        return true;
    }

    // A reposição das melhorias será tratada quando as peças de melhoria forem modeladas
    boolean moverKhanParaAreaDeMelhorias() {
        if (khanNaAreaDeMelhorias) {
            return false;
        }
        khanNaAreaDeMelhorias = true;
        provinciaDoKhan = null;
        return true;
    }

    Provincia getProvinciaDoKhan() {
        return provinciaDoKhan;
    }

    // ------------------------------------------------------ Atacar cidades

    // Custa 1 espada para o 1º tesouro da cidade no turno e 2 para cada um dos seguintes.
    // Quem toma o último tesouro conquista a cidade, põe um yurt nela e revela a próxima.
    boolean atacarCidade(Cidade cidade, TipoTesouro tesouro) {
        if (!cidade.podeSerAtacada() || !cidade.possuiTesouro(tesouro) || !fezParadaAdjacente(cidade)) {
            return false;
        }
        Jogador jogador = getJogadorDaVez();
        int custo = getCustoDoProximoAtaque(cidade);
        if (!jogador.gastarTributos(TipoTributo.ESPADA, custo)) {
            return false;
        }

        cidade.removerTesouro(tesouro);
        jogador.adicionarTesouro(tesouro);
        tesourosTomadosNoTurno.put(cidade, tesourosTomadosNoTurno.getOrDefault(cidade, 0) + 1);

        if (cidade.getQuantidadeTesouros() == 0) {
            cidade.conquistar(jogador);
            jogador.retirarYurtDoEstoque(); // o yurt vai para o centro da cidade
            if (!cidadesOcultas.isEmpty()) {
                cidadesOcultas.remove(0).revelar(pilhaDeTesouros);
            }
        }
        return true;
    }

    int getCustoDoProximoAtaque(Cidade cidade) {
        return tesourosTomadosNoTurno.containsKey(cidade) ? 2 : 1;
    }

    // ------------------------------------------------------- Construir yurts

    // Gasta 1 peça de yurt para construir numa parada feita durante o movimento deste turno
    // que ainda tenha espaço (pág. 11)
    boolean construirYurt(Parada parada) {
        Jogador jogador = getJogadorDaVez();
        if (!paradasDoMovimento.contains(parada) || !parada.temEspacoParaYurt()) {
            return false;
        }
        if (jogador.getYurtsNoEstoque() == 0 || !jogador.gastarTributos(TipoTributo.YURT, 1)) {
            return false;
        }
        jogador.retirarYurtDoEstoque();
        parada.adicionarYurt(jogador);
        return true;
    }

    // ------------------------------------------------------- Votos e fim de jogo

    void registrarVotos(Jogador jogador, int quantidade) {
        jogador.adicionarVotos(quantidade);
        votosNoConselho += quantidade; // a ficha neutra anda sempre que alguém ganha votos
    }

    int getVotosNoConselho() {
        return votosNoConselho;
    }

    int getVotosParaFimDeJogo() {
        return VOTOS_PARA_FIM.get(jogadores.size());
    }

    // Disparado pela trilha do Kurultai ou, mais raramente, quando todas as cidades caem
    boolean fimDeJogoDisparado() {
        if (votosNoConselho >= getVotosParaFimDeJogo()) {
            return true;
        }
        if (todasAsCidades.isEmpty()) {
            return false;
        }
        for (Cidade cidade : todasAsCidades) {
            if (!cidade.estaConquistada()) {
                return false;
            }
        }
        return true;
    }
}
