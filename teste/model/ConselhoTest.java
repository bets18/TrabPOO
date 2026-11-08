package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * Testes das entregas aos conselheiros, das peças de bônus, das cartas de conselheiro
 * aberto e das habilidades do Chagatai, do Ogedei e do Tolui (3ª iteração).
 */

public class ConselhoTest {

    private Tabuleiro t;

    @Before
    public void criarTabuleiro() {
        t = MapaPadrao.criar();
    }

    private Parada parada(String nome) {
        return (Parada) t.getLocal(nome);
    }

    private Conselheiro conselheiro(String parada) {
        return t.getConselheiro(parada(parada));
    }

    // bônus na ordem dos conselheiros: RD1, RD2, PD1, PD2, CD1, CD2, Karakorum
    private Jogo criarJogo(List<CartaAberta> abertas, List<TipoBonus> bonus, Herdeiro... herdeiros) {
        Jogo jogo = new Jogo(t, Arrays.asList(herdeiros));
        jogo.prepararConselho(abertas, new ArrayList<>(Arrays.asList(CartaSecreta.values())), bonus);
        // a carta secreta tem que ser escolhida antes da 1ª rodada
        for (int i = 0; i < herdeiros.length; i++) {
            Jogador jogador = jogo.getJogador(i);
            jogo.escolherCartaSecreta(jogador, jogador.getCartasOferecidas().get(0));
        }
        return jogo;
    }

    @Test(timeout = 2000)
    public void testaSeNinguemJogaAntesDeTodosEscolheremACartaSecreta() {
        Jogo jogo = new Jogo(t, Arrays.asList(Herdeiro.ALTANI, Herdeiro.OGEDEI));
        jogo.prepararConselho(new ArrayList<CartaAberta>(), new ArrayList<>(Arrays.asList(CartaSecreta.values())),
                bonusEmKarakorum(TipoBonus.VOTO));
        Jogador altani = jogo.getJogador(0);
        Jogador ogedei = jogo.getJogador(1);

        assertFalse("Ninguém escolheu ainda", jogo.ativarColuna(Coluna.BRANCA));
        assertTrue(jogo.escolherCartaSecreta(altani, altani.getCartasOferecidas().get(1)));
        assertFalse("O Ogedei ainda não escolheu", jogo.ativarColuna(Coluna.BRANCA));
        assertTrue(jogo.escolherCartaSecreta(ogedei, ogedei.getCartasOferecidas().get(0)));
        assertTrue(jogo.ativarColuna(Coluna.BRANCA));
        assertFalse("Depois da 1ª ativação não troca mais", jogo.escolherCartaSecreta(altani, altani.getCartasOferecidas().get(0)));
    }

    @Test(timeout = 2000)
    public void testaSeYurtInicialDoToluiEmKarakorumNaoContaNaRede() {
        Jogo jogo = criarJogo(new ArrayList<CartaAberta>(), bonusEmKarakorum(TipoBonus.VOTO), Herdeiro.TOLUI, Herdeiro.ALTANI);
        Jogador tolui = jogo.getJogadorDaVez();
        tolui.adicionarTributos(TipoTributo.YURT, 1);
        assertTrue(jogo.construirYurt(parada("C4")));

        assertEquals("Só o yurt de C4 conta, o de Karakorum não", 1, new Pontuacao(jogo, tolui).getMaiorRedeDeYurts());
    }

    private static List<TipoBonus> bonusEmKarakorum(TipoBonus bonus) {
        return Arrays.asList(TipoBonus.DUAS_ESPADAS, TipoBonus.DUAS_ESPADAS, TipoBonus.DUAS_ESPADAS,
                TipoBonus.DUAS_ESPADAS, TipoBonus.DUAS_ESPADAS, TipoBonus.DUAS_ESPADAS, bonus);
    }

    @Test(timeout = 2000)
    public void testaSeKarakorumDaDoisVotosEOBonusParaQuemEntregaPrimeiro() {
        Jogo jogo = criarJogo(new ArrayList<CartaAberta>(), bonusEmKarakorum(TipoBonus.DUAS_MOEDAS),
                Herdeiro.ALTANI, Herdeiro.OGEDEI);
        Jogador altani = jogo.getJogadorDaVez();
        altani.adicionarTesouro(TipoTesouro.FERRO);
        altani.adicionarTesouro(TipoTesouro.PELE);
        altani.adicionarTesouro(TipoTesouro.CARNE);
        Conselheiro karakorum = conselheiro("Karakorum");

        assertFalse("Pedido errado", jogo.entregarTesouros(karakorum, Arrays.asList(TipoTesouro.FERRO,
                TipoTesouro.FERRO, TipoTesouro.CARNE)));
        assertTrue("O peão começou o turno em Karakorum", jogo.entregarTesouros(karakorum, karakorum.getPedido()));

        assertEquals(2, altani.getVotos());
        assertEquals("A ficha neutra anda junto", 2, jogo.getVotosNoConselho());
        assertEquals(0, altani.getTotalDeTesouros());
        assertEquals(Arrays.asList(TipoBonus.DUAS_MOEDAS), altani.getBonusGuardados());
        assertNull("O bônus saiu do conselheiro", karakorum.getBonus());
    }

    @Test(timeout = 2000)
    public void testaSeEntregaExigeParadaNoConselheiroNoTurno() {
        Jogo jogo = criarJogo(new ArrayList<CartaAberta>(), bonusEmKarakorum(TipoBonus.VOTO), Herdeiro.ALTANI, Herdeiro.OGEDEI);
        Jogador altani = jogo.getJogadorDaVez();
        altani.adicionarTesouro(TipoTesouro.LA);
        altani.adicionarTesouro(TipoTesouro.LA);

        assertFalse("Não fez parada em RD1", jogo.entregarTesouros(conselheiro("RD1"), conselheiro("RD1").getPedido()));
        assertEquals(2, altani.getQuantidadeTesouro(TipoTesouro.LA));
    }

    @Test(timeout = 2000)
    public void testaSeConselheiroDeTresQuaisquerAceitaOsTesourosEscolhidos() {
        Jogo jogo = criarJogo(new ArrayList<CartaAberta>(), bonusEmKarakorum(TipoBonus.VOTO), Herdeiro.ALTANI, Herdeiro.OGEDEI);
        Jogador altani = jogo.getJogadorDaVez();
        altani.adicionarTesouro(TipoTesouro.LA);
        altani.adicionarTesouro(TipoTesouro.PELE);
        altani.adicionarTesouro(TipoTesouro.PELE);
        jogo.moverPeao(Arrays.asList(parada("C4"), parada("C5"), parada("CD2")), 3);

        assertTrue(jogo.selecionarConselheiro(conselheiro("CD2")));
        assertTrue(jogo.adicionarTesouroAEntrega(TipoTesouro.PELE));
        assertFalse("Não tem ferro", jogo.adicionarTesouroAEntrega(TipoTesouro.FERRO));
        assertTrue(jogo.adicionarTesouroAEntrega(TipoTesouro.LA));
        assertTrue(jogo.adicionarTesouroAEntrega(TipoTesouro.PELE));

        assertEquals("Entregou os 3: 1 voto + o bônus de 2 espadas de CD2", 1, altani.getVotos());
        assertEquals(0, altani.getTotalDeTesouros());
        assertNull("A seleção acaba com a entrega", jogo.getConselheiroSelecionado());
    }

    @Test(timeout = 2000)
    public void testaSeBonusDeVotoValeNaHora() {
        Jogo jogo = criarJogo(new ArrayList<CartaAberta>(), bonusEmKarakorum(TipoBonus.VOTO), Herdeiro.ALTANI, Herdeiro.OGEDEI);
        Jogador altani = jogo.getJogadorDaVez();
        altani.adicionarTesouro(TipoTesouro.FERRO);
        altani.adicionarTesouro(TipoTesouro.PELE);
        altani.adicionarTesouro(TipoTesouro.CARNE);

        jogo.entregarTesouros(conselheiro("Karakorum"), conselheiro("Karakorum").getPedido());

        assertEquals("2 da entrega + 1 do bônus", 3, altani.getVotos());
        assertEquals(1, altani.getBonusObtidos());
        assertTrue(altani.getBonusGuardados().isEmpty());
    }

    @Test(timeout = 2000)
    public void testaSeBonusDeTesourosDeixaEscolherUmDosTresDaPilha() {
        Jogo jogo = criarJogo(new ArrayList<CartaAberta>(), bonusEmKarakorum(TipoBonus.VOTO), Herdeiro.ALTANI, Herdeiro.OGEDEI);
        jogo.prepararCidades(new ArrayList<Cidade>(), Arrays.asList(TipoTesouro.LA, TipoTesouro.FERRO, TipoTesouro.PELE));
        Jogador altani = jogo.getJogadorDaVez();
        altani.guardarBonus(TipoBonus.TESOUROS);

        assertTrue(jogo.usarBonus(TipoBonus.TESOUROS));
        assertEquals(Arrays.asList(TipoTesouro.LA, TipoTesouro.FERRO, TipoTesouro.PELE), jogo.getTesourosParaEscolher());
        assertFalse("Carne não estava entre os 3", jogo.escolherTesouroDoBonus(TipoTesouro.CARNE));
        assertTrue(jogo.escolherTesouroDoBonus(TipoTesouro.FERRO));
        assertEquals(1, altani.getTotalDeTesouros());
        assertFalse("Bônus só se usa uma vez", jogo.usarBonus(TipoBonus.TESOUROS));
    }

    @Test(timeout = 2000)
    public void testaSeCartaAbertaDaUmVotoSoParaQuemCumprePrimeiro() {
        List<CartaAberta> abertas = Arrays.asList(CartaAberta.QUATRO_ESPADAS, CartaAberta.DEZ_YURTS,
                CartaAberta.DUAS_CIDADES, CartaAberta.TRES_BONUS);
        Jogo jogo = criarJogo(abertas, bonusEmKarakorum(TipoBonus.VOTO), Herdeiro.CHAGATAI, Herdeiro.OGEDEI);
        Jogador chagatai = jogo.getJogadorDaVez();
        chagatai.adicionarTributos(TipoTributo.ESPADA, 3);
        jogo.ativarColuna(Coluna.AMARELA); // Chagatai: 2 cavalos

        assertTrue("Habilidade do Chagatai: 1 cavalo vira 1 espada", jogo.trocarCavaloPorEspada());
        assertFalse("Só 1x por turno", jogo.trocarCavaloPorEspada());
        assertEquals(1, jogo.getMovimentosRestantes());
        assertEquals("Chegou a 4 espadas: ganhou a carta", 1, chagatai.getVotos());
        assertFalse(jogo.getCartasAbertas().contains(CartaAberta.QUATRO_ESPADAS));
    }

    @Test(timeout = 2000)
    public void testaSeOgedeiPagaUmaMoedaPorTesouroDeCidadeLongeMasNaoPeloUltimo() {
        Jogo jogo = criarJogo(new ArrayList<CartaAberta>(), bonusEmKarakorum(TipoBonus.VOTO), Herdeiro.OGEDEI, Herdeiro.ALTANI);
        Cidade cantao = (Cidade) t.getLocal("Cantao");
        jogo.prepararCidades(Arrays.asList(cantao), Arrays.asList(TipoTesouro.LA, TipoTesouro.PELE));
        Jogador ogedei = jogo.getJogadorDaVez();
        ogedei.adicionarTributos(TipoTributo.MOEDA, 1);

        assertTrue("Cantão fica longe, mas a moeda do Ogedei alcança", jogo.pegarTesouroComMoeda(cantao, TipoTesouro.LA));
        assertEquals(1, ogedei.getQuantidadeTesouro(TipoTesouro.LA));
        assertEquals("Tinha 2 moedas e pagou 1", 1, ogedei.getQuantidadeTributo(TipoTributo.MOEDA));
        jogo.ativarColuna(Coluna.BRANCA);
        jogo.moverKhanParaAreaDeMelhorias();
        jogo.encerrarTurno();
        jogo.ativarColuna(Coluna.VERDE); // Altani
        jogo.encerrarTurno();

        ogedei.adicionarTributos(TipoTributo.MOEDA, 1);
        assertFalse("A pele é o último tesouro de Cantão", jogo.pegarTesouroComMoeda(cantao, TipoTesouro.PELE));
    }

    @Test(timeout = 2000)
    public void testaSeToluiConstroiYurtNaParadaVizinhaSemPassarPorEla() {
        Jogo jogo = criarJogo(new ArrayList<CartaAberta>(), bonusEmKarakorum(TipoBonus.VOTO), Herdeiro.TOLUI, Herdeiro.ALTANI);
        Jogador tolui = jogo.getJogadorDaVez();
        tolui.adicionarTributos(TipoTributo.YURT, 2);

        assertTrue("C4 é vizinha de Karakorum", jogo.construirYurt(parada("C4")));
        assertFalse("P1 não é vizinha nem foi parada do turno", jogo.construirYurt(parada("P1")));
    }
}
