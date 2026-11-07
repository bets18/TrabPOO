package model;

// peças de bônus: ficam em cima dos 7 conselheiros e vão para quem fizer a 1ª entrega
// ali. O voto vale na hora; as outras o jogador guarda e usa uma vez, no turno que quiser
enum TipoBonus {
    VOTO,
    DUAS_ESPADAS,
    DUAS_MOEDAS,
    DOIS_YURTS,
    MOVIMENTOS,  // 3 movimentos e 1 ação de pegar tributo
    DOIS_PUNHOS, // 2 ações de pegar tributo
    TESOUROS     // tira 3 tesouros da pilha e fica com 1
}
