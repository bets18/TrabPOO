package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Façade do model: a View e o Controller só falam com esta classe, e só passam String e int,
// nunca as classes de dentro do pacote. É um Singleton (uma partida por vez, pegue com
// getInstancia()) e é o observado do padrão Observer: toda jogada que muda o estado da
// partida avisa os observadores com um dos eventos abaixo
public class HerdeirosDeKhanAPI implements ObservadoIF {
    public static final String EVENTO_PARTIDA_INICIADA = "PARTIDA_INICIADA";
    public static final String EVENTO_CARTA_SECRETA_ESCOLHIDA = "CARTA_SECRETA_ESCOLHIDA";
    public static final String EVENTO_TURNO_INICIADO = "TURNO_INICIADO";
    public static final String EVENTO_COLUNA_ATIVADA = "COLUNA_ATIVADA";
    public static final String EVENTO_PEAO_MOVIDO = "PEAO_MOVIDO";
    public static final String EVENTO_PEAO_SELECIONADO = "PEAO_SELECIONADO";
    public static final String EVENTO_TRIBUTO_PEGO = "TRIBUTO_PEGO";
    public static final String EVENTO_KHAN_MOVIDO = "KHAN_MOVIDO";
    public static final String EVENTO_CIDADE_SELECIONADA = "CIDADE_SELECIONADA";
    public static final String EVENTO_TESOURO_TOMADO = "TESOURO_TOMADO";
    public static final String EVENTO_YURT_CONSTRUIDO = "YURT_CONSTRUIDO";
    public static final String EVENTO_MELHORIA_COMPRADA = "MELHORIA_COMPRADA";
    public static final String EVENTO_MELHORIA_POSICIONADA = "MELHORIA_POSICIONADA";
    public static final String EVENTO_CONSELHEIRO_SELECIONADO = "CONSELHEIRO_SELECIONADO";
    public static final String EVENTO_TESOUROS_ENTREGUES = "TESOUROS_ENTREGUES";
    public static final String EVENTO_BONUS_USADO = "BONUS_USADO";
    public static final String EVENTO_TRIBUTO_GANHO = "TRIBUTO_GANHO";
    public static final String EVENTO_SELECAO_CANCELADA = "SELECAO_CANCELADA";
    public static final String EVENTO_JANELA_DA_CARTELA = "JANELA_DA_CARTELA";
    public static final String EVENTO_JANELA_DE_CARTAS = "JANELA_DE_CARTAS";
    public static final String EVENTO_JOGO_TERMINADO = "JOGO_TERMINADO";

    // onde o khan pode estar além das províncias do Khan
    public static final String AREA_DE_MELHORIAS = "AREA_DE_MELHORIAS";
    // o conselheiro de 3 tesouros quaisquer
    public static final String QUALQUER_TESOURO = "QUALQUER";
    // espaço "-1" da área de melhorias: comprar do topo da pilha
    public static final int TOPO_DA_PILHA = AreaDeMelhorias.TOPO_DA_PILHA;
    public static final int ESPACOS_DA_AREA_DE_MELHORIAS = AreaDeMelhorias.ESPACOS;

    private static HerdeirosDeKhanAPI instancia = null;

    private Jogo jogo = null;
    private final List<ObservadorIF> observadores = new ArrayList<>();

    private HerdeirosDeKhanAPI() {
    }

    public static HerdeirosDeKhanAPI getInstancia() {
        if (instancia == null) {
            instancia = new HerdeirosDeKhanAPI();
        }
        return instancia;
    }

    // partida nova no mapa de verdade. Os nomes vêm na ordem de jogo, ex: "ALTANI", "JOCHI".
    // Embaralha cidades, tesouros (8 de cada tipo), melhorias, cartas e bônus
    public void novaPartida(List<String> herdeirosNaOrdemDeJogo) {
        List<Herdeiro> herdeiros = new ArrayList<>();
        for (String nome : herdeirosNaOrdemDeJogo) {
            Herdeiro herdeiro = converter(Herdeiro.class, nome);
            if (herdeiro == null) {
                throw new IllegalArgumentException("Herdeiro desconhecido: " + nome);
            }
            herdeiros.add(herdeiro);
        }
        Tabuleiro tabuleiro = MapaPadrao.criar();
        Jogo novo = new Jogo(tabuleiro, herdeiros);

        List<Cidade> cidades = tabuleiro.getLocais(Cidade.class);
        List<TipoTesouro> tesouros = new ArrayList<>();
        for (TipoTesouro tipo : TipoTesouro.values()) {
            for (int i = 0; i < 8; i++) {
                tesouros.add(tipo);
            }
        }
        Collections.shuffle(cidades);
        Collections.shuffle(tesouros);
        novo.prepararCidades(cidades, tesouros);

        List<Melhoria> melhorias = criarMelhorias();
        Collections.shuffle(melhorias);
        novo.prepararMelhorias(melhorias);

        List<CartaAberta> abertas = new ArrayList<>(Arrays.asList(CartaAberta.values()));
        List<CartaSecreta> secretas = new ArrayList<>(Arrays.asList(CartaSecreta.values()));
        List<TipoBonus> bonus = new ArrayList<>(Arrays.asList(TipoBonus.values()));
        Collections.shuffle(abertas);
        Collections.shuffle(secretas);
        Collections.shuffle(bonus);
        novo.prepararConselho(abertas, secretas, bonus);

        iniciarPartida(novo);
    }

    // usado nos testes, com um tabuleiro montado à mão
    void iniciarPartida(Jogo jogo) {
        this.jogo = jogo;
        notificar(EVENTO_PARTIDA_INICIADA);
    }

    // as 44 peças de melhoria. As de tesouro são 1 de cada; as outras foram divididas
    // entre as 3 cores de fundo
    private static List<Melhoria> criarMelhorias() {
        List<Melhoria> melhorias = new ArrayList<>();
        adicionarMelhorias(melhorias, TipoMelhoria.MOEDA, 3, 3, 2);
        adicionarMelhorias(melhorias, TipoMelhoria.ESPADA, 3, 2, 3);
        adicionarMelhorias(melhorias, TipoMelhoria.YURT, 2, 3, 3);
        adicionarMelhorias(melhorias, TipoMelhoria.PUNHO, 2, 2, 2);
        adicionarMelhorias(melhorias, TipoMelhoria.CAVALOS, 3, 3, 3);
        melhorias.add(new Melhoria(TipoMelhoria.CARNE, Coluna.AMARELA));
        melhorias.add(new Melhoria(TipoMelhoria.GRAOS, Coluna.AMARELA));
        melhorias.add(new Melhoria(TipoMelhoria.FERRO, Coluna.VERMELHA));
        melhorias.add(new Melhoria(TipoMelhoria.LA, Coluna.VERDE));
        melhorias.add(new Melhoria(TipoMelhoria.PELE, Coluna.VERDE));
        return melhorias;
    }

    private static void adicionarMelhorias(List<Melhoria> melhorias, TipoMelhoria tipo, int amarelas,
            int vermelhas, int verdes) {
        for (int i = 0; i < amarelas; i++) {
            melhorias.add(new Melhoria(tipo, Coluna.AMARELA));
        }
        for (int i = 0; i < vermelhas; i++) {
            melhorias.add(new Melhoria(tipo, Coluna.VERMELHA));
        }
        for (int i = 0; i < verdes; i++) {
            melhorias.add(new Melhoria(tipo, Coluna.VERDE));
        }
    }

    //  Observer

    @Override
    public void adicionarObservador(ObservadorIF observador) {
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    @Override
    public void removerObservador(ObservadorIF observador) {
        observadores.remove(observador);
    }

    // copia a lista porque um observador pode se remover enquanto é avisado
    private void notificar(String evento) {
        for (ObservadorIF observador : new ArrayList<>(observadores)) {
            observador.notificar(this, evento);
        }
    }

    // avisa só se a jogada deu certo
    private boolean avisarSe(boolean feito, String evento) {
        if (feito) {
            notificar(evento);
        }
        return feito;
    }

    // pedidos de exibição: as janelas da cartela e de cartas abrem quando recebem o aviso
    public void exibirCartela() {
        notificar(EVENTO_JANELA_DA_CARTELA);
    }

    public void exibirCartas() {
        notificar(EVENTO_JANELA_DE_CARTAS);
    }

    //  jogadores (identificados pelo índice na ordem de jogo)

    public int getNumeroDeJogadores() {
        return jogo.getNumeroDeJogadores();
    }

    public int getIndiceJogadorDaVez() {
        return jogo.getIndiceJogadorDaVez();
    }

    public String getHerdeiro(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getHerdeiro().name();
    }

    public String getHerdeiroDoJogadorDaVez() {
        return jogo.getJogadorDaVez().getHerdeiro().name();
    }

    // posição do peão ativo
    public String getPosicao(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getPosicao().getNome();
    }

    public String getPosicaoDoJogadorDaVez() {
        return jogo.getJogadorDaVez().getPosicao().getNome();
    }

    // só a Altani tem 2
    public int getNumeroDePeoes(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getNumeroDePeoes();
    }

    public String getPosicaoDoPeao(int indiceJogador, int peao) {
        return jogo.getJogador(indiceJogador).getPosicaoDoPeao(peao).getNome();
    }

    public int getPeaoAtivo(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getPeaoAtivo();
    }

    // do jogador da vez
    public int getQuantidadeTributo(String tipoTributo) {
        return getQuantidadeTributo(jogo.getIndiceJogadorDaVez(), tipoTributo);
    }

    public int getQuantidadeTributo(int indiceJogador, String tipoTributo) {
        TipoTributo tipo = converter(TipoTributo.class, tipoTributo);
        return tipo == null ? 0 : jogo.getJogador(indiceJogador).getQuantidadeTributo(tipo);
    }

    public int getQuantidadeTesouro(int indiceJogador, String tipoTesouro) {
        TipoTesouro tipo = converter(TipoTesouro.class, tipoTesouro);
        return tipo == null ? 0 : jogo.getJogador(indiceJogador).getQuantidadeTesouro(tipo);
    }

    public int getVotos(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getVotos();
    }

    public int getYurtsNoEstoque(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getYurtsNoEstoque();
    }

    public int getCidadesConquistadas(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getCidadesConquistadas();
    }

    // melhorias da coluna, de baixo pra cima, no formato "TIPO:COR" (ex: "CAVALOS:VERDE")
    public List<String> getMelhorias(int indiceJogador, String coluna) {
        List<String> nomes = new ArrayList<>();
        Coluna c = converter(Coluna.class, coluna);
        if (c != null) {
            for (Melhoria melhoria : jogo.getJogador(indiceJogador).getMelhorias(c)) {
                nomes.add(melhoria.getNome());
            }
        }
        return nomes;
    }

    // melhoria do jogador da vez esperando ele escolher a coluna (só o Jochi), ou null
    public String getMelhoriaParaPosicionar() {
        Melhoria melhoria = jogo.getJogadorDaVez().getMelhoriaParaPosicionar();
        return melhoria == null ? null : melhoria.getNome();
    }

    public List<String> getBonusGuardados(int indiceJogador) {
        List<String> nomes = new ArrayList<>();
        for (TipoBonus bonus : jogo.getJogador(indiceJogador).getBonusGuardados()) {
            nomes.add(bonus.name());
        }
        return nomes;
    }

    public int getBonusObtidos(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getBonusObtidos();
    }

    //  cartas de conselheiro

    public List<String> getCartasSecretasOferecidas(int indiceJogador) {
        List<String> nomes = new ArrayList<>();
        for (CartaSecreta carta : jogo.getJogador(indiceJogador).getCartasOferecidas()) {
            nomes.add(carta.name());
        }
        return nomes;
    }

    public boolean escolherCartaSecreta(int indiceJogador, String carta) {
        CartaSecreta c = converter(CartaSecreta.class, carta);
        return avisarSe(c != null && jogo.escolherCartaSecreta(jogo.getJogador(indiceJogador), c),
                EVENTO_CARTA_SECRETA_ESCOLHIDA);
    }

    // null enquanto não escolheu
    public String getCartaSecreta(int indiceJogador) {
        CartaSecreta carta = jogo.getJogador(indiceJogador).getCartaSecreta();
        return carta == null ? null : carta.name();
    }

    // as que ainda estão na mesa
    public List<String> getCartasAbertas() {
        List<String> nomes = new ArrayList<>();
        for (CartaAberta carta : jogo.getCartasAbertas()) {
            nomes.add(carta.name());
        }
        return nomes;
    }

    //  turno: ativar coluna, mover e passar a vez

    public boolean ativarColuna(String coluna) {
        Coluna c = converter(Coluna.class, coluna);
        return avisarSe(c != null && jogo.ativarColuna(c), EVENTO_COLUNA_ATIVADA);
    }

    // true se o jogador da vez ainda não tem ficha nessa coluna
    public boolean colunaLivre(String coluna) {
        Coluna c = converter(Coluna.class, coluna);
        return c != null && jogo.getJogadorDaVez().podeAtivar(c);
    }

    // null enquanto não ativou coluna
    public String getColunaDoTurno() {
        Coluna c = jogo.getColunaDoTurno();
        return c == null ? null : c.name();
    }

    public int getMovimentosRestantes() {
        return jogo.getMovimentosRestantes();
    }

    public int getPunhosRestantes() {
        return jogo.getPunhosRestantes();
    }

    // a coluna ativada tinha o khan e ele ainda não foi movido
    public boolean isKhanPendente() {
        return jogo.isKhanPendente();
    }

    // peças virtuais das melhorias e da coluna: só valem neste turno
    public int getTributosVirtuais(String tipoTributo) {
        TipoTributo tipo = converter(TipoTributo.class, tipoTributo);
        if (tipo == TipoTributo.MOEDA) {
            return jogo.getMoedasVirtuais();
        }
        if (tipo == TipoTributo.ESPADA) {
            return jogo.getEspadasVirtuais();
        }
        return tipo == TipoTributo.YURT ? jogo.getYurtsVirtuais() : 0;
    }

    public int getTesourosVirtuais(String tipoTesouro) {
        TipoTesouro tipo = converter(TipoTesouro.class, tipoTesouro);
        return tipo == null ? 0 : jogo.getTesourosVirtuais(tipo);
    }

    // anda até a parada clicada pelo caminho mais curto, se der com os movimentos que sobraram
    public boolean moverPara(String nomeDaParada) {
        Parada parada = buscar(Parada.class, nomeDaParada);
        return avisarSe(parada != null && jogo.moverPara(parada), EVENTO_PEAO_MOVIDO);
    }

    // Altani: troca o peão que vai jogar (só antes de andar ou agir)
    public boolean selecionarPeao(int peao) {
        return avisarSe(jogo.selecionarPeao(peao), EVENTO_PEAO_SELECIONADO);
    }

    public boolean podePassarVez() {
        return jogo.podeEncerrarTurno();
    }

    // só passa depois de ativar uma coluna, mover o khan (se a coluna pedia) e resolver
    // as escolhas em aberto
    public boolean passarVez() {
        if (!jogo.encerrarTurno()) {
            return false;
        }
        notificar(jogo.isTerminado() ? EVENTO_JOGO_TERMINADO : EVENTO_TURNO_INICIADO);
        return true;
    }

    //  habilidades dos herdeiros

    // Chagatai e Ogedei usam a habilidade 1x por turno
    public boolean isHabilidadeDisponivel() {
        Herdeiro herdeiro = jogo.getJogadorDaVez().getHerdeiro();
        return (herdeiro == Herdeiro.CHAGATAI || herdeiro == Herdeiro.OGEDEI) && !jogo.isHabilidadeUsada();
    }

    // Chagatai: deixa de usar 1 cavalo e ganha 1 espada
    public boolean trocarCavaloPorEspada() {
        return avisarSe(jogo.trocarCavaloPorEspada(), EVENTO_TRIBUTO_GANHO);
    }

    // Ogedei: paga 1 moeda e pega um tesouro de qualquer cidade (menos o último dela)
    public boolean pegarTesouroComMoeda(String nomeDaCidade, String tipoTesouro) {
        Cidade cidade = buscar(Cidade.class, nomeDaCidade);
        TipoTesouro tesouro = converter(TipoTesouro.class, tipoTesouro);
        return avisarSe(cidade != null && tesouro != null && jogo.pegarTesouroComMoeda(cidade, tesouro),
                EVENTO_TESOURO_TOMADO);
    }

    //  tributos e khan

    public boolean pegarTributo(String nomeDaProvincia) {
        Provincia provincia = buscar(Provincia.class, nomeDaProvincia);
        return avisarSe(provincia != null && jogo.pegarTributo(provincia), EVENTO_TRIBUTO_PEGO);
    }

    public boolean isProvinciaDoKhan(String nomeDaProvincia) {
        Provincia provincia = buscar(Provincia.class, nomeDaProvincia);
        return provincia != null && provincia.isDoKhan();
    }

    public int getTributosNaProvincia(String nomeDaProvincia) {
        Provincia provincia = buscar(Provincia.class, nomeDaProvincia);
        return provincia == null ? 0 : provincia.getQuantidade();
    }

    public boolean moverKhan(String nomeDaProvincia) {
        Provincia provincia = buscar(Provincia.class, nomeDaProvincia);
        return avisarSe(provincia != null && jogo.moverKhan(provincia), EVENTO_KHAN_MOVIDO);
    }

    public boolean moverKhanParaAreaDeMelhorias() {
        return avisarSe(jogo.moverKhanParaAreaDeMelhorias(), EVENTO_KHAN_MOVIDO);
    }

    // nome da província do Khan, AREA_DE_MELHORIAS ou null (ainda fora do tabuleiro)
    public String getPosicaoDoKhan() {
        if (jogo.isKhanNaAreaDeMelhorias()) {
            return AREA_DE_MELHORIAS;
        }
        Provincia provincia = jogo.getProvinciaDoKhan();
        return provincia == null ? null : provincia.getNome();
    }

    //  cidades

    public boolean isCidadeRevelada(String nomeDaCidade) {
        Cidade cidade = buscar(Cidade.class, nomeDaCidade);
        return cidade != null && cidade.isRevelada();
    }

    // herdeiro que conquistou, ou null
    public String getConquistadorDaCidade(String nomeDaCidade) {
        Cidade cidade = buscar(Cidade.class, nomeDaCidade);
        return cidade == null || cidade.getConquistador() == null ? null
                : cidade.getConquistador().getHerdeiro().name();
    }

    public int getTesourosNaCidade(String nomeDaCidade, String tipoTesouro) {
        Cidade cidade = buscar(Cidade.class, nomeDaCidade);
        TipoTesouro tipo = converter(TipoTesouro.class, tipoTesouro);
        return cidade == null || tipo == null || !cidade.possuiTesouro(tipo) ? 0 : cidade.getQuantidadeDe(tipo);
    }

    public int getCustoDoProximoAtaque(String nomeDaCidade) {
        Cidade cidade = buscar(Cidade.class, nomeDaCidade);
        return cidade == null ? 0 : jogo.getCustoDoProximoAtaque(cidade);
    }

    // a cidade clicada vira o alvo; a janela de cartas mostra os tesouros dela
    public boolean selecionarCidade(String nomeDaCidade) {
        Cidade cidade = buscar(Cidade.class, nomeDaCidade);
        return avisarSe(cidade != null && jogo.selecionarCidade(cidade), EVENTO_CIDADE_SELECIONADA);
    }

    // null se nenhuma cidade foi escolhida
    public String getCidadeSelecionada() {
        Cidade cidade = jogo.getCidadeSelecionada();
        return cidade == null ? null : cidade.getNome();
    }

    public boolean atacarCidade(String nomeDaCidade, String tipoTesouro) {
        Cidade cidade = buscar(Cidade.class, nomeDaCidade);
        TipoTesouro tesouro = converter(TipoTesouro.class, tipoTesouro);
        return avisarSe(cidade != null && tesouro != null && jogo.atacarCidade(cidade, tesouro),
                EVENTO_TESOURO_TOMADO);
    }

    //  yurts

    public boolean construirYurt(String nomeDaParada) {
        Parada parada = buscar(Parada.class, nomeDaParada);
        return avisarSe(parada != null && jogo.construirYurt(parada), EVENTO_YURT_CONSTRUIDO);
    }

    // um nome de herdeiro por yurt que está na parada
    public List<String> getDonosDeYurts(String nomeDaParada) {
        List<String> donos = new ArrayList<>();
        Parada parada = buscar(Parada.class, nomeDaParada);
        if (parada != null) {
            for (Jogador dono : parada.getDonosDosYurts()) {
                donos.add(dono.getHerdeiro().name());
            }
        }
        return donos;
    }

    //  melhorias

    // "TIPO:COR" da peça no espaço (0 a 4) da área, ou null se estiver vazio
    public String getMelhoriaNaArea(int espaco) {
        if (espaco == TOPO_DA_PILHA) {
            return null; // a pilha fica virada pra baixo
        }
        Melhoria melhoria = jogo.getAreaDeMelhorias().espiar(espaco);
        return melhoria == null ? null : melhoria.getNome();
    }

    public int getTamanhoDaPilhaDeMelhorias() {
        return jogo.getAreaDeMelhorias().getTamanhoDaPilha();
    }

    // espaço da área (0 a 4) ou TOPO_DA_PILHA
    public boolean comprarMelhoria(int espaco) {
        return avisarSe(jogo.comprarMelhoria(espaco), EVENTO_MELHORIA_COMPRADA);
    }

    // completou uma linha e falta escolher a melhoria grátis da coluna branca
    public boolean isMelhoriaGratisPendente() {
        return jogo.isMelhoriaGratisPendente();
    }

    public boolean escolherMelhoriaGratis(int espaco) {
        return avisarSe(jogo.escolherMelhoriaGratis(espaco), EVENTO_MELHORIA_POSICIONADA);
    }

    // Jochi: coloca a melhoria que está esperando na coluna escolhida
    // Jochi no começo do jogo: a melhoria esperando pode ir pra coluna branca
    public boolean isEscolhaInicialDoJochi() {
        return jogo.getJogadorDaVez().getOutraMelhoriaInicial() != null;
    }

    // peças de tributo que ainda estão na reserva ao lado do tabuleiro
    public int getTributosNaReserva(String tipoTributo) {
        TipoTributo tipo = converter(TipoTributo.class, tipoTributo);
        return tipo == null ? 0 : jogo.getNaReserva(tipo);
    }

    // a cidade conquistada tem o yurt do conquistador no meio? (não tem se o estoque dele acabou)
    public boolean temYurtNaCidade(String nomeDaCidade) {
        Cidade cidade = buscar(Cidade.class, nomeDaCidade);
        return cidade != null && cidade.temYurtDoConquistador();
    }

    public boolean posicionarMelhoria(String coluna) {
        Coluna c = converter(Coluna.class, coluna);
        return avisarSe(c != null && jogo.posicionarMelhoria(c), EVENTO_MELHORIA_POSICIONADA);
    }

    //  conselheiros

    // tesouros que ele pede; QUALQUER_TESOURO no lugar de cada um no de 3 quaisquer
    public List<String> getPedidoDoConselheiro(String nomeDaParada) {
        List<String> nomes = new ArrayList<>();
        Conselheiro conselheiro = buscarConselheiro(nomeDaParada);
        if (conselheiro != null) {
            for (TipoTesouro tipo : conselheiro.getPedido()) {
                nomes.add(tipo == null ? QUALQUER_TESOURO : tipo.name());
            }
        }
        return nomes;
    }

    // bônus ainda em cima dele, ou null
    public String getBonusDoConselheiro(String nomeDaParada) {
        Conselheiro conselheiro = buscarConselheiro(nomeDaParada);
        return conselheiro == null || conselheiro.getBonus() == null ? null : conselheiro.getBonus().name();
    }

    // o jogador da vez fez uma parada nele neste turno?
    public boolean podeEntregarEm(String nomeDaParada) {
        Conselheiro conselheiro = buscarConselheiro(nomeDaParada);
        return conselheiro != null && jogo.fezParadaEm(conselheiro.getParada());
    }

    // entrega o conjunto fixo que o conselheiro pede
    public boolean entregarTesouros(String nomeDaParada) {
        Conselheiro conselheiro = buscarConselheiro(nomeDaParada);
        return avisarSe(conselheiro != null && !conselheiro.aceitaQualquer()
                && jogo.entregarTesouros(conselheiro, conselheiro.getPedido()), EVENTO_TESOUROS_ENTREGUES);
    }

    // o de 3 quaisquer: primeiro seleciona, depois escolhe os tesouros um por um
    public boolean selecionarConselheiro(String nomeDaParada) {
        Conselheiro conselheiro = buscarConselheiro(nomeDaParada);
        return avisarSe(conselheiro != null && jogo.selecionarConselheiro(conselheiro), EVENTO_CONSELHEIRO_SELECIONADO);
    }

    public String getConselheiroSelecionado() {
        Conselheiro conselheiro = jogo.getConselheiroSelecionado();
        return conselheiro == null ? null : conselheiro.getParada().getNome();
    }

    public List<String> getTesourosEmEntrega() {
        List<String> nomes = new ArrayList<>();
        for (TipoTesouro tipo : jogo.getEntregaEmMontagem()) {
            nomes.add(tipo.name());
        }
        return nomes;
    }

    // ao completar os 3, a entrega é feita
    public boolean adicionarTesouroAEntrega(String tipoTesouro) {
        TipoTesouro tesouro = converter(TipoTesouro.class, tipoTesouro);
        if (tesouro == null || jogo.getConselheiroSelecionado() == null) {
            return false;
        }
        boolean feito = jogo.adicionarTesouroAEntrega(tesouro);
        boolean entregou = feito && jogo.getConselheiroSelecionado() == null;
        return avisarSe(feito, entregou ? EVENTO_TESOUROS_ENTREGUES : EVENTO_CONSELHEIRO_SELECIONADO);
    }

    public void cancelarSelecao() {
        jogo.cancelarSelecao();
        notificar(EVENTO_SELECAO_CANCELADA);
    }

    //  bônus

    public boolean usarBonus(String tipoBonus) {
        TipoBonus bonus = converter(TipoBonus.class, tipoBonus);
        return avisarSe(bonus != null && jogo.usarBonus(bonus), EVENTO_BONUS_USADO);
    }

    // tesouros tirados pelo bônus esperando a escolha (vazia se não tem)
    public List<String> getTesourosParaEscolher() {
        List<String> nomes = new ArrayList<>();
        for (TipoTesouro tipo : jogo.getTesourosParaEscolher()) {
            nomes.add(tipo.name());
        }
        return nomes;
    }

    public boolean escolherTesouroDoBonus(String tipoTesouro) {
        TipoTesouro tesouro = converter(TipoTesouro.class, tipoTesouro);
        return avisarSe(tesouro != null && jogo.escolherTesouroDoBonus(tesouro), EVENTO_BONUS_USADO);
    }

    //  votos e fim de jogo

    public int getVotosNoConselho() {
        return jogo.getVotosNoConselho();
    }

    public int getVotosParaFimDeJogo() {
        return jogo.getVotosParaFimDeJogo();
    }

    public boolean fimDeJogoDisparado() {
        return jogo.fimDeJogoDisparado();
    }

    public boolean isUltimaRodada() {
        return jogo.isUltimaRodada();
    }

    public boolean isJogoTerminado() {
        return jogo.isTerminado();
    }

    public int getVotosDeInfluencia(int indiceJogador) {
        return jogo.getVotosDeInfluencia(jogo.getJogador(indiceJogador));
    }

    public int getVotosDaCartaSecreta(int indiceJogador) {
        return jogo.getVotosDaCartaSecreta(jogo.getJogador(indiceJogador));
    }

    public int getTotalDeTesouros(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getTotalDeTesouros();
    }

    // herdeiros vencedores (mais de um se o empate persistir)
    public List<String> getVencedores() {
        List<String> nomes = new ArrayList<>();
        for (Jogador jogador : jogo.getVencedores()) {
            nomes.add(jogador.getHerdeiro().name());
        }
        return nomes;
    }

    //  auxiliares

    // null se não existir local com esse nome ou se for de outro tipo
    private <T extends Local> T buscar(Class<T> classe, String nome) {
        Local local = jogo.getTabuleiro().getLocal(nome);
        return classe.isInstance(local) ? classe.cast(local) : null;
    }

    private Conselheiro buscarConselheiro(String nomeDaParada) {
        Parada parada = buscar(Parada.class, nomeDaParada);
        return parada == null ? null : jogo.getTabuleiro().getConselheiro(parada);
    }

    // null se o texto não for um valor do enum
    private static <E extends Enum<E>> E converter(Class<E> classe, String nome) {
        try {
            return Enum.valueOf(classe, nome);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
