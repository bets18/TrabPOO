package model;

// padrão Observer: o lado observado. Quem implementa guarda a lista de observadores e
// avisa todos sempre que o estado da partida muda. Igual ao Cap. 17: add, remove e get
public interface ObservadoIF {
    void adicionarObservador(ObservadorIF observador);

    void removerObservador(ObservadorIF observador);

    // o observador chama depois de ser avisado pra saber o que mudou
    int get(int i);
}
