package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Jogador {
    static final int YURTS_INICIAIS = 12;
    static final int MELHORIAS_POR_COLUNA = 4;

    private final Herdeiro herdeiro;
    private final Map<TipoTributo, Integer> tributos = new HashMap<>();
    private final Map<TipoTesouro, Integer> tesouros = new HashMap<>();
    private int yurtsNoEstoque = YURTS_INICIAIS;
    private int votos = 0;
    // onde está cada peão (só a Altani tem 2). O índice da lista é o número do peão
    private final List<Parada> posicoes = new ArrayList<>();
    private int peaoAtivo = 0; // peão que joga neste turno
    private final Set<Coluna> colunasComFicha = new HashSet<>();

    // melhorias de cada coluna, de baixo pra cima (na branca, o índice é a linha completada)
    private final Map<Coluna, List<Melhoria>> melhorias = new HashMap<>();
    // só o Jochi escolhe a coluna da melhoria; enquanto não escolhe ela fica aqui
    private Melhoria melhoriaParaPosicionar = null;
    // Jochi: a outra das 2 melhorias iniciais, enquanto ele não decide qual vai pra coluna branca
    private Melhoria outraMelhoriaInicial = null;

    private final Set<TipoBonus> bonusGuardados = new HashSet<>(); // cada bônus só existe uma vez no jogo
    private int bonusObtidos = 0;
    private int cidadesConquistadas = 0;

    private final List<CartaSecreta> cartasOferecidas = new ArrayList<>();
    private CartaSecreta cartaSecreta = null;

    Jogador(Herdeiro herdeiro) {
        this.herdeiro = herdeiro;
        int peoes = herdeiro == Herdeiro.ALTANI ? 2 : 1;
        for (int i = 0; i < peoes; i++) {
            posicoes.add(null);
        }
        for (TipoTributo tipo : TipoTributo.values()) {
            tributos.put(tipo, 0);
        }
        for (Coluna coluna : Coluna.values()) {
            melhorias.put(coluna, new ArrayList<Melhoria>());
        }
    }

    Herdeiro getHerdeiro() {
        return herdeiro;
    }

    //  tributos

    void adicionarTributos(TipoTributo tipo, int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade de tributos não pode ser negativa.");
        }
        tributos.put(tipo, tributos.get(tipo) + quantidade);
    }

    // se não tiver peças suficientes devolve false e não gasta nada
    boolean gastarTributos(TipoTributo tipo, int quantidade) {
        int atual = tributos.get(tipo);
        if (quantidade < 0 || atual < quantidade) {
            return false;
        }
        tributos.put(tipo, atual - quantidade);
        return true;
    }

    int getQuantidadeTributo(TipoTributo tipo) {
        return tributos.get(tipo);
    }

    //  tesouros

    void adicionarTesouro(TipoTesouro tipo) {
        tesouros.put(tipo, tesouros.getOrDefault(tipo, 0) + 1);
    }

    boolean gastarTesouro(TipoTesouro tipo) {
        int atual = tesouros.getOrDefault(tipo, 0);
        if (atual == 0) {
            return false;
        }
        tesouros.put(tipo, atual - 1);
        return true;
    }

    int getQuantidadeTesouro(TipoTesouro tipo) {
        return tesouros.getOrDefault(tipo, 0);
    }

    // tesouros ainda não entregues (desempate no fim do jogo)
    int getTotalDeTesouros() {
        int total = 0;
        for (int quantidade : tesouros.values()) {
            total += quantidade;
        }
        return total;
    }

    //  yurts e votos

    int getYurtsNoEstoque() {
        return yurtsNoEstoque;
    }

    boolean retirarYurtDoEstoque() {
        if (yurtsNoEstoque == 0) {
            return false;
        }
        yurtsNoEstoque--;
        return true;
    }

    int getVotos() {
        return votos;
    }

    void adicionarVotos(int quantidade) {
        votos += quantidade;
    }

    void conquistarCidade() {
        cidadesConquistadas++;
    }

    int getCidadesConquistadas() {
        return cidadesConquistadas;
    }

    //  fichas de ativação

    // a branca sempre pode ser ativada, porque é ela que devolve as fichas
    boolean podeAtivar(Coluna coluna) {
        return coluna == Coluna.BRANCA || !colunasComFicha.contains(coluna);
    }

    void colocarFicha(Coluna coluna) {
        colunasComFicha.add(coluna);
    }

    void recuperarFichas() {
        colunasComFicha.clear();
    }

    //  melhorias

    List<Melhoria> getMelhorias(Coluna coluna) {
        return new ArrayList<>(melhorias.get(coluna));
    }

    int getQuantidadeDeMelhorias(Coluna coluna) {
        return melhorias.get(coluna).size();
    }

    boolean temEspacoNaColuna(Coluna coluna) {
        return getQuantidadeDeMelhorias(coluna) < MELHORIAS_POR_COLUNA;
    }

    boolean temEspacoEmColunaColorida() {
        for (Coluna coluna : Coluna.values()) {
            if (coluna.isColorida() && temEspacoNaColuna(coluna)) {
                return true;
            }
        }
        return false;
    }

    // a peça vai pro espaço livre mais baixo da coluna
    boolean adicionarMelhoria(Coluna coluna, Melhoria melhoria) {
        if (!temEspacoNaColuna(coluna)) {
            return false;
        }
        melhorias.get(coluna).add(melhoria);
        return true;
    }

    List<Melhoria> getTodasAsMelhorias() {
        List<Melhoria> todas = new ArrayList<>();
        for (Coluna coluna : Coluna.values()) {
            todas.addAll(melhorias.get(coluna));
        }
        return todas;
    }

    Melhoria getOutraMelhoriaInicial() {
        return outraMelhoriaInicial;
    }

    void setOutraMelhoriaInicial(Melhoria melhoria) {
        outraMelhoriaInicial = melhoria;
    }

    Melhoria getMelhoriaParaPosicionar() {
        return melhoriaParaPosicionar;
    }

    void setMelhoriaParaPosicionar(Melhoria melhoria) {
        melhoriaParaPosicionar = melhoria;
    }

    //  bônus

    // o voto é usado na hora e não fica guardado, mas conta como bônus obtido
    void contarBonusObtido() {
        bonusObtidos++;
    }

    int getBonusObtidos() {
        return bonusObtidos;
    }

    void guardarBonus(TipoBonus bonus) {
        bonusGuardados.add(bonus);
    }

    // false se ele não tem esse bônus guardado
    boolean usarBonus(TipoBonus bonus) {
        return bonusGuardados.remove(bonus);
    }

    List<TipoBonus> getBonusGuardados() {
        return new ArrayList<>(bonusGuardados);
    }

    //  carta de conselheiro secreto

    void oferecerCartasSecretas(List<CartaSecreta> cartas) {
        cartasOferecidas.clear();
        cartasOferecidas.addAll(cartas);
    }

    List<CartaSecreta> getCartasOferecidas() {
        return new ArrayList<>(cartasOferecidas);
    }

    // escolhe uma das 2 recebidas e descarta a outra; só dá pra escolher uma vez
    boolean escolherCartaSecreta(CartaSecreta carta) {
        if (cartaSecreta != null || !cartasOferecidas.contains(carta)) {
            return false;
        }
        cartaSecreta = carta;
        return true;
    }

    CartaSecreta getCartaSecreta() {
        return cartaSecreta;
    }

    //  peões

    // posição do peão ativo
    Parada getPosicao() {
        return posicoes.get(peaoAtivo);
    }

    void setPosicao(Parada posicao) {
        posicoes.set(peaoAtivo, posicao);
    }

    int getNumeroDePeoes() {
        return posicoes.size();
    }

    Parada getPosicaoDoPeao(int peao) {
        return posicoes.get(peao);
    }

    void setPosicaoDoPeao(int peao, Parada posicao) {
        posicoes.set(peao, posicao);
    }

    int getPeaoAtivo() {
        return peaoAtivo;
    }

    void setPeaoAtivo(int peao) {
        peaoAtivo = peao;
    }
}
