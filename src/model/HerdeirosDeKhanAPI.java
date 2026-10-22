package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Única classe pública do pacote. View e Controller só trocam Strings e tipos primitivos
// com o Model, nunca as classes internas. Singleton e Façade serão aplicados na 3ª iteração.
public class HerdeirosDeKhanAPI {
    private final Jogo jogo;

    HerdeirosDeKhanAPI(Jogo jogo) {
        this.jogo = jogo;
    }

    // nova partida no mapa real, com os herdeiros na ordem de jogo (ex.: "ALTANI", "JOCHI").
    // as cidades e os tesouros são embaralhados aqui, na preparação (manual, págs. 3 e 4).
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

        List<Cidade> cidades = MapaPadrao.getCidades(tabuleiro);
        List<TipoTesouro> tesouros = new ArrayList<>();
        for (TipoTesouro tipo : TipoTesouro.values()) {
            for (int i = 0; i < 8; i++) {
                tesouros.add(tipo); // 8 peças de cada tipo
            }
        }
        Collections.shuffle(cidades);
        Collections.shuffle(tesouros);
        jogo.prepararCidades(cidades, tesouros);
    }

    // jogadores, por índice na ordem de jogo

    public int getIndiceJogadorDaVez() {
        return jogo.getIndiceJogadorDaVez();
    }

    public String getHerdeiro(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getHerdeiro().name();
    }

    // posição do peão ativo do jogador
    public String getPosicao(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getPosicao().getNome();
    }

    // só Altani tem 2 peões
    public int getNumeroDePeoes(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getNumeroDePeoes();
    }

    public String getPosicaoDoPeao(int indiceJogador, int peao) {
        return jogo.getJogador(indiceJogador).getPosicaoDoPeao(peao).getNome();
    }

    public int getPeaoAtivo(int indiceJogador) {
        return jogo.getJogador(indiceJogador).getPeaoAtivo();
    }

    // Altani: troca o peão que vai mover (antes de mover neste turno)
    public boolean selecionarPeao(int peao) {
        return jogo.selecionarPeao(peao);
    }

    // herdeiros donos dos yurts desta parada (um nome por yurt)
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

    // peão do jogador da vez, que não seja o ativo, nesta parada; -1 se não houver
    public int getPeaoInativoEm(String nomeDaParada) {
        Parada parada = buscar(Parada.class, nomeDaParada);
        return parada == null ? -1 : jogo.peaoInativoEm(parada);
    }

    // turno: ativar coluna, mover, encerrar

    public boolean ativarColuna(String coluna) {
        Coluna c = converter(Coluna.class, coluna);
        return c != null && jogo.ativarColuna(c);
    }

    // a coluna pode ser ativada pelo jogador da vez (não tem ficha dele)?
    public boolean colunaLivre(String coluna) {
        Coluna c = converter(Coluna.class, coluna);
        return c != null && jogo.getJogadorDaVez().podeAtivar(c);
    }

    // null enquanto o jogador da vez não ativou nenhuma coluna
    public String getColunaDoTurno() {
        Coluna c = jogo.getColunaDoTurno();
        return c == null ? null : c.name();
    }

    public int getMovimentosRestantes() {
        return jogo.getMovimentosRestantes();
    }

    // move o peão da vez até a parada clicada pelo caminho mais curto, se couber nos cavalos
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

    // só passa a vez depois que o jogador ativou uma coluna
    public boolean passarVez() {
        return jogo.encerrarTurno();
    }

    public boolean moverPeao(List<String> nomesDasParadas, int movimentosDisponiveis) {
        List<Parada> caminho = new ArrayList<>();
        for (String nome : nomesDasParadas) {
            Parada parada = buscar(Parada.class, nome);
            if (parada == null) {
                return false;
            }
            caminho.add(parada);
        }
        return jogo.moverPeao(caminho, movimentosDisponiveis);
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

    // retorna null se o local não existir ou não for do tipo esperado
    private <T extends Local> T buscar(Class<T> classe, String nome) {
        Local local = jogo.getTabuleiro().getLocal(nome);
        return classe.isInstance(local) ? classe.cast(local) : null;
    }

    private static <E extends Enum<E>> E converter(Class<E> classe, String nome) {
        try {
            return Enum.valueOf(classe, nome);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
