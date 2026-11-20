package model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * Testes de salvar e carregar a partida, pela HerdeirosDeKhanAPI e no mapa de verdade
 */

public class SalvamentoTest {

    private HerdeirosDeKhanAPI api;
    private File arquivo;
    private File copia;

    @Before
    public void criarPartida() throws IOException {
        api = HerdeirosDeKhanAPI.getInstancia();
        api.novaPartida(Arrays.asList("TOLUI", "ALTANI", "JOCHI", "OGEDEI"), Arrays.asList("Lis", "Rafaela", "Davi", "Ivan"));
        for (int i = 0; i < api.getNumeroDeJogadores(); i++) {
            api.escolherCartaSecreta(i, api.getCartasSecretasOferecidas(i).get(0));
        }
        arquivo = File.createTempFile("partida", ".txt");
        copia = File.createTempFile("partida", ".txt");
    }

    @After
    public void apagarArquivos() {
        arquivo.delete();
        copia.delete();
    }

    // turno simples: ativa a 1ª coluna livre, resolve o que trava o turno e passa a vez
    private void jogarTurno() {
        for (String coluna : Arrays.asList("VERMELHA", "VERDE", "AMARELA", "BRANCA")) {
            if (api.getMelhoriaParaPosicionar() != null) {
                api.posicionarMelhoria(coluna);
            }
        }
        for (String coluna : Arrays.asList("VERMELHA", "VERDE", "AMARELA", "BRANCA")) {
            if (api.ativarColuna(coluna)) {
                break;
            }
        }
        if (api.isKhanPendente()) {
            assertTrue(api.moverKhanParaAreaDeMelhorias() || api.moverKhan("Khan1") || api.moverKhan("Khan2"));
        }
        assertTrue(api.passarVez());
    }

    private List<String> linhas(File f) throws IOException {
        return Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
    }

    @Test(timeout = 5000)
    public void testaSePartidaCarregadaFicaIgualAQueFoiSalva() throws IOException {
        for (int i = 0; i < 6; i++) {
            jogarTurno();
        }
        assertTrue(api.salvarPartida(arquivo.getPath()));

        // estraga a partida atual pra ter certeza de que o estado vem do arquivo
        jogarTurno();
        jogarTurno();
        assertTrue(api.carregarPartida(arquivo.getPath()));
        assertTrue(api.salvarPartida(copia.getPath()));

        assertEquals("Salvar de novo depois de carregar dá o mesmo arquivo", linhas(arquivo), linhas(copia));
    }

    @Test(timeout = 5000)
    public void testaSeCarregarDevolveJogadoresPosicoesEVez() {
        assertTrue(api.ativarColuna("AMARELA")); // Tolui: 1 cavalo e o khan
        assertTrue(api.moverPara("C4"));
        assertTrue(api.moverKhanParaAreaDeMelhorias());
        assertTrue(api.passarVez());
        int votosNoConselho = api.getVotosNoConselho();
        assertTrue(api.salvarPartida(arquivo.getPath()));

        api.novaPartida(Arrays.asList("CHAGATAI", "JOCHI"));
        assertTrue(api.carregarPartida(arquivo.getPath()));

        assertEquals(4, api.getNumeroDeJogadores());
        assertEquals("Lis", api.getNomeDoJogador(0));
        assertEquals("TOLUI", api.getHerdeiro(0));
        assertEquals("C4", api.getPosicao(0));
        assertEquals("É a vez do 2º jogador", 1, api.getIndiceJogadorDaVez());
        assertEquals("Rafaela", api.getNomeDoJogadorDaVez());
        assertEquals(HerdeirosDeKhanAPI.AREA_DE_MELHORIAS, api.getPosicaoDoKhan());
        assertEquals(votosNoConselho, api.getVotosNoConselho());
        assertNotNull("A carta secreta já escolhida volta com a partida", api.getCartaSecreta(0));
        assertTrue("A partida continua: o 2º jogador ativa uma coluna", api.ativarColuna("VERDE"));
        assertTrue(api.passarVez());
        jogarTurno();
        jogarTurno();
        assertFalse("A ficha do Tolui continua na coluna amarela", api.colunaLivre("AMARELA"));
    }

    @Test(timeout = 5000)
    public void testaSeSoSalvaNoComecoDaVez() {
        assertTrue(api.podeSalvar());
        assertTrue(api.ativarColuna("VERMELHA"));

        assertFalse("Depois de ativar a coluna não salva mais", api.podeSalvar());
        assertFalse(api.salvarPartida(arquivo.getPath()));
        assertEquals("Nada foi gravado", 0, arquivo.length());
    }

    @Test(timeout = 5000)
    public void testaSeArquivoInvalidoNaoTrocaAPartida() throws IOException {
        try (PrintWriter saida = new PrintWriter(arquivo, "UTF-8")) {
            saida.println("formato=HerdeirosDeKhan-1");
            saida.println("jogadores=2");
            saida.println("isso não é uma partida");
        }

        assertFalse(api.carregarPartida(arquivo.getPath()));
        assertFalse("Arquivo que não existe", api.carregarPartida(arquivo.getPath() + ".nao.existe"));
        assertEquals("A partida atual continua valendo", 4, api.getNumeroDeJogadores());
        assertEquals("Lis", api.getNomeDoJogadorDaVez());
    }

    @Test(timeout = 5000)
    public void testaSeJogadorSemNomeFicaComONomeDoHerdeiro() {
        api.novaPartida(Arrays.asList("ALTANI", "OGEDEI"));

        assertEquals("Altani", api.getNomeDoJogador(0));
        assertEquals("Ogedei", api.getNomeDoJogador(1));
    }
}
