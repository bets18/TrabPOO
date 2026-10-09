package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProvinciaTest {

    @Test(timeout = 2000)
    public void testaSeAdicionarTributoFuncionaCorretamenteAteOTerceiroTributoNaProvincia() {
        Provincia provincia = new Provincia();
        
        provincia.adicionarTributo(TipoTributo.MOEDA);
        assertEquals("A lista deveria conter 1 tributo após a primeira inserção.", 1, provincia.tributos.size());
        
        provincia.adicionarTributo(TipoTributo.ESPADA);
        assertEquals("A lista deveria conter 2 tributos após a segunda inserção.", 2, provincia.tributos.size());
        
        provincia.adicionarTributo(TipoTributo.YURT);
        assertEquals("A lista deveria conter 3 tributos após a terceira inserção.", 3, provincia.tributos.size());
    }

    @Test(expected = IllegalStateException.class, timeout = 2000)
    public void testaSeExcecaoELancadaAoTentarAdicionarOQuartoTributoNaProvincia() {
        Provincia provincia = new Provincia();
        
        // Adicionando 3 tributos permitidos
        provincia.adicionarTributo(TipoTributo.MOEDA);
        provincia.adicionarTributo(TipoTributo.ESPADA);
        provincia.adicionarTributo(TipoTributo.YURT);
        
        // A tentativa de adicionar o 4º tributo deve lançar a IllegalStateException
        provincia.adicionarTributo(TipoTributo.MOEDA);
    }
}
