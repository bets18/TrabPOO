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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// janela do começo. Cada linha é um jogador: o nome dele e o herdeiro (o peão e a cor) com
// que vai jogar. A ordem das linhas é a ordem de jogo. Também dá pra continuar uma partida
// salva em arquivo
public class JanelaInicial extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final List<String> HERDEIROS = Arrays.asList("ALTANI", "CHAGATAI", "JOCHI", "OGEDEI", "TOLUI");
    private static final String NAO_JOGA = "(não joga)";
    private static final int MINIMO = 2;
    private static final int MAXIMO = 5;
    private static final int TAMANHO_MAXIMO_DO_NOME = 12;

    private final List<JTextField> camposDeNome = new ArrayList<>();
    private final List<JComboBox<String>> caixasDeHerdeiro = new ArrayList<>();
    private final JLabel situacao = new JLabel(" ");
    private final JButton comecar = new JButton("Começar partida");
    private boolean confirmada = false;
    private String arquivoParaCarregar = null;

    // o aviso aparece embaixo (ex: o arquivo escolhido antes não pôde ser carregado)
    private JanelaInicial(String aviso) {
        super((java.awt.Frame) null, "Herdeiros de Khan - nova partida", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JLabel explicacao = new JLabel("<html>Escreva o nome de cada jogador (de 2 a 5) e escolha o herdeiro dele.<br>"
                + "A ordem das linhas é a ordem de jogo: começa quem tiver o maior número de descendentes<br>"
                + "e os outros seguem no sentido horário.</html>");
        explicacao.setFont(new Font("SansSerif", Font.PLAIN, 15));
        explicacao.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        JPanel linhas = new JPanel(new GridLayout(MAXIMO + 1, 3, 10, 6));
        linhas.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        linhas.add(titulo("Ordem de jogo"));
        linhas.add(titulo("Nome do jogador"));
        linhas.add(titulo("Herdeiro (peão)"));
        AoMudar aoMudar = new AoMudar();
        for (int i = 0; i < MAXIMO; i++) {
            JTextField nome = new JTextField(TAMANHO_MAXIMO_DO_NOME);
            nome.setFont(new Font("SansSerif", Font.PLAIN, 15));
            nome.getDocument().addDocumentListener(aoMudar);
            JComboBox<String> herdeiro = new JComboBox<>();
            herdeiro.addItem(NAO_JOGA);
            for (String opcao : HERDEIROS) {
                herdeiro.addItem(EstiloJogador.getNome(opcao));
            }
            herdeiro.setFont(new Font("SansSerif", Font.BOLD, 15));
            herdeiro.addActionListener(aoMudar);
            camposDeNome.add(nome);
            caixasDeHerdeiro.add(herdeiro);
            linhas.add(titulo((i + 1) + "º a jogar"));
            linhas.add(nome);
            linhas.add(herdeiro);
        }

        situacao.setFont(new Font("SansSerif", Font.BOLD, 15));
        situacao.setForeground(new Color(150, 40, 20));
        situacao.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));

        JButton carregar = new JButton("Continuar partida salva...");
        carregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String caminho = SeletorDeArquivo.escolherParaAbrir(JanelaInicial.this);
                if (caminho != null) {
                    arquivoParaCarregar = caminho;
                    dispose();
                }
            }
        });
        JButton limpar = new JButton("Limpar");
        limpar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < MAXIMO; i++) {
                    camposDeNome.get(i).setText("");
                    caixasDeHerdeiro.get(i).setSelectedIndex(0);
                }
            }
        });
        comecar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmada = true;
                dispose();
            }
        });
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.add(carregar);
        rodape.add(limpar);
        rodape.add(comecar);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(linhas, BorderLayout.NORTH);
        centro.add(situacao, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(explicacao, BorderLayout.NORTH);
        getContentPane().add(centro, BorderLayout.CENTER);
        getContentPane().add(rodape, BorderLayout.SOUTH);
        atualizar();
        if (aviso != null) {
            situacao.setText(aviso);
        }
        pack();
        setLocationRelativeTo(null);
    }

    // abre a janela e espera. Depois é só perguntar o que foi decidido nela
    public static JanelaInicial abrir(String aviso) {
        JanelaInicial janela = new JanelaInicial(aviso);
        janela.setVisible(true);
        return janela;
    }

    // true se clicaram em "Começar partida" (false se fecharam a janela ou escolheram um arquivo)
    public boolean isConfirmada() {
        return confirmada;
    }

    // arquivo da partida salva escolhida, ou null
    public String getArquivoParaCarregar() {
        return arquivoParaCarregar;
    }

    // herdeiros na ordem de jogo, como o Model espera ("ALTANI"...)
    public List<String> getHerdeiros() {
        List<String> herdeiros = new ArrayList<>();
        for (int i = 0; i < MAXIMO; i++) {
            if (joga(i)) {
                herdeiros.add(HERDEIROS.get(caixasDeHerdeiro.get(i).getSelectedIndex() - 1));
            }
        }
        return herdeiros;
    }

    // nomes dos jogadores, na mesma ordem dos herdeiros
    public List<String> getNomes() {
        List<String> nomes = new ArrayList<>();
        for (int i = 0; i < MAXIMO; i++) {
            if (joga(i)) {
                nomes.add(camposDeNome.get(i).getText().trim());
            }
        }
        return nomes;
    }

    // a linha conta se tem herdeiro escolhido
    private boolean joga(int linha) {
        return caixasDeHerdeiro.get(linha).getSelectedIndex() > 0;
    }

    private static JLabel titulo(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        return titulo;
    }

    // diz o que falta pra poder começar, ou null se está tudo certo
    private String problema() {
        List<String> herdeiros = getHerdeiros();
        List<String> nomes = getNomes();
        if (herdeiros.size() < MINIMO) {
            return "Escolha o herdeiro de pelo menos " + MINIMO + " jogadores";
        }
        if (new HashSet<>(herdeiros).size() != herdeiros.size()) {
            return "Cada herdeiro só pode ser escolhido por um jogador";
        }
        Set<String> usados = new HashSet<>();
        for (String nome : nomes) {
            if (nome.isEmpty()) {
                return "Falta o nome de um jogador";
            }
            if (nome.length() > TAMANHO_MAXIMO_DO_NOME) {
                return "Os nomes têm no máximo " + TAMANHO_MAXIMO_DO_NOME + " letras";
            }
            if (!usados.add(nome.toLowerCase())) {
                return "Dois jogadores não podem ter o mesmo nome";
            }
        }
        return null;
    }

    private void atualizar() {
        String problema = problema();
        if (problema != null) {
            situacao.setText(problema);
        } else {
            StringBuilder texto = new StringBuilder("Ordem: ");
            List<String> herdeiros = getHerdeiros();
            List<String> nomes = getNomes();
            for (int i = 0; i < nomes.size(); i++) {
                texto.append(i + 1).append("º ").append(EstiloJogador.getRotulo(nomes.get(i), herdeiros.get(i))).append("   ");
            }
            situacao.setText(texto.toString());
        }
        comecar.setEnabled(problema == null);
    }

    // qualquer mudança num nome ou num herdeiro refaz a conferência
    private class AoMudar implements ActionListener, DocumentListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            atualizar();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            atualizar();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            atualizar();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            atualizar();
        }
    }
}
