package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import model.HerdeirosDeKhanAPI;

// placar final: votos ganhos na partida, influência nas regiões, carta secreta e o vencedor
class JanelaResultado extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUNAS = {"Herdeiro", "Na partida", "Influência", "Carta secreta", "Total", "Tesouros"};

    JanelaResultado(Frame dona) {
        super(dona, "Fim de jogo", true);
        HerdeirosDeKhanAPI modelo = HerdeirosDeKhanAPI.getInstancia();

        List<String> vencedores = modelo.getVencedores();
        StringBuilder nomes = new StringBuilder();
        for (String herdeiro : vencedores) {
            if (nomes.length() > 0) {
                nomes.append(" e ");
            }
            nomes.append(EstiloJogador.getNome(herdeiro));
        }
        JLabel titulo = new JLabel((vencedores.size() > 1 ? "Novos Khans: " : "Novo Khan: ") + nomes, SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        titulo.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));
        if (vencedores.size() == 1) {
            titulo.setForeground(EstiloJogador.getCor(vencedores.get(0)));
        }

        JPanel tabela = new JPanel(new GridLayout(0, COLUNAS.length, 12, 6));
        tabela.setBorder(BorderFactory.createEmptyBorder(6, 16, 10, 16));
        for (String coluna : COLUNAS) {
            tabela.add(celula(coluna, true, Color.DARK_GRAY));
        }
        for (int i = 0; i < modelo.getNumeroDeJogadores(); i++) {
            String herdeiro = modelo.getHerdeiro(i);
            int total = modelo.getVotos(i);
            int influencia = modelo.getVotosDeInfluencia(i);
            int secreta = modelo.getVotosDaCartaSecreta(i);
            String carta = modelo.getCartaSecreta(i);
            String nomeDaCarta = carta == null ? "-" : Pecas.nomeDaCartaSecreta(carta);
            tabela.add(celula(EstiloJogador.getNome(herdeiro), true, EstiloJogador.getCor(herdeiro)));
            tabela.add(celula(String.valueOf(total - influencia - secreta), false, Color.BLACK));
            tabela.add(celula(String.valueOf(influencia), false, Color.BLACK));
            tabela.add(celula(nomeDaCarta + ": " + secreta, false, Color.BLACK));
            tabela.add(celula(String.valueOf(total), true, Color.BLACK));
            tabela.add(celula(String.valueOf(modelo.getTotalDeTesouros(i)), false, Color.BLACK));
        }

        JButton fechar = new JButton("Fechar");
        fechar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.add(new JLabel("Desempate: mais tesouros não entregues, depois o peão mais perto de Karakorum  "));
        rodape.add(fechar);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(titulo, BorderLayout.NORTH);
        getContentPane().add(tabela, BorderLayout.CENTER);
        getContentPane().add(rodape, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(dona);
    }

    private static JLabel celula(String texto, boolean negrito, Color cor) {
        JLabel celula = new JLabel(texto);
        celula.setFont(new Font("SansSerif", negrito ? Font.BOLD : Font.PLAIN, 15));
        celula.setForeground(cor);
        return celula;
    }
}
