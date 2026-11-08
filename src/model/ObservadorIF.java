package model;

// padrão Observer: o lado observador. Recebe só o observado e pergunta pra ele o que mudou
// com observado.get(HerdeirosDeKhanAPI.ULTIMO_EVENTO), que devolve uma das constantes EVENTO_
public interface ObservadorIF {
    void notificar(ObservadoIF observado);
}
