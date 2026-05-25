# Pokédex M2

## Participantes

- Nome completo: Maria Isabel Henke

- Nome completo: Mel Izis Godri

---

## Sobre o Projeto

Este projeto é uma aplicação Pokédex desenvolvida em Kotlin Multiplatform com Compose Multiplatform.

A proposta da M2 foi evoluir o projeto inicial da M1, substituindo dados simulados por uma estrutura mais próxima de uma aplicação real, utilizando consumo de API, persistência local, arquitetura com ViewModel e gerenciamento reativo de estado.

O aplicativo permite visualizar uma lista de Pokémons, consultar detalhes, filtrar por tipo, pesquisar por nome e salvar Pokémons favoritos/localizados pelo usuário.

---

## Funcionalidades Implementadas

- Tela inicial de onboarding com imagens personalizadas.
- Tela principal da Pokédex.
- Listagem de Pokémons em cards.
- Busca de Pokémon por nome.
- Filtro por tipo.
- Paginação na listagem.
- Tela de detalhes do Pokémon.
- Consumo de dados da PokeAPI.
- Persistência local dos Pokémons salvos.
- Campo obrigatório para informar onde o Pokémon foi capturado.
- Tela de favoritos/Pokédex salva.
- Limite máximo de 6 Pokémons adicionados.
- Tela/mensagem de erro ao tentar adicionar mais de 6 Pokémons.
- Gerenciamento de estado com ViewModel e StateFlow.
- Tratamento de estados de Loading, Success e Error.

---

## Regra de Negócio

O usuário pode adicionar no máximo 6 Pokémons à sua Pokédex/favoritos.

Caso tente adicionar um sétimo Pokémon, o sistema bloqueia a ação e apresenta uma mensagem de erro informando que o limite máximo foi atingido.

Além disso, ao adicionar um Pokémon, é obrigatório informar o local onde ele foi capturado.

---

## Tecnologias Utilizadas

- Kotlin Multiplatform
- Compose Multiplatform
- Material 3
- Ktor Client
- PokeAPI
- ViewModel
- StateFlow
- Persistência local
- Android Studio

---

## Arquitetura do Projeto

O projeto foi organizado separando responsabilidades entre interface, estado, dados e regras de negócio.

Estrutura principal:

```text
composeApp/
 └── src/
     └── commonMain/
         └── kotlin/
             └── com/example/pokedex_kmp/
                 ├── data/
                 ├── ui/
                 ├── viewmodel/
                 └── navigation/
