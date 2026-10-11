# money-controll

API REST para controle de despesas pessoais, desenvolvida em Java com Spring Boot. O projeto foi criado para praticar a construção de uma API completa, com arquitetura em camadas, validação de dados, persistência e testes automatizados.

## Funcionalidades

- Cadastro de despesas
- Listagem de todas as despesas
- Busca de despesa por ID
- Atualização de despesas
- Remoção de despesas
- Validação dos dados de entrada
- Tratamento centralizado de erros, incluindo despesa não encontrada

## Tecnologias utilizadas

- Java 25
- Spring Boot 4 (Web MVC, Validation, Data JPA)
- PostgreSQL
- H2 (banco em memória para os testes)
- JUnit 5 e Mockito
- Lombok
- Maven

## Estrutura

```
src/main/java/com/example/money_manager/
├── controller/   # Endpoints REST
├── service/      # Regras de negócio
├── repository/   # Acesso a dados com Spring Data JPA
├── model/        # Entidade Expense e enum Category
├── dto/          # ExpenseRequest e ExpenseResponse
└── exception/    # Exceções e tratamento global de erros
```

A API não expõe a entidade diretamente: as requisições e respostas usam DTOs, e os valores monetários são representados com `BigDecimal`.

## Endpoints

| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/api/expenses` | Cria uma despesa |
| GET | `/api/expenses` | Lista todas as despesas |
| GET | `/api/expenses/{id}` | Busca uma despesa por ID |
| PUT | `/api/expenses/{id}` | Atualiza uma despesa |
| DELETE | `/api/expenses/{id}` | Remove uma despesa |

Exemplo de corpo para criação ou atualização:

```json
{
  "description": "Almoço",
  "amount": 35.90,
  "date": "2026-09-10",
  "category": "ALIMENTACAO"
}
```

Categorias disponíveis: `ALIMENTACAO`, `LAZER`, `SAUDE`, `TRANSPORTE` e `OUTROS`.

Regras de validação: `description` é obrigatória, `amount` deve ser positivo, `date` e `category` são obrigatórios.

## Executando o projeto

Pré-requisitos: Java 25 e um banco PostgreSQL chamado `moneymanager` em `localhost:5432`.

Clone o repositório:

```bash
git clone https://github.com/fesqueira/money-controll.git
cd money-controll
```

Ajuste os dados de conexão em `src/main/resources/application.properties` e execute:

```bash
./mvnw spring-boot:run
```

A API fica disponível em `http://localhost:8080`.

## Testes

O projeto possui testes unitários da camada de serviço com JUnit 5 e Mockito, além de testes de integração do controller usando banco H2 em memória.

```bash
./mvnw test
```

## Próximos passos

- Documentação da API com OpenAPI/Swagger
- Autenticação

## Autor

Felipe Siqueira
