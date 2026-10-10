# Herdeiros de Khan

Trabalho desenvolvido para a disciplina de Programação Orientada a Objetos (POO) no semestre 2026.2.

Esse projeto é uma implementação em Java do jogo de tabuleiro Herdeiros de Khan, organizada segundo a arquitetura MVC. Aqui colocamos em prática o que aprendemos sobre orientação a objetos, como encapsulamento, ocultação da informação e testes unitários usando JUnit 4.

## 1ª iteração — componente Model

O pacote `model` contém as regras do jogo. Só a classe `HerdeirosDeKhanAPI` é pública; as demais (tabuleiro, jogador, cidades etc.) são de pacote. A API recebe e devolve apenas `String`s e tipos primitivos. Os padrões Singleton e Façade serão aplicados a ela na 3ª iteração.

Regras implementadas (classe `Jogo`):
* Preparação: 2 a 5 jogadores, peões em Karakorum, 1 moeda para os dois primeiros e 2 para os demais, 1 tributo por província, 3 cidades reveladas com 4 tesouros cada.
* Movimento: só por rotas, sem terminar em parada cheia (simples = 1 peão, dupla = 2, Karakorum = todos). O jogador pode, se quiser, pular os próprios yurts sem gastar movimento; paradas puladas não contam para ações.
* Tributos: só de províncias adjacentes às paradas feitas no turno.
* Khan: só em províncias do Khan, sem permanecer no mesmo lugar, adicionando tributo na província e nas 2 indicadas (máximo 3).
* Ataque a cidades: 1 espada pelo 1º tesouro e 2 pelos seguintes no mesmo turno; quem pega o último conquista a cidade e revela a próxima.
* Yurts: só em paradas feitas durante o movimento e com espaço (Karakorum não aceita).

Os jogadores são identificados pelo herdeiro do seu tabuleiro (Altani, Chagatai, Jochi, Ogedei, Tolui) e os tesouros são de 5 tipos: pele, ferro, carne, grãos e lã.
* Fim de jogo: disparado com 10/14/16/17 votos (2/3/4/5 jogadores) ou com todas as cidades conquistadas.

## Como rodar os testes

Importe a pasta no Eclipse (File > Import > Existing Projects into Workspace). As pastas `src` e `teste` já estão configuradas como source folders e o JUnit 4 está no build path. Clique com o botão direito em `teste` > Run As > JUnit Test.

## Grupo
* Lis Almeida
* Rafaela Bessa
* Davi Rangel
