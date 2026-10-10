package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProvinciaTest {

    @Test(timeout = 2000)
    public void testaSeProvinciaNaoUltrapassaOLimiteDeTresTributos() {
        Provincia provincia = new Provincia("Estepe", Regiao.RUSSIA, TipoTributo.MOEDA, false);

        assertTrue("A 1ª peça deveria ser aceita.", provincia.adicionarTributo());
        assertTrue("A 2ª peça deveria ser aceita.", provincia.adicionarTributo());
        assertTrue("A 3ª peça deveria ser aceita.", provincia.adicionarTributo());
        assertFalse("A 4ª peça deveria ser recusada, pois o máximo é 3 por província.", provincia.adicionarTributo());

        assertEquals("A província deveria continuar com 3 peças.", 3, provincia.getQuantidade());
    }

    @Test(timeout = 2000)
    public void testaSeRemoverTributoDeProvinciaVaziaFalha() {
        Provincia provincia = new Provincia("Estepe", Regiao.RUSSIA, TipoTributo.MOEDA, false);

        assertFalse("Não deveria ser possível retirar tributo de uma província vazia.", provincia.removerTributo());
        assertEquals("A quantidade não pode ficar negativa.", 0, provincia.getQuantidade());
    }
}
