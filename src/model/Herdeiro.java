package model;

import java.util.HashMap;
import java.util.Map;

// os 5 tabuleiros de jogador; cada um representa um herdeiro do Khan (manual, pág. 20).
// as ações de cada coluna foram tiradas das cartelas; a coluna branca é igual para todos.
enum Herdeiro {
    //       vermelha                                  verde                                     amarela
    ALTANI(  new AcoesColuna(1, 2, true, false, false), new AcoesColuna(2, 1, false, false, false), new AcoesColuna(1, 1, false, true, false)),
    CHAGATAI(new AcoesColuna(1, 2, true, false, false), new AcoesColuna(1, 1, false, true, false),  new AcoesColuna(2, 1, false, false, false)),
    JOCHI(   new AcoesColuna(1, 1, false, true, false), new AcoesColuna(1, 2, true, false, false),  new AcoesColuna(2, 1, false, false, false)),
    OGEDEI(  new AcoesColuna(2, 1, false, false, false), new AcoesColuna(1, 1, false, true, false), new AcoesColuna(1, 2, true, false, false)),
    TOLUI(   new AcoesColuna(1, 1, false, true, false), new AcoesColuna(2, 1, false, false, false), new AcoesColuna(1, 2, true, false, false));

    private final Map<Coluna, AcoesColuna> colunas = new HashMap<>();

    Herdeiro(AcoesColuna vermelha, AcoesColuna verde, AcoesColuna amarela) {
        colunas.put(Coluna.VERMELHA, vermelha);
        colunas.put(Coluna.VERDE, verde);
        colunas.put(Coluna.AMARELA, amarela);
        // branca: 2 cavalos, Khan e recuperar todas as fichas de ativação
        colunas.put(Coluna.BRANCA, new AcoesColuna(2, 0, true, false, true));
    }

    AcoesColuna getAcoes(Coluna coluna) {
        return colunas.get(coluna);
    }
}
