package model;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * Testes do mapa real (MapaPadrao): paradas, rotas e cidades transcritas do tabuleiro.
 */

public class MapaPadraoTest {

    private static final String[] PREFIXOS = {"R", "P", "C"};

    @Test(timeout = 2000)
    public void testaSeMapaTemKarakorumE11ParadasPorRegiaoComDuasDuplas() {
        Tabuleiro t = MapaPadrao.criar();

        assertSame("Karakorum deveria ser a parada inicial", t.getKarakorum(), t.getLocal("Karakorum"));
        for (String prefixo : PREFIXOS) {
            for (int i = 1; i <= 9; i++) {
                Parada p = (Parada) t.getLocal(prefixo + i);
                assertEquals("Parada " + prefixo + i + " devia ser simples", TipoParada.SIMPLES, p.getTipo());
            }
            assertEquals(TipoParada.DUPLA, ((Parada) t.getLocal(prefixo + "D1")).getTipo());
            assertEquals(TipoParada.DUPLA, ((Parada) t.getLocal(prefixo + "D2")).getTipo());
        }
    }

    @Test(timeout = 2000)
    public void testaSeKarakorumSeLigaAsQuatroRotasDoCentro() {
        Tabuleiro t = MapaPadrao.criar();

        assertEquals("Karakorum devia ter 4 rotas", 4, t.getParadasVizinhas(t.getKarakorum()).size());
        assertTrue(t.existeRota(t.getKarakorum(), (Parada) t.getLocal("R8")));
        assertTrue(t.existeRota(t.getKarakorum(), (Parada) t.getLocal("RD2")));
        assertTrue(t.existeRota(t.getKarakorum(), (Parada) t.getLocal("P4")));
        assertTrue(t.existeRota(t.getKarakorum(), (Parada) t.getLocal("C4")));
    }

    @Test(timeout = 2000)
    public void testaSeTodaParadaTemPeloMenosDuasRotas() {
        Tabuleiro t = MapaPadrao.criar();

        for (String prefixo : PREFIXOS) {
            for (String sufixo : new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "D1", "D2"}) {
                Parada p = (Parada) t.getLocal(prefixo + sufixo);
                assertTrue("Parada " + p.getNome() + " isolada no mapa", t.getParadasVizinhas(p).size() >= 2);
            }
        }
    }

    @Test(timeout = 2000)
    public void testaSeCadaCidadeFicaNoCentroDeUmAnelDeTresParadas() {
        Tabuleiro t = MapaPadrao.criar();
        Cidade pequim = (Cidade) t.getLocal("Pequim");

        assertTrue(t.saoVizinhos((Parada) t.getLocal("C4"), pequim));
        assertTrue(t.saoVizinhos((Parada) t.getLocal("C5"), pequim));
        assertTrue(t.saoVizinhos((Parada) t.getLocal("C6"), pequim));
        assertFalse("C7 é de Cantão, não de Pequim", t.saoVizinhos((Parada) t.getLocal("C7"), pequim));
        assertEquals("O jogo tem 9 cidades", 9, t.getLocais(Cidade.class).size());
        assertTrue("Pequim tinha que estar na lista", t.getLocais(Cidade.class).contains(pequim));
    }

    @Test(timeout = 2000)
    public void testaSeMapaTem12ProvinciasDeTributoE4DoKhanComTresSetasCada() {
        Tabuleiro t = MapaPadrao.criar();
        int doKhan = 0;
        for (Provincia provincia : t.getLocais(Provincia.class)) {
            if (provincia.isDoKhan()) {
                doKhan++;
                assertNull("Província do Khan não dá tributo", provincia.getTipo());
                assertEquals("Cada Khan tem 3 setas", 3, t.getAfetadasPeloKhan(provincia).size());
            }
            assertFalse("Província " + provincia.getNome() + " sem paradas em volta",
                    t.getParadasAdjacentes(provincia).isEmpty());
        }
        assertEquals(4, doKhan);
        assertEquals(16, t.getLocais(Provincia.class).size());
        assertTrue("R1 fica na borda da província de espada de cima",
                t.saoVizinhos((Parada) t.getLocal("R1"), t.getLocal("Espada1")));
    }

    @Test(timeout = 2000)
    public void testaSeConselheirosFicamNasParadasDuplasEEmKarakorum() {
        Tabuleiro t = MapaPadrao.criar();

        assertEquals(7, t.getConselheiros().size());
        for (Conselheiro conselheiro : t.getConselheiros()) {
            TipoParada tipo = conselheiro.getParada().getTipo();
            assertTrue(tipo == TipoParada.DUPLA || tipo == TipoParada.KARAKORUM);
        }
        assertEquals("Karakorum dá 2 votos", 2, t.getConselheiro(t.getKarakorum()).getVotos());
        assertTrue("CD2 aceita 3 tesouros quaisquer", t.getConselheiro((Parada) t.getLocal("CD2")).aceitaQualquer());
    }
}
