package model;

import java.util.ArrayList;
import java.util.List;

// Única classe pública do pacote. View e Controller só trocam Strings e tipos primitivos
// com o Model, nunca as classes internas. Singleton e Façade serão aplicados na 3ª iteração.
public class HerdeirosDeKhanAPI {
    private final Jogo jogo;

    HerdeirosDeKhanAPI(Jogo jogo) {
        this.jogo = jogo;
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

    public void passarVez() {
        jogo.passarVez();
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

    // Retorna null se o local não existir ou não for do tipo esperado
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
