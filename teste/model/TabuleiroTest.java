package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class TabuleiroTest {

    @Test(timeout = 2000)
    public void testaSeMovimentoValidoRetornaTrueParaLocaisPreviamenteConectados() {
        Tabuleiro tabuleiro = new Tabuleiro();
        Local origem = new Parada();
        Local destino = new Parada();
        
        tabuleiro.adicionarConexao(origem, destino);
        
        boolean resultado = tabuleiro.validarMovimento(origem, destino);
        
        assertTrue("O movimento deveria ser considerado válido pois os locais foram previamente conectados no tabuleiro.", resultado);
    }

    @Test(timeout = 2000)
    public void testaSeMovimentoInvalidoRetornaFalseParaLocaisNaoConectados() {
        Tabuleiro tabuleiro = new Tabuleiro();
        Local origem = new Parada();
        Local destino = new Parada();
        
        boolean resultado = tabuleiro.validarMovimento(origem, destino);
        
        assertFalse("O movimento deveria ser inválido pois não foi adicionada nenhuma conexão.", resultado);
    }

    @Test(timeout = 2000)
    public void testaSeAdicionarConexaoCriaViaDeMaoDuplaNoGrafo() {
        Tabuleiro tabuleiro = new Tabuleiro();
        Local paradaA = new Parada();
        Local paradaB = new Parada();
        
        tabuleiro.adicionarConexao(paradaA, paradaB);
        
        boolean resultado = tabuleiro.validarMovimento(paradaB, paradaA);
        assertTrue("A conexão deveria ser de mão dupla, permitindo que B acesse A.", resultado);
    }
}
