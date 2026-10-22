package model;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * Testes do turno no mapa real: ativação de colunas e movimento por clique (2ª iteração).
 */

public class TurnoTest {

    private Jogo jogo;
    private Tabuleiro t;

    @Before
    public void criarJogo() {
        t = MapaPadrao.criar();
        jogo = new Jogo(t, Arrays.asList(Herdeiro.ALTANI, Herdeiro.OGEDEI));
    }

    private Parada parada(String nome) {
        return (Parada) t.getLocal(nome);
    }

    @Test(timeout = 2000)
    public void testaSeAtivarColunaDaOsCavalosDela() {
        assertTrue(jogo.ativarColuna(Coluna.VERDE)); // Altani: verde tem 2 cavalos

        assertEquals("Coluna verde da Altani tem 2 cavalos", 2, jogo.getMovimentosRestantes());
        assertFalse("Só uma coluna por turno", jogo.ativarColuna(Coluna.VERMELHA));
    }

    @Test(timeout = 2000)
    public void testaSeColunaComFichaNaoPodeSerReativadaAteUsarABranca() {
        Jogador altani = jogo.getJogadorDaVez();
        jogo.ativarColuna(Coluna.VERDE);
        jogo.encerrarTurno();
        jogo.ativarColuna(Coluna.VERDE); // Ogedei
        jogo.encerrarTurno();

        assertFalse("A verde da Altani ainda tem ficha", jogo.ativarColuna(Coluna.VERDE));
        assertTrue("A branca recupera as fichas", jogo.ativarColuna(Coluna.BRANCA));
        assertTrue("Depois da branca a verde volta a ficar livre", altani.podeAtivar(Coluna.VERDE));
    }

    @Test(timeout = 2000)
    public void testaSeMoverParaUsaOMenorCaminhoDentroDosCavalos() {
        jogo.ativarColuna(Coluna.VERDE); // 2 cavalos

        assertFalse("C9 fica a 4 movimentos de Karakorum, mais que os 2 cavalos", jogo.moverPara(parada("C9")));
        assertTrue("Karakorum -> C4 -> C5 cabe em 2 movimentos", jogo.moverPara(parada("C5")));
        assertEquals(0, jogo.getMovimentosRestantes());
        assertSame(parada("C5"), jogo.getJogadorDaVez().getPosicao());
    }

    @Test(timeout = 2000)
    public void testaSeMoverParaSemAtivarColunaNaoFazNada() {
        assertFalse(jogo.moverPara(parada("C4")));
        assertSame("Peao continua em Karakorum", t.getKarakorum(), jogo.getJogadorDaVez().getPosicao());
    }

    @Test(timeout = 2000)
    public void testaSeAltaniComecaComDoisPeoesEmKarakorumEEscolheQualMover() {
        Jogador altani = jogo.getJogadorDaVez();
        assertEquals("Altani tem 2 peoes", 2, altani.getNumeroDePeoes());
        assertTrue(jogo.selecionarPeao(1));
        jogo.ativarColuna(Coluna.VERDE);
        jogo.moverPara(parada("C4"));

        assertSame("O peao 2 foi para C4", parada("C4"), altani.getPosicaoDoPeao(1));
        assertSame("O peao 1 ficou em Karakorum", t.getKarakorum(), altani.getPosicaoDoPeao(0));
        assertFalse("Depois de mover nao pode trocar de peao no mesmo turno", jogo.selecionarPeao(0));
    }

    @Test(timeout = 2000)
    public void testaSeToluiPulaKarakorumPeloYurtInicial() {
        Jogo comTolui = new Jogo(t, Arrays.asList(Herdeiro.TOLUI, Herdeiro.OGEDEI));
        Jogador tolui = comTolui.getJogadorDaVez();
        assertTrue("Tolui comeca com yurt em Karakorum", t.getKarakorum().possuiYurtDe(tolui));
        assertEquals("O yurt sai do estoque dele", Jogador.YURTS_INICIAIS - 1, tolui.getYurtsNoEstoque());

        comTolui.ativarColuna(Coluna.VERMELHA); // 1 cavalo
        comTolui.moverPara(parada("R8"));
        comTolui.encerrarTurno();
        comTolui.ativarColuna(Coluna.BRANCA); // Ogedei
        comTolui.encerrarTurno();
        comTolui.ativarColuna(Coluna.VERDE); // Tolui: 2 cavalos

        assertTrue(comTolui.moverPara(parada("C4")));
        assertEquals("R8 -> C4 pulando Karakorum custa 1 movimento", 1, comTolui.getMovimentosRestantes());
    }

    @Test(timeout = 2000)
    public void testaSeOutroHerdeiroGastaDoisMovimentosPassandoPorKarakorum() {
        jogo.ativarColuna(Coluna.VERMELHA); // Altani: 1 cavalo
        jogo.moverPara(parada("R8"));
        jogo.encerrarTurno();
        jogo.ativarColuna(Coluna.BRANCA); // Ogedei
        jogo.encerrarTurno();
        jogo.ativarColuna(Coluna.VERDE); // Altani: 2 cavalos

        assertTrue(jogo.moverPara(parada("C4")));
        assertEquals("Sem yurt em Karakorum, R8 -> Karakorum -> C4 custa 2", 0, jogo.getMovimentosRestantes());
    }

    @Test(timeout = 2000)
    public void testaSeTurnoSoAcabaDepoisDeAtivarColuna() {
        assertFalse("Sem coluna ativada o turno nao acaba", jogo.encerrarTurno());

        jogo.ativarColuna(Coluna.AMARELA);
        assertTrue(jogo.encerrarTurno());
        assertEquals("Agora e a vez do 2º jogador", 1, jogo.getIndiceJogadorDaVez());
    }
}
