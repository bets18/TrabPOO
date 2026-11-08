package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * Testes das melhorias (3ª iteração): compra, coluna pela cor, linha completa,
 * reposição pelo Khan e a habilidade do Jochi.
 */

public class MelhoriasTest {

    private Tabuleiro t;

    @Before
    public void criarTabuleiro() {
        t = MapaPadrao.criar();
    }

    // pilha em ordem fixa: os 5 primeiros vão pra área
    private static List<Melhoria> pilha(Melhoria... melhorias) {
        return new ArrayList<>(Arrays.asList(melhorias));
    }

    private static Melhoria m(TipoMelhoria tipo, Coluna cor) {
        return new Melhoria(tipo, cor);
    }

    private Jogo criarJogo(List<Melhoria> melhorias, Herdeiro... herdeiros) {
        Jogo jogo = new Jogo(t, Arrays.asList(herdeiros));
        jogo.prepararMelhorias(melhorias);
        return jogo;
    }

    @Test(timeout = 2000)
    public void testaSeMelhoriaCustaDuasMoedasEVaiParaAColunaDaSuaCor() {
        Melhoria cavalos = m(TipoMelhoria.CAVALOS, Coluna.VERDE);
        Jogo jogo = criarJogo(pilha(m(TipoMelhoria.PUNHO, Coluna.AMARELA), cavalos), Herdeiro.ALTANI, Herdeiro.OGEDEI);
        Jogador altani = jogo.getJogadorDaVez();

        assertFalse("Altani começa com 1 moeda só", jogo.comprarMelhoria(1));
        altani.adicionarTributos(TipoTributo.MOEDA, 1);
        assertTrue(jogo.comprarMelhoria(1));

        assertEquals(0, altani.getQuantidadeTributo(TipoTributo.MOEDA));
        assertEquals(Arrays.asList(cavalos), altani.getMelhorias(Coluna.VERDE));
        assertNull(jogo.getAreaDeMelhorias().espiar(0));
        assertNotNull("A peça que sobrou andou pro último espaço", jogo.getAreaDeMelhorias().espiar(4));
    }

    @Test(timeout = 2000)
    public void testaSeMelhoriaCompradaSoValeNoTurnoSeguinte() {
        Jogo jogo = criarJogo(pilha(m(TipoMelhoria.CAVALOS, Coluna.VERDE)), Herdeiro.ALTANI, Herdeiro.OGEDEI);
        Jogador altani = jogo.getJogadorDaVez();
        altani.adicionarTributos(TipoTributo.MOEDA, 1);
        jogo.comprarMelhoria(0);

        jogo.ativarColuna(Coluna.VERDE);
        assertEquals("Comprou neste turno: só os 2 cavalos impressos", 2, jogo.getMovimentosRestantes());

        jogo.encerrarTurno();
        jogo.ativarColuna(Coluna.BRANCA); // Ogedei
        jogo.moverKhanParaAreaDeMelhorias();
        jogo.encerrarTurno();
        jogo.ativarColuna(Coluna.BRANCA); // Altani recupera as fichas
        jogo.moverKhan((Provincia) t.getLocal("Khan1"));
        jogo.encerrarTurno();
        jogo.ativarColuna(Coluna.VERDE); // Ogedei
        jogo.encerrarTurno();

        jogo.ativarColuna(Coluna.VERDE);
        assertEquals("Agora a melhoria soma mais 2 cavalos", 4, jogo.getMovimentosRestantes());
    }

    @Test(timeout = 2000)
    public void testaSeMoedaVirtualDaColunaAjudaAPagar() {
        Jogo jogo = criarJogo(pilha(m(TipoMelhoria.PUNHO, Coluna.AMARELA)), Herdeiro.ALTANI, Herdeiro.OGEDEI);
        Jogador altani = jogo.getJogadorDaVez();

        jogo.ativarColuna(Coluna.AMARELA); // Altani: amarela tem moeda virtual
        assertTrue("1 moeda virtual + 1 de verdade", jogo.comprarMelhoria(0));
        assertEquals(0, altani.getQuantidadeTributo(TipoTributo.MOEDA));
        assertEquals(0, jogo.getMoedasVirtuais());
    }

    @Test(timeout = 2000)
    public void testaSeCompletarUmaLinhaDaMelhoriaGratisNaColunaBranca() {
        Melhoria gratis = m(TipoMelhoria.MOEDA, Coluna.VERDE);
        Jogo jogo = criarJogo(pilha(m(TipoMelhoria.ESPADA, Coluna.AMARELA), gratis), Herdeiro.ALTANI, Herdeiro.OGEDEI);
        Jogador altani = jogo.getJogadorDaVez();
        altani.adicionarMelhoria(Coluna.VERMELHA, m(TipoMelhoria.PUNHO, Coluna.VERMELHA));
        altani.adicionarMelhoria(Coluna.VERDE, m(TipoMelhoria.PUNHO, Coluna.VERDE));
        altani.adicionarTributos(TipoTributo.MOEDA, 1);
        jogo.ativarColuna(Coluna.VERDE);

        assertTrue(jogo.comprarMelhoria(0)); // fecha a 1ª linha
        assertTrue(jogo.isMelhoriaGratisPendente());
        assertFalse("Não passa a vez sem escolher a melhoria grátis", jogo.encerrarTurno());

        assertTrue(jogo.escolherMelhoriaGratis(4)); // a verde andou pro último espaço
        assertEquals("Na branca não importa a cor", Arrays.asList(gratis), altani.getMelhorias(Coluna.BRANCA));
        assertTrue(jogo.encerrarTurno());
    }

    @Test(timeout = 2000)
    public void testaSeKhanNaAreaDeMelhoriasRepoeEDaMoedaEEspadaDosEspacosVazios() {
        Jogo jogo = criarJogo(pilha(m(TipoMelhoria.PUNHO, Coluna.AMARELA), m(TipoMelhoria.PUNHO, Coluna.AMARELA),
                m(TipoMelhoria.PUNHO, Coluna.AMARELA), m(TipoMelhoria.PUNHO, Coluna.AMARELA),
                m(TipoMelhoria.PUNHO, Coluna.AMARELA), m(TipoMelhoria.YURT, Coluna.VERDE),
                m(TipoMelhoria.YURT, Coluna.VERDE)), Herdeiro.ALTANI, Herdeiro.OGEDEI);
        Jogador altani = jogo.getJogadorDaVez();
        altani.adicionarTributos(TipoTributo.MOEDA, 3);
        jogo.comprarMelhoria(0);
        jogo.comprarMelhoria(1); // sobraram 3, nos espaços 2, 3 e 4
        jogo.ativarColuna(Coluna.VERMELHA); // khan

        assertTrue(jogo.moverKhanParaAreaDeMelhorias());
        assertEquals("Espaço da moeda estava vazio", 1, altani.getQuantidadeTributo(TipoTributo.MOEDA));
        assertEquals("Espaço da espada estava ocupado", 0, altani.getQuantidadeTributo(TipoTributo.ESPADA));
        assertNotNull("Reposto com a pilha", jogo.getAreaDeMelhorias().espiar(0));
        assertNotNull(jogo.getAreaDeMelhorias().espiar(1));
        assertEquals(0, jogo.getAreaDeMelhorias().getTamanhoDaPilha());
    }

    @Test(timeout = 2000)
    public void testaSeJochiEscolheQualDasDuasMelhoriasIniciaisVaiParaABranca() {
        Melhoria primeira = m(TipoMelhoria.CAVALOS, Coluna.VERDE);
        Melhoria segunda = m(TipoMelhoria.ESPADA, Coluna.VERMELHA);
        List<Melhoria> melhorias = pilha(m(TipoMelhoria.PUNHO, Coluna.AMARELA), m(TipoMelhoria.PUNHO, Coluna.AMARELA),
                m(TipoMelhoria.PUNHO, Coluna.AMARELA), m(TipoMelhoria.PUNHO, Coluna.AMARELA),
                m(TipoMelhoria.PUNHO, Coluna.AMARELA), primeira, segunda);
        Jogo jogo = criarJogo(melhorias, Herdeiro.JOCHI, Herdeiro.OGEDEI);
        Jogador jochi = jogo.getJogadorDaVez();

        assertTrue("As 2 esperam a escolha", jochi.getMelhorias(Coluna.BRANCA).isEmpty());
        assertSame(primeira, jochi.getMelhoriaParaPosicionar());
        assertTrue("Pôs a 1ª numa colorida: a 2ª vai pra branca", jogo.posicionarMelhoria(Coluna.AMARELA));
        assertEquals(Arrays.asList(primeira), jochi.getMelhorias(Coluna.AMARELA));
        assertEquals(Arrays.asList(segunda), jochi.getMelhorias(Coluna.BRANCA));
        assertNull(jochi.getMelhoriaParaPosicionar());
    }

    @Test(timeout = 2000)
    public void testaSeJochiPodeMandarAPrimeiraMelhoriaInicialParaABranca() {
        Melhoria primeira = m(TipoMelhoria.CAVALOS, Coluna.VERDE);
        Melhoria segunda = m(TipoMelhoria.ESPADA, Coluna.VERMELHA);
        List<Melhoria> melhorias = pilha(m(TipoMelhoria.PUNHO, Coluna.AMARELA), m(TipoMelhoria.PUNHO, Coluna.AMARELA),
                m(TipoMelhoria.PUNHO, Coluna.AMARELA), m(TipoMelhoria.PUNHO, Coluna.AMARELA),
                m(TipoMelhoria.PUNHO, Coluna.AMARELA), primeira, segunda);
        Jogo jogo = criarJogo(melhorias, Herdeiro.JOCHI, Herdeiro.OGEDEI);
        Jogador jochi = jogo.getJogadorDaVez();

        assertTrue(jogo.posicionarMelhoria(Coluna.BRANCA));
        assertEquals(Arrays.asList(primeira), jochi.getMelhorias(Coluna.BRANCA));
        assertSame("Agora escolhe a coluna da 2ª", segunda, jochi.getMelhoriaParaPosicionar());
        assertFalse("A branca já foi usada", jogo.posicionarMelhoria(Coluna.BRANCA));
        assertTrue("Vermelha de fundo, mas o Jochi põe onde quiser", jogo.posicionarMelhoria(Coluna.AMARELA));
        assertEquals(Arrays.asList(segunda), jochi.getMelhorias(Coluna.AMARELA));
    }
}
