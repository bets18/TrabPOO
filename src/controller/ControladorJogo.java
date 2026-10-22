package controller;

import java.util.List;

import model.HerdeirosDeKhanAPI;
import view.AcoesDoJogador;
import view.JanelaJogo;

// conduz as etapas do turno: recebe os cliques da View, pede ao Model que aplique a regra e,
// se a jogada foi aceita, manda a View se redesenhar. Jogada inválida não faz nada (sem mensagem),
// como pede o PDF da 2ª iteração.
public class ControladorJogo implements AcoesDoJogador {
    private final HerdeirosDeKhanAPI modelo;
    private JanelaJogo janela;

    public ControladorJogo(List<String> herdeirosNaOrdemDeJogo) {
        modelo = new HerdeirosDeKhanAPI(herdeirosNaOrdemDeJogo);
    }

    public void iniciar() {
        janela = new JanelaJogo(modelo, this);
        janela.setVisible(true);
    }

    // 1º passo do turno: colocar a ficha de ativação numa coluna do tabuleiro do herdeiro
    @Override
    public void colunaClicada(String coluna) {
        if (modelo.ativarColuna(coluna)) {
            janela.atualizar();
        }
    }

    // clique numa parada: se ali está o outro peão da Altani, ela passa a mover esse peão;
    // senão é o destino do movimento, e o Model decide se dá para ir
    @Override
    public void paradaClicada(String parada) {
        int outroPeao = modelo.getPeaoInativoEm(parada);
        if (outroPeao >= 0 && modelo.selecionarPeao(outroPeao)) {
            janela.atualizar();
            return;
        }
        if (modelo.moverPara(parada)) {
            janela.atualizar();
        }
    }

    @Override
    public void passarVezClicado() {
        if (modelo.passarVez()) {
            janela.atualizar();
        }
    }
}
