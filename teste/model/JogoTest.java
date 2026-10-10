package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * mapa reduzido usado nos testes:
 *
 *   karakorum  P1  P2  P3(dupla)  P4
 *                  |      |                    |
 *          provEspada  Samarcanda     provMoeda, Bagda
 *
 * provEspada e provKhan2 são províncias do Khan; as setas de provEspada apontam para
 * provMoeda e provYurt. Cidades na ordem: Samarcanda, Bagda, Cabul (reveladas) e Kiev (oculta).
 */
public class JogoTest {

    private Tabuleiro tabuleiro;
    private Parada karakorum, p1, p2, p3, p4;
    private Provincia provEspada, provMoeda, provYurt, provKhan2;
    private Cidade samarcanda, bagda, cabul, kiev;

    @Before
    public void montarTabuleiro() {
        tabuleiro = new Tabuleiro();
        karakorum = new Parada("Karakorum", null, TipoParada.KARAKORUM);
        p1 = new Parada("P1", Regiao.PERSIA, TipoParada.SIMPLES);
        p2 = new Parada("P2", Regiao.PERSIA, TipoParada.SIMPLES);
        p3 = new Parada("P3", Regiao.PERSIA, TipoParada.DUPLA);
        p4 = new Parada("P4", Regiao.PERSIA, TipoParada.SIMPLES);
        provEspada = new Provincia("ProvEspada", Regiao.PERSIA, TipoTributo.ESPADA, true);
        provMoeda = new Provincia("ProvMoeda", Regiao.PERSIA, TipoTributo.MOEDA, false);
        provYurt = new Provincia("ProvYurt", Regiao.PERSIA, TipoTributo.YURT, false);
        provKhan2 = new Provincia("ProvKhan2", Regiao.CHINA, TipoTributo.MOEDA, true);
        samarcanda = new Cidade("Samarcanda", Regiao.PERSIA);
        bagda = new Cidade("Bagda", Regiao.PERSIA);
        cabul = new Cidade("Cabul", Regiao.PERSIA);
        kiev = new Cidade("Kiev", Regiao.RUSSIA);

        for (Local local : Arrays.<Local>asList(karakorum, p1, p2, p3, p4, provEspada, provMoeda,
                provYurt, provKhan2, samarcanda, bagda, cabul, kiev)) {
            tabuleiro.adicionarLocal(local);
        }
        tabuleiro.adicionarRota(karakorum, p1);
        tabuleiro.adicionarRota(p1, p2);
        tabuleiro.adicionarRota(p2, p3);
        tabuleiro.adicionarRota(p3, p4);
        tabuleiro.adicionarVizinhanca(p1, provEspada);
        tabuleiro.adicionarVizinhanca(p2, samarcanda);
        tabuleiro.adicionarVizinhanca(p4, provMoeda);
        tabuleiro.adicionarVizinhanca(p4, bagda);
        tabuleiro.definirAfetadasPeloKhan(provEspada, provMoeda, provYurt);
    }

    private Jogo criarJogo(Herdeiro... herdeiros) {
        Jogo jogo = new Jogo(tabuleiro, Arrays.asList(herdeiros));
        // pilha "Embaralhada" em ordem fixa para o teste ser determinístico:
        // samarcanda recebe PELE, FERRO, CARNE, GRAOS
        List<TipoTesouro> pilha = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            pilha.addAll(Arrays.asList(TipoTesouro.values()));
        }
        jogo.prepararCidades(Arrays.asList(samarcanda, bagda, cabul, kiev), pilha);
        return jogo;
    }

    //  preparação 

    @Test(timeout = 2000)
    public void testaSePreparacaoDaUmaMoedaAosDoisPrimeirosEDuasAosDemais() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI, Herdeiro.JOCHI, Herdeiro.OGEDEI);

        assertEquals("Erro: 1º jogador tem que ter 1 moeda", 1, jogo.getJogador(0).getQuantidadeTributo(TipoTributo.MOEDA));
        assertEquals("Erro: 2º jogador tem que ter 1 moeda", 1, jogo.getJogador(1).getQuantidadeTributo(TipoTributo.MOEDA));
        assertEquals("Erro: 3º jogador tem que ter 2 moedas", 2, jogo.getJogador(2).getQuantidadeTributo(TipoTributo.MOEDA));
        assertEquals("Erro: 4º jogador tem que ter 2 moedas", 2, jogo.getJogador(3).getQuantidadeTributo(TipoTributo.MOEDA));
    }

    @Test(timeout = 2000)
    public void testaSePreparacaoPoePeoesEmKarakorum() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);
        assertSame("Peao devia iniciar em Karakorum", karakorum, jogo.getJogador(1).getPosicao());
    }

    @Test(timeout = 2000)
    public void testaSePreparacaoColocaUmTributoPorProvincia() {
        criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);
        assertEquals("Erro na qtde inicial de tributo", 1, provYurt.getQuantidade());
    }

    @Test(timeout = 2000)
    public void testaSePreparacaoRevelaAsTresPrimeirasCidadesDaPilha() {
        criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);
        assertTrue("Samarcanda devia estar revelada", samarcanda.isRevelada());
        assertFalse("Kiev devia continuar oculta", kiev.isRevelada());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 2000)
    public void testaSeJogoRejeitaPartidaComApenasUmJogador() {
        criarJogo(Herdeiro.ALTANI);
    }

    //  mover 

    @Test(timeout = 2000)
    public void testaSeMovimentoFalhaQuandoCaminhoExigeMaisMovimentosQueOsDisponiveis() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);

        boolean resultado = jogo.moverPeao(Arrays.asList(p1, p2, p3), 2);

        assertFalse("Move de 3 passos c/ 2 movs devia falhar", resultado);
        assertSame("Peao n devia sair do lugar", karakorum, jogo.getJogadorDaVez().getPosicao());
    }

    @Test(timeout = 2000)
    public void testaSeMovimentoFalhaEntreParadasSemRota() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);

        assertFalse("Não há rota direta entre Karakorum e P2.", jogo.moverPeao(Arrays.asList(p2), 5));
    }

    @Test(timeout = 2000)
    public void testaSePeaoPassaPorParadaOcupadaMasNaoTerminaNela() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);
        jogo.moverPeao(Arrays.asList(p1), 1); // aLTANI ocupa P1
        jogo.passarVez();

        assertFalse("Nao pode terminar em parada simples ocupada", jogo.moverPeao(Arrays.asList(p1), 1));
        assertTrue("Devia poder passar por parada ocupada", jogo.moverPeao(Arrays.asList(p1, p2), 2));
    }

    @Test(timeout = 2000)
    public void testaSeDoisPeoesPodemTerminarNaMesmaParadaDupla() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);
        jogo.moverPeao(Arrays.asList(p1, p2, p3), 3);
        jogo.passarVez();

        assertTrue("Parada dupla devia aceitar 2 peoes", jogo.moverPeao(Arrays.asList(p1, p2, p3), 3));
    }

    // prepara yurt do ALTANI em P1 p/ os testes
    private Jogo criarJogoComYurtDoAltaniEmP1() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);
        jogo.getJogadorDaVez().adicionarTributos(TipoTributo.YURT, 1);
        jogo.moverPeao(Arrays.asList(p1), 1);
        jogo.construirYurt(p1);
        jogo.moverPeao(Arrays.asList(karakorum), 1);
        jogo.passarVez();
        jogo.passarVez();
        return jogo;
    }

    @Test(timeout = 2000)
    public void testaSePularProprioYurtLevaAParadaSeguinteEmUmMovimento() {
        Jogo jogo = criarJogoComYurtDoAltaniEmP1();

        assertTrue("Pular proprio yurt custa 1 mov", jogo.moverPeao(Arrays.asList(p2), 1));
        assertFalse("P1 pulada n da tributo", jogo.pegarTributo(provEspada));
    }

    @Test(timeout = 2000)
    public void testaSePularYurtEOpcionalPermitindoPararNele() {
        Jogo jogo = criarJogoComYurtDoAltaniEmP1();

        assertTrue("Pode escolher parar msm tendo yurt", jogo.moverPeao(Arrays.asList(p1, p2), 2));
        assertTrue("Parou em P1, entao pega tributo", jogo.pegarTributo(provEspada));
    }

    @Test(timeout = 2000)
    public void testaSeNaoEPossivelPularYurtDeOutroJogador() {
        Jogo jogo = criarJogoComYurtDoAltaniEmP1();
        jogo.passarVez(); // vez do CHAGATAI, em Karakorum

        assertFalse("O yurt em P1 é do ALTANI, então CHAGATAI não pode pulá-lo.", jogo.moverPeao(Arrays.asList(p2), 1));
    }

    //  pegar tributo

    @Test(timeout = 2000)
    public void testaSeTributoSoPodeSerPegoDeProvinciaAdjacenteAParadaDoTurno() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);

        assertFalse("Karakorum não é adjacente a ProvEspada.", jogo.pegarTributo(provEspada));

        jogo.moverPeao(Arrays.asList(p1, p2), 2); // passa por P1, adjacente a ProvEspada

        assertTrue("P1 foi uma parada do turno, então ProvEspada deveria ceder o tributo.", jogo.pegarTributo(provEspada));
        assertEquals("O jogador deveria ter recebido 1 espada.", 1, jogo.getJogadorDaVez().getQuantidadeTributo(TipoTributo.ESPADA));
        assertFalse("ProvEspada ficou vazia e não pode ceder outro tributo.", jogo.pegarTributo(provEspada));
    }

    //  atacar cidades

    @Test(timeout = 2000)
    public void testaSeSegundoTesouroDaMesmaCidadeNoTurnoCustaDuasEspadas() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);
        Jogador altani = jogo.getJogadorDaVez();
        altani.adicionarTributos(TipoTributo.ESPADA, 3);
        jogo.moverPeao(Arrays.asList(p1, p2), 2);

        assertTrue("O 1º tesouro deveria custar 1 espada.", jogo.atacarCidade(samarcanda, TipoTesouro.PELE));
        assertEquals(2, altani.getQuantidadeTributo(TipoTributo.ESPADA));
        assertTrue("O 2º tesouro deveria custar 2 espadas.", jogo.atacarCidade(samarcanda, TipoTesouro.FERRO));
        assertEquals("As 3 espadas deveriam ter sido gastas.", 0, altani.getQuantidadeTributo(TipoTributo.ESPADA));
        assertEquals("O jogador deveria ter recebido a pele.", 1, altani.getQuantidadeTesouro(TipoTesouro.PELE));
    }

    @Test(timeout = 2000)
    public void testaSeAtaqueFalhaSemParadaAdjacenteACidade() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);
        jogo.getJogadorDaVez().adicionarTributos(TipoTributo.ESPADA, 5);
        jogo.moverPeao(Arrays.asList(p1), 1);

        assertFalse("P1 e Karakorum não são adjacentes a Samarcanda.", jogo.atacarCidade(samarcanda, TipoTesouro.PELE));
        assertEquals("Nenhuma espada deveria ter sido gasta.", 5, jogo.getJogadorDaVez().getQuantidadeTributo(TipoTributo.ESPADA));
    }

    @Test(timeout = 2000)
    public void testaSeTomarUltimoTesouroConquistaCidadeERevelaAProxima() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);
        Jogador altani = jogo.getJogadorDaVez();
        altani.adicionarTributos(TipoTributo.ESPADA, 7); // 1 + 2 + 2 + 2
        jogo.moverPeao(Arrays.asList(p1, p2), 2);

        jogo.atacarCidade(samarcanda, TipoTesouro.PELE);
        jogo.atacarCidade(samarcanda, TipoTesouro.FERRO);
        jogo.atacarCidade(samarcanda, TipoTesouro.CARNE);
        jogo.atacarCidade(samarcanda, TipoTesouro.GRAOS);

        assertSame("ALTANI deveria ter conquistado Samarcanda.", altani, samarcanda.getConquistador());
        assertEquals("Um yurt do ALTANI deveria ter ido para a cidade.", Jogador.YURTS_INICIAIS - 1, altani.getYurtsNoEstoque());
        assertTrue("Kiev, próxima da pilha, deveria ter sido revelada.", kiev.isRevelada());
        assertEquals("Kiev deveria ter recebido 4 tesouros.", 4, kiev.getQuantidadeTesouros());
    }

    //  khan

    @Test(timeout = 2000)
    public void testaSeKhanAdicionaTributoNaProvinciaENasDuasIndicadasRespeitandoOMaximo() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);
        provMoeda.adicionarTributo();
        provMoeda.adicionarTributo(); // provMoeda já está no máximo (3)

        assertTrue(jogo.moverKhan(provEspada));

        assertEquals("ProvEspada deveria passar de 1 para 2 tributos.", 2, provEspada.getQuantidade());
        assertEquals("ProvYurt deveria passar de 1 para 2 tributos.", 2, provYurt.getQuantidade());
        assertEquals("ProvMoeda já tinha 3 e deveria continuar com 3.", 3, provMoeda.getQuantidade());
    }

    @Test(timeout = 2000)
    public void testaSeKhanNaoPodePermanecerNaMesmaProvinciaNemIrParaProvinciaComum() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);
        jogo.moverKhan(provEspada);

        assertFalse("O Khan não pode ser mantido onde está.", jogo.moverKhan(provEspada));
        assertFalse("ProvYurt não tem o ícone do Khan.", jogo.moverKhan(provYurt));
        assertTrue("O Khan pode ir para outra província do Khan.", jogo.moverKhan(provKhan2));
    }

    //  yurts

    @Test(timeout = 2000)
    public void testaSeConstruirYurtExigePecaDeYurtEParadaFeitaNoTurno() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);
        Jogador altani = jogo.getJogadorDaVez();
        jogo.moverPeao(Arrays.asList(p1), 1);

        assertFalse("Sem peça de tributo de yurt não é possível construir.", jogo.construirYurt(p1));

        altani.adicionarTributos(TipoTributo.YURT, 1);
        assertFalse("P2 não foi uma parada deste turno.", jogo.construirYurt(p2));
        assertTrue(jogo.construirYurt(p1));
        assertTrue("P1 deveria ter um yurt do ALTANI.", p1.possuiYurtDe(altani));
        assertEquals("A peça de tributo de yurt deveria ter sido gasta.", 0, altani.getQuantidadeTributo(TipoTributo.YURT));
    }

    @Test(timeout = 2000)
    public void testaSeNaoEPossivelConstruirYurtNaParadaDePartidaDoTurno() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI);
        Jogador altani = jogo.getJogadorDaVez();
        jogo.moverPeao(Arrays.asList(p1), 1);
        jogo.passarVez();
        jogo.passarVez(); // aLTANI começa este turno em P1
        altani.adicionarTributos(TipoTributo.YURT, 1);

        assertFalse("Yurts só podem ser construídos em paradas feitas durante o movimento.", jogo.construirYurt(p1));
    }

    //  fim de jogo

    @Test(timeout = 2000)
    public void testaSeFimDeJogoEDisparadoAos14VotosComTresJogadores() {
        Jogo jogo = criarJogo(Herdeiro.ALTANI, Herdeiro.CHAGATAI, Herdeiro.OGEDEI);

        jogo.registrarVotos(jogo.getJogador(0), 8);
        jogo.registrarVotos(jogo.getJogador(1), 5);
        assertFalse("Com 13 votos no conselho o fim ainda não foi disparado.", jogo.fimDeJogoDisparado());

        jogo.registrarVotos(jogo.getJogador(2), 1);
        assertTrue("Com 3 jogadores o fim é disparado aos 14 votos.", jogo.fimDeJogoDisparado());
    }
}
