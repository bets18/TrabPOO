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
* Fim de jogo: disparado com 10/14/16/17 votos (2/3/4/5 jogadores) ou com todas as cidades conquistadas.

Os jogadores são identificados pelo herdeiro do seu tabuleiro (Altani, Chagatai, Jochi, Ogedei, Tolui) e os tesouros são de 5 tipos: pele, ferro, carne, grãos e lã.

## 2ª iteração — interface gráfica (View e Controller)

* `model`: o mapa real do tabuleiro (`MapaPadrao`: 33 paradas, 6 delas duplas, mais Karakorum, 54 rotas e 9 cidades), as colunas de ativação de cada herdeiro e o movimento por clique (`moverPara`, que acha o menor caminho dentro dos cavalos disponíveis).
* `view`: janela Swing de até 1400 x 1050. O tabuleiro e o tabuleiro do herdeiro são desenhados com `drawImage()` do `Graphics2D`; os peões, com formas do Java2D. A faixa do painel lateral fica na cor do jogador da vez.
* `controller`: recebe os cliques (pela interface `view.AcoesDoJogador`), pede a jogada ao Model e manda a View se redesenhar. Jogada inválida não faz nada. Nenhum evento usa expressão lambda.

Como jogar: clique numa coluna do tabuleiro do herdeiro (os cavalos dela são os movimentos), depois clique na parada de destino; ao terminar, clique em "Passar a vez".

Habilidades de herdeiro que afetam o movimento: Altani tem 2 peões e, antes de mover, escolhe qual usar clicando nele; Tolui começa com um yurt em Karakorum e pode pulá-la como pula os próprios yurts. Os yurts aparecem no tabuleiro na cor do dono.

## Como executar

No Eclipse, rode `controller.Principal` (Run As > Java Application) com o diretório de trabalho na pasta do projeto, para que a pasta `assets` seja encontrada. Sem argumentos, a partida começa com Altani, Chagatai, Jochi e Tolui; para escolher outros, passe os nomes como argumentos (ex.: `ALTANI OGEDEI TOLUI`).

## Como rodar os testes

Importe a pasta no Eclipse (File > Import > Existing Projects into Workspace). As pastas `src` e `teste` já estão configuradas como source folders e o JUnit 4 está no build path. Clique com o botão direito em `teste` > Run As > JUnit Test.

## Grupo
* Lis Almeida
* Rafaela Bessa
* Davi Rangel
