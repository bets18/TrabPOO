package controller;

import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

// ponto de entrada. As janelas iniciais (escolha de herdeiros) não são exigidas na 2ª iteração;
// por isso a partida começa com os herdeiros passados como argumento ou, sem argumentos,
// com 4 jogadores. Ex.: java controller.Principal ALTANI OGEDEI TOLUI
public class Principal {
    private static final List<String> HERDEIROS_PADRAO = Arrays.asList("ALTANI", "CHAGATAI", "JOCHI", "TOLUI");

    public static void main(String[] args) {
        final List<String> herdeiros = args.length > 0 ? Arrays.asList(args) : HERDEIROS_PADRAO;
        // a interface Swing tem de ser criada na thread de eventos
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ControladorJogo(herdeiros).iniciar();
            }
        });
    }
}
