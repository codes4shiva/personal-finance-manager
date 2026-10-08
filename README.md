# Personal Finance Manager

A RESTful backend service built with Spring Boot to track income and expenses, organize custom and system categories, track savings goal progress, and generate financial summaries.

---

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.5.7 (Spring Web, Spring Data JPA, Spring Validation)
- **Security:** Spring Security 6 with stateless JWT authentication
- **JWT Library:** JJWT 0.12.6 (HMAC-SHA384)
- **Database:** In-memory H2 (default / testing) and PostgreSQL (production / Render)
- **Connection Pool:** HikariCP with prepared statement cache optimization for connection poolers
- **Testing & Coverage:** JUnit 5, MockMvc, JaCoCo (strictly enforcing $\ge 80\%$ line coverage)
- **Build Tool:** Apache Maven (via Maven Wrapper)

---

## Authentication Flow

Authentication uses JSON Web Tokens (JWT) stored in secure, tamper-resistant HTTP cookies:

1. **Registration:** `POST /api/auth/register` creates a user account.
2. **Login:** `POST /api/auth/login` validates credentials and sets two HttpOnly cookies:
   - `accessToken`: 1-hour expiration, scoped to path `/`, evaluated on protected API endpoints.
   - `refreshToken`: 10-day expiration, scoped to path `/api/auth`.
3. **Fallback Header:** In addition to cookies, requests can authenticate using the standard `Authorization: Bearer <token>` header.
4. **Cookie Security:** The cookie `secure` flag is configurable (`app.cookie.secure`). It defaults to `false` for local HTTP development and is set to `true` (HTTPS) in production.
5. **Logout:** `POST /api/auth/logout` requires authentication and expires both cookies immediately (Max-Age 0).

---

## Environment Variables & Configuration

The application runs out of the box with in-memory H2 without requiring external dependencies. To connect to an external database like PostgreSQL on Render, set the following environment variables:

| Environment Variable | Application Property | Default / Fallback | Description |
| :--- | :--- | :--- | :--- |
| `PORT` | `server.port` | `8080` | Web server listening port |
| `DB_URL` | `spring.datasource.url` | `jdbc:h2:mem:pfm;DB_CLOSE_DELAY=-1` | JDBC connection URL (H2 or PostgreSQL) |
| `DB_USERNAME` / `DB_USER` | `spring.datasource.username` | `sa` | Database user |
| `DB_PASSWORD` | `spring.datasource.password` | *(empty)* | Database password |
| `JWT_SECRET` | `jwt.secret` | *(built-in development secret)* | Base64-encoded HMAC secret key |
| `APP_COOKIE_SECURE` / `COOKIE_SECURE` | `app.cookie.secure` | `false` | Set to `true` on HTTPS environments |
| `APP_TIMEZONE` | `app.timezone` | `Asia/Kolkata` | Application timezone for date validations |

---

## Running Locally

### Prerequisites
- JDK 21 installed (`java -version`)
- Git

### Build and Run

```bash
# Clone the repository
git clone <repository-url>
cd personal-finance-manager

# Run with Maven Wrapper (defaults to in-memory H2 on port 8080)
# Linux / macOS:
./mvnw spring-boot:run

# Windows (PowerShell):
.\mvnw.cmd spring-boot:run
```

The application will start at `http://localhost:8080`.

---

## Running Tests & Coverage

Execute the full suite of unit and integration tests and enforce the JaCoCo coverage gate:

```bash
# Linux / macOS:
./mvnw clean verify

# Windows (PowerShell):
.\mvnw.cmd clean verify
```

The JaCoCo coverage report is generated at:
```text
target/site/jacoco/index.html
```
The build enforces a minimum of **80% line coverage** across project classes.

---

## API Reference & Status Codes

All API endpoints are prefixed with `/api`. Errors return a uniform JSON format:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": ["amount: Amount must be greater than 0"]
}
```

### Endpoints Table

| Category | Endpoint | Method | Success | Error Status Codes | Description |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **Health** | `/api/health` | `GET` | `200` | — | Service liveness probe (public) |
| **Auth** | `/api/auth/register` | `POST` | `201` | `400`, `409` | Register a new user |
| **Auth** | `/api/auth/login` | `POST` | `200` | `400`, `401` | Authenticate and issue JWT cookies |
| **Auth** | `/api/auth/logout` | `POST` | `200` | `401` | Invalidate and clear JWT cookies |
| **Transactions** | `/api/transactions` | `POST` | `201` | `400`, `401` | Create a transaction (income or expense) |
| **Transactions** | `/api/transactions` | `GET` | `200` | `400`, `401` | List transactions with optional filters (`startDate`, `endDate`, `categoryId`, `category`, `type`) |
| **Transactions** | `/api/transactions/{id}` | `PUT` | `200` | `400`, `401`, `404` | Partial update of transaction fields |
| **Transactions** | `/api/transactions/{id}` | `DELETE` | `200` | `401`, `404` | Delete transaction by ID |
| **Categories** | `/api/categories` | `GET` | `200` | `401` | List default system and user custom categories |
| **Categories** | `/api/categories` | `POST` | `201` | `400`, `401`, `409` | Create a custom category |
| **Categories** | `/api/categories/{name}` | `DELETE` | `200` | `400`, `401`, `403`, `404` | Delete custom category (`403` for default categories, `400` if tied to transactions) |
| **Goals** | `/api/goals` | `POST` | `201` | `400`, `401` | Create a savings goal |
| **Goals** | `/api/goals` | `GET` | `200` | `401` | List all savings goals with calculated progress |
| **Goals** | `/api/goals/{id}` | `GET` | `200` | `400`, `401`, `403`, `404` | Get savings goal details by ID |
| **Goals** | `/api/goals/{id}` | `PUT` | `200` | `400`, `401`, `403`, `404` | Partial update of goal fields |
| **Goals** | `/api/goals/{id}` | `DELETE` | `200` | `401`, `403`, `404` | Delete savings goal |
| **Reports** | `/api/reports/monthly/{year}/{month}` | `GET` | `200` | `400`, `401` | Aggregated monthly income, expenses, and net savings |
| **Reports** | `/api/reports/yearly/{year}` | `GET` | `200` | `400`, `401` | Aggregated yearly income, expenses, and net savings |

---

## Live Deployment (Render)

- **Live URL:** `https://personal-finance-manager-4fau.onrender.com` *(or configured Render Web Service URL)*
- **Health Check Endpoint:** `https://personal-finance-manager-4fau.onrender.com/api/health`
