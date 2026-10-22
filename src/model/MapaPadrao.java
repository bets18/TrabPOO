package model;

import java.util.ArrayList;
import java.util.List;

// o mapa real do tabuleiro: 33 paradas (11 por região, 2 delas duplas) mais Karakorum,
// as rotas entre elas e as 9 cidades, cada uma no centro de um anel de 3 paradas.
// os nomes das paradas são o "contrato" com a View, que guarda onde cada uma fica na imagem.
// R = Rússia, P = Pérsia, C = China; RD/PD/CD = paradas duplas (de conselheiro).
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
        return t;
    }

    // as 9 cidades do jogo, na ordem em que foram cadastradas
    static List<Cidade> getCidades(Tabuleiro t) {
        List<Cidade> cidades = new ArrayList<>();
        String[] nomes = {"Kiev", "Moscou", "Sarai", "Samarcanda", "Bagda", "Cabul", "Kaesong", "Pequim", "Cantao"};
        for (String nome : nomes) {
            cidades.add((Cidade) t.getLocal(nome));
        }
        return cidades;
    }

    // 9 paradas simples (X1..X9) e 2 duplas (XD1, XD2) por região
    private static void adicionarParadas(Tabuleiro t, Regiao regiao, String prefixo) {
        for (int i = 1; i <= 9; i++) {
            t.adicionarLocal(new Parada(prefixo + i, regiao, TipoParada.SIMPLES));
        }
        t.adicionarLocal(new Parada(prefixo + "D1", regiao, TipoParada.DUPLA));
        t.adicionarLocal(new Parada(prefixo + "D2", regiao, TipoParada.DUPLA));
    }

    // recebe os nomes em pares: (a, b), (c, d), ...
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
}
