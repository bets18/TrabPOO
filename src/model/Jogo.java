package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// estado da partida e as regras. No turno guardamos a coluna ativada e quantos cavalos sobram.
// Os punhos e o khan da coluna ainda não são cobrados, isso fica pra 3ª iteração.
class Jogo {
    static final int MIN_JOGADORES = 2;
    static final int MAX_JOGADORES = 5;
    static final int CIDADES_REVELADAS_NO_INICIO = 3;

    // votos no conselho que disparam o fim, pelo número de jogadores
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

    private Provincia provinciaDoKhan = null;   // null = khan ainda fora do tabuleiro
    private boolean khanNaAreaDeMelhorias = false;
    private int votosNoConselho = 0;            // ficha neutra

    // coisas do turno atual
    private final Set<Parada> paradasDoTurno = new HashSet<>();      // inclui de onde o peão saiu
    private final Set<Parada> paradasDoMovimento = new HashSet<>();  // só onde ele chegou andando
    private final Map<Cidade, Integer> tesourosTomadosNoTurno = new HashMap<>();
    private Coluna colunaDoTurno = null;    // null enquanto não ativou nenhuma
    private int movimentosRestantes = 0;

    // a lista já vem na ordem de jogo
    Jogo(Tabuleiro tabuleiro, List<Herdeiro> herdeirosNaOrdemDeJogo) {
        if (herdeirosNaOrdemDeJogo.size() < MIN_JOGADORES || herdeirosNaOrdemDeJogo.size() > MAX_JOGADORES) {
            throw new IllegalArgumentException("O jogo deve ter de 2 a 5 jogadores.");
        }
        // cada herdeiro é uma cor, não dá pra repetir
        if (new HashSet<>(herdeirosNaOrdemDeJogo).size() != herdeirosNaOrdemDeJogo.size()) {
            throw new IllegalArgumentException("Cada herdeiro só pode aparecer uma vez.");
        }
        if (tabuleiro.getKarakorum() == null) {
            throw new IllegalStateException("O tabuleiro precisa ter Karakorum.");
        }
        this.tabuleiro = tabuleiro;

        for (int i = 0; i < herdeirosNaOrdemDeJogo.size(); i++) {
            Jogador jogador = new Jogador(herdeirosNaOrdemDeJogo.get(i));
            // os dois primeiros começam com 1 moeda, o resto com 2
            jogador.adicionarTributos(TipoTributo.MOEDA, i < 2 ? 1 : 2);
            for (int peao = 0; peao < jogador.getNumeroDePeoes(); peao++) {
                tabuleiro.getKarakorum().adicionarPeao(jogador);
                jogador.setPosicaoDoPeao(peao, tabuleiro.getKarakorum());
            }
            // habilidade do Tolui: já começa com um yurt em Karakorum
            if (jogador.getHerdeiro() == Herdeiro.TOLUI) {
                jogador.retirarYurtDoEstoque();
                tabuleiro.getKarakorum().adicionarYurtInicial(jogador);
            }
            jogadores.add(jogador);
        }

        for (Provincia provincia : tabuleiro.getLocais(Provincia.class)) {
            provincia.adicionarTributo();
        }

        iniciarTurno();
    }

    // revela as 3 primeiras cidades (4 tesouros cada) e o resto fica na pilha.
    // as listas já chegam embaralhadas
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

    int getIndiceJogadorDaVez() {
        return indiceJogadorDaVez;
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
        // a parada de onde saiu também conta pra pegar tributo
        paradasDoTurno.add(getJogadorDaVez().getPosicao());
        paradasDoMovimento.clear();
        tesourosTomadosNoTurno.clear();
        colunaDoTurno = null;
        movimentosRestantes = 0;
    }

    //  ativar coluna

    // coloca a ficha na coluna e o jogador ganha os cavalos dela. A branca devolve todas
    // as fichas. Só dá pra ativar uma por turno e não pode ser coluna que já tem ficha
    boolean ativarColuna(Coluna coluna) {
        Jogador jogador = getJogadorDaVez();
        if (colunaDoTurno != null || !jogador.podeAtivar(coluna)) {
            return false;
        }
        AcoesColuna acoes = jogador.getHerdeiro().getAcoes(coluna);
        colunaDoTurno = coluna;
        movimentosRestantes = acoes.getCavalos();
        if (acoes.recuperaFichas()) {
            jogador.recuperarFichas();
        } else {
            jogador.colocarFicha(coluna);
        }
        return true;
    }

    Coluna getColunaDoTurno() {
        return colunaDoTurno;
    }

    int getMovimentosRestantes() {
        return movimentosRestantes;
    }

    // Altani: escolhe qual dos 2 peões vai jogar. Depois que andou não troca mais
    boolean selecionarPeao(int peao) {
        Jogador jogador = getJogadorDaVez();
        if (peao < 0 || peao >= jogador.getNumeroDePeoes() || !paradasDoMovimento.isEmpty()) {
            return false;
        }
        jogador.setPeaoAtivo(peao);
        paradasDoTurno.clear();
        paradasDoTurno.add(jogador.getPosicao());
        return true;
    }

    // só deixa passar a vez depois de ativar uma coluna
    boolean encerrarTurno() {
        if (colunaDoTurno == null) {
            return false;
        }
        passarVez();
        return true;
    }

    //  mover

    // anda com o peão pelas paradas do caminho (sem a de origem); cada parada é 1 movimento.
    // Os yurts do próprio jogador podem ser pulados e não entram no caminho, então essas
    // paradas não contam pra pegar tributo. Pode passar por parada ocupada, só não pode
    // terminar numa que está cheia
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

    private boolean alcancaEmUmMovimento(Jogador jogador, Parada origem, Parada destino) {
        return alcancaveisEmUmMovimento(jogador, origem).contains(destino);
    }

    // paradas que dá pra alcançar com 1 movimento: as vizinhas e as que ficam depois
    // de yurts do próprio jogador (busca em largura passando só pelos yurts dele)
    private Set<Parada> alcancaveisEmUmMovimento(Jogador jogador, Parada origem) {
        Set<Parada> alcancaveis = new HashSet<>();
        Set<Parada> visitadas = new HashSet<>();
        List<Parada> fila = new ArrayList<>();
        fila.add(origem);
        visitadas.add(origem);
        while (!fila.isEmpty()) {
            Parada atual = fila.remove(0);
            for (Parada vizinha : tabuleiro.getParadasVizinhas(atual)) {
                alcancaveis.add(vizinha);
                if (vizinha.possuiYurtDe(jogador) && !visitadas.contains(vizinha)) {
                    visitadas.add(vizinha);
                    fila.add(vizinha);
                }
            }
        }
        alcancaveis.remove(origem);
        return alcancaveis;
    }

    //  mover por clique

    // o jogador só clica no destino. Achamos o caminho mais curto e usamos o moverPeao.
    // Se não der com os cavalos que sobraram, nada muda
    boolean moverPara(Parada destino) {
        if (colunaDoTurno == null) {
            return false;
        }
        List<Parada> caminho = menorCaminho(getJogadorDaVez(), destino);
        if (caminho == null || !moverPeao(caminho, movimentosRestantes)) {
            return false;
        }
        movimentosRestantes -= caminho.size();
        return true;
    }

    // busca em largura, cada passo é 1 movimento. Devolve o caminho sem a origem,
    // ou null se não tiver como chegar
    private List<Parada> menorCaminho(Jogador jogador, Parada destino) {
        Parada origem = jogador.getPosicao();
        Map<Parada, Parada> anterior = new HashMap<>();
        List<Parada> fila = new ArrayList<>();
        fila.add(origem);
        anterior.put(origem, null);
        while (!fila.isEmpty()) {
            Parada atual = fila.remove(0);
            if (atual == destino) {
                break;
            }
            for (Parada proxima : alcancaveisEmUmMovimento(jogador, atual)) {
                if (!anterior.containsKey(proxima)) {
                    anterior.put(proxima, atual);
                    fila.add(proxima);
                }
            }
        }
        if (destino == origem || !anterior.containsKey(destino)) {
            return null;
        }
        List<Parada> caminho = new ArrayList<>();
        for (Parada p = destino; p != origem; p = anterior.get(p)) {
            caminho.add(0, p);
        }
        return caminho;
    }

    private boolean fezParadaAdjacente(Local local) {
        for (Parada parada : paradasDoTurno) {
            if (tabuleiro.saoVizinhos(parada, local)) {
                return true;
            }
        }
        return false;
    }

    //  pegar tributo

    // pega 1 peça de uma província do lado de alguma parada do turno
    boolean pegarTributo(Provincia provincia) {
        if (!fezParadaAdjacente(provincia) || !provincia.removerTributo()) {
            return false;
        }
        getJogadorDaVez().adicionarTributos(provincia.getTipo(), 1);
        return true;
    }

    //  khan

    // o khan não pode ficar onde está. A província dele e as 2 das setas ganham 1 tributo
    // cada (o máximo de 3 é a Provincia que controla)
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

    // ainda não repõe as melhorias, porque elas não existem no model
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

    //  atacar cidades

    // o 1º tesouro de uma cidade no turno custa 1 espada, os outros custam 2.
    // Quem pega o último conquista a cidade, coloca um yurt nela e revela a próxima
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
            jogador.retirarYurtDoEstoque(); // vai pro meio da cidade
            if (!cidadesOcultas.isEmpty()) {
                cidadesOcultas.remove(0).revelar(pilhaDeTesouros);
            }
        }
        return true;
    }

    int getCustoDoProximoAtaque(Cidade cidade) {
        return tesourosTomadosNoTurno.containsKey(cidade) ? 2 : 1;
    }

    //  construir yurt

    // gasta 1 peça de yurt pra construir numa parada por onde passou andando neste turno,
    // se ainda tiver espaço nela
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

    //  votos e fim de jogo

    void registrarVotos(Jogador jogador, int quantidade) {
        jogador.adicionarVotos(quantidade);
        votosNoConselho += quantidade; // a ficha neutra anda junto
    }

    int getVotosNoConselho() {
        return votosNoConselho;
    }

    int getVotosParaFimDeJogo() {
        return VOTOS_PARA_FIM.get(jogadores.size());
    }

    // o fim vem pelos votos no conselho ou, mais raro, quando todas as cidades caem
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
