package view;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

// cor, cartela e ficha de cada herdeiro. O Model só passa o nome ("ALTANI"...)
// e aqui ele vira a chave dos HashMaps pra achar a imagem
final class EstiloJogador {
    private static final Map<String, Color> CORES = new HashMap<>();
    private static final Map<String, String> CARTELAS = new HashMap<>();
    private static final Map<String, String> FICHAS = new HashMap<>();

    static {
        registrar("ALTANI", new Color(46, 139, 70), "cartelas/cartela1.jpg", "cartoes/cartoes4/cartoes4-5.jpg");
        registrar("JOCHI", new Color(110, 110, 110), "cartelas/cartela2.jpg", "cartoes/cartoes4/cartoes4-3.jpg");
        registrar("CHAGATAI", new Color(200, 40, 40), "cartelas/cartela3.jpg", "cartoes/cartoes4/cartoes4-4.jpg");
        registrar("TOLUI", new Color(230, 180, 20), "cartelas/cartela4.jpg", "cartoes/cartoes4/cartoes4-2.jpg");
        registrar("OGEDEI", new Color(40, 80, 200), "cartelas/cartela5.jpg", "cartoes/cartoes4/cartoes4-1.jpg");
    }

    private EstiloJogador() {
    }

    private static void registrar(String herdeiro, Color cor, String cartela, String ficha) {
        CORES.put(herdeiro, cor);
        CARTELAS.put(herdeiro, cartela);
        FICHAS.put(herdeiro, ficha);
    }

    static Color getCor(String herdeiro) {
        return CORES.get(herdeiro);
    }

    static String getCartela(String herdeiro) {
        return CARTELAS.get(herdeiro);
    }

    static String getFicha(String herdeiro) {
        return FICHAS.get(herdeiro);
    }

    // nome para exibir: "ALTANI" -> "Altani"
    static String getNome(String herdeiro) {
        return herdeiro.charAt(0) + herdeiro.substring(1).toLowerCase();
    }
}
