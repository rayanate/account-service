# Account Service

A Spring Boot microservice for managing financial accounts with idempotent transaction processing, in-memory H2 database, and structured JSON logging.

## Overview

The Account Service provides RESTful APIs for:
- **Idempotent transaction application** — Gateway-safe with automatic deduplication via eventId
- **Balance computation** — Real-time Σ credits − Σ debits (no stored running balance)
- **Account details** — Account info with recent transaction history
- **Health monitoring** — Actuator health with database status
- **H2 database console** — Web UI for SQL inspection

**Key Features:**
- ✓ Idempotent POST (retry-safe)
- ✓ JSON structured logging (Logback + Logstash encoder)
- ✓ H2 in-memory database with DDL auto-update
- ✓ JPA/Hibernate repositories
- ✓ Spring Boot Actuator health checks
- ✓ Docker & Docker Compose ready

## Prerequisites

- **Java 17+** (tested with OpenJDK 17.0.10)
- **Maven 3.9+** (or use bundled `mvnw`)
- **Docker & Docker Compose** (optional, for containerized deployment)

## Quick Start

### 1. Clone and Build

```bash
cd accountService
./mvnw clean install
```

On Windows:
```powershell
.\mvnw clean install
```

### 2. Run Locally

```bash
java -jar target/accountService-0.0.1-SNAPSHOT.jar
```

The application starts on **port 8082** (configured in `src/main/resources/application.properties`).

### 3. Verify Health

```bash
curl http://localhost:8082/health

# Or with PowerShell
Invoke-RestMethod http://localhost:8082/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

## API Endpoints

### POST /accounts/{accountId}/transactions
Apply a transaction (idempotent by eventId).

**Request:**
```bash
curl -X POST http://localhost:8082/accounts/acc123/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "evt-001",
    "amount": 123.45
  }'
```

**Response (201 Created):**
```json
{
  "eventId": "evt-001",
  "accountId": "acc123",
  "amount": 123.45,
  "appliedAt": "2026-06-13T15:30:00.123456"
}
```

**Idempotency:** Sending the same `eventId` twice returns success with the same transaction (no duplicate stored).

---

### GET /accounts/{accountId}/balance
Retrieve computed account balance (Σ all transaction amounts).

**Request:**
```bash
curl http://localhost:8082/accounts/acc123/balance
```

**Response (200 OK):**
```json
{
  "accountId": "acc123",
  "balance": 123.45
}
```

---

### GET /accounts/{accountId}
Retrieve account details with recent transactions.

**Request:**
```bash
curl http://localhost:8082/accounts/acc123
```

**Response (200 OK):**
```json
{
  "accountId": "acc123",
  "name": "John Doe",
  "balance": 123.45,
  "createdAt": "2026-06-13T14:00:00",
  "recentTransactions": [
    {
      "eventId": "evt-002",
      "accountId": "acc123",
      "amount": 50.00,
      "appliedAt": "2026-06-13T15:35:00"
    },
    {
      "eventId": "evt-001",
      "accountId": "acc123",
      "amount": 123.45,
      "appliedAt": "2026-06-13T15:30:00"
    }
  ]
}
```

---

### GET /health
Health check with database status (Actuator).

**Request:**
```bash
curl http://localhost:8082/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", ... },
    "diskSpace": { "status": "UP", ... },
    "livenessState": { "status": "UP" },
    "readinessState": { "status": "UP" }
  }
}
```

---

### GET /h2-console
H2 Database Web Console (browser).

**URL:** `http://localhost:8082/h2-console`

**Login Credentials:**
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

## Project Structure

```
accountService/
├── src/main/java/com/charlesschwab/accountService/
│   ├── AccountServiceApplication.java       # Spring Boot entry point
│   ├── config/
│   │   └── H2ConsoleConfig.java            # H2 servlet registration
│   ├── controller/
│   │   └── TransactionController.java      # REST endpoints
│   ├── service/
│   │   └── TransactionService.java         # Business logic & idempotency
│   ├── repository/
│   │   ├── AppliedTransactionRepository.java
│   │   └── AccountRepository.java
│   ├── entity/
│   │   ├── AppliedTransaction.java         # Transaction entity (eventId = PK)
│   │   └── Account.java                    # Account entity
│   └── dto/
│       ├── TransactionRequest.java
│       ├── TransactionResponse.java
│       ├── BalanceResponse.java
│       └── AccountDetailResponse.java
│
├── src/main/resources/
│   ├── application.properties               # Spring Boot config
│   └── logback-spring.xml                   # JSON logging config (Logstash encoder)
│
├── src/test/java/
│   └── com/charlesschwab/accountService/
│       └── AccountServiceApplicationTests.java
│
├── pom.xml                                  # Maven dependencies
├── Dockerfile                               # Docker build (multi-stage)
├── docker-compose.yml                       # Docker Compose orchestration
├── .dockerignore                            # Docker build context exclusions
├── DOCKER_README.md                         # Docker usage guide
└── README.md                                # This file
```

## Configuration

### application.properties

```properties
spring.application.name=accountService
server.port=8082
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
management.endpoints.web.base-path=/
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=always
```

**Key Settings:**
- `server.port=8082` — Application port
- `spring.datasource.url=jdbc:h2:mem:testdb` — H2 in-memory database
- `spring.jpa.hibernate.ddl-auto=update` — Auto-create/update tables on startup
- `management.endpoints.web.exposure.include=health` — Expose health endpoint
- `management.endpoint.health.show-details=always` — Show detailed health info

### Persistent H2 Database (Optional)

To use file-based instead of in-memory, change:
```properties
spring.datasource.url=jdbc:h2:file:./data/testdb
```

### Logging

Logs are emitted as **structured JSON** via Logback + Logstash encoder. Example log line:
```json
{"@timestamp":"2026-06-13T15:30:00.123Z","level":"INFO","thread":"main","logger":"com.charlesschwab.accountService.service.TransactionService","message":"applied.transaction saved eventId=evt-001 accountId=acc123 amount=123.45","app":"accountService"}
```

See `src/main/resources/logback-spring.xml` for configuration.

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 4.1.0 | Framework |
| Spring Data JPA | 4.1.0 | ORM & repositories |
| H2 Database | 2.4.240 | In-memory DB |
| Logback | 1.5.34 | Logging |
| Logstash Encoder | 7.4 | JSON structured logs |
| Actuator | 4.1.0 | Health & metrics |

See `pom.xml` for full dependency list.

## Database Schema

### Table: APPLIED_TRANSACTIONS
| Column | Type | Notes |
|--------|------|-------|
| EVENT_ID | VARCHAR | Primary Key (idempotency key) |
| ACCOUNT_ID | VARCHAR | Foreign key to ACCOUNTS |
| AMOUNT | DECIMAL(19,2) | Transaction amount (± for credit/debit) |
| APPLIED_AT | TIMESTAMP | Creation timestamp |

### Table: ACCOUNTS
| Column | Type | Notes |
|--------|------|-------|
| ACCOUNT_ID | VARCHAR | Primary Key |
| NAME | VARCHAR | Account holder name |
| CREATED_AT | TIMESTAMP | Account creation timestamp |

## Idempotency & Retry Safety

The transaction endpoint is **idempotent by design**:

1. **First request** (eventId=evt-001):
   - Transaction saved to database
   - Returns HTTP 201 with transaction details

2. **Duplicate request** (same eventId=evt-001):
   - Constraint violation caught internally
   - Existing transaction looked up and returned
   - Returns HTTP 201 (same response as first request)
   - **No duplicate stored in database**

This makes the service safe for gateway retries and distributed systems where requests may be retried on transient failures.

## Running with Docker

### Quick Start
```bash
docker-compose up --build
```

### View Logs
```bash
docker-compose logs -f
```

### Stop
```bash
docker-compose down
```

See `DOCKER_README.md` for full Docker Compose guide.

## Example Workflow

```bash
# 1. Apply a transaction
curl -X POST http://localhost:8082/accounts/user123/transactions \
  -H "Content-Type: application/json" \
  -d '{"eventId":"tx-1001","amount":500.00}'

# 2. Apply another transaction (same account, different event)
curl -X POST http://localhost:8082/accounts/user123/transactions \
  -H "Content-Type: application/json" \
  -d '{"eventId":"tx-1002","amount":-100.00}'

# 3. Check balance
curl http://localhost:8082/accounts/user123/balance
# Returns: {"accountId":"user123","balance":400.00}

# 4. Get account details with recent transactions
curl http://localhost:8082/accounts/user123

# 5. Retry first transaction (idempotent — no error, no duplicate)
curl -X POST http://localhost:8082/accounts/user123/transactions \
  -H "Content-Type: application/json" \
  -d '{"eventId":"tx-1001","amount":500.00}'
# Returns same result as step 1
```

## Development

### Build Locally
```bash
./mvnw clean package
```

### Run Tests
```bash
./mvnw test
```

### Run Specific Test
```bash
./mvnw test -Dtest=AccountServiceApplicationTests
```

### IDE Setup (IntelliJ)
1. Open project
2. File → Project Structure → Project SDK → Set to Java 17+
3. Maven tool window → Reload All Maven Projects
4. Right-click `AccountServiceApplication.java` → Run

### Rebuild After Code Changes
```bash
./mvnw install -DskipTests
```

## Troubleshooting

### Port 8082 Already in Use
```powershell
# Find and kill the process
Get-NetTCPConnection -LocalPort 8082 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

### H2 Console Returns 404
- Ensure `spring.h2.console.enabled=true` in `application.properties`
- Verify app is listening on the correct port (see logs)
- Rebuild and restart: `./mvnw install -DskipTests && java -jar target/accountService-0.0.1-SNAPSHOT.jar`

### Database Connection Fails
- Check `spring.datasource.url` and credentials in `application.properties`
- Ensure H2 dependency is present in classpath

### JSON Logs Not Appearing
- Verify `src/main/resources/logback-spring.xml` exists
- Rebuild: `./mvnw clean install`
- Check Maven output for logstash-logback-encoder dependency download

## Performance Considerations

- **Balance computation**: O(n) where n = transaction count (no caching; fresh read on each request)
- **Recent transactions**: Limited to 10 records, paginated
- **Idempotency**: O(1) database constraint check; ~2-5ms on typical hardware
- **H2 in-memory**: Single-JVM only; not suitable for multi-instance deployments without external DB

For production:
1. Replace H2 with PostgreSQL/MySQL
2. Add caching (Redis) for frequently-accessed balances
3. Use connection pooling (HikariCP — already included)
4. Configure horizontal scaling with external database

## API Error Handling

All endpoints return appropriate HTTP status codes:

| Status | Meaning |
|--------|---------|
| 200 OK | Successful GET/HEAD operation |
| 201 Created | Transaction successfully applied |
| 400 Bad Request | Invalid input (missing fields, etc.) |
| 404 Not Found | Resource not found |
| 500 Internal Server Error | Unexpected error; see logs |

Error responses include a brief message:
```json
{
  "timestamp": "2026-06-13T15:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "..."
}
```

## Monitoring & Observability

### Actuator Endpoints
- `/health` — Application health with component details
- `/health/liveness` — Liveness probe
- `/health/readiness` — Readiness probe

### Metrics & Logging
- **Structured JSON logs** — All events logged as JSON (Logstash format)
- **Transaction lifecycle** — Request received, DB save, duplicate detection, balance computation
- **Performance indicators** — Message count, balance value, etc.

### Example Log Events
```json
{"message":"request.applyTransaction accountId=acc123 eventId=evt-1 amount=100.50"}
{"message":"applied.transaction saved eventId=evt-1 accountId=acc123 amount=100.50"}
{"message":"computed.balance accountId=acc123 balance=100.50 transactions=1"}
{"message":"duplicate.eventId detected: evt-1 - returning existing record"}
```

## License

(Add your license information here, e.g., MIT, Apache-2.0, etc.)

## Contact & Support

For questions or issues:
- Check `DOCKER_README.md` for Docker-related questions
- Review application logs for error details
- See troubleshooting section above

---

**Last Updated:** June 13, 2026  
**Spring Boot Version:** 4.1.0  
**Java Version:** 17+

