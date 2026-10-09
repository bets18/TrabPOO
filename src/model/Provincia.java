package model;

import java.util.ArrayList;
import java.util.List;

class Provincia extends Local {
    List<TipoTributo> tributos = new ArrayList<>();

    void adicionarTributo(TipoTributo tributo) {
        if (tributos.size() < 3) {
            tributos.add(tributo);
        } else {
            throw new IllegalStateException("Limite máximo de 3 tributos alcançado.");
        }
    }
}
