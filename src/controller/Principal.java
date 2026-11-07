package controller;

import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

// começa o jogo. Sem argumentos abre a janela de escolha dos herdeiros; com argumentos
// (ex: java controller.Principal ALTANI OGEDEI TOLUI) a partida já começa com eles
public class Principal {
    public static void main(String[] args) {
        final List<String> herdeiros = Arrays.asList(args);
        // a interface do Swing tem que ser criada na thread de eventos
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ControladorJogo().iniciar(herdeiros);
            }
        });
    }
}
