# Architecture Specification (ARCHITECTURE.md)

## 1. System Overview
Personal Finance Manager is a REST API built on Java 21 and Spring Boot 3.5.x.
It uses a layered architecture with SOLID principles: small interfaces, dependency
inversion, and explicit extension points so new behavior is added by adding classes,
not editing existing ones.

## 2. Layers and Dependency Rules
Controller -> Service (interface) -> Repository -> Database
- **Controller:** HTTP only (routing, @Valid, status codes). No business logic.
  Depends only on service interfaces and DTOs.
- **Service:** business rules, ownership checks, transactions (@Transactional).
  Depends on repositories, rules/strategies and injected infrastructure interfaces.
  Never touches HttpServletRequest, SecurityContextHolder or LocalDate.now().
- **Repository:** Spring Data JPA only. Entities never leave the service layer.
- **DTOs:** records, separate request/response types. Mappers convert entity <-> DTO.

## 3. Package Structure
Base package: `com.shivanshu.personal_finance_manager`

```text
src/main/java/com/shivanshu/personal_finance_manager
├── PersonalFinanceManagerApplication.java
├── config/            # SecurityConfig, ClockConfig (Clock bean), password encoder
├── controller/        # AuthController, CategoryController, TransactionController,
│                      #   GoalController, ReportController, HealthController
├── service/           # Interfaces: AuthService, CategoryService,
│   │                  #   TransactionService, GoalService, ReportService
│   ├── impl/          # Concrete implementations
│   ├── rule/          # TransactionRule + NotFutureDateRule, CategoryAccessibleRule
│   ├── progress/      # GoalProgressCalculator + NetSavingsSinceStartCalculator
│   └── report/        # ReportGenerator + MonthlyReportGenerator, YearlyReportGenerator
├── repository/        # UserRepository, CategoryRepository, TransactionRepository,
│                      #   SavingsGoalRepository
├── specification/     # TransactionSpecifications (date range, category, type filters)
├── mapper/            # TransactionMapper, CategoryMapper, GoalMapper
├── entity/            # User, Category, Transaction, SavingsGoal, CategoryType
├── dto/
│   ├── request/
│   └── response/
├── exception/         # ApiException (+ factories), GlobalExceptionHandler, ErrorResponse
├── security/          # AppUserDetails, AppUserDetailsService, RestAuthHandlers
│                      #   (JSON 401/403), CurrentUserProvider + implementation
└── util/              # MoneyUtils (scale 2, HALF_UP)

src/test/java/com/shivanshu/personal_finance_manager   # mirrors main packages
├── service/impl/      # Mockito unit tests (services, rules, calculator, generators)
└── integration/       # MockMvc flow tests (auth, categories, transactions, goals, reports)
```

## 4. SOLID Mapping
- **S:** each class has one reason to change (controller = HTTP, service = rules,
  mapper = conversion, handler = error formatting).
- **O:** new transaction validation = new `TransactionRule` bean; new goal formula =
  new `GoalProgressCalculator` bean; new report period = new `ReportGenerator`;
  new filter = new Specification method. No edits to existing services.
- **L:** every implementation honors its interface contract (rules pass or throw
  `ApiException`; calculators return non-null BigDecimal).
- **I:** small interfaces (e.g. `CurrentUserProvider`, `TransactionRule`); no fat services.
- **D:** services depend on abstractions; constructor injection only; `Clock` and
  `CurrentUserProvider` are injected so logic is deterministic and unit-testable.

## 5. Data Model
- `users`: id, username (unique, lowercase email), password (BCrypt), fullName, phoneNumber
- `categories`: id, name, type (INCOME/EXPENSE), custom, user_id (null for defaults)
- `transactions`: id, user_id, category_id, amount (BigDecimal 19,2), transactionDate,
  description. Type is derived from the category.
- `savings_goals`: id, user_id, goalName, targetAmount, targetDate, startDate
- Indexes: (user_id, transaction_date), (user_id, category_id).
- Goal progress is computed from transactions and never stored, so deleting a
  transaction automatically corrects goals and reports.

## 6. Security
- Session-based auth with secure, HTTP-only, SameSite=Lax cookie. CSRF disabled (JSON API).
- Public: POST /api/auth/register, POST /api/auth/login, GET /api/health.
  Everything else requires authentication.
- Login saves the SecurityContext explicitly to the session. Logout invalidates it.
- Unauthenticated -> JSON 401. Data isolation: every query is scoped by userEntity id.

## 7. Error Handling
- One `@RestControllerAdvice` maps `ApiException` (400/401/403/404/409), validation
  errors, malformed JSON and path/type mismatches (all 400), unsupported methods (405),
  and any leftover exception to a generic JSON 500.
- Known scenarios must never produce 5xx. Error body: status, error, message, details.

## 8. Testing
- JUnit 5 + Mockito unit tests for services, rules, calculator and generators.
- MockMvc integration tests for end-to-end flows. JaCoCo line coverage >= 80%.

## 9. Configuration and Deployment
- All config via application.properties with env-var overrides (PORT, DB_URL, DB_USER,
  DB_PASSWORD, COOKIE_SECURE, APP_TIMEZONE). H2 in memory by default.
- `application-local.properties` is git-ignored (local Postgres).
- Docker (Java 21 images) deployed to Render; verify with financial_manager_tests.sh.