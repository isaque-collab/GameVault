# GameVault

O **GameVault** é uma aplicação web para descoberta, organização e avaliação de jogos.

O projeto utiliza a API pública da **RAWG** como fonte externa do catálogo de jogos e mantém no banco de dados apenas os dados próprios da aplicação, como usuários, favoritos, lista de desejos e avaliações.

> **Status:** em desenvolvimento
>
> **Back-end da V1:** concluído e validado.
>
> **Etapa atual:** fechamento documental concluído; projeto preparado para início do front-end.
>
> **Próximo foco:** implementação da interface web consumindo os contratos já consolidados do back-end.

---

## Funcionalidades da V1

* Cadastro e autenticação de usuários.
* Gerenciamento da própria conta.
* Perfil com upload, substituição, consulta e remoção de foto.
* Catálogo público de jogos.
* Busca por nome.
* Filtros e ordenações.
* Jogos populares.
* Lançamentos recentes.
* Jogos mais bem avaliados.
* Detalhes completos dos jogos.
* Favoritos.
* Lista de Desejos.
* Avaliações de 1 a 5 estrelas.
* Integração com a API RAWG.
* Personalização dos detalhes para usuários autenticados.
* Coleções pessoais enriquecidas com dados mínimos dos jogos para montagem de cards.

---

# Tecnologias

## Back-end

* Java 21.
* Spring Boot 4.1.1.
* Spring Web MVC.
* Spring Data JPA.
* Bean Validation.
* Spring Security.
* Spring RestClient.
* Maven.

## Banco de dados e infraestrutura

* MySQL 8.4 LTS.
* Flyway.
* Docker.
* Docker Compose.
* DBeaver.

## Testes

* JUnit 5.
* Mockito.
* MockMvc.
* Spring Boot Test.
* Spring Security Test.
* MockRestServiceServer.

---

# Arquitetura

O projeto segue uma arquitetura de **monólito modular por funcionalidade**.

```text
com.gamevault
├── avaliacao
├── favorito
├── jogo
│   ├── controller
│   ├── dto
│   └── service
├── listadesejos
├── rawg
│   ├── client
│   ├── config
│   ├── dto
│   └── exception
├── shared
└── user
```

Cada funcionalidade utiliza, conforme necessário:

```text
Controller
   ↓
DTO
   ↓
Service
   ↓
Repository
   ↓
Entity
```

As regras de negócio permanecem concentradas na camada de serviço. A integração com a RAWG fica isolada no módulo `rawg`, e o módulo `jogo` converte os contratos externos em DTOs próprios do GameVault.

---

# Persistência

O catálogo completo de jogos **não é persistido localmente**.

As relações próprias do GameVault utilizam:

```text
rawgGameId
```

como referência externa do jogo.

As principais tabelas são:

```text
users
favorites
wishlist
reviews
```

O Flyway controla a evolução do schema e o Hibernate utiliza:

```yaml
ddl-auto: validate
```

Regras principais:

* `username` e `email` são únicos.
* Um jogo não pode aparecer duas vezes nos Favoritos do mesmo usuário.
* Um jogo não pode aparecer duas vezes na Wishlist do mesmo usuário.
* Um usuário possui no máximo uma avaliação por jogo.
* Avaliações aceitam notas inteiras de 1 a 5.
* O mesmo jogo pode estar simultaneamente em Favoritos e Wishlist.
* A exclusão da conta remove os dados próprios relacionados.

---

# Autenticação e segurança

O GameVault utiliza **autenticação baseada em sessão HTTP com Spring Security**.

Após o login, o servidor mantém a autenticação por sessão e pelo cookie:

```text
JSESSIONID
```

Controllers privados utilizam:

```java
@AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal
```

Os endpoints pessoais usam `/me`, evitando receber um `usuarioId` arbitrário fornecido pelo cliente.

## CSRF

A proteção CSRF permanece habilitada.

```text
GET /api/csrf
```

Operações que alteram estado devem enviar um token válido.

## CORS

O back-end possui CORS configurável para a origem do front-end e permite credenciais, requisito necessário para a autenticação por sessão em origens diferentes.

Por padrão:

```text
http://localhost:5173
```

A origem pode ser alterada pela variável:

```env
FRONTEND_ORIGIN=http://localhost:5173
```

## Autenticação

| Método | Endpoint           | Descrição           | Resposta         |
| ------ | ------------------ | ------------------- | ---------------- |
| `GET`  | `/api/csrf`        | Obtém o token CSRF  | `200 OK`         |
| `POST` | `/api/auth/login`  | Autentica o usuário | `204 No Content` |
| `POST` | `/api/auth/logout` | Encerra a sessão    | `204 No Content` |

---

# Usuários e perfil

## Recursos implementados

* Cadastro.
* Validação de username e e-mail únicos.
* Codificação de senha.
* Consulta e edição do próprio perfil.
* Alteração de senha.
* Exclusão da conta.
* Encerramento da sessão após exclusão.
* Remoção dos dados relacionados.
* Upload real de foto de perfil.
* Substituição e remoção de foto.
* Formatos aceitos: JPEG, PNG e WebP.
* Limite da foto: 2 MB.
* Armazenamento em filesystem na V1.
* A chave física interna do arquivo não é exposta no contrato público.

## Endpoints

| Método   | Endpoint                 | Descrição                        | Resposta         |
| -------- | ------------------------ | -------------------------------- | ---------------- |
| `POST`   | `/api/usuarios`          | Cadastra usuário                 | `201 Created`    |
| `GET`    | `/api/usuarios/me`       | Consulta o perfil                | `200 OK`         |
| `PUT`    | `/api/usuarios/me`       | Atualiza nome, username e e-mail | `200 OK`         |
| `PUT`    | `/api/usuarios/me/senha` | Altera a senha                   | `204 No Content` |
| `PUT`    | `/api/usuarios/me/foto`  | Define ou substitui a foto       | `200 OK`         |
| `GET`    | `/api/usuarios/me/foto`  | Obtém a foto                     | `200 OK`         |
| `DELETE` | `/api/usuarios/me/foto`  | Remove a foto                    | `204 No Content` |
| `DELETE` | `/api/usuarios/me`       | Exclui a conta                   | `204 No Content` |

---

# Favoritos

* Adicionar.
* Listar.
* Remover.
* Impedir duplicidade.
* Associar as operações ao usuário autenticado.
* Consultar o estado para composição dos detalhes.
* Enriquecer a listagem com dados mínimos do jogo para o card.
* Preservar o vínculo quando os metadados externos estiverem indisponíveis.

| Método   | Endpoint                                  | Descrição                    | Resposta         |
| -------- | ----------------------------------------- | ---------------------------- | ---------------- |
| `POST`   | `/api/usuarios/me/favoritos`              | Adiciona favorito            | `201 Created`    |
| `GET`    | `/api/usuarios/me/favoritos`              | Lista favoritos enriquecidos | `200 OK`         |
| `DELETE` | `/api/usuarios/me/favoritos/{rawgGameId}` | Remove favorito              | `204 No Content` |

---

# Lista de Desejos

* Adicionar.
* Listar.
* Remover.
* Impedir duplicidade.
* Permitir coexistência com Favoritos.
* Associar operações ao usuário autenticado.
* Consultar o estado para composição dos detalhes.
* Enriquecer a listagem com dados mínimos do jogo para o card.
* Preservar o vínculo quando a RAWG estiver indisponível.

| Método   | Endpoint                                      | Descrição                | Resposta         |
| -------- | --------------------------------------------- | ------------------------ | ---------------- |
| `POST`   | `/api/usuarios/me/lista-desejos`              | Adiciona jogo            | `201 Created`    |
| `GET`    | `/api/usuarios/me/lista-desejos`              | Lista itens enriquecidos | `200 OK`         |
| `DELETE` | `/api/usuarios/me/lista-desejos/{rawgGameId}` | Remove jogo              | `204 No Content` |

## Coleções pessoais e falhas externas

Favoritos e Wishlist persistem somente o vínculo próprio do GameVault. No `GET`, cada item pode ser enriquecido com dados mínimos da RAWG:

```json
{
  "id": 10,
  "rawgGameId": 3498,
  "criadoEm": "2026-09-21T20:00:00",
  "nome": "Grand Theft Auto V",
  "dataLancamento": "2013-09-17",
  "imagemFundo": "https://...",
  "notaRawg": 4.47,
  "metacritic": 92,
  "metadadosDisponiveis": true
}
```

Se os metadados não puderem ser obtidos, o vínculo continua retornando com:

```text
metadadosDisponiveis = false
```

Uma falha da RAWG não exclui o Favorito ou item da Wishlist.

---

# Avaliações

* Notas inteiras entre 1 e 5.
* Uma avaliação por usuário e jogo.
* Criação e atualização.
* Consulta da própria avaliação.
* Remoção.
* Média e quantidade da comunidade GameVault.
* Ausência de avaliações representada por média `null`, não por zero.

| Método   | Endpoint                                    | Descrição                  | Resposta         |
| -------- | ------------------------------------------- | -------------------------- | ---------------- |
| `PUT`    | `/api/usuarios/me/avaliacoes/{rawgGameId}`  | Cria ou atualiza avaliação | `200 OK`         |
| `GET`    | `/api/usuarios/me/avaliacoes/{rawgGameId}`  | Consulta avaliação própria | `200 OK`         |
| `DELETE` | `/api/usuarios/me/avaliacoes/{rawgGameId}`  | Remove avaliação própria   | `204 No Content` |
| `GET`    | `/api/jogos/{rawgGameId}/avaliacoes/resumo` | Média e quantidade         | `200 OK`         |

---

# Catálogo e Home

## Catálogo

```text
GET /api/jogos
```

Filtros opcionais:

* nome;
* gênero;
* plataforma;
* desenvolvedora;
* publicadora;
* período de lançamento;
* ordenação;
* página.

Ordenações:

```text
POPULARIDADE
AVALIACAO_RAWG
METACRITIC
LANCAMENTO
NOME
```

A popularidade não utiliza divisão semanal ou mensal.

## Home

```text
GET /api/jogos/populares
GET /api/jogos/lancamentos-recentes
GET /api/jogos/mais-bem-avaliados
```

Lançamentos recentes consideram os últimos 30 dias corridos.

---

# Detalhes do jogo

```text
GET /api/jogos/{rawgGameId}
```

O endpoint é público e combina dados RAWG com dados próprios do GameVault.

Entre os dados externos estão:

* nome e descrição;
* lançamento e imagem;
* avaliação RAWG e Metacritic;
* classificação etária;
* gêneros;
* plataformas;
* requisitos;
* desenvolvedoras;
* publicadoras;
* screenshots.

Dados públicos do GameVault:

```text
mediaAvaliacoesGameVault
quantidadeAvaliacoesGameVault
```

Quando autenticado, o mesmo endpoint também retorna:

```text
minhaAvaliacao
favoritado
naListaDesejos
```

Para visitantes, os estados pessoais permanecem `null`.

## Decisão de interface da V1

Na Home e no catálogo, os jogos aparecem como cards de descoberta.

```text
card
  ↓ clique
detalhes do jogo
```

As ações pessoais ficam na página de detalhes:

* adicionar/remover Favorito;
* adicionar/remover Wishlist;
* criar/alterar/remover avaliação.

Biblioteca de Favoritos e Wishlist também permitem consultar e remover os respectivos itens.

---

# Integração RAWG

A integração utiliza `RestClient`.

Operações externas principais:

```text
GET /games
GET /games/{id}
GET /games/{id}/screenshots
```

Configuração:

```yaml
rawg:
  base-url: https://api.rawg.io/api
  api-key: ${RAWG_API_KEY:}
```

A integração possui timeout de conexão de 3 segundos e leitura de 10 segundos.

São tratados:

* jogo não encontrado;
* chave ausente;
* erros HTTP;
* corpo ausente;
* resposta inválida;
* falhas de comunicação;
* parâmetros inválidos.

---

# Tratamento global de erros

A API utiliza `@RestControllerAdvice` e `ProblemDetail`.

Entre os status utilizados:

```text
400 Bad Request
401 Unauthorized
404 Not Found
409 Conflict
413 Payload Too Large
502 Bad Gateway
503 Service Unavailable
```

---

# Estratégia de testes

A V1 possui testes em múltiplos níveis:

## Persistência

* usuários;
* unicidade;
* Favoritos;
* Wishlist;
* avaliações;
* constraints;
* agregações.

## Unitários

* usuário e senha;
* foto de perfil;
* Favoritos e Wishlist;
* avaliações;
* catálogo e Home;
* detalhes;
* enriquecimento de coleções;
* integração RAWG.

## MVC

* contratos JSON;
* status HTTP;
* validações;
* segurança;
* CSRF;
* endpoints públicos e privados;
* foto de perfil;
* catálogo, Home e detalhes;
* Favoritos, Wishlist e avaliações;
* respostas de erro.

## RAWG

O `RawgClient` é testado com `MockRestServiceServer`, sem depender da API real durante a suíte automatizada.

## Integração

Os fluxos de integração carregam o contexto Spring e utilizam o MySQL do ambiente de desenvolvimento, cobrindo autenticação, perfil, senha, exclusão, foto, Favoritos, Wishlist e avaliações.

---

# Validação atual

A suíte completa pode ser executada com:

```powershell
mvn test
```

No fechamento do back-end da V1, a suíte completa foi executada com:

```text
Failures: 0
Errors: 0

BUILD SUCCESS
```

O `git status --short` também foi confirmado sem alterações pendentes após os commits funcionais e de testes do fechamento.

---

# Executando o banco com Docker

Crie `.env` na raiz:

```env
MYSQL_ROOT_PASSWORD=sua_senha_root
MYSQL_USER=seu_usuario
MYSQL_PASSWORD=sua_senha
RAWG_API_KEY=sua_chave
FRONTEND_ORIGIN=http://localhost:5173
GAMEVAULT_PROFILE_IMAGE_DIRECTORY=./data/profile-images
```

Suba o banco:

```bash
docker compose up -d
```

Para interromper:

```bash
docker compose stop
```

Para remover containers sem apagar volumes:

```bash
docker compose down
```

> `docker compose down -v` também remove os volumes e apaga os dados armazenados.

Nunca versione credenciais reais nem o arquivo `.env`.

---

# Estado atual do desenvolvimento

## Back-end da V1

**Concluído e validado.**

Inclui:

* infraestrutura e persistência;
* autenticação e segurança;
* usuário e perfil;
* foto de perfil;
* Favoritos;
* Wishlist;
* avaliações;
* catálogo;
* Home;
* detalhes;
* integração RAWG;
* tratamento de erros;
* CORS e CSRF;
* coleções pessoais enriquecidas;
* testes automatizados em múltiplos níveis.

---

# Próximos passos

* Iniciar a implementação do front-end.
* Consumir os contratos consolidados do back-end.
* Implementar Home e catálogo com cards que abrem os detalhes.
* Implementar login, cadastro e perfil.
* Implementar ações pessoais na página de detalhes.
* Implementar Biblioteca de Favoritos e Wishlist.
* Manter o README e a documentação sincronizados ao final de cada bloco.

---

# Autor

**Isaque Costa da Cunha**
