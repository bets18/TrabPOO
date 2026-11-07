package model;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * Testes do fim da partida (3ª iteração): rodada extra depois do disparo, influência
 * nas regiões, cartas de conselheiro secreto e desempate.
 */

public class FimDeJogoTest {

    private Tabuleiro t;
    private Jogo jogo;
    private Jogador altani, ogedei;

    @Before
    public void criarJogo() {
        t = MapaPadrao.criar();
        jogo = new Jogo(t, Arrays.asList(Herdeiro.ALTANI, Herdeiro.OGEDEI));
        jogo.prepararConselho(new ArrayList<CartaAberta>(), Arrays.asList(CartaSecreta.MENSAGEIRO,
                CartaSecreta.SOLDADO, CartaSecreta.VIAJANTE, CartaSecreta.CIDADAO), new ArrayList<TipoBonus>());
        altani = jogo.getJogador(0);
        ogedei = jogo.getJogador(1);
    }

    private Parada parada(String nome) {
        return (Parada) t.getLocal(nome);
    }

    private void jogarTurnoVazio() {
        jogo.ativarColuna(Coluna.BRANCA);
        if (jogo.isKhanPendente()) {
            // alterna o khan entre a área de melhorias e uma província
            if (!jogo.moverKhanParaAreaDeMelhorias()) {
                jogo.moverKhan((Provincia) t.getLocal("Khan1"));
            }
        }
        assertTrue(jogo.encerrarTurno());
    }

    @Test(timeout = 2000)
    public void testaSeAPartidaTerminaDepoisDeUmaRodadaCompletaAposODisparo() {
        jogo.registrarVotos(altani, 10); // 2 jogadores: dispara com 10
        jogarTurnoVazio(); // Altani termina a rodada atual... vez do Ogedei
        assertFalse(jogo.isUltimaRodada());
        jogarTurnoVazio(); // fecha a rodada: começa a última
        assertTrue(jogo.isUltimaRodada());
        assertFalse(jogo.isTerminado());
        jogarTurnoVazio(); // Altani
        jogarTurnoVazio(); // Ogedei

        assertTrue(jogo.isTerminado());
        assertFalse("Acabou: nada mais vale", jogo.ativarColuna(Coluna.VERDE));
    }

    @Test(timeout = 2000)
    public void testaSeInfluenciaDaVotosPelasMelhoriasDaColunaDaRegiao() {
        // Rússia (coluna amarela): Altani 2 yurts, Ogedei 1
        parada("R1").adicionarYurt(altani);
        parada("R2").adicionarYurt(altani);
        parada("R3").adicionarYurt(ogedei);
        for (int i = 0; i < 3; i++) {
            altani.adicionarMelhoria(Coluna.AMARELA, new Melhoria(TipoMelhoria.PUNHO, Coluna.AMARELA));
        }
        ogedei.adicionarMelhoria(Coluna.AMARELA, new Melhoria(TipoMelhoria.PUNHO, Coluna.AMARELA));
        // China: os dois empatam com 1 yurt e ninguém fica em 2º
        parada("C1").adicionarYurt(altani);
        parada("C2").adicionarYurt(ogedei);

        jogo.registrarVotos(altani, 10);
        for (int i = 0; i < 4; i++) {
            jogarTurnoVazio();
        }

        assertEquals("Rússia: 1º com 3 melhorias = 2; China: 1º empatado com 0 = 1", 3, jogo.getVotosDeInfluencia(altani));
        assertEquals("Rússia: 2º com 1 melhoria = 1; China: 1º empatado com 0 = 1", 2, jogo.getVotosDeInfluencia(ogedei));
    }

    @Test(timeout = 2000)
    public void testaSeCartaSecretaDaUmVotoPorMetaAtingida() {
        assertTrue(altani.escolherCartaSecreta(CartaSecreta.MENSAGEIRO));
        assertFalse("Só pode escolher uma das oferecidas", ogedei.escolherCartaSecreta(CartaSecreta.MENSAGEIRO));
        assertTrue(ogedei.escolherCartaSecreta(CartaSecreta.CIDADAO));
        // Mensageiro: yurts em paradas duplas (metas 2, 4, 6)
        parada("RD1").adicionarYurt(altani);
        parada("RD2").adicionarYurt(altani);
        parada("PD1").adicionarYurt(altani);
        parada("PD2").adicionarYurt(altani);

        jogo.registrarVotos(ogedei, 10);
        for (int i = 0; i < 4; i++) {
            jogarTurnoVazio();
        }

        assertEquals("4 paradas duplas: 2 metas", 2, jogo.getVotosDaCartaSecreta(altani));
        assertEquals(0, jogo.getVotosDaCartaSecreta(ogedei));
    }

    @Test(timeout = 2000)
    public void testaSeEmpateEmVotosEDecididoPelosTesourosNaoEntregues() {
        jogo.registrarVotos(altani, 5);
        jogo.registrarVotos(ogedei, 5);
        ogedei.adicionarTesouro(TipoTesouro.LA);
        for (int i = 0; i < 4; i++) {
            jogarTurnoVazio();
        }

        assertEquals(Arrays.asList(ogedei), jogo.getVencedores());
    }
}
