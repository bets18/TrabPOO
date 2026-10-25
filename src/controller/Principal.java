package controller;

import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

// começa o jogo. Na 2ª iteração não precisa das janelas iniciais, então os herdeiros vêm
// pelos argumentos (ex: java controller.Principal ALTANI OGEDEI TOLUI) ou, sem argumentos,
// a partida começa com 4 jogadores
public class Principal {
    private static final List<String> HERDEIROS_PADRAO = Arrays.asList("ALTANI", "CHAGATAI", "JOCHI", "TOLUI");

    public static void main(String[] args) {
        final List<String> herdeiros = args.length > 0 ? Arrays.asList(args) : HERDEIROS_PADRAO;
        // a interface do Swing tem que ser criada na thread de eventos
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ControladorJogo(herdeiros).iniciar();
            }
        });
    }
}
