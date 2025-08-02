# Eagle Bank API

A Spring Boot REST API for Eagle Bank with Keycloak integration, following Test-Driven Development (TDD) best practices.

## Features

- **Spring Boot 3.2.0** with Java 21
- **Keycloak OAuth2** integration for authentication and authorization
- **PostgreSQL** database with H2 for testing
- **Test-Driven Development (TDD)** approach
- **Dev Container** support for consistent development environment
- **Docker Compose** for local development
- **Comprehensive testing** with unit, integration, and end-to-end tests
- **OpenAPI 3.1** specification compliance

## Project Structure

```
eagle-bank-api-java/
├── .devcontainer/          # Dev container configuration
├── src/
│   ├── main/
│   │   ├── java/com/eaglebank/
│   │   │   ├── config/     # Security and application configuration
│   │   │   ├── controller/ # REST controllers
│   │   │   ├── domain/     # Domain models and entities
│   │   │   ├── dto/        # Data Transfer Objects
│   │   │   ├── exception/  # Global exception handling
│   │   │   ├── repository/ # Data access layer
│   │   │   └── service/    # Business logic layer
│   │   └── resources/
│   │       ├── db/         # Database initialization scripts
│   │       └── application.yml # Application configuration
│   └── test/
│       ├── java/com/eaglebank/
│       │   ├── controller/ # Controller tests
│       │   ├── domain/     # Domain model tests
│       │   ├── integration/ # Integration tests
│       │   └── service/    # Service layer tests
│       └── resources/      # Test configuration
├── docs/                   # OpenAPI specification
├── docker-compose.yml      # Local development environment
├── pom.xml                 # Maven dependencies
└── README.md              # This file
```

## Prerequisites

- Docker and Docker Compose
- Java 21
- Maven 3.8+
- VS Code with Dev Containers extension (recommended)

## Quick Start

### Using Dev Container (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd eagle-bank-api-java
   ```

2. **Open in VS Code with Dev Containers**
   - Install the "Dev Containers" extension in VS Code
   - Open the project folder
   - When prompted, click "Reopen in Container"

3. **Start the development environment**
   ```bash
   sudo docker-compose up -d
   ```

4. **Add the dev container to the same docker network where postgres and keycloak are running**
   **Example - sudo docker network connect eagle-bank-api-java_eagle-bank-network clever_albattani**
   ```bash
   sudo docker network connect <docker-network> <vs-code-dev-container-name>
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

## Development Environment

### Services

- **Spring Boot Application**: http://localhost:8080
- **Keycloak Admin Console**: http://localhost:8180
  - Username: `admin`
  - Password: `admin`
- **PostgreSQL**: localhost:5432
  - Database: `eagle_bank`
  - Username: `eagle_user`
  - Password: `eagle_password`

### Keycloak Setup

1. **Access Keycloak Admin Console**
   - URL: http://localhost:8180
   - Login with admin/admin

2. **Create Realm**
   - Click "Create Realm"
   - Name: `eagle-bank`

3. **Create Client**
   - Go to "Clients" → "Create"
   - Client ID: `eagle-bank-api`
   - Client Protocol: `openid-connect`
   - Access Type: `public`

4. **Configure Client**
   - Valid Redirect URIs: `http://localhost:8080/*`
   - Web Origins: `http://localhost:8080`
   - Save

5. **Create User**
   - Go to "Users" → "Add User"
   - Username: `testuser`
   - Email: `test@example.com`
   - Set password in "Credentials" tab

### Getting Authentication Token

To get an authentication token using Postman:

1. **Method**: POST
2. **URL**: `http://localhost:8180/realms/eagle-bank/protocol/openid-connect/token`
3. **Headers**: `Content-Type: application/x-www-form-urlencoded`
4. **Body** (form-data):
   - `grant_type`: `password`
   - `client_id`: `eagle-bank-api`
   - `username`: `testuser`
   - `password`: `Test1234`

**Example cURL command:**
```bash
curl -X POST "http://localhost:8180/realms/eagle-bank/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=eagle-bank-api&username=testuser&password=Test1234"
```

## API Endpoints

### Users
- `POST /api/v1/users` - Create a new user
- `GET /api/v1/users/{userId}` - Get user by ID
- `PATCH /api/v1/users/{userId}` - Update user
- `DELETE /api/v1/users/{userId}` - Delete user

### Bank Accounts
- `POST /api/v1/accounts` - Create a new bank account
- `GET /api/v1/accounts` - List all accounts
- `GET /api/v1/accounts/{accountNumber}` - Get account by number
- `PATCH /api/v1/accounts/{accountNumber}` - Update account
- `DELETE /api/v1/accounts/{accountNumber}` - Delete account

### Transactions
- `POST /api/v1/accounts/{accountNumber}/transactions` - Create transaction
- `GET /api/v1/accounts/{accountNumber}/transactions` - List transactions
- `GET /api/v1/accounts/{accountNumber}/transactions/{transactionId}` - Get transaction

## Testing

### Run All Tests
```bash
mvn test
```

### Run Unit Tests Only
```bash
mvn test -Dtest=*Test
```

### Run Integration Tests Only
```bash
mvn test -Dtest=*IntegrationTest
```

## TDD Approach

This project follows Test-Driven Development principles:

1. **Write a failing test** for the feature you want to implement
2. **Write the minimum code** to make the test pass
3. **Refactor the code** to improve design while keeping tests green
4. **Repeat** for the next feature

### Test Structure

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **End-to-End Tests**: Test complete user workflows

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    address_line3 VARCHAR(255),
    address_town VARCHAR(255) NOT NULL,
    address_county VARCHAR(255) NOT NULL,
    address_postcode VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_timestamp TIMESTAMP NOT NULL,
    updated_timestamp TIMESTAMP NOT NULL
);
```

### Bank Accounts Table
```sql
CREATE TABLE bank_accounts (
    account_number VARCHAR(255) PRIMARY KEY,
    sort_code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    account_type VARCHAR(255) NOT NULL,
    balance DECIMAL(10,2) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    created_timestamp TIMESTAMP NOT NULL,
    updated_timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Transactions Table
```sql
CREATE TABLE transactions (
    id VARCHAR(255) PRIMARY KEY,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    reference VARCHAR(255),
    bank_account_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    created_timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (bank_account_id) REFERENCES bank_accounts(account_number)
);
```

## Security

### Keycloak Integration

The application uses Keycloak for OAuth2 authentication:

- **JWT Token Validation**: Validates tokens from Keycloak
- **Role-Based Access Control**: Uses Keycloak roles for authorization
- **Stateless Authentication**: No server-side session storage

### Security Configuration

- **CORS**: Configured for local development
- **CSRF**: Disabled for API endpoints
- **Session Management**: Stateless (JWT-based)

## Monitoring and Health Checks

### Actuator Endpoints

- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

## Deployment

### (Optional) Generate Maven Wrapper

If your project does not already include the Maven Wrapper (`mvnw` and `.mvn/`), generate it with:

```bash
mvn -N io.takari:maven:wrapper

### Docker Build
```bash
mvn clean package
docker build -t eagle-bank-api .
```

### Docker Run
```bash
docker run -p 8080:8080 eagle-bank-api
```

## Contributing

1. **Follow TDD**: Write tests first
2. **Use meaningful commit messages**
3. **Keep functions small and focused**
4. **Add comprehensive documentation**
5. **Ensure all tests pass before committing**

## Troubleshooting

### Common Issues

1. **Keycloak Connection Issues**
   - Ensure Keycloak is running: `docker-compose ps`
   - Check Keycloak logs: `docker-compose logs keycloak`

2. **Database Connection Issues**
   - Ensure PostgreSQL is running: `docker-compose ps`
   - Check database logs: `docker-compose logs postgres`

3. **Port Conflicts**
   - Check if ports 8080, 8180, 5432 are available
   - Stop conflicting services or change ports in docker-compose.yml

### Logs

- **Application logs**: Check console output or logs directory
- **Docker logs**: `docker-compose logs <service-name>`
- **Database logs**: `docker-compose logs postgres`

## License

This project is licensed under the MIT License.