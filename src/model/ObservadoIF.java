package model;

// padrão Observer: o lado observado. Quem implementa guarda a lista de observadores e
// avisa todos sempre que o estado da partida muda
public interface ObservadoIF {
    void adicionarObservador(ObservadorIF observador);

    void removerObservador(ObservadorIF observador);
}
