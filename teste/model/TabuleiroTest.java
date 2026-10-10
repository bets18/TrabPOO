package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class TabuleiroTest {

    @Test(timeout = 2000)
    public void testaSeExisteRotaRetornaTrueParaParadasPreviamenteConectadas() {
        Tabuleiro tabuleiro = new Tabuleiro();
        Parada origem = new Parada("A", Regiao.PERSIA, TipoParada.SIMPLES);
        Parada destino = new Parada("B", Regiao.PERSIA, TipoParada.SIMPLES);

        tabuleiro.adicionarRota(origem, destino);

        assertTrue("A rota deveria existir, pois as paradas foram conectadas.", tabuleiro.existeRota(origem, destino));
    }

    @Test(timeout = 2000)
    public void testaSeExisteRotaRetornaFalseParaParadasNaoConectadas() {
        Tabuleiro tabuleiro = new Tabuleiro();
        Parada origem = new Parada("A", Regiao.PERSIA, TipoParada.SIMPLES);
        Parada destino = new Parada("B", Regiao.PERSIA, TipoParada.SIMPLES);

        assertFalse("Não deveria haver rota, pois nenhuma conexão foi adicionada.", tabuleiro.existeRota(origem, destino));
    }

    @Test(timeout = 2000)
    public void testaSeAdicionarRotaCriaViaDeMaoDupla() {
        Tabuleiro tabuleiro = new Tabuleiro();
        Parada paradaA = new Parada("A", Regiao.PERSIA, TipoParada.SIMPLES);
        Parada paradaB = new Parada("B", Regiao.PERSIA, TipoParada.SIMPLES);

        tabuleiro.adicionarRota(paradaA, paradaB);

        assertTrue("A rota deveria ser de mão dupla, permitindo ir de B para A.", tabuleiro.existeRota(paradaB, paradaA));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 2000)
    public void testaSeTabuleiroRejeitaDoisLocaisComOMesmoNome() {
        Tabuleiro tabuleiro = new Tabuleiro();
        tabuleiro.adicionarLocal(new Cidade("Samarcanda", Regiao.PERSIA));

        // Os locais são identificados pelo nome na API, então nomes repetidos são proibidos
        tabuleiro.adicionarLocal(new Parada("Samarcanda", Regiao.PERSIA, TipoParada.SIMPLES));
    }
}
