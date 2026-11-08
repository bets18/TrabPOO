package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// estado da partida e as regras. No turno guardamos a coluna ativada e o que ela rendeu
// (cavalos, punhos, khan e as peças virtuais das melhorias); tudo isso some ao passar a vez
class Jogo {
    static final int MIN_JOGADORES = 2;
    static final int MAX_JOGADORES = 5;
    static final int CIDADES_REVELADAS_NO_INICIO = 3;
    static final int CARTAS_ABERTAS_NA_PARTIDA = 4;
    static final int CARTAS_SECRETAS_POR_JOGADOR = 2;
    static final int PRECO_DA_MELHORIA = 2;
    static final int TESOUROS_DO_BONUS = 3;

    // votos no conselho que disparam o fim, pelo número de jogadores
    private static final Map<Integer, Integer> VOTOS_PARA_FIM = new HashMap<>();
    static {
        VOTOS_PARA_FIM.put(2, 10);
        VOTOS_PARA_FIM.put(3, 14);
        VOTOS_PARA_FIM.put(4, 16);
        VOTOS_PARA_FIM.put(5, 17);
    }

    // tabela de influência impressa no tabuleiro: votos pelo nº de melhorias (0 a 4)
    // na coluna da região, para quem tem mais yurts e para o 2º
    private static final int[] VOTOS_DO_PRIMEIRO = {1, 1, 2, 2, 3};
    private static final int[] VOTOS_DO_SEGUNDO = {0, 1, 1, 2, 2};

    private final Tabuleiro tabuleiro;
    private final List<Jogador> jogadores = new ArrayList<>();
    private int indiceJogadorDaVez = 0;

    private final List<Cidade> cidadesOcultas = new ArrayList<>();
    private final List<Cidade> todasAsCidades = new ArrayList<>();
    private List<TipoTesouro> pilhaDeTesouros = new ArrayList<>();
    private AreaDeMelhorias areaDeMelhorias = new AreaDeMelhorias(new ArrayList<Melhoria>());
    private final List<CartaAberta> cartasAbertas = new ArrayList<>();

    private Provincia provinciaDoKhan = null;   // null = khan fora do tabuleiro ou na área de melhorias
    private boolean khanNaAreaDeMelhorias = false;
    private int votosNoConselho = 0;            // ficha neutra

    // reserva de peças de tributo ao lado do tabuleiro: 10 espadas, 10 yurts e 15 moedas (pág. 2).
    // Tudo que entra no jogo sai daqui e tudo que é gasto volta pra cá; acabou, acabou
    private final Map<TipoTributo, Integer> reserva = new HashMap<>();
    // a escolha das cartas secretas só vale antes da 1ª rodada
    private boolean partidaComecou = false;

    // fim de jogo: termina a rodada em que disparou e joga mais uma completa
    private boolean fimDisparado = false;
    private boolean ultimaRodada = false;
    private boolean terminado = false;
    private final Map<Jogador, Integer> votosDeInfluencia = new HashMap<>();
    private final Map<Jogador, Integer> votosDaCartaSecreta = new HashMap<>();

    // coisas do turno atual
    private final Set<Parada> paradasDoTurno = new HashSet<>();      // inclui de onde o peão saiu
    private final Set<Parada> paradasDoMovimento = new HashSet<>();  // só onde ele chegou andando
    private final Map<Cidade, Integer> tesourosTomadosNoTurno = new HashMap<>();
    private Coluna colunaDoTurno = null;    // null enquanto não ativou nenhuma
    private int movimentosRestantes = 0;
    private int punhosRestantes = 0;
    private boolean khanPendente = false;   // a coluna tinha khan e ele ainda não foi movido
    private int moedasVirtuais = 0;
    private int espadasVirtuais = 0;
    private int yurtsVirtuais = 0;
    private final Map<TipoTesouro, Integer> tesourosVirtuais = new HashMap<>();
    private final Set<Melhoria> compradasNoTurno = new HashSet<>(); // não valem neste turno
    private boolean habilidadeUsada = false; // Chagatai e Ogedei: 1x por turno
    private boolean peaoTravado = false;     // Altani não troca de peão depois de agir
    private final List<Integer> linhasComMelhoriaGratis = new ArrayList<>();
    private final List<TipoTesouro> tesourosParaEscolher = new ArrayList<>(); // bônus de tesouros
    // alvos que o jogador escolheu clicando no tabuleiro
    private Cidade cidadeSelecionada = null;
    private Conselheiro conselheiroSelecionado = null;
    private final List<TipoTesouro> entregaEmMontagem = new ArrayList<>();

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
        reserva.put(TipoTributo.ESPADA, 10);
        reserva.put(TipoTributo.YURT, 10);
        reserva.put(TipoTributo.MOEDA, 15);

        for (int i = 0; i < herdeirosNaOrdemDeJogo.size(); i++) {
            Jogador jogador = new Jogador(herdeirosNaOrdemDeJogo.get(i));
            // os dois primeiros começam com 1 moeda, o resto com 2
            darDaReserva(jogador, TipoTributo.MOEDA, i < 2 ? 1 : 2);
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

        // 1 tributo em cada província (as do Khan não têm tributo)
        for (Provincia provincia : tabuleiro.getLocais(Provincia.class)) {
            abastecer(provincia);
        }

        iniciarTurno();
    }

    //  reserva de tributos

    int getNaReserva(TipoTributo tipo) {
        return reserva.get(tipo);
    }

    // dá ao jogador até a quantidade pedida, se tiver na reserva; devolve quantas deu
    private int darDaReserva(Jogador jogador, TipoTributo tipo, int quantidade) {
        int dadas = Math.min(quantidade, reserva.get(tipo));
        reserva.put(tipo, reserva.get(tipo) - dadas);
        jogador.adicionarTributos(tipo, dadas);
        return dadas;
    }

    // põe 1 peça do tipo da província, se ela ainda tiver espaço e a reserva tiver a peça
    private void abastecer(Provincia provincia) {
        TipoTributo tipo = provincia.getTipo();
        if (tipo != null && reserva.get(tipo) > 0 && provincia.adicionarTributo()) {
            reserva.put(tipo, reserva.get(tipo) - 1);
        }
    }

    //  preparação (as listas já chegam embaralhadas)

    // revela as 3 primeiras cidades (4 tesouros cada) e o resto fica na pilha
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

    // revela 5 melhorias. Habilidade do Jochi: pega 2 da pilha, uma vai pra coluna branca
    // e a outra ele coloca numa coluna colorida à escolha dele, no 1º turno
    void prepararMelhorias(List<Melhoria> melhoriasEmbaralhadas) {
        areaDeMelhorias = new AreaDeMelhorias(melhoriasEmbaralhadas);
        for (Jogador jogador : jogadores) {
            if (jogador.getHerdeiro() == Herdeiro.JOCHI) {
                // as 2 ficam esperando: ele decide qual vai pra coluna branca (posicionarMelhoria)
                jogador.setMelhoriaParaPosicionar(areaDeMelhorias.retirar(AreaDeMelhorias.TOPO_DA_PILHA));
                jogador.setOutraMelhoriaInicial(areaDeMelhorias.retirar(AreaDeMelhorias.TOPO_DA_PILHA));
            }
        }
    }

    // 4 cartas abertas na mesa, 2 secretas pra cada um e 1 bônus em cada conselheiro
    void prepararConselho(List<CartaAberta> abertas, List<CartaSecreta> secretas, List<TipoBonus> bonus) {
        cartasAbertas.clear();
        for (int i = 0; i < CARTAS_ABERTAS_NA_PARTIDA && i < abertas.size(); i++) {
            cartasAbertas.add(abertas.get(i));
        }
        List<CartaSecreta> monte = new ArrayList<>(secretas);
        for (Jogador jogador : jogadores) {
            List<CartaSecreta> mao = new ArrayList<>();
            for (int i = 0; i < CARTAS_SECRETAS_POR_JOGADOR && !monte.isEmpty(); i++) {
                mao.add(monte.remove(0));
            }
            jogador.oferecerCartasSecretas(mao);
        }
        List<Conselheiro> conselheiros = tabuleiro.getConselheiros();
        for (int i = 0; i < conselheiros.size() && i < bonus.size(); i++) {
            conselheiros.get(i).colocarBonus(bonus.get(i));
        }
    }

    //  consultas

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

    AreaDeMelhorias getAreaDeMelhorias() {
        return areaDeMelhorias;
    }

    List<CartaAberta> getCartasAbertas() {
        return new ArrayList<>(cartasAbertas);
    }

    Coluna getColunaDoTurno() {
        return colunaDoTurno;
    }

    int getMovimentosRestantes() {
        return movimentosRestantes;
    }

    int getPunhosRestantes() {
        return punhosRestantes;
    }

    boolean isKhanPendente() {
        return khanPendente;
    }

    int getMoedasVirtuais() {
        return moedasVirtuais;
    }

    int getEspadasVirtuais() {
        return espadasVirtuais;
    }

    int getYurtsVirtuais() {
        return yurtsVirtuais;
    }

    int getTesourosVirtuais(TipoTesouro tipo) {
        return tesourosVirtuais.getOrDefault(tipo, 0);
    }

    boolean isHabilidadeUsada() {
        return habilidadeUsada;
    }

    // se não sobrou nenhuma melhoria no jogo, a grátis se perde (senão o turno travaria)
    boolean isMelhoriaGratisPendente() {
        return !linhasComMelhoriaGratis.isEmpty() && areaDeMelhorias.temMelhoria();
    }

    List<TipoTesouro> getTesourosParaEscolher() {
        return new ArrayList<>(tesourosParaEscolher);
    }

    Cidade getCidadeSelecionada() {
        return cidadeSelecionada;
    }

    Conselheiro getConselheiroSelecionado() {
        return conselheiroSelecionado;
    }

    List<TipoTesouro> getEntregaEmMontagem() {
        return new ArrayList<>(entregaEmMontagem);
    }

    // a parada foi feita neste turno pelo peão que está jogando?
    boolean fezParadaEm(Parada parada) {
        return paradasDoTurno.contains(parada);
    }

    //  turno

    void passarVez() {
        if (!fimDisparado && fimDeJogoDisparado()) {
            fimDisparado = true;
        }
        indiceJogadorDaVez = (indiceJogadorDaVez + 1) % jogadores.size();
        // a rodada termina quando volta pro 1º jogador
        if (fimDisparado && indiceJogadorDaVez == 0) {
            if (ultimaRodada) {
                terminarJogo();
                return;
            }
            ultimaRodada = true;
        }
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
        punhosRestantes = 0;
        khanPendente = false;
        moedasVirtuais = 0;
        espadasVirtuais = 0;
        yurtsVirtuais = 0;
        tesourosVirtuais.clear();
        compradasNoTurno.clear();
        habilidadeUsada = false;
        peaoTravado = false;
        linhasComMelhoriaGratis.clear();
        tesourosParaEscolher.clear();
        cancelarSelecao();
    }

    // o que ainda impede de passar a vez: não ativou coluna, não moveu o khan ou tem
    // escolha de melhoria/tesouro em aberto
    boolean podeEncerrarTurno() {
        return !terminado && colunaDoTurno != null && !khanPendente
                && getJogadorDaVez().getMelhoriaParaPosicionar() == null
                && !isMelhoriaGratisPendente() && tesourosParaEscolher.isEmpty();
    }

    boolean encerrarTurno() {
        if (!podeEncerrarTurno()) {
            return false;
        }
        passarVez();
        return true;
    }

    //  ativar coluna

    // coloca a ficha na coluna e o jogador ganha o que está impresso nela mais o que
    // as melhorias dela dão. A branca devolve todas as fichas. Uma coluna por turno e
    // não pode ser coluna que já tem ficha
    boolean ativarColuna(Coluna coluna) {
        Jogador jogador = getJogadorDaVez();
        if (terminado || colunaDoTurno != null || !jogador.podeAtivar(coluna) || !cartasSecretasEscolhidas()) {
            return false;
        }
        partidaComecou = true;
        AcoesColuna acoes = jogador.getHerdeiro().getAcoes(coluna);
        colunaDoTurno = coluna;
        movimentosRestantes += acoes.getCavalos();
        punhosRestantes += acoes.getPunhos();
        if (acoes.temKhan()) {
            khanPendente = true;
        }
        if (acoes.temMoedaVirtual()) {
            moedasVirtuais++;
        }
        for (Melhoria melhoria : jogador.getMelhorias(coluna)) {
            // melhoria comprada neste turno só vale a partir do próximo
            if (!compradasNoTurno.contains(melhoria)) {
                aplicarMelhoria(melhoria);
            }
        }
        if (acoes.recuperaFichas()) {
            jogador.recuperarFichas();
        } else {
            jogador.colocarFicha(coluna);
        }
        verificarCartasAbertas();
        return true;
    }

    private void aplicarMelhoria(Melhoria melhoria) {
        switch (melhoria.getTipo()) {
            case MOEDA:
                moedasVirtuais++;
                break;
            case ESPADA:
                espadasVirtuais++;
                break;
            case YURT:
                yurtsVirtuais++;
                break;
            case PUNHO:
                punhosRestantes++;
                break;
            case CAVALOS:
                movimentosRestantes += 2;
                break;
            default: // tesouro virtual
                TipoTesouro tesouro = melhoria.getTipo().getTesouro();
                tesourosVirtuais.put(tesouro, getTesourosVirtuais(tesouro) + 1);
        }
    }

    // peças virtuais são gastas antes das de verdade, porque somem no fim do turno
    private int getVirtuais(TipoTributo tipo) {
        switch (tipo) {
            case MOEDA:
                return moedasVirtuais;
            case ESPADA:
                return espadasVirtuais;
            default:
                return yurtsVirtuais;
        }
    }

    private boolean podePagar(TipoTributo tipo, int quantidade) {
        return getVirtuais(tipo) + getJogadorDaVez().getQuantidadeTributo(tipo) >= quantidade;
    }

    private void pagar(TipoTributo tipo, int quantidade) {
        int virtuais = Math.min(getVirtuais(tipo), quantidade);
        switch (tipo) {
            case MOEDA:
                moedasVirtuais -= virtuais;
                break;
            case ESPADA:
                espadasVirtuais -= virtuais;
                break;
            default:
                yurtsVirtuais -= virtuais;
        }
        getJogadorDaVez().gastarTributos(tipo, quantidade - virtuais);
        reserva.put(tipo, reserva.get(tipo) + quantidade - virtuais); // peça usada volta pra reserva
    }

    // Altani: escolhe qual dos 2 peões vai jogar. Depois que andou ou agiu não troca mais
    boolean selecionarPeao(int peao) {
        Jogador jogador = getJogadorDaVez();
        if (terminado || peao < 0 || peao >= jogador.getNumeroDePeoes() || peaoTravado
                || !paradasDoMovimento.isEmpty()) {
            return false;
        }
        jogador.setPeaoAtivo(peao);
        paradasDoTurno.clear();
        paradasDoTurno.add(jogador.getPosicao());
        return true;
    }

    // Chagatai: 1x por turno deixa de usar 1 cavalo e ganha 1 espada
    boolean trocarCavaloPorEspada() {
        Jogador jogador = getJogadorDaVez();
        if (terminado || jogador.getHerdeiro() != Herdeiro.CHAGATAI || habilidadeUsada || movimentosRestantes == 0
                || reserva.get(TipoTributo.ESPADA) == 0) {
            return false;
        }
        movimentosRestantes--;
        darDaReserva(jogador, TipoTributo.ESPADA, 1);
        habilidadeUsada = true;
        verificarCartasAbertas();
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
    // Se não der com os movimentos que sobraram, nada muda
    boolean moverPara(Parada destino) {
        if (terminado || movimentosRestantes == 0) {
            return false;
        }
        List<Parada> caminho = menorCaminho(getJogadorDaVez(), destino);
        if (caminho == null || !moverPeao(caminho, movimentosRestantes)) {
            return false;
        }
        movimentosRestantes -= caminho.size();
        peaoTravado = true;
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

    // cada punho pega 1 peça de uma província do lado de alguma parada do turno
    boolean pegarTributo(Provincia provincia) {
        if (terminado || punhosRestantes == 0 || provincia.getTipo() == null) {
            return false;
        }
        if (!fezParadaAdjacente(provincia) || !provincia.removerTributo()) {
            return false;
        }
        punhosRestantes--;
        getJogadorDaVez().adicionarTributos(provincia.getTipo(), 1);
        peaoTravado = true;
        verificarCartasAbertas();
        return true;
    }

    //  khan

    // obrigatório quando a coluna tem o khan. Ele não pode ficar onde está; a província
    // dele (se der tributo) e as das setas ganham 1 tributo cada (o máximo de 3 é a
    // Provincia que controla)
    boolean moverKhan(Provincia destino) {
        if (terminado || !khanPendente || !destino.isDoKhan() || destino == provinciaDoKhan) {
            return false;
        }
        provinciaDoKhan = destino;
        khanNaAreaDeMelhorias = false;
        khanPendente = false;
        abastecer(destino);
        for (Provincia afetada : tabuleiro.getAfetadasPeloKhan(destino)) {
            abastecer(afetada);
        }
        return true;
    }

    // se os espaços com moeda e espada impressas estiverem vazios o jogador pega essas
    // peças da reserva; depois os espaços vazios recebem melhorias novas
    boolean moverKhanParaAreaDeMelhorias() {
        if (terminado || !khanPendente || khanNaAreaDeMelhorias) {
            return false;
        }
        Jogador jogador = getJogadorDaVez();
        if (areaDeMelhorias.espacoVazio(AreaDeMelhorias.ESPACO_DA_MOEDA)) {
            darDaReserva(jogador, TipoTributo.MOEDA, 1);
        }
        if (areaDeMelhorias.espacoVazio(AreaDeMelhorias.ESPACO_DA_ESPADA)) {
            darDaReserva(jogador, TipoTributo.ESPADA, 1);
        }
        areaDeMelhorias.repor();
        khanNaAreaDeMelhorias = true;
        provinciaDoKhan = null;
        khanPendente = false;
        verificarCartasAbertas();
        return true;
    }

    Provincia getProvinciaDoKhan() {
        return provinciaDoKhan;
    }

    boolean isKhanNaAreaDeMelhorias() {
        return khanNaAreaDeMelhorias;
    }

    //  atacar cidades

    // o jogador clicou numa cidade: ela vira o alvo do ataque (a View mostra os tesouros)
    boolean selecionarCidade(Cidade cidade) {
        if (terminado || !cidade.podeSerAtacada()) {
            return false;
        }
        cancelarSelecao();
        cidadeSelecionada = cidade;
        return true;
    }

    // o 1º tesouro de uma cidade no turno custa 1 espada, os outros custam 2.
    // Quem pega o último conquista a cidade, coloca um yurt nela e revela a próxima
    boolean atacarCidade(Cidade cidade, TipoTesouro tesouro) {
        if (terminado || !cidade.podeSerAtacada() || !cidade.possuiTesouro(tesouro) || !fezParadaAdjacente(cidade)) {
            return false;
        }
        int custo = getCustoDoProximoAtaque(cidade);
        if (!podePagar(TipoTributo.ESPADA, custo)) {
            return false;
        }
        pagar(TipoTributo.ESPADA, custo);
        tesourosTomadosNoTurno.put(cidade, tesourosTomadosNoTurno.getOrDefault(cidade, 0) + 1);
        tomarTesouro(cidade, tesouro);
        return true;
    }

    // Ogedei: 1x por turno paga 1 moeda (pode ser virtual) e pega um tesouro de qualquer
    // cidade, mesmo longe, desde que não seja o último dela. Não conta pro custo das espadas
    boolean pegarTesouroComMoeda(Cidade cidade, TipoTesouro tesouro) {
        if (terminado || getJogadorDaVez().getHerdeiro() != Herdeiro.OGEDEI || habilidadeUsada) {
            return false;
        }
        if (!cidade.podeSerAtacada() || cidade.getQuantidadeTesouros() < 2 || !cidade.possuiTesouro(tesouro)
                || !podePagar(TipoTributo.MOEDA, 1)) {
            return false;
        }
        pagar(TipoTributo.MOEDA, 1);
        habilidadeUsada = true;
        tomarTesouro(cidade, tesouro);
        return true;
    }

    private void tomarTesouro(Cidade cidade, TipoTesouro tesouro) {
        Jogador jogador = getJogadorDaVez();
        cidade.removerTesouro(tesouro);
        jogador.adicionarTesouro(tesouro);
        peaoTravado = true;
        if (cidade.getQuantidadeTesouros() == 0) {
            cidade.conquistar(jogador);
            jogador.conquistarCidade();
            // um yurt dele vai pro meio da cidade; sem yurts no estoque, conquista sem o yurt
            if (jogador.retirarYurtDoEstoque()) {
                cidade.colocarYurtDoConquistador();
            }
            if (!cidadesOcultas.isEmpty()) {
                cidadesOcultas.remove(0).revelar(pilhaDeTesouros);
            }
        }
        if (cidadeSelecionada == cidade && !cidade.podeSerAtacada()) {
            cidadeSelecionada = null;
        }
        verificarCartasAbertas();
    }

    int getCustoDoProximoAtaque(Cidade cidade) {
        return tesourosTomadosNoTurno.containsKey(cidade) ? 2 : 1;
    }

    //  construir yurt

    // gasta 1 peça de yurt (pode ser virtual) pra construir numa parada por onde passou
    // andando neste turno, se ainda tiver espaço nela. O Tolui também pode construir nas
    // paradas vizinhas de onde está
    boolean construirYurt(Parada parada) {
        Jogador jogador = getJogadorDaVez();
        boolean vizinhaDoTolui = jogador.getHerdeiro() == Herdeiro.TOLUI
                && tabuleiro.existeRota(jogador.getPosicao(), parada);
        if (terminado || (!paradasDoMovimento.contains(parada) && !vizinhaDoTolui) || !parada.temEspacoParaYurt()) {
            return false;
        }
        if (jogador.getYurtsNoEstoque() == 0 || !podePagar(TipoTributo.YURT, 1)) {
            return false;
        }
        pagar(TipoTributo.YURT, 1);
        jogador.retirarYurtDoEstoque();
        parada.adicionarYurt(jogador);
        peaoTravado = true;
        verificarCartasAbertas();
        return true;
    }

    //  melhorias

    // 2 moedas (virtuais primeiro) por uma melhoria da área ou do topo da pilha. Ela vai
    // pra coluna da cor dela; o Jochi escolhe a coluna depois, com posicionarMelhoria
    boolean comprarMelhoria(int espaco) {
        Jogador jogador = getJogadorDaVez();
        Melhoria melhoria = areaDeMelhorias.espiar(espaco);
        if (terminado || melhoria == null || !podePagar(TipoTributo.MOEDA, PRECO_DA_MELHORIA)) {
            return false;
        }
        boolean jochi = jogador.getHerdeiro() == Herdeiro.JOCHI;
        if (jochi && (jogador.getMelhoriaParaPosicionar() != null || !jogador.temEspacoEmColunaColorida())) {
            return false;
        }
        if (!jochi && !jogador.temEspacoNaColuna(melhoria.getCor())) {
            return false;
        }
        pagar(TipoTributo.MOEDA, PRECO_DA_MELHORIA);
        areaDeMelhorias.retirar(espaco);
        compradasNoTurno.add(melhoria);
        if (jochi) {
            jogador.setMelhoriaParaPosicionar(melhoria);
        } else {
            colocarMelhoria(jogador, melhoria, melhoria.getCor());
        }
        return true;
    }

    // Jochi: escolhe a coluna colorida da melhoria que está esperando
    boolean posicionarMelhoria(Coluna coluna) {
        Jogador jogador = getJogadorDaVez();
        Melhoria melhoria = jogador.getMelhoriaParaPosicionar();
        if (terminado || melhoria == null) {
            return false;
        }
        Melhoria outraInicial = jogador.getOutraMelhoriaInicial();
        if (outraInicial != null) {
            return posicionarMelhoriaInicialDoJochi(jogador, melhoria, outraInicial, coluna);
        }
        if (!coluna.isColorida() || !jogador.temEspacoNaColuna(coluna)) {
            return false;
        }
        jogador.setMelhoriaParaPosicionar(null);
        colocarMelhoria(jogador, melhoria, coluna);
        return true;
    }

    // Jochi, início do jogo: das 2 melhorias, uma vai pra coluna branca e a outra pra uma colorida.
    // Clicando na branca, a 1ª vai pra lá e ele ainda escolhe a coluna da 2ª; clicando numa
    // colorida, a 1ª vai pra ela e a 2ª vai pra branca
    private boolean posicionarMelhoriaInicialDoJochi(Jogador jogador, Melhoria melhoria, Melhoria outra, Coluna coluna) {
        if (coluna == Coluna.BRANCA) {
            jogador.setOutraMelhoriaInicial(null);
            jogador.adicionarMelhoria(Coluna.BRANCA, melhoria);
            jogador.setMelhoriaParaPosicionar(outra);
            return true;
        }
        if (!jogador.temEspacoNaColuna(coluna)) {
            return false;
        }
        jogador.setOutraMelhoriaInicial(null);
        jogador.adicionarMelhoria(Coluna.BRANCA, outra);
        jogador.setMelhoriaParaPosicionar(null);
        colocarMelhoria(jogador, melhoria, coluna);
        return true;
    }

    // se a peça completou uma linha das 3 colunas coloridas, o jogador ganha uma melhoria
    // grátis pro espaço daquela linha na coluna branca (se ainda estiver vazio)
    private void colocarMelhoria(Jogador jogador, Melhoria melhoria, Coluna coluna) {
        jogador.adicionarMelhoria(coluna, melhoria);
        int linha = jogador.getQuantidadeDeMelhorias(coluna) - 1;
        boolean linhaCompleta = true;
        for (Coluna outra : Coluna.values()) {
            if (outra.isColorida() && jogador.getQuantidadeDeMelhorias(outra) <= linha) {
                linhaCompleta = false;
            }
        }
        if (linhaCompleta && jogador.getQuantidadeDeMelhorias(Coluna.BRANCA) <= linha
                && !linhasComMelhoriaGratis.contains(linha)) {
            linhasComMelhoriaGratis.add(linha);
        }
        verificarCartasAbertas();
    }

    // a melhoria grátis sai da área ou do topo da pilha e vai pra coluna branca, sem
    // restrição de cor
    boolean escolherMelhoriaGratis(int espaco) {
        if (terminado || linhasComMelhoriaGratis.isEmpty()) {
            return false;
        }
        Melhoria melhoria = areaDeMelhorias.retirar(espaco);
        if (melhoria == null) {
            return false;
        }
        linhasComMelhoriaGratis.remove(0);
        compradasNoTurno.add(melhoria);
        getJogadorDaVez().adicionarMelhoria(Coluna.BRANCA, melhoria);
        verificarCartasAbertas();
        return true;
    }

    //  entregar tesouros aos conselheiros

    // o jogador clicou no conselheiro que aceita 3 tesouros quaisquer: agora ele escolhe quais
    boolean selecionarConselheiro(Conselheiro conselheiro) {
        if (terminado || !conselheiro.aceitaQualquer() || !paradasDoTurno.contains(conselheiro.getParada())) {
            return false;
        }
        cancelarSelecao();
        conselheiroSelecionado = conselheiro;
        return true;
    }

    // junta mais um tesouro à entrega escolhida; quando completa o pedido, entrega
    boolean adicionarTesouroAEntrega(TipoTesouro tesouro) {
        if (terminado || conselheiroSelecionado == null) {
            return false;
        }
        List<TipoTesouro> teste = new ArrayList<>(entregaEmMontagem);
        teste.add(tesouro);
        if (!temTesouros(teste)) {
            return false;
        }
        entregaEmMontagem.add(tesouro);
        if (entregaEmMontagem.size() < conselheiroSelecionado.getPedido().size()) {
            return true;
        }
        if (!entregarTesouros(conselheiroSelecionado, new ArrayList<>(entregaEmMontagem))) {
            entregaEmMontagem.clear();
            return false;
        }
        return true;
    }

    void cancelarSelecao() {
        cidadeSelecionada = null;
        conselheiroSelecionado = null;
        entregaEmMontagem.clear();
    }

    // precisa ter feito uma parada no conselheiro neste turno. Cada entrega dá os votos dele
    // e a 1ª leva a peça de bônus. Tesouro virtual substitui um de verdade
    boolean entregarTesouros(Conselheiro conselheiro, List<TipoTesouro> tesouros) {
        if (terminado || !paradasDoTurno.contains(conselheiro.getParada()) || !conselheiro.atende(tesouros)
                || !temTesouros(tesouros)) {
            return false;
        }
        Jogador jogador = getJogadorDaVez();
        for (TipoTesouro tipo : tesouros) {
            int virtuais = getTesourosVirtuais(tipo);
            if (virtuais > 0) {
                tesourosVirtuais.put(tipo, virtuais - 1);
            } else {
                jogador.gastarTesouro(tipo);
            }
        }
        cancelarSelecao();
        peaoTravado = true;
        registrarVotos(jogador, conselheiro.getVotos());
        TipoBonus bonus = conselheiro.retirarBonus();
        if (bonus != null) {
            jogador.contarBonusObtido();
            if (bonus == TipoBonus.VOTO) {
                registrarVotos(jogador, 1);
            } else {
                jogador.guardarBonus(bonus);
            }
        }
        verificarCartasAbertas();
        return true;
    }

    private boolean temTesouros(List<TipoTesouro> tesouros) {
        Jogador jogador = getJogadorDaVez();
        for (Map.Entry<TipoTesouro, Integer> pedido : Conselheiro.contar(tesouros).entrySet()) {
            TipoTesouro tipo = pedido.getKey();
            if (tipo == null || jogador.getQuantidadeTesouro(tipo) + getTesourosVirtuais(tipo) < pedido.getValue()) {
                return false;
            }
        }
        return true;
    }

    //  bônus

    // cada bônus guardado é usado uma vez, quando o jogador quiser durante o turno dele
    boolean usarBonus(TipoBonus bonus) {
        Jogador jogador = getJogadorDaVez();
        if (terminado || !jogador.getBonusGuardados().contains(bonus)) {
            return false;
        }
        if (bonus == TipoBonus.TESOUROS && (pilhaDeTesouros.isEmpty() || !tesourosParaEscolher.isEmpty())) {
            return false;
        }
        jogador.usarBonus(bonus);
        switch (bonus) {
            case DUAS_ESPADAS:
                darDaReserva(jogador, TipoTributo.ESPADA, 2);
                break;
            case DUAS_MOEDAS:
                darDaReserva(jogador, TipoTributo.MOEDA, 2);
                break;
            case DOIS_YURTS:
                darDaReserva(jogador, TipoTributo.YURT, 2);
                break;
            case MOVIMENTOS:
                movimentosRestantes += 3;
                punhosRestantes += 1;
                break;
            case DOIS_PUNHOS:
                punhosRestantes += 2;
                break;
            case TESOUROS:
                for (int i = 0; i < TESOUROS_DO_BONUS && !pilhaDeTesouros.isEmpty(); i++) {
                    tesourosParaEscolher.add(pilhaDeTesouros.remove(0));
                }
                break;
            default: // o voto é usado assim que ganha
                registrarVotos(jogador, 1);
        }
        verificarCartasAbertas();
        return true;
    }

    // fica com um dos tesouros tirados pelo bônus; os outros são descartados
    boolean escolherTesouroDoBonus(TipoTesouro tesouro) {
        if (terminado || !tesourosParaEscolher.contains(tesouro)) {
            return false;
        }
        tesourosParaEscolher.clear();
        getJogadorDaVez().adicionarTesouro(tesouro);
        verificarCartasAbertas();
        return true;
    }

    //  cartas de conselheiro

    // conferido depois de cada ação: quem cumprir primeiro ganha 1 voto e a carta sai
    private void verificarCartasAbertas() {
        Jogador jogador = getJogadorDaVez();
        Pontuacao pontuacao = new Pontuacao(this, jogador);
        for (int i = cartasAbertas.size() - 1; i >= 0; i--) {
            if (cartasAbertas.get(i).cumprida(pontuacao)) {
                cartasAbertas.remove(i);
                registrarVotos(jogador, 1);
            }
        }
    }

    // cada um fica com 1 das 2 cartas antes da 1ª rodada; depois não dá mais pra escolher
    boolean escolherCartaSecreta(Jogador jogador, CartaSecreta carta) {
        return !partidaComecou && jogador.escolherCartaSecreta(carta);
    }

    // ninguém joga enquanto algum jogador que recebeu cartas não escolheu a sua
    private boolean cartasSecretasEscolhidas() {
        for (Jogador jogador : jogadores) {
            if (!jogador.getCartasOferecidas().isEmpty() && jogador.getCartaSecreta() == null) {
                return false;
            }
        }
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

    boolean isUltimaRodada() {
        return ultimaRodada;
    }

    boolean isTerminado() {
        return terminado;
    }

    // conta a influência nas 3 regiões e as cartas secretas
    private void terminarJogo() {
        terminado = true;
        cancelarSelecao();
        for (Jogador jogador : jogadores) {
            votosDeInfluencia.put(jogador, 0);
        }
        for (Regiao regiao : Regiao.values()) {
            pontuarRegiao(regiao);
        }
        for (Jogador jogador : jogadores) {
            CartaSecreta carta = jogador.getCartaSecreta();
            int votosDaCarta = carta == null ? 0 : carta.votos(new Pontuacao(this, jogador));
            votosDaCartaSecreta.put(jogador, votosDaCarta);
            jogador.adicionarVotos(votosDeInfluencia.get(jogador) + votosDaCarta);
        }
    }

    // empate em 1º: todos ficam em 1º e ninguém fica em 2º. Empate em 2º: todos são 2º
    private void pontuarRegiao(Regiao regiao) {
        Map<Jogador, Integer> yurts = new HashMap<>();
        int maior = 0;
        for (Jogador jogador : jogadores) {
            int quantidade = new Pontuacao(this, jogador).getYurtsNaRegiao(regiao);
            yurts.put(jogador, quantidade);
            maior = Math.max(maior, quantidade);
        }
        if (maior == 0) {
            return;
        }
        int primeiros = 0;
        int segundo = 0;
        for (Jogador jogador : jogadores) {
            int quantidade = yurts.get(jogador);
            if (quantidade == maior) {
                primeiros++;
            } else {
                segundo = Math.max(segundo, quantidade);
            }
        }
        for (Jogador jogador : jogadores) {
            int quantidade = yurts.get(jogador);
            int melhorias = Math.min(jogador.getQuantidadeDeMelhorias(regiao.getColuna()), Jogador.MELHORIAS_POR_COLUNA);
            int votos = 0;
            if (quantidade == maior) {
                votos = VOTOS_DO_PRIMEIRO[melhorias];
            } else if (primeiros == 1 && segundo > 0 && quantidade == segundo) {
                votos = VOTOS_DO_SEGUNDO[melhorias];
            }
            votosDeInfluencia.put(jogador, votosDeInfluencia.get(jogador) + votos);
        }
    }

    int getVotosDeInfluencia(Jogador jogador) {
        return votosDeInfluencia.getOrDefault(jogador, 0);
    }

    int getVotosDaCartaSecreta(Jogador jogador) {
        return votosDaCartaSecreta.getOrDefault(jogador, 0);
    }

    // mais votos; no empate, mais tesouros não entregues; depois, o peão mais perto de
    // Karakorum. Se ainda empatar, todos vencem juntos
    List<Jogador> getVencedores() {
        List<Jogador> vencedores = new ArrayList<>();
        if (!terminado) {
            return vencedores;
        }
        for (Jogador jogador : jogadores) {
            if (vencedores.isEmpty()) {
                vencedores.add(jogador);
                continue;
            }
            int comparacao = comparar(jogador, vencedores.get(0));
            if (comparacao > 0) {
                vencedores.clear();
                vencedores.add(jogador);
            } else if (comparacao == 0) {
                vencedores.add(jogador);
            }
        }
        return vencedores;
    }

    private int comparar(Jogador a, Jogador b) {
        if (a.getVotos() != b.getVotos()) {
            return Integer.compare(a.getVotos(), b.getVotos());
        }
        if (a.getTotalDeTesouros() != b.getTotalDeTesouros()) {
            return Integer.compare(a.getTotalDeTesouros(), b.getTotalDeTesouros());
        }
        return Integer.compare(distanciaDeKarakorum(b), distanciaDeKarakorum(a));
    }

    private int distanciaDeKarakorum(Jogador jogador) {
        int menor = Integer.MAX_VALUE;
        for (int peao = 0; peao < jogador.getNumeroDePeoes(); peao++) {
            int distancia = tabuleiro.distancia(jogador.getPosicaoDoPeao(peao), tabuleiro.getKarakorum());
            if (distancia >= 0) {
                menor = Math.min(menor, distancia);
            }
        }
        return menor;
    }
}
