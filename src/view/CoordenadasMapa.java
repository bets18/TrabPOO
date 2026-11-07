package view;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// posição de cada coisa na imagem assets/tabuleiro.jpg (2905 x 2919). Isso é coisa da View,
// o Model só sabe os nomes. Parada dupla tem 2 círculos; província e cidade têm um centro
// (o ícone/desenho); conselheiro, espaço de melhoria e o Khan da área de melhorias são retângulos
final class CoordenadasMapa {
    static final int LARGURA_IMAGEM = 2905;
    static final int ALTURA_IMAGEM = 2919;
    static final String KARAKORUM = "Karakorum";

    private static final int RAIO_CLIQUE = 60;     // círculo de uma parada
    private static final int RAIO_PROVINCIA = 75;  // ícone de tributo ou cabeça do Khan
    private static final int RAIO_CIDADE = 150;

    // parte de baixo do escudo de Karakorum (em cima fica o pergaminho do conselheiro)
    private static final int[] KARAKORUM_RETANGULO = {1200, 1300, 1690, 1540};

    private static final Map<String, int[][]> PARADAS = new HashMap<>();
    private static final Map<String, int[]> PROVINCIAS = new LinkedHashMap<>();
    private static final Map<String, int[]> CIDADES = new LinkedHashMap<>();
    // {x1, y1, x2, y2} do pergaminho e do quadrado onde fica a peça de bônus
    private static final Map<String, int[]> CONSELHEIROS = new LinkedHashMap<>();
    private static final Map<String, int[]> BONUS = new HashMap<>();
    // os 5 espaços da área de melhorias, da esquerda pra direita
    private static final int[][] ESPACOS_DE_MELHORIA = {
        {190, 2739, 415, 2859}, {455, 2739, 680, 2859}, {715, 2739, 945, 2859},
        {980, 2739, 1210, 2859}, {1245, 2739, 1470, 2859}};
    // cabeça do Khan à esquerda da área de melhorias
    private static final int[] AREA_DO_KHAN = {40, 2730, 170, 2880};

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
        // em Karakorum os peões ficam lado a lado na parte de baixo do escudo
        PARADAS.put(KARAKORUM, new int[][] {{1445, 1450}});

        // ícones das províncias (mesmos nomes do MapaPadrao)
        PROVINCIAS.put("Yurt1", new int[] {630, 415});
        PROVINCIAS.put("Espada1", new int[] {860, 355});
        PROVINCIAS.put("Moeda1", new int[] {800, 940});
        PROVINCIAS.put("Khan1", new int[] {630, 580});
        PROVINCIAS.put("Yurt2", new int[] {1245, 920});
        PROVINCIAS.put("Espada2", new int[] {2165, 1040});
        PROVINCIAS.put("Moeda2", new int[] {1960, 750});
        PROVINCIAS.put("Khan2", new int[] {1745, 910});
        PROVINCIAS.put("Yurt3", new int[] {1520, 1625});
        PROVINCIAS.put("Espada3", new int[] {520, 1210});
        PROVINCIAS.put("Moeda3", new int[] {610, 1660});
        PROVINCIAS.put("Khan3", new int[] {810, 1720});
        PROVINCIAS.put("Yurt4", new int[] {2065, 1819});
        PROVINCIAS.put("Espada4", new int[] {1690, 1909});
        PROVINCIAS.put("Moeda4", new int[] {2395, 1559});
        PROVINCIAS.put("Khan4", new int[] {2135, 2059});

        CIDADES.put("Kiev", new int[] {290, 370});
        CIDADES.put("Moscou", new int[] {1090, 610});
        CIDADES.put("Sarai", new int[] {1730, 430});
        CIDADES.put("Samarcanda", new int[] {800, 1390});
        CIDADES.put("Bagda", new int[] {265, 1690});
        CIDADES.put("Cabul", new int[] {1270, 2030});
        CIDADES.put("Kaesong", new int[] {2550, 830});
        CIDADES.put("Pequim", new int[] {2095, 1420});
        CIDADES.put("Cantao", new int[] {2460, 2230});

        conselheiro("RD1", new int[] {1015, 25, 1490, 290}, new int[] {1280, 60, 1490, 270});
        conselheiro("RD2", new int[] {2045, 160, 2555, 440}, new int[] {2310, 180, 2515, 390});
        conselheiro("PD1", new int[] {105, 650, 385, 1150}, new int[] {130, 930, 335, 1135});
        conselheiro("PD2", new int[] {350, 1959, 860, 2249}, new int[] {615, 1985, 820, 2190});
        conselheiro("CD1", new int[] {2615, 1300, 2880, 1810}, new int[] {2630, 1584, 2875, 1804});
        // o pergaminho de CD2 encosta na parada; começa um pouco abaixo pra não roubar o clique dela
        conselheiro("CD2", new int[] {1335, 2285, 1840, 2514}, new int[] {1590, 2289, 1800, 2499});
        conselheiro(KARAKORUM, new int[] {1195, 1040, 1690, 1295}, new int[] {1370, 1320, 1520, 1440});
    }

    private CoordenadasMapa() {
    }

    private static void simples(String nome, int x, int y) {
        PARADAS.put(nome, new int[][] {{x, y}});
    }

    private static void dupla(String nome, int x1, int y1, int x2, int y2) {
        PARADAS.put(nome, new int[][] {{x1, y1}, {x2, y2}});
    }

    private static void conselheiro(String parada, int[] pergaminho, int[] bonus) {
        CONSELHEIROS.put(parada, pergaminho);
        BONUS.put(parada, bonus);
    }

    //  paradas

    static Iterable<String> getNomes() {
        return PARADAS.keySet();
    }

    // centro do(s) círculo(s) da parada, em coordenadas da imagem original
    static int[][] getPontos(String parada) {
        return PARADAS.get(parada);
    }

    // nome da parada que está no ponto (x, y) da imagem original, ou null
    static String paradaEm(double x, double y) {
        if (dentro(KARAKORUM_RETANGULO, x, y)) {
            return KARAKORUM;
        }
        for (Map.Entry<String, int[][]> entrada : PARADAS.entrySet()) {
            if (entrada.getKey().equals(KARAKORUM)) {
                continue;
            }
            for (int[] p : entrada.getValue()) {
                if (perto(p, x, y, RAIO_CLIQUE)) {
                    return entrada.getKey();
                }
            }
        }
        return null;
    }

    //  províncias e cidades

    static Iterable<String> getProvincias() {
        return PROVINCIAS.keySet();
    }

    static int[] getCentroDaProvincia(String provincia) {
        return PROVINCIAS.get(provincia);
    }

    static String provinciaEm(double x, double y) {
        return maisPerto(PROVINCIAS, x, y, RAIO_PROVINCIA);
    }

    static Iterable<String> getCidades() {
        return CIDADES.keySet();
    }

    static int[] getCentroDaCidade(String cidade) {
        return CIDADES.get(cidade);
    }

    static String cidadeEm(double x, double y) {
        return maisPerto(CIDADES, x, y, RAIO_CIDADE);
    }

    //  conselheiros, área de melhorias e Khan

    static Iterable<String> getConselheiros() {
        return CONSELHEIROS.keySet();
    }

    static int[] getPergaminho(String parada) {
        return CONSELHEIROS.get(parada);
    }

    static int[] getQuadradoDoBonus(String parada) {
        return BONUS.get(parada);
    }

    static String conselheiroEm(double x, double y) {
        for (Map.Entry<String, int[]> entrada : CONSELHEIROS.entrySet()) {
            if (dentro(entrada.getValue(), x, y)) {
                return entrada.getKey();
            }
        }
        return null;
    }

    static int[] getEspacoDeMelhoria(int espaco) {
        return ESPACOS_DE_MELHORIA[espaco];
    }

    // índice do espaço (0 a 4) ou -1
    static int espacoDeMelhoriaEm(double x, double y) {
        for (int i = 0; i < ESPACOS_DE_MELHORIA.length; i++) {
            if (dentro(ESPACOS_DE_MELHORIA[i], x, y)) {
                return i;
            }
        }
        return -1;
    }

    static int[] getAreaDoKhan() {
        return AREA_DO_KHAN;
    }

    static boolean naAreaDoKhan(double x, double y) {
        return dentro(AREA_DO_KHAN, x, y);
    }

    //  auxiliares

    private static boolean dentro(int[] r, double x, double y) {
        return x >= r[0] && x <= r[2] && y >= r[1] && y <= r[3];
    }

    private static boolean perto(int[] p, double x, double y, int raio) {
        double dx = x - p[0], dy = y - p[1];
        return dx * dx + dy * dy <= raio * raio;
    }

    private static String maisPerto(Map<String, int[]> pontos, double x, double y, int raio) {
        for (Map.Entry<String, int[]> entrada : pontos.entrySet()) {
            if (perto(entrada.getValue(), x, y, raio)) {
                return entrada.getKey();
            }
        }
        return null;
    }
}
