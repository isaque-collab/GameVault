# GameVault

O **GameVault** é uma aplicação web para descoberta, organização e avaliação de jogos.

O projeto utiliza a API pública da **RAWG** como fonte externa do catálogo de jogos e mantém no banco de dados apenas os dados próprios da aplicação, como usuários, favoritos, lista de desejos e avaliações.

> **Status:** em desenvolvimento
> **Etapa atual:** desenvolvimento Back-end — autenticação, gerenciamento de usuários, Favoritos, Lista de Desejos e Avaliações concluídos e validados.
> **Próximo foco:** integração com a API RAWG.

---

## Funcionalidades planejadas para a V1

* Cadastro e autenticação de usuários.
* Gerenciamento da própria conta.
* Perfil do usuário com imagem.
* Busca de jogos.
* Listagem de jogos populares.
* Lançamentos recentes.
* Jogos mais bem avaliados.
* Favoritos.
* Lista de Desejos.
* Avaliação de jogos com notas de 1 a 5.
* Integração com a API RAWG.

---

# Tecnologias

## Back-end

* Java 21
* Spring Boot 4.1.1
* Spring Web
* Spring Data JPA
* Bean Validation
* Spring Security
* Maven

## Banco de dados e infraestrutura

* MySQL 8.4 LTS
* Docker
* Docker Compose
* Flyway
* DBeaver

## Testes

* JUnit 5
* Mockito
* MockMvc
* Spring Boot Test
* Spring Security Test

---

# Arquitetura

O projeto segue uma arquitetura de **monólito modular por funcionalidade**.

A estrutura atual é organizada em módulos como:

```text
com.gamevault
├── avaliacao
├── favorito
├── listadesejos
├── shared
└── user
```

Cada funcionalidade utiliza, conforme necessário, uma estrutura semelhante a:

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

As regras de negócio permanecem concentradas na camada de serviço, enquanto os controllers são responsáveis pela comunicação HTTP.

A integração com a RAWG será isolada em um cliente próprio para evitar acoplamento direto entre a API externa e as demais funcionalidades do sistema.

---

# Persistência

O catálogo completo de jogos **não é persistido localmente**.

Os jogos são identificados internamente pelo ID fornecido pela RAWG.

O banco de dados do GameVault possui atualmente:

```text
users
favorites
wishlist
reviews
```

O Flyway controla a evolução do schema através de migrations versionadas.

A migration inicial está localizada em:

```text
src/main/resources/db/migration/V1__create_initial_schema.sql
```

O Hibernate utiliza:

```yaml
ddl-auto: validate
```

Dessa forma:

* o Flyway controla o schema;
* o Hibernate apenas valida a compatibilidade entre entidades e banco.

---

## Regras de integridade implementadas

* `username` deve ser único.
* `email` deve ser único.
* Um jogo não pode aparecer duas vezes nos Favoritos do mesmo usuário.
* Um jogo não pode aparecer duas vezes na Lista de Desejos do mesmo usuário.
* Um usuário pode possuir apenas uma avaliação por jogo.
* Avaliações aceitam notas inteiras de 1 a 5.
* Um jogo pode estar simultaneamente nos Favoritos e na Lista de Desejos.
* A exclusão de um usuário remove automaticamente seus Favoritos, itens da Lista de Desejos e Avaliações através de `ON DELETE CASCADE`.

---

# Autenticação e segurança

O GameVault utiliza **autenticação baseada em sessão HTTP com Spring Security**.

Após o login bem-sucedido, o servidor mantém a autenticação através da sessão e do cookie `JSESSIONID`.

O usuário autenticado é representado internamente por `UsuarioPrincipal`.

As funcionalidades privadas utilizam:

```java
@AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal
```

para obter o usuário da sessão.

Dessa forma, os endpoints privados não recebem mais um `usuarioId` fornecido pelo cliente.

Exemplo:

```text
/api/usuarios/me/favoritos
```

em vez de:

```text
/api/usuarios/{usuarioId}/favoritos
```

Isso evita que um usuário tente acessar dados pertencentes a outro usuário simplesmente alterando um ID na URL.

---

## CSRF

O Spring Security mantém proteção contra **CSRF** habilitada.

O endpoint:

```text
GET /api/csrf
```

pode ser utilizado pelo cliente para obter as informações necessárias para requisições protegidas por CSRF.

Requisições que alteram estado, como `POST`, `PUT` e `DELETE`, devem utilizar um token CSRF válido quando aplicável.

---

## Endpoints de autenticação

| Método | Endpoint           | Descrição                       | Resposta esperada |
| ------ | ------------------ | ------------------------------- | ----------------- |
| `GET`  | `/api/csrf`        | Obtém informações do token CSRF | `200 OK`          |
| `POST` | `/api/auth/login`  | Autentica o usuário             | `204 No Content`  |
| `POST` | `/api/auth/logout` | Encerra a sessão autenticada    | `204 No Content`  |

O login utiliza os parâmetros:

```text
email
senha
```

Em caso de credenciais inválidas:

```text
401 Unauthorized
```

O logout:

* invalida a sessão;
* limpa a autenticação;
* remove o cookie `JSESSIONID`.

---

# Usuários e gerenciamento de conta

O módulo `user` implementa atualmente:

```text
user
├── controller
├── dto
├── entity
├── exception
├── repository
├── security
└── service
```

## Regras implementadas

* Cadastro de usuário.
* Validação de username único.
* Validação de e-mail único.
* Validação e codificação da senha.
* Consulta do próprio perfil.
* Alteração de nome.
* Alteração de username.
* Alteração de e-mail.
* Alteração da URL da imagem de perfil.
* Alteração de senha.
* Validação da senha atual antes da troca.
* Exclusão da própria conta.
* Encerramento da sessão após a exclusão.
* Remoção automática dos dados relacionados através de `ON DELETE CASCADE`.

---

## Endpoints de usuário

| Método   | Endpoint                 | Descrição                 | Resposta esperada |
| -------- | ------------------------ | ------------------------- | ----------------- |
| `POST`   | `/api/usuarios`          | Cadastra um usuário       | `201 Created`     |
| `GET`    | `/api/usuarios/me`       | Consulta o próprio perfil | `200 OK`          |
| `PUT`    | `/api/usuarios/me`       | Atualiza o próprio perfil | `200 OK`          |
| `PUT`    | `/api/usuarios/me/senha` | Altera a própria senha    | `204 No Content`  |
| `DELETE` | `/api/usuarios/me`       | Exclui a própria conta    | `204 No Content`  |

O cadastro é público.

Os demais endpoints de gerenciamento exigem uma sessão autenticada.

---

# Favoritos

A funcionalidade de Favoritos está concluída no back-end.

Estrutura:

```text
favorito
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

## Regras implementadas

* Adicionar um jogo aos Favoritos.
* Listar os Favoritos do usuário autenticado.
* Remover um jogo dos Favoritos.
* Impedir duplicidade.
* Retornar erro controlado quando um Favorito não existe.
* Garantir a unicidade também no banco de dados.
* Associar todas as operações exclusivamente ao usuário autenticado.

## Endpoints

| Método   | Endpoint                                  | Descrição                      | Resposta esperada |
| -------- | ----------------------------------------- | ------------------------------ | ----------------- |
| `POST`   | `/api/usuarios/me/favoritos`              | Adiciona um jogo aos Favoritos | `201 Created`     |
| `GET`    | `/api/usuarios/me/favoritos`              | Lista os Favoritos             | `200 OK`          |
| `DELETE` | `/api/usuarios/me/favoritos/{rawgGameId}` | Remove um jogo dos Favoritos   | `204 No Content`  |

---

# Lista de Desejos

A funcionalidade de Lista de Desejos está concluída no back-end.

Estrutura:

```text
listadesejos
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

## Regras implementadas

* Adicionar um jogo à Lista de Desejos.
* Listar os jogos da Lista de Desejos.
* Remover um jogo.
* Impedir duplicidade.
* Retornar erro controlado quando um item não existe.
* Garantir unicidade também no banco.
* Associar todas as operações ao usuário autenticado.
* Permitir que o mesmo jogo esteja simultaneamente nos Favoritos e na Lista de Desejos.

## Endpoints

| Método   | Endpoint                                      | Descrição                           | Resposta esperada |
| -------- | --------------------------------------------- | ----------------------------------- | ----------------- |
| `POST`   | `/api/usuarios/me/lista-desejos`              | Adiciona um jogo à Lista de Desejos | `201 Created`     |
| `GET`    | `/api/usuarios/me/lista-desejos`              | Lista os jogos da Lista de Desejos  | `200 OK`          |
| `DELETE` | `/api/usuarios/me/lista-desejos/{rawgGameId}` | Remove um jogo da Lista de Desejos  | `204 No Content`  |

---

# Avaliações

A funcionalidade de Avaliações está concluída no back-end.

Estrutura:

```text
avaliacao
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

## Regras implementadas

* Permitir notas entre 1 e 5.
* Garantir apenas uma avaliação por usuário e jogo.
* Atualizar a avaliação existente quando o usuário muda sua nota.
* Manter o mesmo registro durante a atualização.
* Consultar a avaliação do usuário autenticado.
* Remover uma avaliação.
* Calcular a média das avaliações de um jogo.
* Contar a quantidade de avaliações.
* Representar jogos sem avaliações com média ausente.
* Rejeitar notas inválidas.
* Associar avaliações privadas exclusivamente ao usuário autenticado.

## Endpoints

| Método   | Endpoint                                    | Descrição                                 | Resposta esperada |
| -------- | ------------------------------------------- | ----------------------------------------- | ----------------- |
| `PUT`    | `/api/usuarios/me/avaliacoes/{rawgGameId}`  | Cria ou atualiza uma avaliação            | `200 OK`          |
| `GET`    | `/api/usuarios/me/avaliacoes/{rawgGameId}`  | Consulta a própria avaliação              | `200 OK`          |
| `DELETE` | `/api/usuarios/me/avaliacoes/{rawgGameId}`  | Remove a própria avaliação                | `204 No Content`  |
| `GET`    | `/api/jogos/{rawgGameId}/avaliacoes/resumo` | Consulta média e quantidade de avaliações | `200 OK`          |

O resumo das avaliações de um jogo é público.

Um jogo sem avaliações é representado conceitualmente como:

```json
{
  "rawgGameId": 3498,
  "media": null,
  "quantidade": 0
}
```

A ausência de avaliações não é confundida com uma avaliação de nota zero.

---

# Tratamento de erros

A API utiliza tratamento global de exceções com:

```java
@RestControllerAdvice
```

e respostas no padrão:

```text
ProblemDetail
```

Entre os cenários tratados estão:

* recurso inexistente — `404 Not Found`;
* dados de usuário duplicados — `409 Conflict`;
* Favorito duplicado — `409 Conflict`;
* item duplicado na Lista de Desejos — `409 Conflict`;
* senha inválida — `400 Bad Request`;
* avaliação inválida — `400 Bad Request`;
* validação de requisição — `400 Bad Request`;
* acesso sem autenticação — `401 Unauthorized`.

---

# Estratégia de testes

O projeto utiliza diferentes níveis de testes.

## Testes de persistência

Validam:

* persistência de usuários;
* restrições de unicidade;
* relacionamentos entre usuários e demais entidades;
* Favoritos duplicados;
* itens duplicados na Lista de Desejos;
* avaliações duplicadas;
* intervalo permitido para notas;
* consultas e agregações de avaliações.

---

## Testes unitários

Os serviços são testados com JUnit e Mockito.

Entre as regras validadas estão:

* cadastro de usuário;
* validação e codificação de senha;
* atualização de perfil;
* alteração de senha;
* exclusão de conta;
* Favoritos;
* Lista de Desejos;
* Avaliações;
* cenários de erro.

---

## Testes MVC

Os controllers são testados com `MockMvc`.

São validados:

* status HTTP;
* DTOs;
* validação de requisições;
* tratamento de exceções;
* autenticação;
* autorização;
* CSRF;
* utilização do usuário autenticado.

---

## Testes de integração

Os fluxos de integração carregam o contexto completo do Spring e utilizam o MySQL real do ambiente de desenvolvimento.

O fluxo validado percorre:

```text
HTTP
 ↓
Spring Security
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
MySQL
```

Entre os fluxos validados estão:

### Autenticação

* cadastro público;
* login;
* criação da sessão;
* rejeição de credenciais inválidas;
* proteção de endpoints privados;
* logout;
* invalidação da sessão;
* CSRF.

### Perfil

* consulta do próprio perfil;
* atualização do perfil;
* persistência das alterações;
* proteção contra acesso não autenticado.

### Alteração de senha

* validação da senha atual;
* geração de novo hash;
* rejeição da senha antiga após a alteração;
* autenticação com a nova senha.

### Exclusão de conta

* remoção do usuário;
* remoção dos Favoritos;
* remoção da Lista de Desejos;
* remoção das Avaliações;
* validação real do `ON DELETE CASCADE`;
* invalidação da sessão após a exclusão.

### Favoritos

* adição;
* persistência;
* listagem;
* remoção;
* prevenção de duplicidade;
* associação ao usuário autenticado.

### Lista de Desejos

* adição;
* persistência;
* listagem;
* remoção;
* prevenção de duplicidade;
* coexistência do mesmo jogo nos Favoritos e na Lista de Desejos;
* associação ao usuário autenticado.

### Avaliações

* criação;
* persistência;
* atualização da nota;
* manutenção do mesmo registro;
* consulta;
* média;
* quantidade;
* remoção;
* associação ao usuário autenticado.

A suíte completa é executada com:

```powershell
mvn test
```

e atualmente deve finalizar com:

```text
BUILD SUCCESS
```

---

# Executando o banco com Docker

Crie um arquivo `.env` na raiz do projeto:

```env
MYSQL_ROOT_PASSWORD=sua_senha_root
MYSQL_USER=seu_usuario
MYSQL_PASSWORD=sua_senha
```

> O arquivo `.env` não deve ser versionado.

Suba o banco:

```bash
docker compose up -d
```

Verifique:

```bash
docker ps
```

Para interromper:

```bash
docker compose stop
```

Para iniciar novamente:

```bash
docker compose start
```

Para remover os containers sem apagar os volumes:

```bash
docker compose down
```

> `docker compose down -v` também remove os volumes e apaga os dados armazenados.

---

# Configuração da aplicação

A aplicação utiliza as credenciais definidas no `.env`.

Exemplo:

```env
MYSQL_ROOT_PASSWORD=sua_senha_root
MYSQL_USER=seu_usuario
MYSQL_PASSWORD=sua_senha
```

Por padrão:

```text
host: localhost
porta: 3306
database: gamevault
```

Também podem ser configuradas:

```env
DB_HOST=localhost
DB_PORT=3306
```

A integração com a RAWG utilizará:

```env
RAWG_API_KEY=sua_chave
```

> Nunca versione o arquivo `.env` nem credenciais reais.

---

# Estado atual do desenvolvimento

## Concluído

### Infraestrutura

* Configuração inicial do Spring Boot.
* Java 21.
* Maven.
* MySQL 8.4.
* Docker.
* Docker Compose.
* Flyway.
* Migration inicial.
* Entidades JPA.
* Repositories Spring Data JPA.
* Tratamento global de exceções.

### Segurança

* Spring Security.
* Autenticação por sessão.
* Login por e-mail e senha.
* Logout.
* `UsuarioPrincipal`.
* Proteção dos endpoints privados.
* Utilização do usuário autenticado nas regras privadas.
* Proteção CSRF.
* Endpoint para obtenção do token CSRF.

### Usuários

* Cadastro.
* Validação de username e e-mail únicos.
* Codificação de senha.
* Consulta do próprio perfil.
* Atualização do perfil.
* Alteração de senha.
* Validação da senha atual.
* Exclusão da conta.
* Invalidação da sessão após exclusão.
* Remoção em cascata dos dados relacionados.

### Favoritos

* Adição.
* Listagem.
* Remoção.
* Prevenção de duplicidade.
* Associação ao usuário autenticado.
* Testes unitários, MVC e integração.

### Lista de Desejos

* Adição.
* Listagem.
* Remoção.
* Prevenção de duplicidade.
* Associação ao usuário autenticado.
* Coexistência com Favoritos.
* Testes unitários, MVC e integração.

### Avaliações

* Criação e atualização.
* Consulta.
* Remoção.
* Média.
* Quantidade.
* Notas entre 1 e 5.
* Uma avaliação por usuário e jogo.
* Associação ao usuário autenticado.
* Endpoint público de resumo.
* Testes unitários, MVC e integração.

### Validação

* Fluxos principais validados ponta a ponta.
* Suíte completa validada com Maven.
* Build finalizado com `BUILD SUCCESS`.

---

# Próximos passos

* Implementar a integração com a API RAWG.
* Implementar busca de jogos.
* Implementar listagem de jogos populares.
* Implementar lançamentos recentes.
* Implementar jogos mais bem avaliados.
* Integrar os dados da RAWG com Favoritos, Lista de Desejos e Avaliações.
* Preparar posteriormente a camada de front-end.

---

# Autor

**Isaque Costa da Cunha**
