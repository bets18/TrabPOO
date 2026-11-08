package model;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * Testes da classe HerdeirosDeKhanAPI
 */

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
        api = HerdeirosDeKhanAPI.getInstancia();
        api.iniciarPartida(new Jogo(tabuleiro, Arrays.asList(Herdeiro.TOLUI, Herdeiro.ALTANI)));
    }

    @Test(timeout = 2000)
    public void testaSeApiEUmaInstanciaSo() {
        assertSame("Singleton: getInstancia sempre devolve o mesmo objeto", api, HerdeirosDeKhanAPI.getInstancia());
    }

    // observador de teste que só guarda os eventos recebidos (pegos pelo get, como no Cap. 17)
    private static class Espiao implements ObservadorIF {
        private final java.util.List<Integer> eventos = new java.util.ArrayList<>();

        @Override
        public void notificar(ObservadoIF observado) {
            eventos.add(observado.get(HerdeirosDeKhanAPI.ULTIMO_EVENTO));
        }
    }

    @Test(timeout = 2000)
    public void testaSeObservadorEAvisadoSoQuandoAJogadaDaCerto() {
        Espiao espiao = new Espiao();
        api.adicionarObservador(espiao);
        try {
            assertFalse(api.moverPara("P1")); // sem cavalos
            assertTrue(api.ativarColuna("VERMELHA"));
            assertTrue(api.moverPara("P1"));
            api.exibirCartas();

            assertEquals(Arrays.asList(HerdeirosDeKhanAPI.EVENTO_COLUNA_ATIVADA, HerdeirosDeKhanAPI.EVENTO_PEAO_MOVIDO,
                    HerdeirosDeKhanAPI.EVENTO_JANELA_DE_CARTAS), espiao.eventos);
        } finally {
            api.removerObservador(espiao);
        }
    }

    @Test(timeout = 2000)
    public void testaSeApiExecutaJogadaUsandoApenasNomes() {
        assertFalse("Sem coluna ativada nao tem cavalo pra andar", api.moverPara("P1"));
        assertTrue(api.ativarColuna("VERMELHA")); // Tolui: 1 cavalo
        assertTrue(api.moverPara("P1"));
        assertTrue(api.pegarTributo("Estepe"));

        assertEquals("P1", api.getPosicaoDoJogadorDaVez());
        assertEquals("O 1º jogador começa com 1 moeda e pegou mais 1.", 2, api.getQuantidadeTributo("MOEDA"));
    }

    @Test(timeout = 2000)
    public void testaSeApiRecusaNomesInexistentesOuDeTipoErrado() {
        api.ativarColuna("VERMELHA");
        assertFalse("Não existe parada chamada Atlantida.", api.moverPara("Atlantida"));
        assertFalse("Estepe é uma província, não uma parada.", api.moverPara("Estepe"));
        assertFalse("Estepe é uma província, não uma parada.", api.construirYurt("Estepe"));
        assertFalse("OURO não é um tipo de tesouro.", api.atacarCidade("Estepe", "OURO"));
        assertEquals("TOLUI", api.getHerdeiroDoJogadorDaVez());
    }
}
