# Microservice de gestion des comptes bancaires

Reprise de la démarche des vidéos de Mohamed Youssfi :

1. [Développement d'un micro-service REST](https://www.youtube.com/watch?v=2-qIoZcvhAw)
2. [Connecteur GraphQL](https://www.youtube.com/watch?v=FsdR09jlqaE)

Le service expose **trois connecteurs** : API REST manuelle, Spring Data REST (avec projections) et GraphQL. La persistance utilise **H2 en mémoire**.

## Prérequis

- JDK 17+ (testé avec JDK 21)
- Maven Wrapper inclus (`./mvnw`)

## Lancer l'application

```bash
cd bank-account-service
./mvnw spring-boot:run
```

Au démarrage, un `CommandLineRunner` insère 3 clients et 10 comptes par client (test de la couche DAO). Les logs affichent les comptes persistés.

| Interface | URL |
|-----------|-----|
| API REST | http://localhost:8081/api/bankAccounts |
| Spring Data REST | http://localhost:8081/bankAccounts |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| OpenAPI JSON | http://localhost:8081/v3/api-docs |
| GraphiQL | http://localhost:8081/graphiql |
| Console H2 | http://localhost:8081/h2-console (JDBC URL : `jdbc:h2:mem:account-db`) |

## Architecture

```
web (REST + GraphQL)
  → service (métier)
    → mappers / dto
      → repositories (Spring Data JPA + Data REST)
        → entities (BankAccount, Customer)
```

Dans la vidéo, l'entité s'appelle **BankAccount** (compte bancaire : id, date, solde, devise, type).

## 1 à 4 — Projet, entité, repository, test DAO

Dépendances : Web, Spring Data JPA, H2, Lombok, Data REST, GraphQL, springdoc OpenAPI.

- Entité `BankAccount` + enum `AccountType` (`CURRENT_ACCOUNT`, `SAVING_ACCOUNT`)
- `BankAccountRepository extends JpaRepository<BankAccount, String>`
- Test DAO : `CommandLineRunner` au démarrage + test `BankAccountRepositoryTest`

## 5 et 6 — API REST et tests (Postman / curl)

Préfixe `/api` pour ne pas collisionner avec Spring Data REST.

| Méthode | URL | Description |
|---------|-----|-------------|
| GET | `/api/bankAccounts` | Liste des comptes |
| GET | `/api/bankAccounts/{id}` | Compte par id |
| POST | `/api/bankAccounts` | Création (`BankAccountRequestDTO`) |
| PUT | `/api/bankAccounts/{id}` | Mise à jour partielle |
| DELETE | `/api/bankAccounts/{id}` | Suppression |

Collection Postman : `postman/BankAccount-Microservice.postman_collection.json`

Exemples curl :

```bash
curl http://localhost:8081/api/bankAccounts
curl -X POST http://localhost:8081/api/bankAccounts \
  -H "Content-Type: application/json" \
  -d '{"balance":8000,"currency":"EUR","type":"SAVING_ACCOUNT"}'
```

## 7 — Documentation Swagger / OpenAPI

Ouvrir http://localhost:8081/swagger-ui.html puis tester GET / POST / PUT / DELETE.

## 8 — Spring Data REST et projections

`@RepositoryRestResource` sur le repository expose automatiquement HAL + pagination.

- Liste : http://localhost:8081/bankAccounts
- Pagination : http://localhost:8081/bankAccounts?page=0&size=2
- Recherche : http://localhost:8081/bankAccounts/search/byType?t=CURRENT_ACCOUNT
- Projection P1 (id, type) : http://localhost:8081/bankAccounts?projection=p1
- Projection P2 (id, balance, type) : http://localhost:8081/bankAccounts/{id}?projection=p2

## 9 et 10 — DTOs, Mapper, couche Service

- `BankAccountRequestDTO` : balance, currency, type
- `BankAccountResponseDTO` : id, createdAt, balance, currency, type
- `AccountMapper` : `BeanUtils.copyProperties`
- `AccountService` / `AccountServiceImpl` : création, mise à jour, consultation, suppression (transactionnel)

Le POST REST passe par la couche métier (DTO → entité → persistance → DTO).

## 11 — API GraphQL

Schéma : `src/main/resources/graphql/schema.graphqls`  
Contrôleur : `BankAccountGraphQLController`  
Exceptions métier : `GraphQLExceptionHandler`

### Requêtes

```graphql
query {
  accountsList { id balance currency type customer { name } }
}

query {
  bankAccountById(id: "REMPLACER_ID") { id balance type customer { name } }
}

query {
  customersList {
    id
    name
    bankAccounts { id balance type }
  }
}
```

### Mutations

```graphql
mutation($bankAccount: BankAccountDTO) {
  addAccount(bankAccount: $bankAccount) { id balance currency type }
}

mutation($id: String, $bankAccount: BankAccountDTO) {
  updateAccount(id: $id, bankAccount: $bankAccount) { id balance currency }
}

mutation($id: String) {
  deleteAccount(id: $id)
}
```

Variables d'exemple :

```json
{
  "id": "REMPLACER_ID",
  "bankAccount": {
    "balance": 4000,
    "currency": "USD",
    "type": "CURRENT_ACCOUNT"
  }
}
```

`@JsonProperty(WRITE_ONLY)` sur `BankAccount.customer` évite la boucle infinie JSON côté REST, sans empêcher GraphQL de naviguer dans le graphe.

## Tests automatisés

```bash
./mvnw test
```
