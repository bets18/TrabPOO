package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class CidadeTest {

    @Test(timeout = 2000)
    public void testaSeRevelarCidadeRetiraQuatroTesourosDaPilha() {
        Cidade cidade = new Cidade("Pequim", Regiao.CHINA);
        List<TipoTesouro> pilha = new ArrayList<>(Arrays.asList(
                TipoTesouro.PELE, TipoTesouro.FERRO, TipoTesouro.CARNE,
                TipoTesouro.GRAOS, TipoTesouro.LA, TipoTesouro.PELE));

        cidade.revelar(pilha);

        assertEquals("A cidade revelada deveria receber 4 tesouros.", 4, cidade.getQuantidadeTesouros());
        assertEquals("A pilha deveria ficar só com os 2 tesouros restantes.", 2, pilha.size());
        assertTrue("A cidade deveria poder ser atacada após ser revelada.", cidade.podeSerAtacada());
    }

    @Test(timeout = 2000)
    public void testaSeCidadeNaoReveladaNaoPodeSerAtacada() {
        Cidade cidade = new Cidade("Pequim", Regiao.CHINA);

        assertFalse("Uma cidade ainda na pilha não pode ser atacada.", cidade.podeSerAtacada());
    }
}
