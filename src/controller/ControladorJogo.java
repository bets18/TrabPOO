package controller;

import java.util.List;

import model.HerdeirosDeKhanAPI;
import view.AcoesDoJogador;
import view.JanelaJogo;

// recebe os cliques da View, pede pro Model fazer a jogada e, se deu certo, manda a
// janela redesenhar. Se a jogada não vale, não acontece nada (sem mensagem), como o
// enunciado pede
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

    // primeiro passo do turno: colocar a ficha numa coluna
    @Override
    public void colunaClicada(String coluna) {
        if (modelo.ativarColuna(coluna)) {
            janela.atualizar();
        }
    }

    // a parada clicada é o destino; quem decide se dá pra ir é o Model
    @Override
    public void paradaClicada(String parada) {
        if (modelo.moverPara(parada)) {
            janela.atualizar();
        }
    }

    // clicou no outro peão da Altani: ela passa a jogar com ele
    @Override
    public void peaoClicado(int peao) {
        if (modelo.selecionarPeao(peao)) {
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
