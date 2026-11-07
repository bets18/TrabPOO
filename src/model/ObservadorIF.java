package model;

// padrão Observer: o lado observador. O evento diz o que mudou (as constantes EVENTO_ da
// HerdeirosDeKhanAPI) e o observador consulta o observado pra saber o estado novo
public interface ObservadorIF {
    void notificar(ObservadoIF observado, String evento);
}
