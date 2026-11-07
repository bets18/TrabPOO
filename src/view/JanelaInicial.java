package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;

// janela do começo: os jogadores escolhem os herdeiros (de 2 a 5) na ordem em que vão jogar
public class JanelaInicial extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final List<String> HERDEIROS = Arrays.asList("ALTANI", "CHAGATAI", "JOCHI", "OGEDEI", "TOLUI");
    private static final int MINIMO = 2;

    private final List<String> ordem = new ArrayList<>();
    private final JLabel textoDaOrdem = new JLabel(" ");
    private final JButton comecar = new JButton("Começar partida");
    private final List<JToggleButton> botoes = new ArrayList<>();
    private boolean confirmado = false;

    private JanelaInicial() {
        super((java.awt.Frame) null, "Herdeiros de Khan - nova partida", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JLabel explicacao = new JLabel("<html>Clique nos herdeiros na ordem de jogo (de 2 a 5 jogadores).<br>"
                + "Começa quem tiver o maior número de descendentes.</html>");
        explicacao.setFont(new Font("SansSerif", Font.PLAIN, 15));
        explicacao.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        JPanel escolhas = new JPanel(new GridLayout(1, HERDEIROS.size(), 8, 0));
        escolhas.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        for (String herdeiro : HERDEIROS) {
            JToggleButton botao = new JToggleButton(EstiloJogador.getNome(herdeiro));
            botao.setFont(new Font("SansSerif", Font.BOLD, 15));
            botao.setForeground(EstiloJogador.getCor(herdeiro));
            botao.addActionListener(new Alternar(herdeiro, botao));
            botoes.add(botao);
            escolhas.add(botao);
        }

        textoDaOrdem.setFont(new Font("SansSerif", Font.BOLD, 15));
        textoDaOrdem.setForeground(new Color(90, 60, 20));
        textoDaOrdem.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));

        JButton limpar = new JButton("Limpar");
        limpar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ordem.clear();
                for (JToggleButton botao : botoes) {
                    botao.setSelected(false);
                }
                atualizar();
            }
        });
        comecar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmado = true;
                dispose();
            }
        });
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.add(limpar);
        rodape.add(comecar);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(escolhas, BorderLayout.NORTH);
        centro.add(textoDaOrdem, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(explicacao, BorderLayout.NORTH);
        getContentPane().add(centro, BorderLayout.CENTER);
        getContentPane().add(rodape, BorderLayout.SOUTH);
        atualizar();
        pack();
        setLocationRelativeTo(null);
    }

    // abre a janela e espera; devolve os herdeiros na ordem ou null se fecharam a janela
    public static List<String> escolherHerdeiros() {
        JanelaInicial janela = new JanelaInicial();
        janela.setVisible(true);
        return janela.confirmado ? new ArrayList<>(janela.ordem) : null;
    }

    private void atualizar() {
        StringBuilder texto = new StringBuilder("Ordem: ");
        for (int i = 0; i < ordem.size(); i++) {
            texto.append(i + 1).append("º ").append(EstiloJogador.getNome(ordem.get(i))).append("   ");
        }
        textoDaOrdem.setText(ordem.isEmpty() ? "Ordem: nenhum herdeiro escolhido" : texto.toString());
        comecar.setEnabled(ordem.size() >= MINIMO);
    }

    private class Alternar implements ActionListener {
        private final String herdeiro;
        private final JToggleButton botao;

        Alternar(String herdeiro, JToggleButton botao) {
            this.herdeiro = herdeiro;
            this.botao = botao;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (botao.isSelected()) {
                ordem.add(herdeiro);
            } else {
                ordem.remove(herdeiro);
            }
            atualizar();
        }
    }
}
