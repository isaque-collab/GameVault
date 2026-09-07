# GameVault

O **GameVault** é uma aplicação web para descoberta, organização e avaliação de jogos. O projeto utiliza a API pública da **RAWG** como fonte externa do catálogo e mantém no banco de dados apenas os dados próprios da aplicação, como usuários, favoritos, wishlist e avaliações.

> **Status:** em desenvolvimento  
> **Etapa atual:** Desenvolvimento Back-end — infraestrutura e camada de persistência concluídas.

## Funcionalidades planejadas para a V1

- Cadastro, autenticação e gerenciamento de conta.
- Perfil do usuário com foto.
- Busca de jogos.
- Listagem de jogos populares.
- Lançamentos recentes.
- Jogos mais bem avaliados.
- Favoritos.
- Wishlist.
- Avaliação de jogos com notas de 1 a 5.
- Integração com a API RAWG.

## Tecnologias

### Back-end

- Java 21
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Security
- Maven

### Banco de dados e infraestrutura

- MySQL 8.4 LTS
- Docker
- Docker Compose
- Flyway
- DBeaver

### Testes

- JUnit 5
- Spring Boot Test

## Arquitetura

O projeto segue uma arquitetura de **monólito modular por funcionalidade**.

```text
com.gamevault
├── auth
├── user
├── game
├── favorite
├── wishlist
├── review
└── shared
```

A separação interna dos módulos é construída conforme cada funcionalidade é implementada, utilizando camadas como:

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

A integração com a RAWG será isolada em um cliente próprio, evitando acoplamento direto entre a API externa e as demais camadas da aplicação.

## Persistência

O catálogo de jogos **não é persistido localmente**. Os jogos são identificados pelo ID fornecido pela RAWG.

O banco de dados do GameVault contém atualmente:

```text
users
favorites
wishlist
reviews
```

O Flyway controla a evolução do schema por meio de migrations versionadas.

### Regras já implementadas no banco

- `username` único.
- `email` único.
- Um mesmo jogo não pode aparecer duas vezes nos favoritos do mesmo usuário.
- Um mesmo jogo não pode aparecer duas vezes na wishlist do mesmo usuário.
- Um usuário pode possuir apenas uma avaliação por jogo.
- Avaliações aceitam notas inteiras de 1 a 5.
- A exclusão de um usuário remove seus favoritos, itens da wishlist e avaliações por `ON DELETE CASCADE`.
- Um jogo pode estar simultaneamente nos favoritos e na wishlist do mesmo usuário.

## Estrutura de banco atual

A migration inicial está em:

```text
src/main/resources/db/migration/V1__create_initial_schema.sql
```

Ela cria as tabelas:

```text
users
favorites
wishlist
reviews
```

O Hibernate está configurado com:

```yaml
ddl-auto: validate
```

Assim, o schema é controlado pelo Flyway e o Hibernate apenas valida se as entidades correspondem ao banco.

## Executando o banco com Docker

Crie um arquivo `.env` na raiz do projeto:

```env
MYSQL_ROOT_PASSWORD=sua_senha_root
MYSQL_USER=seu_usuario
MYSQL_PASSWORD=sua_senha
```

> O arquivo `.env` não deve ser versionado.

Suba o MySQL:

```bash
docker compose up -d
```

Verifique se o container está em execução:

```bash
docker ps
```

Para interromper o container:

```bash
docker compose stop
```

Para iniciar novamente:

```bash
docker compose start
```

Para remover o container sem apagar o volume:

```bash
docker compose down
```

> `docker compose down -v` remove também o volume persistente e apaga os dados do banco.

## Configurando a aplicação

A aplicação utiliza variáveis de ambiente para não manter credenciais no código.

Configure:

```text
DB_URL=jdbc:mysql://localhost:3306/gamevault
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha
```

A chave da RAWG será necessária quando a integração com a API for implementada:

```text
RAWG_API_KEY=sua_chave
```

## Executando os testes

Com o MySQL do Docker em execução e as variáveis de banco disponíveis no ambiente:

### Windows

```powershell
.\mvnw.cmd clean verify
```

O build deve terminar com:

```text
BUILD SUCCESS
```

## Testes de integração atuais

A camada de persistência já possui testes de integração para:

- persistência e consulta de usuários;
- relacionamento entre favoritos e usuários;
- bloqueio de favoritos duplicados;
- relacionamento entre wishlist e usuários;
- bloqueio de itens duplicados na wishlist;
- persistência de avaliações;
- bloqueio de avaliações duplicadas;
- rejeição de notas abaixo de 1;
- rejeição de notas acima de 5.

Os testes utilizam transações para que os dados criados durante a execução sejam revertidos ao final de cada teste.

## Estado atual do desenvolvimento

### Concluído

- Configuração inicial do projeto Spring Boot.
- Java 21 e Maven.
- MySQL 8.4 em Docker.
- Docker Compose.
- Flyway.
- Migration inicial.
- Entidades JPA:
  - `User`
  - `Favorite`
  - `Wishlist`
  - `Review`
- Repositories Spring Data JPA.
- Testes de integração da camada de persistência.
- Validação do build com Maven.

### Próximos passos

- Cadastro de usuário.
- Autenticação.
- Spring Security.
- JWT e refresh token.
- Proteção de endpoints.
- Integração com a API RAWG.
- Implementação das regras de Favoritos, Wishlist e Avaliações na camada de serviço e API REST.

## Versionamento

O projeto utiliza **Conventional Commits**, com mensagens em português.

Exemplos:

```text
feat: adiciona entidade de favoritos
fix: corrige mapeamento do campo de avaliação
test: adiciona testes de integração do repositório de avaliações
refactor: reorganiza camada de persistência
docs: atualiza documentação do projeto
chore: ajusta configuração do ambiente
```

## Autor

**Isaque Costa da Cunha**

Estudante de Engenharia de Software com foco em desenvolvimento back-end Java.
