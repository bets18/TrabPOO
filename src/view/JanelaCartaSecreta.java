package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

// antes da 1ª rodada cada jogador vê as 2 cartas de conselheiro secreto que recebeu e
// clica na que vai ficar com ele. As cartas são desenhadas com drawImage. A janela é a
// mesma pra todos; os outros não devem olhar
public class JanelaCartaSecreta extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final int LARGURA_CARTA = 230;
    private static final int MARGEM = 20;

    private final List<String> cartas;
    private String escolhida = null;

    private JanelaCartaSecreta(String herdeiro, List<String> cartas) {
        super((java.awt.Frame) null, "Carta de conselheiro secreto", true);
        this.cartas = cartas;
        // só fecha escolhendo uma das cartas
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JLabel titulo = new JLabel("<html>Vez de <b>" + EstiloJogador.getNome(herdeiro) + "</b>: clique na carta que vai "
                + "ficar com você (a outra é descartada). Ela pode dar até 3 votos no fim do jogo.</html>");
        titulo.setFont(new Font("SansSerif", Font.PLAIN, 15));
        titulo.setForeground(EstiloJogador.getCor(herdeiro));
        titulo.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        Desenho desenho = new Desenho();
        BufferedImage exemplo = Imagens.carregar(Pecas.cartaSecreta(cartas.get(0)), LARGURA_CARTA);
        desenho.setPreferredSize(new Dimension(MARGEM + cartas.size() * (LARGURA_CARTA + MARGEM),
                exemplo.getHeight() + 2 * MARGEM + 20));
        desenho.addMouseListener(new CliqueNaCarta());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(titulo, BorderLayout.NORTH);
        getContentPane().add(desenho, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    // abre a janela e espera a escolha
    public static String escolher(String herdeiro, List<String> cartas) {
        JanelaCartaSecreta janela = new JanelaCartaSecreta(herdeiro, cartas);
        janela.setVisible(true);
        return janela.escolhida;
    }

    private Rectangle areaDaCarta(int i) {
        BufferedImage carta = Imagens.carregar(Pecas.cartaSecreta(cartas.get(i)), LARGURA_CARTA);
        return new Rectangle(MARGEM + i * (LARGURA_CARTA + MARGEM), MARGEM, carta.getWidth(), carta.getHeight());
    }

    private class Desenho extends JPanel {
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.setColor(Color.DARK_GRAY);
            for (int i = 0; i < cartas.size(); i++) {
                Rectangle area = areaDaCarta(i);
                g2.drawImage(Imagens.carregar(Pecas.cartaSecreta(cartas.get(i)), LARGURA_CARTA), area.x, area.y, null);
                g2.drawString(Pecas.nomeDaCartaSecreta(cartas.get(i)), area.x, area.y + area.height + 18);
            }
        }
    }

    private class CliqueNaCarta extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            for (int i = 0; i < cartas.size(); i++) {
                if (areaDaCarta(i).contains(e.getPoint())) {
                    escolhida = cartas.get(i);
                    dispose();
                    return;
                }
            }
        }
    }
}
