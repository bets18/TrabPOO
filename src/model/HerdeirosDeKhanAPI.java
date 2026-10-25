package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// única classe pública do model. A View e o Controller só falam com ela, e só passam
// String e int, nunca as classes de dentro do pacote. Singleton fica pra 3ª iteração
public class HerdeirosDeKhanAPI {
    private final Jogo jogo;

    // usado nos testes, com um tabuleiro montado à mão
    HerdeirosDeKhanAPI(Jogo jogo) {
        this.jogo = jogo;
    }

    // partida nova no mapa de verdade. Os nomes vêm na ordem de jogo, ex: "ALTANI", "JOCHI"
    public HerdeirosDeKhanAPI(List<String> herdeirosNaOrdemDeJogo) {
        List<Herdeiro> herdeiros = new ArrayList<>();
        for (String nome : herdeirosNaOrdemDeJogo) {
            Herdeiro herdeiro = converter(Herdeiro.class, nome);
            if (herdeiro == null) {
                throw new IllegalArgumentException("Herdeiro desconhecido: " + nome);
            }
            herdeiros.add(herdeiro);
        }
        Tabuleiro tabuleiro = MapaPadrao.criar();
        jogo = new Jogo(tabuleiro, herdeiros);

        // embaralha as cidades e os tesouros (8 de cada tipo) antes de revelar
        List<Cidade> cidades = tabuleiro.getLocais(Cidade.class);
        List<TipoTesouro> tesouros = new ArrayList<>();
        for (TipoTesouro tipo : TipoTesouro.values()) {
            for (int i = 0; i < 8; i++) {
                tesouros.add(tipo);
            }
        }
        Collections.shuffle(cidades);
        Collections.shuffle(tesouros);
        jogo.prepararCidades(cidades, tesouros);
    }

    // os jogadores são identificados pelo índice na ordem de jogo

    public int getIndiceJogadorDaVez() {
        return jogo.getIndiceJogadorDaVez();
    }

    public String getHerdeiro(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getHerdeiro().name();
    }

    // posição do peão ativo
    public String getPosicao(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getPosicao().getNome();
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

    // Altani: troca o peão que vai jogar (só antes de andar)
    public boolean selecionarPeao(int peao) {
        return jogo.selecionarPeao(peao);
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

    // turno: ativar coluna, mover e passar a vez

    public boolean ativarColuna(String coluna) {
        Coluna c = converter(Coluna.class, coluna);
        return c != null && jogo.ativarColuna(c);
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

    // anda até a parada clicada pelo caminho mais curto, se der com os cavalos que sobraram
    public boolean moverPara(String nomeDaParada) {
        Parada parada = buscar(Parada.class, nomeDaParada);
        return parada != null && jogo.moverPara(parada);
    }

    public int getNumeroDeJogadores() {
        return jogo.getNumeroDeJogadores();
    }

    public String getHerdeiroDoJogadorDaVez() {
        return jogo.getJogadorDaVez().getHerdeiro().name();
    }

    public String getPosicaoDoJogadorDaVez() {
        return jogo.getJogadorDaVez().getPosicao().getNome();
    }

    public int getQuantidadeTributo(String tipoTributo) {
        TipoTributo tipo = converter(TipoTributo.class, tipoTributo);
        return tipo == null ? 0 : jogo.getJogadorDaVez().getQuantidadeTributo(tipo);
    }

    // só passa depois de ativar uma coluna
    public boolean passarVez() {
        return jogo.encerrarTurno();
    }

    public boolean pegarTributo(String nomeDaProvincia) {
        Provincia provincia = buscar(Provincia.class, nomeDaProvincia);
        return provincia != null && jogo.pegarTributo(provincia);
    }

    public boolean moverKhan(String nomeDaProvincia) {
        Provincia provincia = buscar(Provincia.class, nomeDaProvincia);
        return provincia != null && jogo.moverKhan(provincia);
    }

    public boolean atacarCidade(String nomeDaCidade, String tipoTesouro) {
        Cidade cidade = buscar(Cidade.class, nomeDaCidade);
        TipoTesouro tesouro = converter(TipoTesouro.class, tipoTesouro);
        return cidade != null && tesouro != null && jogo.atacarCidade(cidade, tesouro);
    }

    public boolean construirYurt(String nomeDaParada) {
        Parada parada = buscar(Parada.class, nomeDaParada);
        return parada != null && jogo.construirYurt(parada);
    }

    public boolean fimDeJogoDisparado() {
        return jogo.fimDeJogoDisparado();
    }

    // null se não existir local com esse nome ou se for de outro tipo
    private <T extends Local> T buscar(Class<T> classe, String nome) {
        Local local = jogo.getTabuleiro().getLocal(nome);
        return classe.isInstance(local) ? classe.cast(local) : null;
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
