package model;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class HerdeirosDeKhanAPITest {

    private HerdeirosDeKhanAPI api;

    @Before
    public void criarApi() {
        Tabuleiro tabuleiro = new Tabuleiro();
        Parada karakorum = new Parada("Karakorum", null, TipoParada.KARAKORUM);
        Parada p1 = new Parada("P1", Regiao.RUSSIA, TipoParada.SIMPLES);
        Provincia estepe = new Provincia("Estepe", Regiao.RUSSIA, TipoTributo.MOEDA, false);
        tabuleiro.adicionarLocal(karakorum);
        tabuleiro.adicionarLocal(p1);
        tabuleiro.adicionarLocal(estepe);
        tabuleiro.adicionarRota(karakorum, p1);
        tabuleiro.adicionarVizinhanca(p1, estepe);
        api = new HerdeirosDeKhanAPI(new Jogo(tabuleiro, Arrays.asList(Herdeiro.TOLUI, Herdeiro.ALTANI)));
    }

    @Test(timeout = 2000)
    public void testaSeApiExecutaJogadaUsandoApenasNomes() {
        assertTrue(api.moverPeao(Arrays.asList("P1"), 1));
        assertTrue(api.pegarTributo("Estepe"));

        assertEquals("P1", api.getPosicaoDoJogadorDaVez());
        assertEquals("O 1º jogador começa com 1 moeda e pegou mais 1.", 2, api.getQuantidadeTributo("MOEDA"));
    }

    @Test(timeout = 2000)
    public void testaSeApiRecusaNomesInexistentesOuDeTipoErrado() {
        assertFalse("Não existe parada chamada Atlantida.", api.moverPeao(Arrays.asList("Atlantida"), 1));
        assertFalse("Estepe é uma província, não uma parada.", api.construirYurt("Estepe"));
        assertFalse("OURO não é um tipo de tesouro.", api.atacarCidade("Estepe", "OURO"));
        assertEquals("TOLUI", api.getHerdeiroDoJogadorDaVez());
    }
}
