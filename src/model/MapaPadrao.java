package model;

// monta o mapa do tabuleiro de verdade: 33 paradas + Karakorum, as rotas, as 9 cidades,
// as 16 províncias e os 7 conselheiros. Os nomes (R1, PD2, Moeda3, Khan1, ...) são os
// mesmos que a View usa pra achar cada coisa na imagem.
// R = Rússia, P = Pérsia, C = China e o D indica parada dupla
final class MapaPadrao {
    static final String KARAKORUM = "Karakorum";

    private MapaPadrao() {
    }

    static Tabuleiro criar() {
        Tabuleiro t = new Tabuleiro();
        t.adicionarLocal(new Parada(KARAKORUM, null, TipoParada.KARAKORUM));

        adicionarParadas(t, Regiao.RUSSIA, "R");
        adicionarParadas(t, Regiao.PERSIA, "P");
        adicionarParadas(t, Regiao.CHINA, "C");

        // Rússia
        rotas(t, "R1", "R2", "R2", "R3", "R3", "R1",           // anel de Kiev
                 "R4", "R5", "R5", "R6", "R6", "R4",           // anel de Moscou
                 "R7", "R9", "R9", "R8", "R8", "R7",           // anel de Sarai
                 "R1", "RD1", "R1", "R4", "R3", "PD1", "R4", "PD1", "R6", "P1",
                 "RD1", "R5", "R5", "R8", "R9", "RD2", "RD2", "C1",
                 "RD2", KARAKORUM, "R8", KARAKORUM);
        // Pérsia
        rotas(t, "P1", "P2", "P2", "P4", "P4", "P1",           // anel de Samarcanda
                 "P3", "P5", "P5", "P6", "P6", "P3",           // anel de Bagdá
                 "P7", "P8", "P8", "P9", "P9", "P7",           // anel de Cabul
                 "PD1", "P1", "PD1", "P3", "P2", "P3", "P5", "PD2", "PD2", "P7",
                 "P4", "P7", "P4", KARAKORUM, "P7", "C5", "P8", "CD2");
        // China
        rotas(t, "C1", "C2", "C2", "C3", "C3", "C1",           // anel de Kaesong
                 "C4", "C5", "C5", "C6", "C6", "C4",           // anel de Pequim
                 "C7", "C8", "C8", "C9", "C9", "C7",           // anel de Cantão
                 "C3", "C4", "C3", "CD1", "C4", KARAKORUM, "C6", "C7", "CD1", "C7",
                 "CD2", "C8", "CD2", "C5");

        cidade(t, "Kiev", Regiao.RUSSIA, "R1", "R2", "R3");
        cidade(t, "Moscou", Regiao.RUSSIA, "R4", "R5", "R6");
        cidade(t, "Sarai", Regiao.RUSSIA, "R7", "R8", "R9");
        cidade(t, "Samarcanda", Regiao.PERSIA, "P1", "P2", "P4");
        cidade(t, "Bagda", Regiao.PERSIA, "P3", "P5", "P6");
        cidade(t, "Cabul", Regiao.PERSIA, "P7", "P8", "P9");
        cidade(t, "Kaesong", Regiao.CHINA, "C1", "C2", "C3");
        cidade(t, "Pequim", Regiao.CHINA, "C4", "C5", "C6");
        cidade(t, "Cantao", Regiao.CHINA, "C7", "C8", "C9");

        adicionarProvincias(t);
        adicionarConselheiros(t);
        return t;
    }

    // 12 províncias com tributo (4 de cada tipo) e 4 do Khan. Cada uma é a área entre as
    // rotas onde o ícone está desenhado, e as paradas listadas são as da borda dessa área.
    // As do Khan dividem a área com uma província comum
    private static void adicionarProvincias(Tabuleiro t) {
        Provincia yurt1 = provincia(t, "Yurt1", Regiao.RUSSIA, TipoTributo.YURT, "R1", "R3", "PD1", "R4");
        Provincia espada1 = provincia(t, "Espada1", Regiao.RUSSIA, TipoTributo.ESPADA, "R1", "RD1", "R5", "R4");
        Provincia moeda1 = provincia(t, "Moeda1", Regiao.RUSSIA, TipoTributo.MOEDA, "R4", "R6", "P1", "PD1");
        Provincia khan1 = provincia(t, "Khan1", Regiao.RUSSIA, null, "R1", "R3", "PD1", "R4");

        Provincia yurt2 = provincia(t, "Yurt2", Regiao.RUSSIA, TipoTributo.YURT, "R5", "R6", "R8", KARAKORUM, "P4", "P1");
        Provincia espada2 = provincia(t, "Espada2", Regiao.RUSSIA, TipoTributo.ESPADA, "RD2", "C1", "C3", "C4", KARAKORUM);
        Provincia moeda2 = provincia(t, "Moeda2", Regiao.RUSSIA, TipoTributo.MOEDA, "R8", "R9", "RD2", KARAKORUM);
        Provincia khan2 = provincia(t, "Khan2", Regiao.RUSSIA, null, "R8", "R9", "RD2", KARAKORUM);

        Provincia yurt3 = provincia(t, "Yurt3", Regiao.PERSIA, TipoTributo.YURT, KARAKORUM, "P4", "P7", "C5", "C4");
        Provincia espada3 = provincia(t, "Espada3", Regiao.PERSIA, TipoTributo.ESPADA, "PD1", "P3", "P2", "P1");
        Provincia moeda3 = provincia(t, "Moeda3", Regiao.PERSIA, TipoTributo.MOEDA, "P2", "P3", "P5", "PD2", "P7", "P4");
        Provincia khan3 = provincia(t, "Khan3", Regiao.PERSIA, null, "P2", "P3", "P5", "PD2", "P7", "P4");

        Provincia yurt4 = provincia(t, "Yurt4", Regiao.CHINA, TipoTributo.YURT, "C5", "C6", "C7", "C8", "CD2");
        Provincia espada4 = provincia(t, "Espada4", Regiao.CHINA, TipoTributo.ESPADA, "P7", "C5", "CD2", "P8");
        Provincia moeda4 = provincia(t, "Moeda4", Regiao.CHINA, TipoTributo.MOEDA, "C4", "C6", "C7", "CD1", "C3");
        Provincia khan4 = provincia(t, "Khan4", Regiao.CHINA, null, "C5", "C6", "C7", "C8", "CD2");

        // as 3 setas que saem de cada cabeça do Khan
        t.definirAfetadasPeloKhan(khan1, yurt1, espada1, moeda1);
        t.definirAfetadasPeloKhan(khan2, moeda2, espada2, yurt2);
        t.definirAfetadasPeloKhan(khan3, moeda3, espada3, yurt3);
        t.definirAfetadasPeloKhan(khan4, espada4, yurt4, moeda4);
    }

    // os pergaminhos do tabuleiro: 5 pedem 2 tesouros iguais, 1 pede 3 quaisquer
    // e Karakorum pede ferro, pele e carne e dá 2 votos
    private static void adicionarConselheiros(Tabuleiro t) {
        conselheiro(t, "RD1", 1, TipoTesouro.LA, TipoTesouro.LA);
        conselheiro(t, "RD2", 1, TipoTesouro.FERRO, TipoTesouro.FERRO);
        conselheiro(t, "PD1", 1, TipoTesouro.PELE, TipoTesouro.PELE);
        conselheiro(t, "PD2", 1, TipoTesouro.GRAOS, TipoTesouro.GRAOS);
        conselheiro(t, "CD1", 1, TipoTesouro.CARNE, TipoTesouro.CARNE);
        conselheiro(t, "CD2", 1, null, null, null);
        conselheiro(t, KARAKORUM, 2, TipoTesouro.FERRO, TipoTesouro.PELE, TipoTesouro.CARNE);
    }

    // por região: 9 simples (X1 a X9) e 2 duplas (XD1 e XD2)
    private static void adicionarParadas(Tabuleiro t, Regiao regiao, String prefixo) {
        for (int i = 1; i <= 9; i++) {
            t.adicionarLocal(new Parada(prefixo + i, regiao, TipoParada.SIMPLES));
        }
        t.adicionarLocal(new Parada(prefixo + "D1", regiao, TipoParada.DUPLA));
        t.adicionarLocal(new Parada(prefixo + "D2", regiao, TipoParada.DUPLA));
    }

    // os nomes vêm de 2 em 2: liga o 1º com o 2º, o 3º com o 4º...
    private static void rotas(Tabuleiro t, String... pares) {
        for (int i = 0; i < pares.length; i += 2) {
            t.adicionarRota((Parada) t.getLocal(pares[i]), (Parada) t.getLocal(pares[i + 1]));
        }
    }

    private static void cidade(Tabuleiro t, String nome, Regiao regiao, String... paradas) {
        Cidade cidade = new Cidade(nome, regiao);
        t.adicionarLocal(cidade);
        for (String parada : paradas) {
            t.adicionarVizinhanca((Parada) t.getLocal(parada), cidade);
        }
    }

    // tipo null = província do Khan
    private static Provincia provincia(Tabuleiro t, String nome, Regiao regiao, TipoTributo tipo, String... paradas) {
        Provincia provincia = new Provincia(nome, regiao, tipo, tipo == null);
        t.adicionarLocal(provincia);
        for (String parada : paradas) {
            t.adicionarVizinhanca((Parada) t.getLocal(parada), provincia);
        }
        return provincia;
    }

    private static void conselheiro(Tabuleiro t, String parada, int votos, TipoTesouro... pedido) {
        t.adicionarConselheiro(new Conselheiro((Parada) t.getLocal(parada), votos, pedido));
    }
}
