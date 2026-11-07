package model;

// cartas de conselheiro secreto: cada jogador fica com uma e no fim ela dá de 0 a 3 votos,
// 1 por meta atingida (as metas são os 3 números impressos na carta)
enum CartaSecreta {
    VIAJANTE(3, 6, 8) { // yurts conectados numa rede
        @Override
        int valor(Pontuacao p) {
            return p.getMaiorRedeDeYurts();
        }
    },
    SOLDADO(4, 7, 10) { // peças de melhoria
        @Override
        int valor(Pontuacao p) {
            return p.getTotalDeMelhorias();
        }
    },
    COLECIONADOR(4, 6, 7) { // melhorias com símbolos diferentes
        @Override
        int valor(Pontuacao p) {
            return p.getSimbolosDiferentes();
        }
    },
    SACERDOTE(1, 2, 4) { // pares de melhorias com o mesmo símbolo
        @Override
        int valor(Pontuacao p) {
            return p.getParesDeMelhorias();
        }
    },
    GOVERNADOR(1, 2, 3) { // regiões com 2+ yurts e 2+ melhorias na coluna dela
        @Override
        int valor(Pontuacao p) {
            return p.getRegioesComDoisYurtsEDuasMelhorias();
        }
    },
    BAJULADOR(2, 3, 4) { // províncias do Khan com 2+ yurts adjacentes
        @Override
        int valor(Pontuacao p) {
            return p.getProvinciasDoKhanComYurts(2);
        }
    },
    PREFEITO(2, 3, 4) { // cidades com 2+ yurts adjacentes
        @Override
        int valor(Pontuacao p) {
            return p.getCidadesComYurts(2);
        }
    },
    CIDADAO(3, 6, 8) { // cidades com 1+ yurt adjacente
        @Override
        int valor(Pontuacao p) {
            return p.getCidadesComYurts(1);
        }
    },
    CAVALEIRO(2, 4, 5) { // melhorias com cavalos ou punhos
        @Override
        int valor(Pontuacao p) {
            return p.getMelhoriasDeCavaloOuPunho();
        }
    },
    MENSAGEIRO(2, 4, 6) { // paradas de entrega (duplas) com 1+ yurt
        @Override
        int valor(Pontuacao p) {
            return p.getParadasDuplasComYurt();
        }
    };

    private final int[] metas;

    CartaSecreta(int... metas) {
        this.metas = metas;
    }

    abstract int valor(Pontuacao p);

    int votos(Pontuacao p) {
        int valor = valor(p);
        int votos = 0;
        for (int meta : metas) {
            if (valor >= meta) {
                votos++;
            }
        }
        return votos;
    }
}
