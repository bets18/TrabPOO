package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class JogadorTest {

    @Test(timeout = 2000)
    public void testaSeJogadorIniciaComRecursosZerados() {
        Jogador jogador = new Jogador();
        
        assertEquals("O jogador deveria iniciar com 0 moedas.", 0, jogador.getQuantidadeTributo(TipoTributo.MOEDA));
        assertEquals("O jogador deveria iniciar com 0 espadas.", 0, jogador.getQuantidadeTributo(TipoTributo.ESPADA));
        assertEquals("O jogador deveria iniciar com 0 yurts.", 0, jogador.getQuantidadeTributo(TipoTributo.YURT));
    }

    @Test(timeout = 2000)
    public void testaSeJogadorAcumulaTributosCorretamente() {
        Jogador jogador = new Jogador();
        
        jogador.adicionarTributos(TipoTributo.ESPADA, 2);
        
        assertEquals("O jogador deveria ter 2 espadas acumuladas.", 2, jogador.getQuantidadeTributo(TipoTributo.ESPADA));
    }
}
