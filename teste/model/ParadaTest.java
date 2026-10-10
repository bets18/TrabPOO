package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ParadaTest {

    @Test(timeout = 2000)
    public void testaSeParadaDuplaAceitaDoisPeoesMasNaoTres() {
        Parada parada = new Parada("Conselheiro", Regiao.CHINA, TipoParada.DUPLA);

        parada.adicionarPeao(new Jogador(Herdeiro.ALTANI));
        parada.adicionarPeao(new Jogador(Herdeiro.CHAGATAI));

        assertFalse("Uma parada dupla com 2 peões não deveria ter espaço para um 3º.", parada.temEspacoParaPeao());
    }

    @Test(timeout = 2000)
    public void testaSeKarakorumAceitaTodosOsPeoesMasNenhumYurt() {
        Parada karakorum = new Parada("Karakorum", null, TipoParada.KARAKORUM);
        for (Herdeiro herdeiro : Herdeiro.values()) {
            karakorum.adicionarPeao(new Jogador(herdeiro));
        }

        assertTrue("Karakorum deveria ter espaço mesmo com os 5 peões.", karakorum.temEspacoParaPeao());
        assertFalse("Karakorum não pode ter yurts.", karakorum.temEspacoParaYurt());
    }
}
