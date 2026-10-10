package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class JogadorTest {

    @Test(timeout = 2000)
    public void testaSeJogadorAcumulaTributosCorretamente() {
        Jogador jogador = new Jogador(Herdeiro.ALTANI);

        jogador.adicionarTributos(TipoTributo.ESPADA, 2);
        jogador.adicionarTributos(TipoTributo.ESPADA, 1);

        assertEquals("O jogador deveria ter 3 espadas acumuladas.", 3, jogador.getQuantidadeTributo(TipoTributo.ESPADA));
        assertEquals("Adicionar espadas não deveria alterar as moedas.", 0, jogador.getQuantidadeTributo(TipoTributo.MOEDA));
    }

    @Test(timeout = 2000)
    public void testaSeGastarTributosSemSaldoSuficienteFalhaSemAlterarOSaldo() {
        Jogador jogador = new Jogador(Herdeiro.ALTANI);
        jogador.adicionarTributos(TipoTributo.ESPADA, 1);

        boolean resultado = jogador.gastarTributos(TipoTributo.ESPADA, 2);

        assertFalse("Não deveria ser possível gastar 2 espadas tendo apenas 1.", resultado);
        assertEquals("O saldo de espadas deveria continuar 1 após a tentativa que falhou.", 1, jogador.getQuantidadeTributo(TipoTributo.ESPADA));
    }

    @Test(timeout = 2000)
    public void testaSeJogadorNaoRetiraYurtQuandoEstoqueAcaba() {
        Jogador jogador = new Jogador(Herdeiro.ALTANI);
        for (int i = 0; i < Jogador.YURTS_INICIAIS; i++) {
            assertTrue("Deveria ser possível retirar o yurt número " + (i + 1) + ".", jogador.retirarYurtDoEstoque());
        }

        assertFalse("Após retirar os 12 yurts iniciais, o estoque deveria estar vazio.", jogador.retirarYurtDoEstoque());
    }
}
