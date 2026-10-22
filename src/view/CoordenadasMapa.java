package view;

import java.util.HashMap;
import java.util.Map;

// onde cada parada fica na imagem assets/tabuleiro.jpg (2905 x 2919 px). A geometria é da View;
// o Model só conhece os nomes e as rotas. Paradas duplas têm dois círculos.
final class CoordenadasMapa {
    static final int LARGURA_IMAGEM = 2905;
    static final int ALTURA_IMAGEM = 2919;
    static final String KARAKORUM = "Karakorum";

    private static final int RAIO_CLIQUE = 60; // raio de um círculo de parada na imagem original

    // escudo de Karakorum na imagem
    private static final int KARAKORUM_X1 = 1200, KARAKORUM_Y1 = 1040, KARAKORUM_X2 = 1690, KARAKORUM_Y2 = 1540;

    private static final Map<String, int[][]> PARADAS = new HashMap<>();
    static {
        // Rússia
        simples("R1", 490, 250);
        simples("R2", 130, 540);
        simples("R3", 470, 540);
        simples("R4", 855, 555);
        simples("R5", 1270, 495);
        simples("R6", 1085, 840);
        simples("R7", 1755, 230);
        simples("R8", 1555, 670);
        simples("R9", 1890, 590);
        dupla("RD1", 855, 170, 930, 250);
        dupla("RD2", 2180, 495, 2290, 495);
        // Pérsia
        simples("P1", 842, 1200);
        simples("P2", 570, 1410);
        simples("P3", 215, 1492);
        simples("P4", 956, 1552);
        simples("P5", 452, 1640);
        simples("P6", 180, 1915);
        simples("P7", 1270, 1820);
        simples("P8", 1468, 2050);
        simples("P9", 1215, 2230);
        dupla("PD1", 470, 845, 455, 950);
        dupla("PD2", 668, 1880, 785, 1880);
        // China
        simples("C1", 2495, 655);
        simples("C2", 2770, 875);
        simples("C3", 2525, 1070);
        simples("C4", 2140, 1255);
        simples("C5", 1890, 1600);
        simples("C6", 2170, 1680);
        simples("C7", 2525, 2030);
        simples("C8", 2235, 2230);
        simples("C9", 2585, 2410);
        dupla("CD1", 2555, 1530, 2555, 1615);
        dupla("CD2", 1765, 2215, 1880, 2240);
        // Karakorum: os peões ficam lado a lado na parte de baixo do escudo
        PARADAS.put(KARAKORUM, new int[][] {{1445, 1450}});
    }

    private CoordenadasMapa() {
    }

    private static void simples(String nome, int x, int y) {
        PARADAS.put(nome, new int[][] {{x, y}});
    }

    private static void dupla(String nome, int x1, int y1, int x2, int y2) {
        PARADAS.put(nome, new int[][] {{x1, y1}, {x2, y2}});
    }

    static Iterable<String> getNomes() {
        return PARADAS.keySet();
    }

    // círculo(s) da parada, em coordenadas da imagem original
    static int[][] getPontos(String parada) {
        return PARADAS.get(parada);
    }

    // nome da parada sob o ponto (x, y) da imagem original, ou null se não houver
    static String paradaEm(double x, double y) {
        if (x >= KARAKORUM_X1 && x <= KARAKORUM_X2 && y >= KARAKORUM_Y1 && y <= KARAKORUM_Y2) {
            return KARAKORUM;
        }
        for (Map.Entry<String, int[][]> entrada : PARADAS.entrySet()) {
            if (entrada.getKey().equals(KARAKORUM)) {
                continue;
            }
            for (int[] p : entrada.getValue()) {
                double dx = x - p[0], dy = y - p[1];
                if (dx * dx + dy * dy <= RAIO_CLIQUE * RAIO_CLIQUE) {
                    return entrada.getKey();
                }
            }
        }
        return null;
    }
}
