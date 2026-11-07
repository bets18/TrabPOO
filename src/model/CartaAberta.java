package model;

// cartas de conselheiro aberto: 4 ficam na mesa e a 1ª pessoa que cumprir a
// condição ganha 1 voto e tira a carta do jogo
enum CartaAberta {
    QUATRO_ESPADAS {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getEspadas() >= 4;
        }
    },
    SEIS_YURTS {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getYurtsNoMapa() >= 6;
        }
    },
    DEZ_YURTS {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getYurtsNoMapa() >= 10;
        }
    },
    QUATRO_TESOUROS {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getTesouros() >= 4;
        }
    },
    CINCO_TESOUROS_DIFERENTES {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getTiposDeTesouro() >= 5;
        }
    },
    UM_YURT_POR_REGIAO {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getMenorQuantidadeDeYurtsPorRegiao() >= 1;
        }
    },
    DOIS_YURTS_POR_REGIAO {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getMenorQuantidadeDeYurtsPorRegiao() >= 2;
        }
    },
    DOIS_BONUS {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getBonusObtidos() >= 2;
        }
    },
    TRES_BONUS {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getBonusObtidos() >= 3;
        }
    },
    DUAS_CIDADES {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getCidadesConquistadas() >= 2;
        }
    },
    TRES_CIDADES {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getCidadesConquistadas() >= 3;
        }
    },
    LINHA_DE_MELHORIAS {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getLinhasCompletas() >= 1;
        }
    },
    DUAS_COLUNAS_COM_DUAS {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getColunasComPeloMenos(2) >= 2;
        }
    },
    QUATRO_NUMA_COLUNA {
        @Override
        boolean cumprida(Pontuacao p) {
            return p.getMaiorColuna() >= 4;
        }
    };

    abstract boolean cumprida(Pontuacao p);
}
